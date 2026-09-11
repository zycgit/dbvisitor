/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.DataType;
import io.milvus.grpc.FieldSchema;
import io.milvus.param.ParamUtils;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.utils.SchemaUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;
import static net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema.collectionFields;

public final class MilvusCommandsForTable extends MilvusCommands {
    private MilvusCommandsForTable() {
    }

    private static final JdbcColumn COL_CREATE_STRING       = new JdbcColumn("CREATE SCRIPT", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_DIMENSION_INTEGER   = new JdbcColumn("DIMENSION", AdapterType.Int, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_PRIMARY_BOOL        = new JdbcColumn("PRIMARY", AdapterType.Boolean, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_AUTO_ID_BOOL        = new JdbcColumn("AUTO_ID", AdapterType.Boolean, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_DESCRIPTION_STRING  = new JdbcColumn("DESCRIPTION", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_NULLABLE_BOOL       = new JdbcColumn("NULLABLE", AdapterType.Boolean, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_ELEMENT_STRING      = new JdbcColumn("ELEMENT_TYPE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_CAPACITY_INT        = new JdbcColumn("MAX_CAPACITY", AdapterType.Int, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_LENGTH_INT          = new JdbcColumn("MAX_LENGTH", AdapterType.Int, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_PARTITION_KEY_BOOL  = new JdbcColumn("PARTITION_KEY", AdapterType.Boolean, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_CLUSTERING_KEY_BOOL = new JdbcColumn("CLUSTERING_KEY", AdapterType.Boolean, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    // Collection creation and lifecycle

    public static Future<?> execCreateTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && collectionExists(cmd, collectionName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateCollectionReq.CreateCollectionReqBuilder builder = CreateCollectionReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName)//
                .description("");

        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder().build();
        for (FieldDefinitionContext fieldCtx : c.fieldDefinition()) {
            CreateCollectionReq.FieldSchema field = SchemaUtils.convertFromGrpcFieldSchema(ParamUtils.ConvertField(MilvusFieldDefinition.readFieldDefinition(fieldCtx)));
            MilvusFunctions.configureField(field, fieldCtx.propertiesList(), argIndex, request);
            schema.getFieldSchemaList().add(field);
        }
        MilvusFunctions.addFunctions(schema, c.functionDefinition(), argIndex, request);
        schema.setEnableDynamicField(false);
        builder.collectionSchema(schema).enableDynamicField(false);
        MilvusCollectionOptions.apply(builder, schema, c.propertiesList(), argIndex, request);

        cmd.createCollection(builder.build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!collectionExists(cmd, collectionName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            }
            throw new SQLException("collection not exists.");
        }

        DropCollectionReq param = DropCollectionReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).build();
        cmd.dropCollection(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execTruncate(Future<Object> future, MilvusCmd cmd, HintCommandContext h, TruncateCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        String collectionName = truncateName(c.collectionName);
        String databaseName = c.dbName == null ? cmd.getCatalog() : truncateName(c.dbName);
        TruncateCollectionReq truncate = TruncateCollectionReq.builder().databaseName(databaseName).collectionName(collectionName).build();
        checkActive(request);
        cmd.truncateCollection(truncate);
        checkActive(request);
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    private static String truncateName(IdentifierContext name) throws SQLException {
        String value = readName(name);
        if (name.ARG() != null || value == null || value.trim().isEmpty()) {
            throw new SQLException("TRUNCATE requires non-empty collection and database names, not value parameters.");
        }
        return value;
    }

    public static Future<?> execRenameCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, RenameCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String oldName = readName(c.collectionName);
        String newName = readName(c.newName);

        RenameCollectionReq param = RenameCollectionReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(oldName)//
                .newCollectionName(newName)//
                .targetDbName(readName(c.targetDatabase))//
                .build();

        cmd.renameCollection(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    // Collection metadata and CREATE script

    public static Future<?> execShowTables(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        ListCollectionsReq param = ListCollectionsReq.builder().databaseName(cmd.getCatalog()).build();
        ListCollectionsResp resp = cmd.listCollectionsV2(param);

        List<String> names = resp.getCollectionNames();

        receive.responseResult(request, listResult(request, COL_TABLE_STRING, names));
        return completed(future);
    }

    public static Future<?> execShowTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);

        DescribeCollectionReq param = DescribeCollectionReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).build();
        DescribeCollectionResp resp = cmd.describeCollection(param);

        List<FieldSchema> fields = collectionFields(resp);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FieldSchema field : fields) {
            Map<String, Object> row = new HashMap<>();
            row.put(COL_FIELD_STRING.name, field.getName());
            row.put(COL_TYPE_STRING.name, field.getDataType().name());

            // Extract dimension from type_params if possible, or if it is vector type
            String dim = "";
            for (io.milvus.grpc.KeyValuePair kv : field.getTypeParamsList()) {
                if (MilvusCommandKeys.DIMENSION.equalsIgnoreCase(kv.getKey())) {
                    dim = kv.getValue();
                    break;
                }
            }
            if (StringUtils.isBlank(dim) && field.getDataType() == DataType.VarChar) {
                for (io.milvus.grpc.KeyValuePair kv : field.getTypeParamsList()) {
                    if (MilvusCommandKeys.MAX_LENGTH.equalsIgnoreCase(kv.getKey())) {
                        dim = kv.getValue();
                        break;
                    }
                }
            }

            row.put(COL_DIMENSION_INTEGER.name, StringUtils.isBlank(dim) ? 0 : Integer.parseInt(dim));
            row.put(COL_PRIMARY_BOOL.name, field.getIsPrimaryKey());
            row.put(COL_AUTO_ID_BOOL.name, field.getAutoID());
            row.put(COL_PARTITION_KEY_BOOL.name, field.getIsPartitionKey());
            row.put(COL_CLUSTERING_KEY_BOOL.name, field.getIsClusteringKey());
            row.put(COL_DESCRIPTION_STRING.name, field.getDescription());
            row.put(COL_NULLABLE_BOOL.name, field.getNullable());
            row.put(COL_ELEMENT_STRING.name, field.getDataType() == DataType.Array ? field.getElementType().name() : null);
            for (io.milvus.grpc.KeyValuePair option : field.getTypeParamsList()) {
                if (MilvusCommandKeys.MAX_CAPACITY.equals(option.getKey())) {
                    row.put(COL_CAPACITY_INT.name, Integer.valueOf(option.getValue()));
                }
                if (MilvusCommandKeys.MAX_LENGTH.equals(option.getKey())) {
                    row.put(COL_LENGTH_INT.name, Integer.valueOf(option.getValue()));
                }
            }
            result.add(row);
        }

        // @formatter:off
        receive.responseResult(request, listResult(request, Arrays.asList(
            COL_FIELD_STRING,
            COL_TYPE_STRING,
            COL_DIMENSION_INTEGER,
            COL_PRIMARY_BOOL,
            COL_AUTO_ID_BOOL,
            COL_DESCRIPTION_STRING,
            COL_NULLABLE_BOOL,
            COL_ELEMENT_STRING,
            COL_CAPACITY_INT,
            COL_LENGTH_INT,
            COL_PARTITION_KEY_BOOL,
            COL_CLUSTERING_KEY_BOOL
        ), result));
        // @formatter:on
        return completed(future);
    }

    public static Future<?> execShowCreateTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);

        DescribeCollectionReq param = DescribeCollectionReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).build();
        DescribeCollectionResp resp = cmd.describeCollection(param);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(collectionName).append(" (");

        List<FieldSchema> fields = collectionFields(resp);
        for (int i = 0; i < fields.size(); i++) {
            FieldSchema field = fields.get(i);
            if (i > 0) {
                sql.append(", ");
            }

            sql.append(field.getName()).append(" ");

            DataType type = field.getDataType();
            String dim = "";
            for (io.milvus.grpc.KeyValuePair kv : field.getTypeParamsList()) {
                if (MilvusCommandKeys.DIMENSION.equalsIgnoreCase(kv.getKey()) || MilvusCommandKeys.MAX_LENGTH.equalsIgnoreCase(kv.getKey())) {
                    dim = kv.getValue();
                    break;
                }
            }

            switch (type) {
                case Bool:
                    sql.append("bool");
                    break;
                case Int8:
                    sql.append("int8");
                    break;
                case Int16:
                    sql.append("int16");
                    break;
                case Int32:
                    sql.append("int32");
                    break;
                case Int64:
                    sql.append("int64");
                    break;
                case Float:
                    sql.append("float");
                    break;
                case Double:
                    sql.append("double");
                    break;
                case JSON:
                    sql.append("json");
                    break;
                case String:
                    sql.append("string");
                    break;
                case VarChar:
                    sql.append("varchar(").append(dim).append(")");
                    break;
                case FloatVector:
                    sql.append("float_vector(").append(dim).append(")");
                    break;
                case BinaryVector:
                    sql.append("binary_vector(").append(dim).append(")");
                    break;
                case Float16Vector:
                    sql.append("float16_vector(").append(dim).append(")");
                    break;
                case BFloat16Vector:
                    sql.append("bfloat16_vector(").append(dim).append(")");
                    break;
                case Int8Vector:
                    sql.append("int8_vector(").append(dim).append(")");
                    break;
                case SparseFloatVector:
                    sql.append("sparse_float_vector");
                    break;
                case Array:
                    sql.append("array<").append(field.getElementType() == DataType.Bool ? "bool" : field.getElementType().name().toLowerCase(Locale.ROOT));
                    if (field.getElementType() == DataType.VarChar) {
                        sql.append('(').append(field.getTypeParamsList().stream().filter(p -> MilvusCommandKeys.MAX_LENGTH.equals(p.getKey())).map(io.milvus.grpc.KeyValuePair::getValue).findFirst().orElse("")).append(')');
                    }
                    sql.append(">(").append(field.getTypeParamsList().stream().filter(p -> MilvusCommandKeys.MAX_CAPACITY.equals(p.getKey())).map(io.milvus.grpc.KeyValuePair::getValue).findFirst().orElse("")).append(')');
                    break;
                default:
                    sql.append(type.name());
                    break;
            }

            if (field.getIsPrimaryKey()) {
                sql.append(" PRIMARY KEY");
            }
            if (field.getAutoID()) {
                sql.append(" AUTO_ID");
            }
            if (field.getIsPartitionKey()) {
                sql.append(" PARTITION KEY");
            }
            if (field.getIsClusteringKey()) {
                sql.append(" CLUSTERING KEY");
            }
            sql.append(field.getNullable() ? " NULL" : " NOT NULL");
            if (field.hasDefaultValue()) {
                Object value = io.milvus.param.ParamUtils.valueFieldToObject(field.getDefaultValue(), field.getDataType());
                if (value != null) {
                    sql.append(" DEFAULT ");
                    if (value instanceof String) {
                        sql.append("'").append(((String) value).replace("'", "''")).append("'");
                    } else {
                        sql.append(value);
                    }
                }
            }

            if (StringUtils.isNotBlank(field.getDescription())) {
                sql.append(" COMMENT '").append(field.getDescription().replace("'", "''")).append("'");
            }
            MilvusFunctions.appendFieldOptions(sql, field);
        }
        MilvusFunctions.appendFunctions(sql, resp.getCollectionSchema());
        sql.append(")");
        MilvusCollectionOptions.append(sql, resp);

        receive.responseResult(request, twoResult(request, COL_TABLE_STRING, collectionName, COL_CREATE_STRING, sql.toString()));
        return completed(future);
    }
    // Collection lookup for IF [NOT] EXISTS

    private static boolean collectionExists(MilvusCmd milvusCmd, String collectionName) throws SQLException {
        return Boolean.TRUE.equals(milvusCmd.hasCollection(HasCollectionReq.builder().databaseName(milvusCmd.getCatalog())//
                .collectionName(collectionName).build()));
    }
}
