/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
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
import io.milvus.v2.service.collection.request.CreateCollectionReq;
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
        if (dataType == DataType.VarChar && value instanceof java.util.Date date) {
            return temporalText(date);
        }
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

    /** Stable JDBC text for VARCHAR storage and filter templates, not a native Milvus temporal type. */
    public static String temporalText(java.util.Date value) {
        if (value instanceof java.sql.Date || value instanceof java.sql.Time || value instanceof java.sql.Timestamp) {
            return value.toString();
        }
        return new java.sql.Timestamp(value.getTime()).toString();
    }

    public static String adapterType(DataType type) {
        if (type == null) {
            return AdapterType.Unknown;
        }
        return switch (type) {
            case Bool -> AdapterType.Boolean;
            case Int8 -> AdapterType.Byte;
            case Int16 -> AdapterType.Short;
            case Int32 -> AdapterType.Int;
            case Int64 -> AdapterType.Long;
            case Float -> AdapterType.Float;
            case Double -> AdapterType.Double;
            case String, VarChar -> AdapterType.String;
            case JSON -> "JSON";
            case Array, FloatVector -> AdapterType.Array;
            case BinaryVector, Float16Vector, BFloat16Vector, Int8Vector -> AdapterType.Bytes;
            case SparseFloatVector -> "SPARSE_FLOAT_VECTOR";
            default -> AdapterType.Unknown;
        };
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
        for (CreateCollectionReq.FieldSchema field : description.getCollectionSchema().getFieldSchemaList()) {
            fields.add(SchemaUtils.convertToGrpcFieldSchema(field));
        }
        return fields;
    }
}
