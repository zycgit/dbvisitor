/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.service.utility.request.FlushAllReq;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.utility.request.GetFlushAllStateReq;
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
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/** Native synchronous flush and read-only observation of a flush-all timestamp. */
public final class MilvusCommandsForFlush extends MilvusCommands {
    // Retain the existing collection FLUSH default; zero explicitly selects unlimited SDK waiting.
    private static final long       DEFAULT_WAIT_TIMEOUT_MS = 60_000L;
    private static final JdbcColumn TIMESTAMP               = new JdbcColumn("FLUSH_ALL_TS", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn FLUSHED                 = new JdbcColumn("FLUSHED", AdapterType.Boolean, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    private MilvusCommandsForFlush() {
    }

    public static Future<?> execFlush(Future<Object> future, MilvusCmd cmd, HintCommandContext h, FlushCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        long timeout = readTimeout(readProperties(args, request, c.propertiesList()));
        checkActive(request);
        if (c.TABLES() != null) {
            FlushAllReq param = FlushAllReq.builder().databaseName(database(cmd, c.database)).waitFlushedTimeoutMs(timeout).build();
            Long timestamp = cmd.flushAll(param).getFlushAllTs();
            checkActive(request);
            receive.responseResult(request, singleResult(request, TIMESTAMP, timestamp));
        } else {
            String database = c.dbName == null ? cmd.getCatalog() : objectName(c.dbName);
            List<String> collections = new ArrayList<>();
            for (IdentifierContext collection : c.collections.identifier()) {
                collections.add(objectName(collection));
            }
            FlushReq param = FlushReq.builder().databaseName(database).collectionNames(collections).waitFlushedTimeoutMs(timeout).build();
            cmd.flush(param);
            checkActive(request);
            receive.responseUpdateCount(request, 0);
        }
        return completed(future);
    }

    public static Future<?> execShow(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        long timestamp = readBound(c.flushTimestamp, args, request, TIMESTAMP.name, 0);
        GetFlushAllStateReq param = GetFlushAllStateReq.builder().databaseName(database(cmd, c.database)).flushAllTs(timestamp).build();
        checkActive(request);
        Boolean flushed = cmd.getFlushAllState(param).getFlushed();
        receive.responseResult(request, twoResult(request, TIMESTAMP, timestamp, FLUSHED, flushed));
        return completed(future);
    }

    private static String database(MilvusCmd cmd, PrivilegeScopeContext scope) throws SQLException {
        if (scope == null) {
            return cmd.getCatalog();
        }
        // An empty native database scope explicitly addresses all databases, not the current one.
        return scope.STAR() == null ? objectName(scope.identifier()) : "";
    }

    private static String objectName(IdentifierContext identifier) throws SQLException {
        String name = readName(identifier);
        if (identifier.ARG() != null || name.isBlank()) {
            throw new SQLException("FLUSH collection/database names require non-empty SQL identifiers, not value parameters.");
        }
        return name;
    }

    private static long readTimeout(Map<String, Object> options) throws SQLException {
        if (options.isEmpty()) {
            return DEFAULT_WAIT_TIMEOUT_MS;
        }
        if (options.size() != 1 || !options.containsKey(MilvusCommandKeys.WAIT_FLUSHED_TIMEOUT_MS)) {
            throw new SQLException("FLUSH WITH accepts only wait_flushed_timeout_ms.");
        }
        return integerBound(options.get(MilvusCommandKeys.WAIT_FLUSHED_TIMEOUT_MS), MilvusCommandKeys.WAIT_FLUSHED_TIMEOUT_MS, 0);
    }
}
