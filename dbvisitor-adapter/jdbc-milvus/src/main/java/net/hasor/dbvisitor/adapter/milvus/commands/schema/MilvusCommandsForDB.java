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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DescribeDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import io.milvus.v2.service.database.response.DescribeDatabaseResp;
import io.milvus.v2.service.database.response.ListDatabasesResp;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CreateCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.DropCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

public final class MilvusCommandsForDB extends MilvusCommands {
    private static final Gson       JSON           = new Gson();
    private static final JdbcColumn COL_PROPERTIES = new JdbcColumn("PROPERTIES", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    private MilvusCommandsForDB() {
    }

    public static Future<?> execCreateDatabase(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String dbName = readDatabaseName(c.dbName, cmd);
        Map<String, String> properties = readStringProperties(argIndex, request, c.propertiesList());

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && dbExists(cmd, dbName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateDatabaseReq param = CreateDatabaseReq.builder().databaseName(dbName).properties(properties).build();
        cmd.createDatabase(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropDatabase(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String dbName = readDatabaseName(c.dbName, cmd);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!dbExists(cmd, dbName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("database not exists.");
            }
        }

        DropDatabaseReq param = DropDatabaseReq.builder().databaseName(dbName).build();
        cmd.dropDatabase(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowDatabases(Future<Object> future, MilvusCmd cmd, HintCommandContext h, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        ListDatabasesResp resp = cmd.listDatabases();

        List<String> listResult = resp == null ? Collections.emptyList() : resp.getDatabaseNames();

        receive.responseResult(request, listResult(request, COL_DATABASE_STRING, listResult));
        return completed(future);
    }

    public static Future<?> execShowDatabase(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        DescribeDatabaseResp database = cmd.describeDatabase(DescribeDatabaseReq.builder().databaseName(readDatabaseName(c.dbName, cmd)).build());
        receive.responseResult(request, twoResult(request, COL_DATABASE_STRING, database.getDatabaseName(), COL_PROPERTIES, JSON.toJson(database.getProperties())));
        return completed(future);
    }

    // Database lookup for IF [NOT] EXISTS

    private static boolean dbExists(MilvusCmd cmd, String dbName) throws SQLException {
        ListDatabasesResp resp = cmd.listDatabases();

        ListDatabasesResp data = resp;
        if (data == null) {
            return false;
        }

        for (String name : data.getDatabaseNames()) {
            if (StringUtils.equals(name, dbName)) {
                return true;
            }
        }
        return false;
    }
}
