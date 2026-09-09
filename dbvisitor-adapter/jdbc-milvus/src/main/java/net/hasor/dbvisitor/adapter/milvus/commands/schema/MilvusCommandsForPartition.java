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
package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.DropPartitionReq;
import io.milvus.v2.service.partition.request.HasPartitionReq;
import io.milvus.v2.service.partition.request.ListPartitionsReq;
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
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

public final class MilvusCommandsForPartition extends MilvusCommands {
    private MilvusCommandsForPartition() {
    }

    // Partition lifecycle

    public static Future<?> execCreatePartition(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String partitionName = readName(c.partitionName);
        String dbName = cmd.getCatalog();

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && partitionExists(cmd, dbName, collectionName, partitionName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreatePartitionReq.CreatePartitionReqBuilder builder = CreatePartitionReq.builder()//
                .collectionName(collectionName)//
                .partitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.databaseName(dbName);
        }

        cmd.createPartition(builder.build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropPartition(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String partitionName = readName(c.partitionName);
        String dbName = cmd.getCatalog();

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!partitionExists(cmd, dbName, collectionName, partitionName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            }
            throw new SQLException("partition not exists.");
        }

        DropPartitionReq.DropPartitionReqBuilder builder = DropPartitionReq.builder()//
                .collectionName(collectionName)//
                .partitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.databaseName(dbName);
        }

        cmd.dropPartition(builder.build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    // Partition metadata

    public static Future<?> execShowPartition(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String partitionName = readName(c.partitionName);
        String dbName = cmd.getCatalog();

        ListPartitionsReq.ListPartitionsReqBuilder builder = ListPartitionsReq.builder()//
                .collectionName(collectionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.databaseName(dbName);
        }

        List<String> result = cmd.listPartitions(builder.build());

        List<String> partitions = result == null ? new ArrayList<>() : new ArrayList<>(result);

        partitions.removeIf(name -> !StringUtils.equals(name, partitionName));
        receive.responseResult(request, listResult(request, COL_PARTITION_STRING, partitions));
        return completed(future);
    }

    public static Future<?> execShowPartitions(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = readName(c.collectionName);
        String dbName = cmd.getCatalog();

        ListPartitionsReq.ListPartitionsReqBuilder builder = ListPartitionsReq.builder()//
                .collectionName(collectionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.databaseName(dbName);
        }

        List<String> result = cmd.listPartitions(builder.build());

        List<String> partitions = result == null ? new ArrayList<>() : new ArrayList<>(result);

        receive.responseResult(request, listResult(request, COL_PARTITION_STRING, partitions));
        return completed(future);
    }

    // Existence checks for IF [NOT] EXISTS

    private static boolean partitionExists(MilvusCmd milvusCmd, String dbName, String collectionName, String partitionName) throws SQLException {
        HasPartitionReq.HasPartitionReqBuilder builder = HasPartitionReq.builder().collectionName(collectionName).partitionName(partitionName);
        if (StringUtils.isNotBlank(dbName)) {
            builder.databaseName(dbName);
        }

        return Boolean.TRUE.equals(milvusCmd.hasPartition(builder.build()));
    }
}
