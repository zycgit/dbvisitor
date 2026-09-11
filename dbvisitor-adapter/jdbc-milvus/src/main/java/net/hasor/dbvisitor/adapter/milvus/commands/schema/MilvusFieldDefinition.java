package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.DataType;
import io.milvus.param.ParamUtils;
import io.milvus.param.collection.FieldType;
import io.milvus.v2.service.collection.request.AddCollectionFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.utils.SchemaUtils;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.FieldConstraintContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.FieldDefinitionContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.FieldTypeContext;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.getIdentifier;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** SQL column types, constraints and defaults used by collection definitions. */
final class MilvusFieldDefinition {
    private MilvusFieldDefinition() {
    }

    static AddCollectionFieldReq readAddedField(FieldDefinitionContext context, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        CreateCollectionReq.FieldSchema field = SchemaUtils.convertFromGrpcFieldSchema(ParamUtils.ConvertField(readFieldDefinition(context)));
        if (Boolean.TRUE.equals(field.getIsPartitionKey()) || Boolean.TRUE.equals(field.getIsClusteringKey())) {
            throw new SQLException("ADD COLUMN cannot add a partition key or a clustering key.");
        }
        if (!Boolean.TRUE.equals(field.getIsNullable())) {
            throw new SQLException("ADD COLUMN requires a nullable field; declare NULL explicitly.");
        }
        if (Boolean.TRUE.equals(field.getIsPrimaryKey()) || Boolean.TRUE.equals(field.getAutoID())) {
            throw new SQLException("ADD COLUMN cannot add a primary key or an AUTO_ID field.");
        }
        MilvusFunctions.configureField(field, context.propertiesList(), argIndex, request);
        AddCollectionFieldReq.AddCollectionFieldReqBuilder builder = AddCollectionFieldReq.builder().fieldName(field.getName()).description(field.getDescription()).dataType(field.getDataType()).isNullable(field.getIsNullable()).maxLength(field.getMaxLength()).dimension(field.getDimension()).elementType(field.getElementType()).maxCapacity(field.getMaxCapacity()).enableAnalyzer(field.getEnableAnalyzer()).analyzerParams(field.getAnalyzerParams()).enableMatch(field.getEnableMatch()).typeParams(field.getTypeParams());
        if (field.getDefaultValue() != null) {
            builder.defaultValue(field.getDefaultValue());
        }
        return builder.build();
    }

    // Schema field definitions and defaults

