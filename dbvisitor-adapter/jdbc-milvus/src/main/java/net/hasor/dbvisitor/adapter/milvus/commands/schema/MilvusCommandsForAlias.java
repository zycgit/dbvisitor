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
import io.milvus.v2.service.utility.request.*;
import io.milvus.v2.service.utility.response.DescribeAliasResp;
import io.milvus.v2.service.utility.response.ListAliasResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** Collection alias definitions. */
public final class MilvusCommandsForAlias extends MilvusCommands {
    private static final JdbcColumn COL_ALIAS = new JdbcColumn("ALIAS", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    private MilvusCommandsForAlias() {
    }

    // Collection aliases

    public static Future<?> execShowAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        if (c.ALIASES() != null) {
            ListAliasResp aliases = cmd.listAliases(ListAliasesReq.builder().databaseName(cmd.getCatalog()).collectionName(readName(c.collectionName)).build());
            List<Map<String, Object>> rows = new ArrayList<>();
            for (String alias : aliases.getAlias()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(COL_ALIAS.name, alias);
                row.put(COL_TABLE_STRING.name, aliases.getCollectionName());
                rows.add(row);
            }
            receive.responseResult(request, listResult(request, Arrays.asList(COL_ALIAS, COL_TABLE_STRING), rows));
        } else {
            DescribeAliasResp alias = cmd.describeAlias(DescribeAliasReq.builder().databaseName(cmd.getCatalog()).alias(readName(c.aliasName)).build());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(COL_DATABASE_STRING.name, alias.getDatabaseName());
            row.put(COL_ALIAS.name, alias.getAlias());
            row.put(COL_TABLE_STRING.name, alias.getCollectionName());
            receive.responseResult(request, listResult(request, Arrays.asList(COL_DATABASE_STRING, COL_ALIAS, COL_TABLE_STRING), Collections.singletonList(row)));
        }
        return completed(future);
    }

    public static Future<?> execCreateAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = readName(c.aliasName);
        String collectionName = readName(c.collectionName);

        CreateAliasReq param = CreateAliasReq.builder().databaseName(cmd.getCatalog())//
                .alias(aliasName)//
                .collectionName(collectionName)//
                .build();

        cmd.createAlias(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = readName(c.aliasName);

        DropAliasReq param = DropAliasReq.builder().databaseName(cmd.getCatalog())//
                .alias(aliasName)//
                .build();

        try {
            cmd.dropAlias(param);
        } catch (SQLException e) {
            boolean ifExists = c.IF() != null && c.EXISTS() != null;
            if (!ifExists || e.getMessage() == null || !e.getMessage().contains("alias does not exist")) {
                throw e;
            }
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlterAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = readName(c.aliasName);
        String collectionName = readName(c.collectionName);

        AlterAliasReq param = AlterAliasReq.builder().databaseName(cmd.getCatalog())//
                .alias(aliasName)//
                .collectionName(collectionName)//
                .build();

        cmd.alterAlias(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

}
