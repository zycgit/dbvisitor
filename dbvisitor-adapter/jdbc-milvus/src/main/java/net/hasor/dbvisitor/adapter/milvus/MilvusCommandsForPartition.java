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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.ShowPartitionsResponse;
import io.milvus.param.R;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.DropPartitionParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.param.partition.ShowPartitionsParam;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.CreateCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.DropCmdContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HintCommandContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.ShowCmdContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MilvusCommandsForPartition extends MilvusCommands {
    private static boolean partitionExists(MilvusCmd milvusCmd, String dbName, String collectionName, String partitionName) throws SQLException {
        HasPartitionParam.Builder builder = HasPartitionParam.newBuilder().withCollectionName(collectionName).withPartitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.withDatabaseName(dbName);
        }

        R<Boolean> resp = milvusCmd.getClient().hasPartition(builder.build());
        if (resp.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(resp.getMessage());
        }
        Boolean data = resp.getData();
        return data != null && data;
    }

    //

    public static Future<?> execCreatePartition(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String partitionName = argAsName(argIndex, request, c.partitionName);
        String dbName = cmd.getCatalog();

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && partitionExists(cmd, dbName, collectionName, partitionName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreatePartitionParam.Builder builder = CreatePartitionParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withPartitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.withDatabaseName(dbName);
        }

        R<?> result = cmd.getClient().createPartition(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropPartition(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String partitionName = argAsName(argIndex, request, c.partitionName);
        String dbName = cmd.getCatalog();

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!partitionExists(cmd, dbName, collectionName, partitionName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            }
            throw new SQLException("partition not exists.");
        }

        DropPartitionParam.Builder builder = DropPartitionParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withPartitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.withDatabaseName(dbName);
        }

        R<?> result = cmd.getClient().dropPartition(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowPartition(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String partitionName = argAsName(argIndex, request, c.partitionName);
        String dbName = cmd.getCatalog();

        ShowPartitionsParam.Builder builder = ShowPartitionsParam.newBuilder()//
                .withCollectionName(collectionName)//
                .addPartitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.withDatabaseName(dbName);
        }

        R<ShowPartitionsResponse> result = cmd.getClient().showPartitions(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        ArrayList<String> partitions = new ArrayList<>();
        ShowPartitionsResponse data = result.getData();
        if (data != null) {
            partitions.addAll(data.getPartitionNamesList());
        }

        receive.responseResult(request, listResult(request, COL_PARTITION_STRING, partitions));
        return completed(future);
    }

    public static Future<?> execShowPartitions(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = argAsName(argIndex, request, c.collectionName);
        String dbName = cmd.getCatalog();

        ShowPartitionsParam.Builder builder = ShowPartitionsParam.newBuilder()//
                .withCollectionName(collectionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.withDatabaseName(dbName);
        }

        R<ShowPartitionsResponse> result = cmd.getClient().showPartitions(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        List<String> partitions = new ArrayList<>();
        ShowPartitionsResponse data = result.getData();
        if (data != null) {
            partitions.addAll(data.getPartitionNamesList());
        }

        receive.responseResult(request, listResult(request, COL_PARTITION_STRING, partitions));
        return completed(future);
    }
}
