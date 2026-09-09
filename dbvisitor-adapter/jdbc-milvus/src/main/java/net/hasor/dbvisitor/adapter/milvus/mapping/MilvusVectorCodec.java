package net.hasor.dbvisitor.adapter.milvus.mapping;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;
import io.milvus.common.utils.Float16Utils;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FieldSchema;
import io.milvus.param.ParamUtils;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.vector.request.data.*;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;

/** Schema-directed vector encoding. SQL binding never guesses a vector type from byte[]. */
public final class MilvusVectorCodec {
    private MilvusVectorCodec() {
    }

    static boolean isVector(DataType type) {
        return type != null && ParamUtils.isVectorDataType(type);
    }

    /** Values accepted by the official SDK row-based INSERT/UPSERT JSON encoder. */
    static Object writeValue(FieldSchema field, Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        DataType type = field.getDataType();
        Object encoded;
        int dimension = 0;
        switch (type) {
            case FloatVector:
                List<Float> floats = floats(value);
                encoded = floats;
                dimension = floats.size();
                break;
            case BinaryVector:
                byte[] binary = bytes(value);
                encoded = binary;
                dimension = Math.multiplyExact(binary.length, Byte.SIZE);
                break;
            case Float16Vector:
            case BFloat16Vector:
                byte[] half;
                if (value instanceof byte[] || value instanceof ByteBuffer) {
                    half = bytes(value);
                } else {
                    List<Float> numbers = floats(value);
                    ByteBuffer packed = type == DataType.Float16Vector ? Float16Utils.f32VectorToFp16Buffer(numbers) : Float16Utils.f32VectorToBf16Buffer(numbers);
                    half = sdkBytes(packed);
                }
                if (half.length % Short.BYTES != 0) {
                    throw new SQLException(type + " requires two bytes per dimension.");
                }
                // FP16 conversion can overflow even if the original float was finite.
                List<Float> decoded = type == DataType.Float16Vector ? Float16Utils.fp16BufferToVector(ByteBuffer.wrap(half)) : Float16Utils.bf16BufferToVector(ByteBuffer.wrap(half));
                for (Float number : decoded) {
                    if (!Float.isFinite(number))
                        throw new SQLException(type + " requires finite values.");
                }
                encoded = half;
                dimension = half.length / Short.BYTES;
                break;
            case SparseFloatVector:
                return sparse(value);
            default:
                throw new SQLException("Unsupported vector field type: " + type);
        }
        int expected = field.getTypeParamsList().stream().filter(p -> MilvusCommandKeys.DIMENSION.equals(p.getKey())).mapToInt(p -> Integer.parseInt(p.getValue())).findFirst().orElse(0);
        if (dimension == 0 || (expected > 0 && expected != dimension)) {
            throw new SQLException("Vector dimension mismatch for '" + field.getName() + "': expected=" + expected + ", actual=" + dimension);
        }
        return encoded;
    }

    public static BaseVector searchValue(FieldSchema field, Object value, MetricType metric) throws SQLException {
        if (field == null)
            throw new SQLException("Search field is absent from the collection schema.");
        if (value == null)
            throw new SQLException("Search requires a non-null query vector.");
        DataType type = field.getDataType();
        boolean binaryMetric = metric == MetricType.HAMMING || metric == MetricType.JACCARD;
        if (type == DataType.BinaryVector) {
            if (!binaryMetric)
                throw new SQLException("BinaryVector requires HAMMING or JACCARD.");
        } else if (type == DataType.SparseFloatVector) {
            if (metric != MetricType.IP && metric != MetricType.BM25)
                throw new SQLException("SparseFloatVector requires IP or BM25.");
            if (metric == MetricType.BM25 && value instanceof String)
                return new EmbeddedText((String) value);
        } else if (binaryMetric || metric == MetricType.BM25) {
            throw new SQLException(metric + " does not match field type " + type + ".");
        }
        if (value instanceof String && (type == DataType.FloatVector || type == DataType.Float16Vector || type == DataType.BFloat16Vector)) {
            // TextEmbedding output fields accept raw text; the server resolves the schema function.
            return new EmbeddedText((String) value);
        }
        Object encoded = writeValue(field, value);
        switch (type) {
            case FloatVector:
                return new FloatVec((List<Float>) encoded);
            case BinaryVector:
                return new BinaryVec((byte[]) encoded);
            case Float16Vector:
                return new Float16Vec((byte[]) encoded);
            case BFloat16Vector:
                return new BFloat16Vec((byte[]) encoded);
            case SparseFloatVector:
                return new SparseFloatVec((SortedMap<Long, Float>) encoded);
            default:
                throw new SQLException("Field '" + field.getName() + "' is not a supported vector field.");
        }
    }

