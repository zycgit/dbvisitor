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
package net.hasor.dbvisitor.adapter.milvus;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

class MilvusCommandsForTable extends MilvusCommands {
    protected static final JdbcColumn COL_DIMENSION_INTEGER  = new JdbcColumn("DIMENSION", AdapterType.Int, "", "", "");
    protected static final JdbcColumn COL_PRIMARY_BOOL       = new JdbcColumn("PRIMARY", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_AUTO_ID_BOOL       = new JdbcColumn("AUTO_ID", AdapterType.Boolean, "", "", "");
    protected static final JdbcColumn COL_DESCRIPTION_STRING = new JdbcColumn("DESCRIPTION", AdapterType.String, "", "", "");

    private static boolean collectionExists(MilvusCmd milvusCmd, String collectionName) throws SQLException {
        R<Boolean> resp = milvusCmd.getClient().hasCollection(HasCollectionParam.newBuilder()//
                .withCollectionName(collectionName).build());
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }
        Boolean data = resp.getData();
        return data != null && data;
    }

    //

    public static Future<?> execCreateTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && collectionExists(cmd, collectionName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateCollectionParam.Builder builder = CreateCollectionParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withDescription("");

        for (FieldDefinitionContext fieldCtx : c.fieldDefinition()) {
            String fieldName = argAsName(argIndex, request, fieldCtx.fieldName);
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
            } else if (typeCtx.SPARSE_FLOAT_VECTOR() != null) {
                fieldBuilder.withDataType(DataType.SparseFloatVector);
                fieldBuilder.withDimension(Integer.parseInt(typeCtx.INTEGER().getText()));
            } else if (typeCtx.ARRAY() != null) {
                fieldBuilder.withDataType(DataType.Array);
                // Array logic handling requires more context, keeping simple for now or assuming unsupported by basic DDL parser yet
            }

            for (FieldConstraintContext constraint : fieldCtx.fieldConstraint()) {
                if (constraint.PRIMARY() != null && constraint.KEY() != null) {
                    fieldBuilder.withPrimaryKey(true);
                }
                if (constraint.AUTO_ID() != null) {
                    fieldBuilder.withAutoID(true);
                }
                // Other constraints like NOT NULL, DEFAULT, COMMENT might need SDK support or are just for metadata
                if (constraint.COMMENT() != null) {
                    String comment = constraint.STRING_LITERAL().getText();
                    fieldBuilder.withDescription(getIdentifier(comment));
                }
            }

            builder.addFieldType(fieldBuilder.build());
        }

        R<?> result = cmd.getClient().createCollection(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!collectionExists(cmd, collectionName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            }
            throw new SQLException("collection not exists.");
        }

        DropCollectionParam param = DropCollectionParam.newBuilder().withCollectionName(collectionName).build();
        R<?> result = cmd.getClient().dropCollection(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execRenameCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, RenameCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String oldName = getIdentifier(c.collectionName.getText());
        String newName = getIdentifier(c.newName.getText());

        RenameCollectionParam param = RenameCollectionParam.newBuilder()//
                .withOldCollectionName(oldName)//
                .withNewCollectionName(newName)//
                .build();

        R<?> result = cmd.getClient().renameCollection(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowTables(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        ShowCollectionsParam param = ShowCollectionsParam.newBuilder().build();
        R<ShowCollectionsResponse> resp = cmd.getClient().showCollections(param);
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        List<String> names = resp.getData().getCollectionNamesList();

        receive.responseResult(request, listResult(request, COL_TABLE_STRING, names));
        return completed(future);
    }

    public static Future<?> execShowTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);

        DescribeCollectionParam param = DescribeCollectionParam.newBuilder().withCollectionName(collectionName).build();
        R<DescribeCollectionResponse> resp = cmd.getClient().describeCollection(param);
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        List<FieldSchema> fields = resp.getData().getSchema().getFieldsList();
        List<Map<String, Object>> result = new ArrayList<>();

        for (FieldSchema field : fields) {
            Map<String, Object> row = new HashMap<>();
            row.put(COL_FIELD_STRING.name, field.getName());
            row.put(COL_TYPE_STRING.name, field.getDataType().name());

            // Extract dimension from type_params if possible, or if it is vector type
            String dim = "";
            for (io.milvus.grpc.KeyValuePair kv : field.getTypeParamsList()) {
                if ("dim".equalsIgnoreCase(kv.getKey())) {
                    dim = kv.getValue();
                    break;
                }
            }
            if (StringUtils.isBlank(dim) && field.getDataType() == DataType.VarChar) {
                for (io.milvus.grpc.KeyValuePair kv : field.getTypeParamsList()) {
                    if ("max_length".equalsIgnoreCase(kv.getKey())) {
                        dim = kv.getValue();
                        break;
                    }
                }
            }

            row.put(COL_DIMENSION_INTEGER.name, StringUtils.isBlank(dim) ? 0 : Integer.parseInt(dim));
            row.put(COL_PRIMARY_BOOL.name, field.getIsPrimaryKey());
            row.put(COL_AUTO_ID_BOOL.name, field.getAutoID());
            row.put(COL_DESCRIPTION_STRING.name, field.getDescription());
            result.add(row);
        }

        List<JdbcColumn> columns = Arrays.asList(//
                COL_FIELD_STRING,     //
                COL_TYPE_STRING,      //
                COL_DIMENSION_INTEGER,//
                COL_PRIMARY_BOOL,     //
                COL_AUTO_ID_BOOL,     //
                COL_DESCRIPTION_STRING);
        receive.responseResult(request, listResult(request, columns, result));
        return completed(future);
    }

    public static Future<?> execShowCreateTable(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);

        DescribeCollectionParam param = DescribeCollectionParam.newBuilder().withCollectionName(collectionName).build();
        R<DescribeCollectionResponse> resp = cmd.getClient().describeCollection(param);
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(collectionName).append(" (");

        List<FieldSchema> fields = resp.getData().getSchema().getFieldsList();
        for (int i = 0; i < fields.size(); i++) {
            FieldSchema field = fields.get(i);
            if (i > 0) {
                sql.append(", ");
            }

            sql.append(field.getName()).append(" ");

            DataType type = field.getDataType();
            String dim = "";
            for (io.milvus.grpc.KeyValuePair kv : field.getTypeParamsList()) {
                if ("dim".equalsIgnoreCase(kv.getKey()) || "max_length".equalsIgnoreCase(kv.getKey())) {
                    dim = kv.getValue();
                    break;
                }
            }

            switch (type) {
                case Bool:
                    sql.append("boolean");
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
                case SparseFloatVector:
                    sql.append("sparse_float_vector(").append(dim).append(")");
                    break;
                case Array:
                    sql.append("array");
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
            if (StringUtils.isNotBlank(field.getDescription())) {
                sql.append(" COMMENT '").append(field.getDescription().replace("'", "''")).append("'");
            }
        }
        sql.append(")");

        receive.responseResult(request, twoResult(request, COL_TABLE_STRING, collectionName, COL_CREATE_STRING, sql.toString()));
        return completed(future);
    }
}
