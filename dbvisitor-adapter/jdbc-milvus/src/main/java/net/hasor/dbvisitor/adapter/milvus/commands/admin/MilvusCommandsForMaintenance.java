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
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.LoadState;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.collection.response.GetLoadStateResp;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import io.milvus.v2.service.partition.request.ReleasePartitionsReq;
import io.milvus.v2.service.utility.request.FlushReq;
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
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

public final class MilvusCommandsForMaintenance extends MilvusCommands {
    private MilvusCommandsForMaintenance() {
    }

    private static final long       DEFAULT_WAIT_TIMEOUT_MS  = 60_000L;
    private static final long       DEFAULT_WAIT_INTERVAL_MS = 100L;
    // Preserve the previous synchronous flush timeout instead of V2\'s unlimited wait.
    private static final long       FLUSH_WAIT_TIMEOUT_MS    = 60_000L;
    private static final JdbcColumn COL_PROGRESS             = new JdbcColumn("PROGRESS", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);

    public static Future<?> execFlushCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, FlushCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);

        FlushReq param = FlushReq.builder().databaseName(cmd.getCatalog()).waitFlushedTimeoutMs(FLUSH_WAIT_TIMEOUT_MS)//
                .collectionNames(Collections.singletonList(collectionName))//
                .build();

        cmd.flush(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    // Loading progress

    public static Future<?> execShowProgressLoading(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String partitionName = readName(c.partitionName);

        GetLoadStateReq.GetLoadStateReqBuilder builder = GetLoadStateReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName);

        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionName(partitionName);
        }

        GetLoadStateResp result = cmd.getLoadStateV2(builder.build());

        receive.responseResult(request, singleResult(request, COL_PROGRESS, result.getProgress()));
        return completed(future);
    }

    // Collection / partition loading and release

    public static Future<?> execLoadCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, LoadCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hints = readHints(argIndex, request, h.hint());
        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = null;

        if (c.partitionName != null) {
            partitionName = getIdentifier(c.partitionName.getText());
            LoadPartitionsReq param = LoadPartitionsReq.builder().databaseName(cmd.getCatalog()).sync(false)//
                    .collectionName(collectionName)//
                    .partitionNames(Collections.singletonList(partitionName))//
                    .build();

            cmd.loadPartitions(param);
        } else {
            LoadCollectionReq param = LoadCollectionReq.builder().databaseName(cmd.getCatalog()).sync(false)//
                    .collectionName(collectionName)//
                    .build();

            cmd.loadCollection(param);
        }
        if (hintAsBoolean(hints, MilvusCommandKeys.SYNC, true)) {
            waitForLoadState(cmd, collectionName, partitionName, LoadState.LoadStateLoaded, request, hintAsLong(hints, MilvusCommandKeys.TIMEOUT, DEFAULT_WAIT_TIMEOUT_MS));
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execReleaseCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ReleaseCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hints = readHints(argIndex, request, h.hint());
        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = null;

        if (c.partitionName != null) {
            partitionName = getIdentifier(c.partitionName.getText());
            ReleasePartitionsReq param = ReleasePartitionsReq.builder().databaseName(cmd.getCatalog())//
                    .collectionName(collectionName)//
                    .partitionNames(Collections.singletonList(partitionName))//
                    .build();

            cmd.releasePartitions(param);
        } else {
            ReleaseCollectionReq param = ReleaseCollectionReq.builder().databaseName(cmd.getCatalog())//
                    .collectionName(collectionName)//
                    .build();

            cmd.releaseCollection(param);
        }

        if (hintAsBoolean(hints, MilvusCommandKeys.SYNC, true)) {
            waitForLoadState(cmd, collectionName, partitionName, LoadState.LoadStateNotLoad, request, hintAsLong(hints, MilvusCommandKeys.TIMEOUT, DEFAULT_WAIT_TIMEOUT_MS));
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    // Polling for synchronous load/release hints

    private static void waitForLoadState(MilvusCmd cmd, String collectionName, String partitionName, LoadState expectedState, AdapterRequest request, long timeoutMillis) throws SQLException {
        long endTime = System.currentTimeMillis() + timeoutMillis;
        LoadState lastState = null;
        while (System.currentTimeMillis() <= endTime) {
            checkActive(request);
            GetLoadStateReq.GetLoadStateReqBuilder builder = GetLoadStateReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName);
            if (StringUtils.isNotBlank(partitionName)) {
                builder.partitionName(partitionName);
            }

            GetLoadStateResp stateResp = cmd.getLoadStateV2(builder.build());

            lastState = stateResp.getState();
            if (lastState == expectedState) {
                return;
            }
            sleepQuietly(DEFAULT_WAIT_INTERVAL_MS);
        }

        throw new SQLException("Timeout waiting load state, collection=" + collectionName + ", partition=" + partitionName + ", expected=" + expectedState + ", actual=" + lastState);
    }
}
