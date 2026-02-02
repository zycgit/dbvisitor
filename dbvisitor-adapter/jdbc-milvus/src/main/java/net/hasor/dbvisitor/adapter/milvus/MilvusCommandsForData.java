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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.grpc.ImportResponse;
import io.milvus.grpc.QueryResults;
import io.milvus.param.R;
import io.milvus.param.bulkinsert.BulkInsertParam;
import io.milvus.param.collection.DescribeCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;
import io.milvus.param.dml.*;
import io.milvus.param.partition.LoadPartitionsParam;
import io.milvus.param.partition.ReleasePartitionsParam;
import io.milvus.response.QueryResultsWrapper;
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
        Map<String, Object> hints = readHints(argIndex, request, h.hint());
        boolean useUpsert = Boolean.TRUE.equals(hints.get("upsert"));

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

        if (useUpsert) {
            UpsertParam.Builder upsertBuilder = UpsertParam.newBuilder()//
                    .withCollectionName(collectionName)//
                    .withFields(fields);

            if (StringUtils.isNotBlank(partitionName)) {
                upsertBuilder.withPartitionName(partitionName);
            }

            R<?> result = cmd.getClient().upsert(upsertBuilder.build());
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
        } else {
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
        }

        receive.responseUpdateCount(request, 1);
        return completed(future);
    }

    public static Future<?> execUpdateCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, UpdateCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;

        // 1. Parse SET Clause FIRST (to consume args in order)
        Map<String, Object> newValues = new HashMap<>();
        for (SetClauseContext setCtx : c.setClauseList().setClause()) {
            String colName = getIdentifier(setCtx.columnName.getText());
            Object val = parseValue(setCtx.value, argIndex, request);
            newValues.put(colName, val);
        }

        // 2. Try SortClause (KNN Search Update)
        if (c.sortClause() != null) {
            return execUpdateBySearch(future, cmd, collectionName, partitionName, c, argIndex, request, receive, newValues);
        }

        // 3. Try Vector Range Expression (Range Search Update)
        VectorRangeExpr vectorRange = parseVectorRange(c.expression(), argIndex, request);
        if (vectorRange != null) {
            return execUpdateByRange(future, cmd, collectionName, partitionName, vectorRange, c, argIndex, request, receive, newValues);
        }

        // 4. Normal Scalar Update
        String expr = (c.expression() != null) ? rebuildExpression(argIndex, request, c.expression()) : "";

        long limit = 16384; // Default to a large number for UPDATE if not specified
        if (c.limit != null) {
            Object limitVal;
            if (c.limit.getType() == MilvusParser.ARG) {
                limitVal = getArg(argIndex, request);
            } else {
                limitVal = Long.parseLong(c.limit.getText());
            }
            limit = ((Number) limitVal).longValue();
        }

        QueryParam.Builder queryBuilder = QueryParam.newBuilder().withCollectionName(collectionName).withExpr(expr).withOutFields(Collections.singletonList("*")).withLimit(limit);

        if (StringUtils.isNotBlank(partitionName)) {
            queryBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        R<QueryResults> queryResult = cmd.getClient().query(queryBuilder.build());
        if (queryResult.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(queryResult.getMessage());
        }

        QueryResultsWrapper wrapper = new QueryResultsWrapper(queryResult.getData());
        List<Map<String, Object>> records = wrapper.getRowRecords().stream().map(r -> r.getFieldValues()).collect(Collectors.toList());

        return executeUpdateProcess(future, cmd, collectionName, partitionName, records, request, receive, newValues);
    }

    private static Future<?> execUpdateBySearch(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            UpdateCmdContext c, AtomicInteger argIndex, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {

        // WHERE (parse first)
        String filter = "";
        ExpressionContext filterExpr = c.expression();
        if (filterExpr != null) {
            filter = rebuildExpression(argIndex, request, filterExpr);
        }

        // ORDER BY (parse second)
        SortClauseContext sortClause = c.sortClause();
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

        // LIMIT
        int topK = 10; // default
        if (c.limit != null) {
            Object limitVal;
            if (c.limit.getType() == MilvusParser.ARG) {
                limitVal = getArg(argIndex, request);
            } else {
                limitVal = Integer.parseInt(c.limit.getText());
            }
            topK = ((Number) limitVal).intValue();
        }

        SearchParam.Builder searchBuilder = SearchParam.newBuilder().withCollectionName(collectionName).withMetricType(io.milvus.param.MetricType.L2) // Default to L2 for <->
                .withTopK(topK).withVectors(Collections.singletonList(vectorValue)).withVectorFieldName(annsField).withExpr(filter).withOutFields(Collections.singletonList("*"));

        if (partitionName != null) {
            searchBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        return executeSearchAndUpdate(future, cmd, collectionName, partitionName, searchBuilder.build(), request, receive, newValues);
    }

    private static Future<?> execUpdateByRange(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            VectorRangeExpr rangeExpr, UpdateCmdContext c, AtomicInteger argIndex, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {

        SearchParam.Builder searchBuilder = SearchParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withMetricType(io.milvus.param.MetricType.L2)//
                .withTopK(16384)// Use a large TopK to simulate "Update All in Range"
                .withVectors(Collections.singletonList(toFloatList(rangeExpr.vectorValue)))//
                .withVectorFieldName(rangeExpr.fieldName)//
                .withExpr(rangeExpr.scalarFilter)//
                .withOutFields(Collections.singletonList("*"));

        // Add Radius param
        if (rangeExpr.radius != null) {
            searchBuilder.withParams("{\"radius\": " + rangeExpr.radius + ", \"range_filter\": " + 0.0 + "}");
        }

        if (partitionName != null) {
            searchBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        return executeSearchAndUpdate(future, cmd, collectionName, partitionName, searchBuilder.build(), request, receive, newValues);
    }

    private static Future<?> executeSearchAndUpdate(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            SearchParam searchParam, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {
        R<io.milvus.grpc.SearchResults> searchRes = cmd.getClient().search(searchParam);
        if (searchRes.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(searchRes.getMessage());
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(searchRes.getData().getResults());
        List<Map<String, Object>> records = wrapper.getRowRecords().stream().map(r -> r.getFieldValues()).collect(Collectors.toList());

        return executeUpdateProcess(future, cmd, collectionName, partitionName, records, request, receive, newValues);
    }

    private static Future<?> executeUpdateProcess(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, List<Map<String, Object>> records, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {
        if (records.isEmpty()) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        // Apply updates to records
        for (Map<String, Object> rec : records) {
            rec.putAll(newValues);
        }

        // Transform to Column-based for Upsert
        Map<String, List<Object>> columnData = new HashMap<>();
        if (!records.isEmpty()) {
            Map<String, Object> first = records.get(0);
            for (String k : first.keySet())
                columnData.put(k, new ArrayList<>());
            for (String k : newValues.keySet())
                columnData.putIfAbsent(k, new ArrayList<>());
        }

        for (Map<String, Object> rec : records) {
            for (String col : columnData.keySet()) {
                columnData.get(col).add(rec.get(col));
            }
        }

        List<UpsertParam.Field> fields = new ArrayList<>();
        for (Map.Entry<String, List<Object>> entry : columnData.entrySet()) {
            fields.add(new UpsertParam.Field(entry.getKey(), entry.getValue()));
        }

        // Upsert
        UpsertParam.Builder upsertBuilder = UpsertParam.newBuilder().withCollectionName(collectionName).withFields(fields);

        if (StringUtils.isNotBlank(partitionName)) {
            upsertBuilder.withPartitionName(partitionName);
        }

        R<?> upsertResult = cmd.getClient().upsert(upsertBuilder.build());
        if (upsertResult.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(upsertResult.getMessage());
        }

        receive.responseUpdateCount(request, records.size());
        return completed(future);
    }

    private static Object parseValue(MilvusParser.TermContext term, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        if (term.literal() != null) {
            return parseLiteral(term.literal(), argIndex, request);
        }
        throw new SQLException("Unsupported value type in SET clause: " + term.getText());
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
