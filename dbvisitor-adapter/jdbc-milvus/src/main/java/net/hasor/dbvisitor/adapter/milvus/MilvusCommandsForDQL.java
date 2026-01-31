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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

class MilvusCommandsForDQL extends MilvusCommands {
    public static Future<?> execSelectCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, SelectCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;
        List<String> outFields = new ArrayList<>();
        Map<String, String> properties = readProperties(argIndex, request, c.propertiesList());

        // Select Elements
        boolean isStar = c.selectElements().STAR() != null;
        if (isStar) {
            outFields.add("*");
        } else {
            for (SelectElementContext ele : c.selectElements().selectElement()) {
                outFields.add(getIdentifier(ele.fieldName.getText()));
            }
        }

        // Check for Vector Search (ORDER BY vector <-> vector)
        SortClauseContext sortCtx = c.sortClause();
        boolean isSearch = sortCtx != null;

        List<Map<String, Object>> resultData;

        if (isSearch) {
            String expr = (c.expression() != null) ? rebuildExpression(argIndex, request, c.expression()) : "";
            resultData = execSearch(cmd, collectionName, partitionName, expr, outFields, sortCtx, c, argIndex, request, properties);
        } else {
            // Check if expression contains Vector Range (making it a search)
            AtomicInteger tempIndex = new AtomicInteger(argIndex.get());
            VectorRangeExpr rangeExpr = (c.expression() != null) ? parseVectorRange(c.expression(), tempIndex, request) : null;
            if (rangeExpr != null) {
                // It is a range search. Update main argIndex
                argIndex.set(tempIndex.get());
                resultData = execRangeSearch(cmd, collectionName, partitionName, rangeExpr, outFields, c, argIndex, request, properties);
            } else {
                // Scalar Query
                String expr = (c.expression() != null) ? rebuildExpression(argIndex, request, c.expression()) : "";
                resultData = execQuery(cmd, collectionName, partitionName, expr, outFields, c, argIndex, request);
            }
        }

        // Build Result Cursor
        List<JdbcColumn> columns = new ArrayList<>();
        if (resultData != null && !resultData.isEmpty()) {
            Map<String, Object> firstRow = resultData.get(0);
            for (String key : firstRow.keySet()) {
                columns.add(new JdbcColumn(key, AdapterType.String, "", "", ""));
            }
        } else {
            if (!isStar) {
                for (String field : outFields) {
                    columns.add(new JdbcColumn(field, AdapterType.String, "", "", ""));
                }
            }
        }

