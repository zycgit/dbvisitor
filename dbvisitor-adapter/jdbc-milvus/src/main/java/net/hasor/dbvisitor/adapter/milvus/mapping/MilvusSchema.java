/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus.mapping;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FieldSchema;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.utils.SchemaUtils;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

/** Pure field-value conversion and JDBC type mappings; no SQL parsing or remote calls. */
public final class MilvusSchema {
    private MilvusSchema() {
    }

    public static Object convertFieldValue(FieldSchema field, Object value) throws SQLException {
        if (field == null) {
            throw new SQLException("Field is absent from the collection schema.");
        }
        String fieldName = field.getName();
        DataType dataType = field.getDataType();
        if (MilvusVectorCodec.isVector(dataType)) {
            return MilvusVectorCodec.writeValue(field, value);
        }
        if (dataType == DataType.Array && value != null) {
            return arrayValue(field, value);
        }

        if (value instanceof Number number && dataType != null) {
            switch (dataType) {
                case Int64:
                    return number.longValue();
                case Int32:
                case Int16:
                case Int8:
                    return number.intValue();
                case Float:
                    return number.floatValue();
                case Double:
                    return number.doubleValue();
                default:
                    break;
            }
        }

        // The schema distinguishes JSON numeric arrays from FloatVector values.
        if (dataType == DataType.JSON && value != null) {
            Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();
            try {
                return value instanceof String ? gson.fromJson((String) value, JsonElement.class) : gson.toJsonTree(value);
            } catch (RuntimeException e) {
                throw new SQLException("Invalid JSON value for field '" + fieldName + "'.", e);
            }
        }
        return value;
    }

    // JDBC type mapping

    public static String adapterType(DataType type) {
        if (type == null) {
            return AdapterType.Unknown;
        }
        switch (type) {
            case Bool:
                return AdapterType.Boolean;
            case Int8:
                return AdapterType.Byte;
            case Int16:
                return AdapterType.Short;
            case Int32:
                return AdapterType.Int;
            case Int64:
                return AdapterType.Long;
            case Float:
                return AdapterType.Float;
            case Double:
                return AdapterType.Double;
            case String:
            case VarChar:
                return AdapterType.String;
            case JSON:
                return "JSON";
            case Array:
            case FloatVector:
                return AdapterType.Array;
            case BinaryVector:
            case Float16Vector:
            case BFloat16Vector:
                return AdapterType.Bytes;
            case SparseFloatVector:
                return "SPARSE_FLOAT_VECTOR";
            default:
                return AdapterType.Unknown;
        }
    }

    // Collection metadata is shared by DDL, writes and JDBC result columns.

    public static JdbcColumn column(FieldSchema field, String collection, String catalog) {
        String elementType = field.getDataType() == DataType.Array ? adapterType(field.getElementType()) : field.getDataType() == DataType.FloatVector ? AdapterType.Float : AdapterType.Array;
        return new JdbcColumn(field.getName(), adapterType(field.getDataType()), collection, catalog == null ? "" : catalog, "", field.getNullable() ? ResultSetMetaData.columnNullable : ResultSetMetaData.columnNoNulls, field.getAutoID(), elementType);
    }

    private static List<Object> arrayValue(FieldSchema field, Object value) throws SQLException {
        if (value instanceof java.sql.Array) {
            value = ((java.sql.Array) value).getArray();
        }
        List<?> values;
        if (value instanceof List<?>) {
            values = (List<?>) value;
        } else if (value.getClass().isArray()) {
            List<Object> array = new ArrayList<>();
            for (int i = 0; i < java.lang.reflect.Array.getLength(value); i++) {
                array.add(java.lang.reflect.Array.get(value, i));
            }
            values = array;
        } else {
            throw new SQLException("ARRAY field requires java.sql.Array, List or a Java array.");
        }
        Map<String, String> params = new HashMap<>();
        field.getTypeParamsList().forEach(p -> params.put(p.getKey(), p.getValue()));
        int capacity = Integer.parseInt(params.getOrDefault(MilvusCommandKeys.MAX_CAPACITY, "0"));
        if (values.size() > capacity) {
            throw new SQLException("ARRAY field '" + field.getName() + "' exceeds max_capacity=" + capacity);
        }
        List<Object> result = new ArrayList<>(values.size());
        for (Object item : values) {
            if (item == null || item instanceof Collection || item.getClass().isArray()) {
                throw new SQLException("ARRAY elements must be non-null scalars.");
            }
            try {
                switch (field.getElementType()) {
                    case Bool:
                        if (!(item instanceof Boolean)) {
                            throw new IllegalArgumentException("Expected boolean");
                        }
                        result.add(item);
                        break;
                    case VarChar:
                        if (!(item instanceof String) || ((String) item).getBytes(StandardCharsets.UTF_8).length > Integer.parseInt(params.get(MilvusCommandKeys.MAX_LENGTH))) {
                            throw new IllegalArgumentException("Expected VARCHAR within max_length");
                        }
                        result.add(item);
                        break;
                    case Int8:
                        result.add((int) new BigDecimal(item.toString()).byteValueExact());
                        break;
                    case Int16:
                        result.add((int) new BigDecimal(item.toString()).shortValueExact());
                        break;
                    case Int32:
                        result.add(new BigDecimal(item.toString()).intValueExact());
                        break;
                    case Int64:
                        result.add(new BigDecimal(item.toString()).longValueExact());
                        break;
                    case Float:
                        float f = Float.parseFloat(item.toString());
                        if (!Float.isFinite(f)) {
                            throw new IllegalArgumentException("Expected finite FLOAT");
                        }
                        result.add(f);
                        break;
                    case Double:
                        double d = Double.parseDouble(item.toString());
                        if (!Double.isFinite(d)) {
                            throw new IllegalArgumentException("Expected finite DOUBLE");
                        }
                        result.add(d);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported ARRAY element type");
                }
            } catch (IllegalArgumentException | ArithmeticException e) {
                throw new SQLException("Invalid ARRAY element for field '" + field.getName() + "': " + item, e);
            }
        }
        return result;
    }

    public static List<FieldSchema> collectionFields(DescribeCollectionResp description) {
        List<FieldSchema> fields = new ArrayList<>();
        for (io.milvus.v2.service.collection.request.CreateCollectionReq.FieldSchema field : description.getCollectionSchema().getFieldSchemaList()) {
            fields.add(SchemaUtils.convertToGrpcFieldSchema(field));
        }
        return fields;
    }
}
