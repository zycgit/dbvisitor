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
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.GetLoadingProgressResponse;
import io.milvus.param.R;
import io.milvus.param.alias.AlterAliasParam;
import io.milvus.param.alias.CreateAliasParam;
import io.milvus.param.alias.DropAliasParam;
import io.milvus.param.collection.GetLoadingProgressParam;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

class MilvusCommandsForOther extends MilvusCommands {
    private static final JdbcColumn COL_PROGRESS = new JdbcColumn("PROGRESS", AdapterType.Long, "", "", "");

    public static Future<?> execCreateAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = argAsName(argIndex, request, c.aliasName);
        String collectionName = argAsName(argIndex, request, c.collectionName);

        CreateAliasParam param = CreateAliasParam.newBuilder()//
                .withAlias(aliasName)//
                .withCollectionName(collectionName)//
                .build();

        R<?> result = cmd.getClient().createAlias(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = argAsName(argIndex, request, c.aliasName);

        DropAliasParam param = DropAliasParam.newBuilder()//
                .withAlias(aliasName)//
                .build();

        R<?> result = cmd.getClient().dropAlias(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            boolean ifExists = c.IF() != null && c.EXISTS() != null;
            if (ifExists && result.getMessage().contains("alias does not exist")) {
                // ignore
            } else {
                throw new SQLException(result.getMessage());
            }
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlterAlias(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String aliasName = argAsName(argIndex, request, c.aliasName);
        String collectionName = argAsName(argIndex, request, c.collectionName);

        AlterAliasParam param = AlterAliasParam.newBuilder()//
                .withAlias(aliasName)//
                .withCollectionName(collectionName)//
                .build();

        R<?> result = cmd.getClient().alterAlias(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowProgressLoading(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String partitionName = argAsName(argIndex, request, c.partitionName);

        GetLoadingProgressParam.Builder builder = GetLoadingProgressParam.newBuilder()//
                .withCollectionName(collectionName);

        if (StringUtils.isNotBlank(partitionName)) {
            builder.withPartitionNames(Collections.singletonList(partitionName));
        }

        R<GetLoadingProgressResponse> result = cmd.getClient().getLoadingProgress(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseResult(request, singleResult(request, COL_PROGRESS, result.getData().getProgress()));
        return completed(future);
    }
}