        receive.responseResult(request, listResult(request, columns, resultData));
        return completed(future);
    }

    private static List<Map<String, Object>> execSearch(MilvusCmd cmd, String collectionName, String partitionName, String expr, List<String> outFields, //
            SortClauseContext sortCtx, SelectCmdContext c, AtomicInteger argIndex, AdapterRequest request, Map<String, String> properties) throws SQLException {
        // Search
        String annsField = getIdentifier(sortCtx.fieldName.getText());
        Object vectorData = null;
        if (sortCtx.vectorValue().ARG() != null) {
            vectorData = getArg(argIndex, request);
        } else {
            vectorData = parseListLiteral(sortCtx.vectorValue().listLiteral(), argIndex, request);
        }

        // Ensure vectorData is a List<List<Float>>
        List<List<Float>> vectors = new ArrayList<>();
        if (vectorData instanceof List) {
            List<?> listVector = (List<?>) vectorData;
            if (!listVector.isEmpty() && listVector.get(0) instanceof List) {
                // List of Vectors
                for (Object v : listVector) {
                    vectors.add(toFloatList(v));
                }
            } else {
                // Single Vector
                vectors.add(toFloatList(listVector));
            }
        } else {
            // Try to treat as single vector (e.g. if parsed somewhat differently, though unlikely for vector field)
            vectors.add(toFloatList(vectorData));
        }

        Integer topK = null;
        if (c.limit != null) {
            if (c.limit.getText().startsWith("?")) {
                topK = ((Number) getArg(argIndex, request)).intValue();
            } else {
                topK = Integer.parseInt(c.limit.getText());
            }
        }
        if (topK == null) {
            topK = 10; // Default TopK
        }

        SearchParam.Builder searchBuilder = SearchParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withExpr(expr)//
                .withVectorFieldName(annsField)//
                .withVectors(vectors)//
                .withTopK(topK)//
                .withOutFields(outFields);

        if (StringUtils.isNotBlank(partitionName)) {
            searchBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        // Params
        try {
            if (!properties.isEmpty()) {
                StringBuilder json = new StringBuilder("{");
                int idx = 0;
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if (idx > 0)
                        json.append(",");
                    json.append("\"").append(entry.getKey()).append("\":");
                    String v = entry.getValue();
                    if (v.matches("-?\\d+(\\.\\d+)?") || "true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                        json.append(v);
                    } else {
                        json.append("\"").append(v).append("\"");
                    }
                    idx++;
                }
                json.append("}");
                searchBuilder.withParams(json.toString());
            }
        } catch (Exception e) {
            throw new SQLException("Failed to serialize properties: " + e.getMessage());
        }

        R<SearchResults> callback = cmd.getClient().search(searchBuilder.build());
        if (callback.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(callback.getMessage());
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(callback.getData().getResults());
        return wrapper.getRowRecords().stream().map(row -> (Map<String, Object>) row.getFieldValues()).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> execRangeSearch(MilvusCmd cmd, String collectionName, String partitionName, VectorRangeExpr rangeExpr, List<String> outFields, //
            SelectCmdContext c, AtomicInteger argIndex, AdapterRequest request, Map<String, String> properties) throws SQLException {

        // Similar to execSearch but with explicit radius
        List<Float> vector = toFloatList(rangeExpr.vectorValue);

        Integer topK = null;
        if (c.limit != null) {
            if (c.limit.getText().startsWith("?")) {
                topK = ((Number) getArg(argIndex, request)).intValue();
            } else {
                topK = Integer.parseInt(c.limit.getText());
            }
        }
        if (topK == null) {
            topK = 16384; // Default large TopK for range search if not specified, or limitation?
            // Actually, for range search, usually we want all matches within radius. But Milvus still requires TopK.
        }

        SearchParam.Builder searchBuilder = SearchParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withExpr(rangeExpr.scalarFilter)//
                .withVectorFieldName(rangeExpr.fieldName)//
                .withVectors(Collections.singletonList(vector))//
                .withTopK(topK)//
                .withOutFields(outFields);

        if (StringUtils.isNotBlank(partitionName)) {
            searchBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        // Add Radius to properties
        properties.put("radius", String.valueOf(rangeExpr.radius));
        // properties.put("range_filter", "0.0"); // Optional: if we want ring search, but assume < radius for now.

        // Params
        try {
            if (!properties.isEmpty()) {
                StringBuilder json = new StringBuilder("{");
                int idx = 0;
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if (idx > 0)
                        json.append(",");
                    json.append("\"").append(entry.getKey()).append("\":");
                    String v = entry.getValue();
                    if (v.matches("-?\\d+(\\.\\d+)?") || "true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                        json.append(v);
                    } else {
                        json.append("\"").append(v).append("\"");
                    }
                    idx++;
                }
                json.append("}");
                searchBuilder.withParams(json.toString());
            }
        } catch (Exception e) {
            throw new SQLException("Failed to serialize properties: " + e.getMessage());
        }

        R<SearchResults> callback = cmd.getClient().search(searchBuilder.build());
        if (callback.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(callback.getMessage());
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(callback.getData().getResults());
        return wrapper.getRowRecords().stream().map(row -> (Map<String, Object>) row.getFieldValues()).collect(Collectors.toList());
    }

    private static List<Map<String, Object>> execQuery(MilvusCmd cmd, String collectionName, String partitionName, String expr, List<String> outFields, //
            SelectCmdContext c, AtomicInteger argIndex, AdapterRequest request) throws SQLException {
        // Query
        QueryParam.Builder queryBuilder = QueryParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withExpr(expr)//
                .withOutFields(outFields);

        if (StringUtils.isNotBlank(partitionName)) {
            queryBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        if (c.limit != null) {
            long limitVal;
            if (c.limit.getText().startsWith("?")) {
                limitVal = ((Number) getArg(argIndex, request)).longValue();
            } else {
                limitVal = Long.parseLong(c.limit.getText());
            }
            queryBuilder.withLimit(limitVal);
        }

        if (c.offset != null) {
            long offsetVal;
            if (c.offset.getText().startsWith("?")) {
                offsetVal = ((Number) getArg(argIndex, request)).longValue();
            } else {
                offsetVal = Long.parseLong(c.offset.getText());
            }
            queryBuilder.withOffset(offsetVal);
        }

        R<QueryResults> callback = cmd.getClient().query(queryBuilder.build());
        if (callback.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(callback.getMessage());
        }

        QueryResultsWrapper wrapper = new QueryResultsWrapper(callback.getData());
        return wrapper.getRowRecords().stream().map(row -> row.getFieldValues()).collect(Collectors.toList());
    }

    public static Future<?> execCountCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CountCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;
        String expr = (c.expression() != null) ? rebuildExpression(argIndex, request, c.expression()) : "";

        // Query with count(*)
        QueryParam.Builder queryBuilder = QueryParam.newBuilder()//
                .withCollectionName(collectionName)//
                .withExpr(expr)//
                .withOutFields(Collections.singletonList("count(*)"));

        if (StringUtils.isNotBlank(partitionName)) {
            queryBuilder.withPartitionNames(Collections.singletonList(partitionName));
        }

        R<QueryResults> callback = cmd.getClient().query(queryBuilder.build());
        if (callback.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(callback.getMessage());
        }

        QueryResultsWrapper wrapper = new QueryResultsWrapper(callback.getData());

        long count = 0;
        // Milvus 2.2+ returns a single row with field "count(*)"
        if (!wrapper.getRowRecords().isEmpty()) {
            Map<String, Object> row = wrapper.getRowRecords().get(0).getFieldValues();
            Object val = row.get("count(*)");
            if (val instanceof Number) {
                count = ((Number) val).longValue();
            }
        }

        receive.responseResult(request, singleResult(request, COL_COUNT_LONG, count));
        return completed(future);
    }
}