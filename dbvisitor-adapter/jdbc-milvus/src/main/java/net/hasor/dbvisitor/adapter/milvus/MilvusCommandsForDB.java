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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.ListDatabasesResponse;
import io.milvus.param.R;
import io.milvus.param.collection.AlterDatabaseParam;
import io.milvus.param.collection.CreateDatabaseParam;
import io.milvus.param.collection.DropDatabaseParam;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.AlterCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CreateCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.DropCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MilvusCommandsForDB extends MilvusCommands {
    public static Future<?> execCreateDatabase(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, c.dbName, cmd);

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && dbExists(cmd, dbName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateDatabaseParam param = CreateDatabaseParam.newBuilder().withDatabaseName(dbName).build();
        R<?> result = cmd.getClient().createDatabase(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlterDatabase(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, c.dbName, cmd);
        Map<String, String> properties = readProperties(argIndex, request, c.propertiesList());

        AlterDatabaseParam.Builder builder = AlterDatabaseParam.newBuilder().withDatabaseName(dbName);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            builder.withProperty(entry.getKey(), entry.getValue());
        }

        R<?> result = cmd.getClient().alterDatabase(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropDatabase(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, c.dbName, cmd);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!dbExists(cmd, dbName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("database not exists.");
            }
        }

        DropDatabaseParam param = DropDatabaseParam.newBuilder().withDatabaseName(dbName).build();
        R<?> result = cmd.getClient().dropDatabase(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowDatabases(Future<Object> future, MilvusCmd cmd, HintCommandContext h, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        R<ListDatabasesResponse> resp = cmd.getClient().listDatabases();
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        List<String> listResult = resp.getData() == null ? Collections.emptyList() : resp.getData().getDbNamesList();

        receive.responseResult(request, listResult(request, COL_DATABASE_STRING, listResult));
        return completed(future);
    }

    //

    private static boolean dbExists(MilvusCmd cmd, String dbName) throws SQLException {
        R<ListDatabasesResponse> resp = cmd.getClient().listDatabases();
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }

        ListDatabasesResponse data = resp.getData();
        if (data == null) {
            return false;
        }

        for (String name : data.getDbNamesList()) {
            if (StringUtils.equals(name, dbName)) {
                return true;
            }
        }
        return false;
    }
}