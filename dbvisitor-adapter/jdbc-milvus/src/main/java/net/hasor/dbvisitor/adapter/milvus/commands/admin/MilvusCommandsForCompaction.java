/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import io.milvus.v2.service.utility.request.CompactReq;
import io.milvus.v2.service.utility.request.GetCompactionPlansReq;
import io.milvus.v2.service.utility.request.GetCompactionStateReq;
import io.milvus.v2.service.utility.response.GetCompactionPlansResp;
import io.milvus.v2.service.utility.response.GetCompactionStateResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CompactCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/** Native asynchronous compaction: submit once, then observe the returned task ID. */
public final class MilvusCommandsForCompaction extends MilvusCommands {
    private static final JdbcColumn ID        = column("COMPACTION_ID", AdapterType.Long);
    private static final JdbcColumn STATE     = column("STATE", AdapterType.String);
    private static final JdbcColumn EXECUTING = column("EXECUTING_PLANS", AdapterType.Long);
    private static final JdbcColumn COMPLETED = column("COMPLETED_PLANS", AdapterType.Long);
    private static final JdbcColumn TIMEOUT   = column("TIMEOUT_PLANS", AdapterType.Long);
    private static final JdbcColumn PLANS     = column("PLANS", AdapterType.String);
    private static final Gson       JSON      = new Gson();

    private MilvusCommandsForCompaction() {
    }

    public static Future<?> execCompact(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CompactCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        CompactReq.CompactReqBuilder builder = CompactReq.builder().databaseName(cmd.getCatalog()).collectionName(readName(c.collectionName));
        for (Map.Entry<String, Object> option : readProperties(args, request, c.propertiesList()).entrySet()) {
            String key = option.getKey();
            Object value = option.getValue();
            if (MilvusCommandKeys.IS_CLUSTERING.equals(key) || MilvusCommandKeys.IS_L0.equals(key)) {
                if (!(value instanceof Boolean)) {
                    throw new SQLException("WITH " + key + " requires a boolean.");
                }
                if (MilvusCommandKeys.IS_CLUSTERING.equals(key)) {
                    builder.isClustering((Boolean) value);
                } else {
                    builder.isL0((Boolean) value);
                }
            } else if (MilvusCommandKeys.TARGET_SIZE.equals(key)) {
                try {
                    if (!(value instanceof Number)) {
                        throw new NumberFormatException();
                    }
                    long size = new BigDecimal(value.toString()).longValueExact();
                    if (size <= 0) {
                        throw new NumberFormatException();
                    }
                    builder.targetSize(size);
                } catch (ArithmeticException | NumberFormatException e) {
                    throw new SQLException("WITH " + key + " requires a positive long integer in MB.", e);
                }
            } else {
                throw new SQLException("Unknown compaction option: " + key);
            }
        }
        receive.responseResult(request, singleResult(request, ID, cmd.compact(builder.build()).getCompactionID()));
        return completed(future);
    }

    public static Future<?> execShow(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        long id = readBound(c.compactionId, args, request, ID.name, 0);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(ID.name, id);
        if (c.PLANS() != null) {
            GetCompactionPlansResp result = cmd.getCompactionPlans(GetCompactionPlansReq.builder().compactionID(id).build());
            row.put(STATE.name, result.getState().name());
            row.put(PLANS.name, JSON.toJson(result.getPlans()));
            receive.responseResult(request, listResult(request, Arrays.asList(ID, STATE, PLANS), Collections.singletonList(row)));
        } else {
            GetCompactionStateResp result = cmd.getCompactionState(GetCompactionStateReq.builder().compactionID(id).build());
            row.put(STATE.name, result.getState().name());
            row.put(EXECUTING.name, result.getExecutingPlanNo());
            row.put(COMPLETED.name, result.getCompletedPlanNo());
            row.put(TIMEOUT.name, result.getTimeoutPlanNo());
            receive.responseResult(request, listResult(request, Arrays.asList(ID, STATE, EXECUTING, COMPLETED, TIMEOUT), Collections.singletonList(row)));
        }
        return completed(future);
    }

    private static JdbcColumn column(String name, String type) {
        return new JdbcColumn(name, type, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }
}