    private static List<Float> floats(Object value) throws SQLException {
        List<Float> result = toFloatList(value);
        for (Float number : result) {
            if (number == null || !Float.isFinite(number))
                throw new SQLException("Vector elements must be finite numbers.");
        }
        return result;
    }

    private static SortedMap<Long, Float> sparse(Object value) throws SQLException {
        if (!(value instanceof Map<?, ?> values) || values.isEmpty()) {
            throw new SQLException("SparseFloatVector requires a non-empty Map of dimension indices to weights.");
        }
        SortedMap<Long, Float> result = new TreeMap<>();
        try {
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                long index = new BigDecimal(String.valueOf(entry.getKey())).longValueExact();
                if (index < 0 || index >= (1L << Integer.SIZE) - 1)
                    throw new IllegalArgumentException("Index outside the SDK uint32 range");
                if (!(entry.getValue() instanceof Number number) || !Float.isFinite(number.floatValue())) {
                    throw new IllegalArgumentException("Weight must be a finite number");
                }
                if (result.put(index, number.floatValue()) != null)
                    throw new IllegalArgumentException("Duplicate dimension index");
            }
        } catch (IllegalArgumentException | ArithmeticException e) {
            throw new SQLException("Invalid SparseFloatVector: " + e.getMessage(), e);
        }
        return result;
    }

    private static byte[] bytes(Object value) throws SQLException {
        if (value instanceof byte[])
            return ((byte[]) value).clone();
        if (value instanceof ByteBuffer) {
            ByteBuffer view = ((ByteBuffer) value).duplicate();
            byte[] result = new byte[view.remaining()];
            view.get(result);
            return result;
        }
        if (value instanceof List<?> list) {
            byte[] result = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                try {
                    int number = new BigDecimal(String.valueOf(list.get(i))).intValueExact();
                    if (number < Byte.MIN_VALUE || number > 255)
                        throw new IllegalArgumentException("Byte outside -128..255");
                    result[i] = (byte) number;
                } catch (IllegalArgumentException | ArithmeticException e) {
                    throw new SQLException("Invalid packed byte at index " + i, e);
                }
            }
            return result;
        }
        throw new SQLException("Packed vector requires byte[], ByteBuffer or a list of byte values.");
    }

    /** SDK result buffers are filled using put(), so their position is at the end. */
    public static byte[] sdkBytes(ByteBuffer value) {
        ByteBuffer view = value.duplicate();
        view.rewind();
        byte[] result = new byte[view.remaining()];
        view.get(result);
        return result;
    }

    private static List<Float> toFloatList(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[] || obj instanceof short[] || obj instanceof int[] || obj instanceof long[] || obj instanceof float[] || obj instanceof double[]) {
            int length = java.lang.reflect.Array.getLength(obj);
            List<Float> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(((Number) java.lang.reflect.Array.get(obj, i)).floatValue());
            }
            return values;
        }
        if (!(obj instanceof List<?> list)) {
            throw new SQLException("FloatVector requires a List or a numeric primitive array (byte[], short[], int[], long[], float[], double[]).");
        }
        if (list.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        List<Float> floatList = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof List || (item != null && item.getClass().isArray())) {
                throw new SQLException("A vector distance expression requires a single query vector; nested vectors are not supported.");
            }

            if (item instanceof Number) {
                floatList.add(((Number) item).floatValue());
            } else {
                throw new SQLException("Vector elements must be numbers: " + item);
            }
        }
        return floatList;
    }
}
