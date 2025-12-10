package net.hasor.dbvisitor.types.handler.json;

import java.sql.SQLException;
import java.util.*;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.reflect.resolvable.ResolvableType;
import net.hasor.dbvisitor.types.NoCache;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;

/**
 * 通用的 BSON -> List<POJO> TypeHandler。
 * <p/>
 * 用法：
 * - 单对象：new BsonTypeHandler<>(Address.class)
 * - 列表对象：new BsonTypeHandler<>(List.class, OrderItem.class)
 */
@NoCache
public class BsonListTypeHandler extends AbstractBsonTypeHandler {
    private static final Map<Class<?>, Class<?>> CLASS_MAPPING_MAP = new HashMap<>();
    private final        Class<?>                fieldType;
    private final        Class<?>                elementType;

    static {
        CLASS_MAPPING_MAP.put(Iterable.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Collection.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(List.class, ArrayList.class);
        CLASS_MAPPING_MAP.put(Set.class, LinkedHashSet.class);
        CLASS_MAPPING_MAP.put(Map.class, LinkedHashMap.class);
    }

    public BsonListTypeHandler(ResolvableType documentType) {
        if (documentType == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }

        Class<?> rawClass = documentType.getRawClass();
        this.fieldType = Objects.requireNonNull(CLASS_MAPPING_MAP.get(rawClass), "Unsupported collection type: " + rawClass.getName());
        this.elementType = documentType.resolveGeneric(0);
    }

    public Class<?> getFieldType() {
        return this.fieldType;
    }

    public Class<?> getElementType() {
        return this.elementType;
    }

    @Override
    protected Object toObject(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }

        if (obj instanceof List) {
            List<?> src = (List<?>) obj;
            Object result = ClassUtils.newInstance(this.fieldType);
            for (Object item : src) {
                if (item instanceof Document) {
                    Document doc = (Document) item;
                    BsonDocument bsonDoc = doc.toBsonDocument(this.elementType, CODEC_REGISTRY);
                    Codec<?> codec = CODEC_REGISTRY.get(elementType);
                    Object decoded = codec.decode(new BsonDocumentReader(bsonDoc), DecoderContext.builder().build());
                    addItem(result, decoded);
                } else {
                    addItem(result, item);
                }
            }

            return result;
        }

        throw new SQLException("Unsupported type: " + obj.getClass().getName());
    }

    private void addItem(Object array, Object item) {
        if (array instanceof Collection) {
            ((Collection<Object>) array).add(item);
        } else {
            throw new UnsupportedOperationException("Unsupported collection type: " + array.getClass().getName());
        }
    }
}