    static FieldType readFieldDefinition(FieldDefinitionContext fieldCtx) throws SQLException {
        String fieldName = readName(fieldCtx.fieldName);
        FieldTypeContext typeCtx = fieldCtx.fieldType();

        FieldType.Builder fieldBuilder = FieldType.newBuilder().withName(fieldName);

        if (typeCtx.BOOL() != null) {
            fieldBuilder.withDataType(DataType.Bool);
        } else if (typeCtx.INT8() != null) {
            fieldBuilder.withDataType(DataType.Int8);
        } else if (typeCtx.INT16() != null) {
            fieldBuilder.withDataType(DataType.Int16);
        } else if (typeCtx.INT32() != null) {
            fieldBuilder.withDataType(DataType.Int32);
        } else if (typeCtx.INT64() != null) {
            fieldBuilder.withDataType(DataType.Int64);
        } else if (typeCtx.FLOAT() != null) {
            fieldBuilder.withDataType(DataType.Float);
        } else if (typeCtx.DOUBLE() != null) {
            fieldBuilder.withDataType(DataType.Double);
        } else if (typeCtx.JSON() != null) {
            fieldBuilder.withDataType(DataType.JSON);
        } else if (typeCtx.VARCHAR() != null) {
            fieldBuilder.withDataType(DataType.VarChar);
            fieldBuilder.withMaxLength(Integer.parseInt(typeCtx.INTEGER().getText()));
        } else if (typeCtx.FLOAT_VECTOR() != null) {
            fieldBuilder.withDataType(DataType.FloatVector);
            fieldBuilder.withDimension(Integer.parseInt(typeCtx.INTEGER().getText()));
        } else if (typeCtx.BINARY_VECTOR() != null) {
            fieldBuilder.withDataType(DataType.BinaryVector);
            fieldBuilder.withDimension(Integer.parseInt(typeCtx.INTEGER().getText()));
        } else if (typeCtx.FLOAT16_VECTOR() != null) {
            fieldBuilder.withDataType(DataType.Float16Vector);
            fieldBuilder.withDimension(Integer.parseInt(typeCtx.INTEGER().getText()));
        } else if (typeCtx.BFLOAT16_VECTOR() != null) {
            fieldBuilder.withDataType(DataType.BFloat16Vector);
            fieldBuilder.withDimension(Integer.parseInt(typeCtx.INTEGER().getText()));
        } else if (typeCtx.INT8_VECTOR() != null) {
            fieldBuilder.withDataType(DataType.Int8Vector);
            fieldBuilder.withDimension(Integer.parseInt(typeCtx.INTEGER().getText()));
        } else if (typeCtx.SPARSE_FLOAT_VECTOR() != null) {
            fieldBuilder.withDataType(DataType.SparseFloatVector);
        } else if (typeCtx.ARRAY() != null) {
            if (typeCtx.arrayElementType() == null) {
                throw new SQLException("ARRAY requires ARRAY<element_type>(max_capacity).");
            }
            fieldBuilder.withDataType(DataType.Array);
            String element = typeCtx.arrayElementType().getStart().getText().toUpperCase(Locale.ROOT);
            fieldBuilder.withElementType(scalarType(element));
            int capacity = Integer.parseInt(typeCtx.capacity.getText());
            if (capacity < 1 || capacity > 4096) {
                throw new SQLException("ARRAY max_capacity must be between 1 and 4096.");
            }
            fieldBuilder.withMaxCapacity(capacity);
            if (typeCtx.arrayElementType().VARCHAR() != null) {
                fieldBuilder.withMaxLength(Integer.parseInt(typeCtx.arrayElementType().INTEGER().getText()));
            }
        }

        Boolean nullable = null;
        for (FieldConstraintContext constraint : fieldCtx.fieldConstraint()) {
            if (constraint.PRIMARY() != null && constraint.KEY() != null) {
                fieldBuilder.withPrimaryKey(true);
            }
            if (constraint.PARTITION() != null) {
                fieldBuilder.withPartitionKey(true);
            }
            if (constraint.CLUSTERING() != null) {
                fieldBuilder.withClusteringKey(true);
            }
            if (constraint.AUTO_ID() != null) {
                fieldBuilder.withAutoID(true);
            }
            if (constraint.NULL() != null) {
                boolean option = constraint.NOT() == null;
                if (nullable != null && nullable != option) {
                    throw new SQLException("Conflicting NULL / NOT NULL on field '" + fieldName + "'.");
                }
                nullable = option;
                fieldBuilder.withNullable(option);
            }
            if (constraint.COMMENT() != null) {
                String comment = constraint.STRING_LITERAL().getText();
                fieldBuilder.withDescription(getIdentifier(comment));
            }
        }

        FieldType fieldType = fieldBuilder.build();
        if (fieldType.isPrimaryKey() && Boolean.TRUE.equals(nullable)) {
            throw new SQLException("Primary keys cannot be nullable.");
        }
        if (fieldType.isPartitionKey() && Boolean.TRUE.equals(nullable)) {
            throw new SQLException("Partition keys cannot be nullable.");
        }
        boolean hasDefault = false;
        for (FieldConstraintContext constraint : fieldCtx.fieldConstraint()) {
            if (constraint.DEFAULT() != null) {
                if (hasDefault) {
                    throw new SQLException("Duplicate DEFAULT for field '" + fieldName + "'.");
                }
                hasDefault = true;
                String defaultText = constraint.getText().substring(constraint.DEFAULT().getText().length());
                fieldBuilder.withDefaultValue(defaultValue(fieldType, defaultText));
            }
        }
        return fieldBuilder.build();
    }

    private static Object defaultValue(FieldType field, String text) throws SQLException {
        if (field.isPrimaryKey()) {
            throw new SQLException("DEFAULT is not supported on primary key field '" + field.getName() + "'.");
        }
        String value = getIdentifier(text);
        try {
            switch (field.getDataType()) {
                case VarChar:
                    if (value.getBytes(StandardCharsets.UTF_8).length > field.getMaxLength()) {
                        throw new IllegalArgumentException("VARCHAR default exceeds max_length in UTF-8 bytes");
                    }
                    return value;
                case Bool:
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException("Expected true or false");
                    }
                    return Boolean.valueOf(value);
                case Int8:
                    return (short) new BigDecimal(value).byteValueExact();
                case Int16:
                    return new BigDecimal(value).shortValueExact();
                case Int32:
                    return new BigDecimal(value).intValueExact();
                case Int64:
                    return new BigDecimal(value).longValueExact();
                case Float:
                    float floatValue = Float.parseFloat(value);
                    if (!Float.isFinite(floatValue)) {
                        throw new IllegalArgumentException("Expected finite FLOAT");
                    }
                    return floatValue;
                case Double:
                    double doubleValue = Double.parseDouble(value);
                    if (!Double.isFinite(doubleValue)) {
                        throw new IllegalArgumentException("Expected finite DOUBLE");
                    }
                    return doubleValue;
                default:
                    throw new SQLException("DEFAULT is not supported for " + field.getDataType() + ".");
            }
        } catch (IllegalArgumentException | ArithmeticException e) {
            throw new SQLException("Invalid DEFAULT for field '" + field.getName() + "' (" + field.getDataType() + "): " + text, e);
        }
    }

    private static DataType scalarType(String name) throws SQLException {
        switch (name) {
            case "BOOL":
                return DataType.Bool;
            case "INT8":
                return DataType.Int8;
            case "INT16":
                return DataType.Int16;
            case "INT32":
                return DataType.Int32;
            case "INT64":
                return DataType.Int64;
            case "FLOAT":
                return DataType.Float;
            case "DOUBLE":
                return DataType.Double;
            case "VARCHAR":
                return DataType.VarChar;
            default:
                throw new SQLException("Unsupported ARRAY element type: " + name);
        }
    }

}
