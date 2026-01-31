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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.grpc.ImportResponse;
import io.milvus.param.R;
import io.milvus.param.bulkinsert.BulkInsertParam;
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.partition.LoadPartitionsParam;
import io.milvus.param.partition.ReleasePartitionsParam;
import io.milvus.response.SearchResultsWrapper;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import org.antlr.v4.runtime.Token;

class MilvusCommandsForData extends MilvusCommands {
    public static Future<?> execInsertCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, InsertCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;

        List<InsertParam.Field> fields = new ArrayList<>();
        List<IdentifierContext> colNames = c.columnList.identifier();
        List<LiteralContext> colValues = c.valueList.literal();

        if (colNames.size() != colValues.size()) {
            throw new SQLException("Column count doesn't match value count.");
        }

        for (int i = 0; i < colNames.size(); i++) {
            String colName = getIdentifier(colNames.get(i).getText());
            Object value = parseLiteral(colValues.get(i), argIndex, request);

            // Auto convert vector list if needed (Milvus requires List<Float>)
            if (value instanceof List) {
                List<?> listVal = (List<?>) value;
                if (!listVal.isEmpty() && !(listVal.get(0) instanceof List)) {
                    // Simple list, try convert to List<Float> just in case it's Double/BigDecimal
                    value = toFloatList(value);
                }
            }

            fields.add(new InsertParam.Field(colName, Collections.singletonList(value)));
        }

        InsertParam.Builder insertBuilder = InsertParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withFields(fields);

        if (StringUtils.isNotBlank(partitionName)) {
            insertBuilder.withPartitionName(partitionName);
        }

        R<?> result = cmd.getClient().insert(insertBuilder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 1);
        return completed(future);
    }

    public static Future<?> execDeleteCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DeleteCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;

        // 1. Try SortClause (KNN Search Delete)
        if (c.sortClause() != null) {
            return execDeleteBySearch(future, cmd, collectionName, partitionName, c.sortClause(), c.limit, c.expression(), argIndex, request, receive);
        }

        // 2. Try Vector Range Expression (Range Search Delete)
        VectorRangeExpr vectorRange = parseVectorRange(c.expression(), argIndex, request);
        if (vectorRange != null) {
            return execDeleteByRange(future, cmd, collectionName, partitionName, vectorRange, argIndex, request, receive);
        }

        // 3. Normal Scalar Delete
        String expr = (c.expression() != null) ? rebuildExpression(argIndex, request, c.expression()) : "";
        if (StringUtils.isBlank(expr)) {
            throw new SQLException("DELETE must have a WHERE clause.");
        }

        DeleteParam.Builder deleteBuilder = DeleteParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withExpr(expr);

        if (StringUtils.isNotBlank(partitionName)) {
            deleteBuilder.withPartitionName(partitionName);
        }

        R<?> result = cmd.getClient().delete(deleteBuilder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, Statement.SUCCESS_NO_INFO);
        return completed(future);
    }

    private static Future<?> execDeleteBySearch(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            SortClauseContext sortClause, Token limit, ExpressionContext filterExpr, //
            AtomicInteger argIndex, AdapterRequest request, AdapterReceive receive) throws SQLException {

        String annsField = getIdentifier(sortClause.fieldName.getText());
        Object rawVector = null;
        if (sortClause.vectorValue().listLiteral() != null) {
            rawVector = parseListLiteral(sortClause.vectorValue().listLiteral(), argIndex, request);
        } else if (sortClause.vectorValue().ARG() != null) {
            rawVector = getArg(argIndex, request);
        }

        List<Float> vectorValue = toFloatList(rawVector);
        if (vectorValue.isEmpty()) {
            throw new SQLException("Vector value must be a non-empty list of numbers.");
        }

        int topK = 10; // default
        if (limit != null) {
            Object limitVal;
            if (limit.getType() == MilvusParser.ARG) {
                limitVal = getArg(argIndex, request);
            } else {
                limitVal = Integer.parseInt(limit.getText());
            }
            topK = ((Number) limitVal).intValue();
        }

        String filter = "";
        if (filterExpr != null) {
            filter = rebuildExpression(argIndex, request, filterExpr);
        }

        SearchParam.Builder searchBuilder = SearchParam.newBuilder().withCollectionName(collectionName).withMetricType(io.milvus.param.MetricType.L2) // Default to L2 for <->
                .withTopK(topK).withVectors(Collections.singletonList(vectorValue)).withVectorFieldName(annsField).withExpr(filter);

        if (partitionName != null) {
            searchBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        return executeSearchAndDelete(future, cmd, collectionName, partitionName, searchBuilder.build(), receive, request);
    }

    private static Future<?> execDeleteByRange(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            VectorRangeExpr rangeExpr, AtomicInteger argIndex, AdapterRequest request, AdapterReceive receive) throws SQLException {

        SearchParam.Builder searchBuilder = SearchParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withMetricType(io.milvus.param.MetricType.L2)//
                .withTopK(16384)// Use a large TopK to simulate "Delete All in Range"
                .withVectors(Collections.singletonList(toFloatList(rangeExpr.vectorValue)))//
                .withVectorFieldName(rangeExpr.fieldName)//
                .withExpr(rangeExpr.scalarFilter);

        // Add Radius param
        if (rangeExpr.radius != null) {
            searchBuilder.withParams("{\"radius\": " + rangeExpr.radius + ", \"range_filter\": " + 0.0 + "}");
        }

        if (partitionName != null) {
            searchBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        return executeSearchAndDelete(future, cmd, collectionName, partitionName, searchBuilder.build(), receive, request);
    }

    private static Future<?> executeSearchAndDelete(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName,//
            SearchParam searchParam, AdapterReceive receive, AdapterRequest request) throws SQLException {
        R<io.milvus.grpc.SearchResults> searchRes = cmd.getClient().search(searchParam);
        if (searchRes.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(searchRes.getMessage());
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(searchRes.getData().getResults());
        List<Object> idsToDelete = new ArrayList<>();
        if (!wrapper.getRowRecords().isEmpty()) {
            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
            for (SearchResultsWrapper.IDScore idScore : scores) {
                if (idScore.getLongID() != 0 || idScore.getStrID().isEmpty()) {
                    idsToDelete.add(idScore.getLongID());
                } else {
                    idsToDelete.add(idScore.getStrID());
                }
            }
        }

        if (idsToDelete.isEmpty()) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        // Get Primary Key Name
        R<DescribeCollectionResponse> descRes = cmd.getClient().describeCollection(DescribeCollectionParam.newBuilder()//
                .withCollectionName(collectionName)//
                .build());

        if (descRes.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException("Failed to describe collection to find Primary Key: " + descRes.getMessage());
        }
        String pkName = descRes.getData().getSchema().getFieldsList().stream()//
                .filter(FieldSchema::getIsPrimaryKey)//
                .map(FieldSchema::getName)//
                .findFirst()//
                .orElse("id"); // fallback

        // Batch delete
        String idListStr = idsToDelete.stream().map(id -> {
            if (id instanceof String)
                return "\"" + id + "\"";
            return String.valueOf(id);
        }).collect(Collectors.joining(","));

        String idExpr = pkName + " in [" + idListStr + "]";
        DeleteParam.Builder deleteBuilder = DeleteParam.newBuilder().withCollectionName(collectionName).withExpr(idExpr);

        if (partitionName != null) {
            deleteBuilder.withPartitionName(partitionName);
        }

        R<?> delRes = cmd.getClient().delete(deleteBuilder.build());
        if (delRes.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(delRes.getMessage());
        }

        receive.responseUpdateCount(request, idsToDelete.size());
        return completed(future);
    }

    // ... import, load, release ...
    public static Future<?> execImportCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ImportCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;
        String fileName = getIdentifier(c.fileName.getText());

        BulkInsertParam.Builder builder = BulkInsertParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withFiles(Collections.singletonList(fileName));

        if (partitionName != null) {
            builder.withPartitionName(partitionName);
        }

        R<ImportResponse> result = cmd.getClient().bulkInsert(builder.build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0); // Bulk insert is async, return 0
        return completed(future);
    }

    public static Future<?> execLoadCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, LoadCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = getIdentifier(c.collectionName.getText());

        if (c.partitionName != null) {
            String partitionName = getIdentifier(c.partitionName.getText());
            LoadPartitionsParam param = LoadPartitionsParam.newBuilder()//
                    .withCollectionName(collectionName)//
                    .withPartitionNames(Collections.singletonList(partitionName))//
                    .build();

            R<?> result = cmd.getClient().loadPartitions(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
        } else {
            LoadCollectionParam param = LoadCollectionParam.newBuilder()//
                    .withCollectionName(collectionName)//
                    .build();

            R<?> result = cmd.getClient().loadCollection(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execReleaseCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ReleaseCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String collectionName = getIdentifier(c.collectionName.getText());

        if (c.partitionName != null) {
            String partitionName = getIdentifier(c.partitionName.getText());
            ReleasePartitionsParam param = ReleasePartitionsParam.newBuilder()//
                    .withCollectionName(collectionName)//
                    .withPartitionNames(Collections.singletonList(partitionName))//
                    .build();

            R<?> result = cmd.getClient().releasePartitions(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
        } else {
            ReleaseCollectionParam param = ReleaseCollectionParam.newBuilder()//
                    .withCollectionName(collectionName)//
                    .build();

            R<?> result = cmd.getClient().releaseCollection(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }
}
