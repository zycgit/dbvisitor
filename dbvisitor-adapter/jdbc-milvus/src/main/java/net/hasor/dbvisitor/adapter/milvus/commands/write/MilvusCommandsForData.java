/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.write;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.QueryResultsWrapper.RowRecord;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.SearchIteratorReqV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.MilvusRequest;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.Filter;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusRetry;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusVectorCodec;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import org.antlr.v4.runtime.Token;
import static net.hasor.dbvisitor.adapter.milvus.MilvusRequest.checkActive;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.parseTerm;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.parseWhere;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusVector.*;
import static net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema.collectionFields;
import static net.hasor.dbvisitor.adapter.milvus.mapping.MilvusSchema.convertFieldValue;

public final class MilvusCommandsForData extends MilvusCommands {
    private MilvusCommandsForData() {
    }

    // UPDATE: select primary keys, then write only the SET fields

    public static Future<?> execUpdateCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, UpdateCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;

        // Bind SET before WHERE, ORDER BY and LIMIT to preserve JDBC parameter order.
        if (c.sortClause() != null && c.sortClause().vectorValue() == null) {
            throw new java.sql.SQLFeatureNotSupportedException("Scalar ORDER BY is not supported for UPDATE.");
        }
        Map<String, Object> newValues = new HashMap<>();
        for (SetClauseContext setCtx : c.setClauseList().setClause()) {
            String colName = getIdentifier(setCtx.columnName.getText());
            Object val = parseTerm(setCtx.value, argIndex, request);
            newValues.put(colName, val);
        }
        Map<String, FieldSchema> fields = cmd.describeFields(collectionName, request);
        String pkName = fields.values().stream().filter(FieldSchema::getIsPrimaryKey).map(FieldSchema::getName).findFirst().orElseThrow(() -> new SQLException("Collection has no primary key: " + collectionName));
        if (newValues.containsKey(pkName)) {
            throw new SQLException("Milvus UPDATE cannot modify primary key field '" + pkName + "'.");
        }
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            entry.setValue(convertFieldValue(fields.get(entry.getKey()), entry.getValue()));
        }

        // ORDER BY selects the nearest entities.
        if (c.sortClause() != null && c.sortClause().vectorValue() != null) {
            return execUpdateBySearch(future, cmd, collectionName, partitionName, pkName, c, argIndex, request, receive, newValues);
        }

        // A vector range uses search; scalar predicates use query.
        VectorRangeExpr vectorRange = parseVectorRange(c.expression(), argIndex, request);
        if (vectorRange != null) {
            Integer topK = readTopK(c.limit, argIndex, request);
            return execUpdateByRangeIterator(future, cmd, collectionName, partitionName, pkName, vectorRange, topK, request, receive, newValues);
        }

        // Scalar UPDATE
        Filter expr = parseWhere(c.expression(), argIndex, request);

        Long limit = readLimit(c.limit, argIndex, request);
        return execUpdateByQueryIterator(future, cmd, collectionName, partitionName, pkName, expr, limit, request, receive, newValues);
    }

    private static Future<?> execUpdateBySearch(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, String pkName, UpdateCmdContext c,//
            AtomicInteger argIndex, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {

        // WHERE (parse first)
        if (isVectorRange(c.expression())) {
            throw new SQLException("Combining a vector range with ORDER BY is not supported.");
        }
        Filter filter = parseWhere(c.expression(), argIndex, request);

        // ORDER BY (parse second)
        SortClauseContext sortClause = c.sortClause();
        String annsField = getIdentifier(sortClause.fieldName.getText());
        Object rawVector = readVectorValue(sortClause.vectorValue(), argIndex, request);

        // LIMIT
        Integer topK = readTopK(c.limit, argIndex, request);
        return execUpdateBySearchIterator(future, cmd, collectionName, partitionName, pkName, annsField, rawVector, //
                filter, vectorMetric(sortClause.distanceOperator()), topK, request, receive, newValues);
    }

    private static Future<?> execUpdateBySearchIterator(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, String pkName, //
            String annsField, Object vectorValue, Filter filter, MetricType metricType, Integer topK, //
            AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {
        SearchIteratorReqV2.SearchIteratorReqV2Builder builder = newSearchIteratorBuilder(cmd, collectionName, partitionName, annsField, vectorValue, filter, //
                Collections.singletonList(pkName), metricType, request);
        if (topK != null) {
            builder.limit(topK.longValue());
        }
        checkActive(request);
        SearchIteratorV2 result = cmd.searchIteratorV2(builder.build());

        long updateCount = writePages(request, "UPDATE", result, page -> executeUpdatePage(cmd, collectionName, partitionName, pkName, page, newValues, request));
        receive.responseUpdateCount(request, updateCount);
        return completed(future);
    }

    private static Future<?> execUpdateByQueryIterator(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, String pkName, Filter expr, //
            Long limit, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {
        QueryIteratorReq.QueryIteratorReqBuilder builder = newQueryIteratorBuilder(cmd, collectionName, partitionName, pkName, expr, limit, request);

        checkActive(request);
        QueryIterator result = cmd.queryIterator(builder.build());

        long updateCount = writePages(request, "UPDATE", result, page -> executeUpdatePage(cmd, collectionName, partitionName, pkName, page, newValues, request));
        receive.responseUpdateCount(request, updateCount);
        return completed(future);
    }

    private static Future<?> execUpdateByRangeIterator(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, String pkName, //
            VectorRangeExpr rangeExpr, Integer topK, AdapterRequest request, AdapterReceive receive, Map<String, Object> newValues) throws SQLException {
        SearchIteratorReqV2.SearchIteratorReqV2Builder builder = newRangeIteratorBuilder(cmd, collectionName, partitionName, rangeExpr, Collections.singletonList(pkName), request);
        if (topK != null) {
            builder.limit(topK.longValue());
        }
        checkActive(request);
        SearchIteratorV2 result = cmd.searchIteratorV2(builder.build());

        long updateCount = writePages(request, "UPDATE", result, page -> executeUpdatePage(cmd, collectionName, partitionName, pkName, page, newValues, request));
        receive.responseUpdateCount(request, updateCount);
        return completed(future);
    }

    private static long executeUpdatePage(MilvusCmd cmd, String collectionName, String partitionName, String pkName, //
            List<QueryResultsWrapper.RowRecord> page, Map<String, Object> newValues, AdapterRequest request) throws SQLException {
        if (page.isEmpty()) {
            return 0;
        }

        Gson gson = new com.google.gson.GsonBuilder().serializeNulls().create();
        List<JsonObject> records = new ArrayList<>(page.size());
        for (QueryResultsWrapper.RowRecord row : page) {
            Object primaryKey = row.get(pkName);
            if (primaryKey == null) {
                throw new SQLException("Milvus UPDATE selection did not return primary key field '" + pkName + "'.");
            }
            JsonObject record = new JsonObject();
            record.add(pkName, gson.toJsonTree(primaryKey));
            for (Map.Entry<String, Object> entry : newValues.entrySet()) {
                record.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
            }
            records.add(record);
        }

        UpsertReq.UpsertReqBuilder builder = UpsertReq.builder()//
                .collectionName(collectionName)//
                .data(records)//
                .partialUpdate(true);
        if (StringUtils.isNotBlank(cmd.getCatalog())) {
            builder.databaseName(cmd.getCatalog());
        }
        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionName(partitionName);
        }
        UpsertReq upsertRequest = builder.build();
        UpsertResp response = MilvusRetry.execute(request, "Milvus page partial upsert", () -> cmd.upsert(upsertRequest));
        if (response.getUpsertCnt() != records.size()) {
            throw new SQLException("Milvus page partial upsert returned an unexpected affected row count: expected=" + records.size() + ", actual=" + response.getUpsertCnt());
        }
        return response.getUpsertCnt();
    }

    // DELETE: native scalar deletion, or primary-key deletion in bounded pages

    public static Future<?> execDeleteCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DeleteCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        if (c.sortClause() != null && c.sortClause().vectorValue() == null) {
            throw new java.sql.SQLFeatureNotSupportedException("Scalar ORDER BY is not supported for DELETE.");
        }

        String collectionName = getIdentifier(c.collectionName.getText());
        String partitionName = c.partitionName != null ? getIdentifier(c.partitionName.getText()) : null;

        // ORDER BY selects the nearest entities.
        if (c.sortClause() != null && c.sortClause().vectorValue() != null) {
            return execDeleteBySearch(future, cmd, collectionName, partitionName, c.sortClause(), c.limit, c.expression(), argIndex, request, receive);
        }

        // A vector range uses search.
        VectorRangeExpr vectorRange = parseVectorRange(c.expression(), argIndex, request);
        if (vectorRange != null) {
            Integer topK = readTopK(c.limit, argIndex, request);
            return execDeleteByRangeIterator(future, cmd, collectionName, partitionName, vectorRange, topK, request, receive);
        }

        // Without LIMIT, a scalar filter can be deleted directly by the server.
        Filter expr;
        if (c.expression() == null) {
            // Primary keys cannot be NULL. Unlike a constant true expression, this filter also works on 2.6.2.
            String primaryKey = getPrimaryKeyName(cmd, collectionName);
            expr = new Filter(primaryKey + " is not null", Collections.emptyMap());
        } else {
            expr = parseWhere(c.expression(), argIndex, request);
        }

        if (c.limit != null) {
            Long limit = readLimit(c.limit, argIndex, request);
            return execDeleteByQueryIterator(future, cmd, collectionName, partitionName, expr, limit, request, receive);
        }

        DeleteReq.DeleteReqBuilder deleteBuilder = DeleteReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName)//
                .filter(expr.expression()).filterTemplateValues(expr.parameters());

        if (StringUtils.isNotBlank(partitionName)) {
            deleteBuilder.partitionName(partitionName);
        }

        DeleteReq deleteParam = deleteBuilder.build();
        DeleteResp result = MilvusRetry.execute(request, "Milvus delete", () -> cmd.delete(deleteParam));

        receive.responseUpdateCount(request, result.getDeleteCnt());
        return completed(future);
    }

    private static Future<?> execDeleteBySearch(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            SortClauseContext sortClause, Token limit, ExpressionContext filterExpr, //
            AtomicInteger argIndex, AdapterRequest request, AdapterReceive receive) throws SQLException {

        if (isVectorRange(filterExpr)) {
            throw new SQLException("Combining a vector range with ORDER BY is not supported.");
        }
        Filter filter = parseWhere(filterExpr, argIndex, request);
        String annsField = getIdentifier(sortClause.fieldName.getText());
        Object rawVector = readVectorValue(sortClause.vectorValue(), argIndex, request);

        Integer topK = readTopK(limit, argIndex, request);
        return execDeleteBySearchIterator(future, cmd, collectionName, partitionName, annsField, rawVector, //
                filter, vectorMetric(sortClause.distanceOperator()), topK, request, receive);
    }

    private static Future<?> execDeleteBySearchIterator(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            String annsField, Object vectorValue, Filter filter, MetricType metricType, Integer topK, AdapterRequest request, AdapterReceive receive) throws SQLException {
        String pkName = getPrimaryKeyName(cmd, collectionName);
        SearchIteratorReqV2.SearchIteratorReqV2Builder builder = newSearchIteratorBuilder(cmd, collectionName, partitionName, annsField, vectorValue, filter, //
                Collections.singletonList(pkName), metricType, request);
        if (topK != null) {
            builder.limit(topK.longValue());
        }
        checkActive(request);
        SearchIteratorV2 result = cmd.searchIteratorV2(builder.build());

        long deleteCount = writePages(request, "DELETE", result, page -> executeDeletePage(cmd, collectionName, partitionName, pkName, page, request));
        receive.responseUpdateCount(request, deleteCount);
        return completed(future);
    }

    private static Future<?> execDeleteByRangeIterator(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, //
            VectorRangeExpr rangeExpr, Integer topK, AdapterRequest request, AdapterReceive receive) throws SQLException {
        String pkName = getPrimaryKeyName(cmd, collectionName);
        SearchIteratorReqV2.SearchIteratorReqV2Builder builder = newRangeIteratorBuilder(cmd, collectionName, partitionName, rangeExpr, Collections.singletonList(pkName), request);
        if (topK != null) {
            builder.limit(topK.longValue());
        }
        checkActive(request);
        SearchIteratorV2 result = cmd.searchIteratorV2(builder.build());

        long deleteCount = writePages(request, "DELETE", result, page -> executeDeletePage(cmd, collectionName, partitionName, pkName, page, request));
        receive.responseUpdateCount(request, deleteCount);
        return completed(future);
    }

    private static Future<?> execDeleteByQueryIterator(Future<Object> future, MilvusCmd cmd, String collectionName, String partitionName, Filter expr, //
            Long limit, AdapterRequest request, AdapterReceive receive) throws SQLException {
        String pkName = getPrimaryKeyName(cmd, collectionName);

        QueryIteratorReq.QueryIteratorReqBuilder queryBuilder = newQueryIteratorBuilder(cmd, collectionName, partitionName, pkName, expr, limit, request);

        checkActive(request);
        QueryIterator queryResult = cmd.queryIterator(queryBuilder.build());

        long deleteCount = writePages(request, "DELETE", queryResult, page -> executeDeletePage(cmd, collectionName, partitionName, pkName, page, request));
        receive.responseUpdateCount(request, deleteCount);
        return completed(future);
    }

    private static String getPrimaryKeyName(MilvusCmd cmd, String collectionName) throws SQLException {
        DescribeCollectionResp descRes = cmd.describeCollection(DescribeCollectionReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName)//
                .build());
        return collectionFields(descRes).stream()//
                .filter(FieldSchema::getIsPrimaryKey)//
                .map(FieldSchema::getName)//
                .findFirst()//
                .orElseThrow(() -> new SQLException("Collection has no primary key: " + collectionName));
    }

    private static long executeDeletePage(MilvusCmd cmd, String collectionName, String partitionName, String pkName, //
            List<QueryResultsWrapper.RowRecord> page, AdapterRequest request) throws SQLException {
        List<Object> idsToDelete = page.stream()//
                .map(row -> row.getFieldValues().get(pkName))//
                .filter(Objects::nonNull)//
                .collect(Collectors.toList());
        if (idsToDelete.isEmpty()) {
            return 0;
        }

        DeleteReq.DeleteReqBuilder deleteBuilder = DeleteReq.builder().databaseName(cmd.getCatalog()).collectionName(collectionName).filter(pkName + " in {ids}").filterTemplateValues(Collections.singletonMap("ids", idsToDelete));

        if (partitionName != null) {
            deleteBuilder.partitionName(partitionName);
        }

        DeleteReq deleteParam = deleteBuilder.build();
        DeleteResp delRes = MilvusRetry.execute(request, "Milvus page delete", () -> cmd.delete(deleteParam));

        long deleteCount = delRes.getDeleteCnt();
        if (deleteCount > idsToDelete.size()) {
            throw new SQLException("Milvus deleted more rows than selected: selected=" + idsToDelete.size() + ", deleted=" + deleteCount);
        }
        return deleteCount;
    }

    // Shared selection requests for paged UPDATE and DELETE

    private static QueryIteratorReq.QueryIteratorReqBuilder newQueryIteratorBuilder(MilvusCmd cmd, String collectionName, String partitionName, String pkName, Filter expr, Long limit, AdapterRequest request) {
        QueryIteratorReq.QueryIteratorReqBuilder builder = QueryIteratorReq.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName)//
                .expr(expr.expression()).filterTemplateValues(expr.parameters())//
                .outputFields(Collections.singletonList(pkName));
        if (limit != null) {
            builder.limit(limit);
        }
        applyFetchSize(request, builder);
        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionNames(Collections.singletonList(partitionName));
        }
        applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
        return builder;
    }

    private static SearchIteratorReqV2.SearchIteratorReqV2Builder newRangeIteratorBuilder(MilvusCmd cmd, String collectionName, String partitionName, VectorRangeExpr rangeExpr, //
            List<String> outFields, AdapterRequest request) throws SQLException {
        SearchIteratorReqV2.SearchIteratorReqV2Builder builder = newSearchIteratorBuilder(cmd, collectionName, partitionName, rangeExpr.fieldName, rangeExpr.vectorValue, rangeExpr.scalarFilter, outFields, rangeExpr.metricType, request);
        builder.searchParams(new LinkedHashMap<>(Collections.singletonMap(MilvusCommandKeys.RADIUS, rangeExpr.radius)));
        return builder;
    }

    private static SearchIteratorReqV2.SearchIteratorReqV2Builder newSearchIteratorBuilder(MilvusCmd cmd, String collectionName, String partitionName, String annsField, //
            Object vectorValue, Filter filter, List<String> outFields, MetricType metricType, AdapterRequest request) throws SQLException {
        FieldSchema field = cmd.describeFields(collectionName, request).get(annsField);
        SearchIteratorReqV2.SearchIteratorReqV2Builder builder = SearchIteratorReqV2.builder().databaseName(cmd.getCatalog())//
                .collectionName(collectionName)//
                .metricType(metricType)//
                .vectors(Collections.singletonList(MilvusVectorCodec.searchValue(field, vectorValue, metricType)))//
                .vectorFieldName(annsField)//
                .filter(filter.expression()).filterTemplateValues(filter.parameters())//
                .outputFields(outFields);
        if (StringUtils.isNotBlank(partitionName)) {
            builder.partitionNames(Collections.singletonList(partitionName));
        }
        applyConsistencyLevel(((MilvusRequest) request).getConsistencyLevel(), builder);
        applyFetchSize(request, builder);
        return builder;
    }

    // Paged DML: the command supplies the single-page write operation.

    @FunctionalInterface
    private interface PageWriter {
        long write(List<RowRecord> page) throws SQLException;
    }

    private static long writePages(AdapterRequest request, String operation, QueryIterator iterator, PageWriter writer) throws SQLException {
        if (iterator == null) {
            return 0;
        }
        return writePages(request, operation, "query", iterator::next, iterator::close, writer);
    }

    private static long writePages(AdapterRequest request, String operation, SearchIteratorV2 iterator, PageWriter writer) throws SQLException {
        if (iterator == null) {
            return 0;
        }
        return writePages(request, operation, "search", () -> searchRecords(iterator.next()), iterator::close, writer);
    }

    private static long writePages(AdapterRequest request, String operation, String iteratorType, Supplier<List<RowRecord>> nextPage, Runnable closeIterator, PageWriter writer) throws SQLException {
        long confirmedRows = 0;
        long confirmedPages = 0;
        long pageNumber = 1;
        int currentPageRows = 0;
        String phase = "read";
        Throwable failure = null;
        try {
            while (true) {
                phase = "read";
                pageNumber = confirmedPages + 1;
                currentPageRows = 0;
                checkActive(request);
                List<RowRecord> page = nextPage.get();
                checkActive(request);
                if (page == null || page.isEmpty()) {
                    break;
                }

                phase = "write";
                currentPageRows = page.size();
                confirmedRows = Math.addExact(confirmedRows, writer.write(page));
                confirmedPages++;
            }
        } catch (SQLException | RuntimeException e) {
            SQLException error = pageFailure(operation, iteratorType, phase, pageNumber, confirmedPages, confirmedRows, currentPageRows, e);
            failure = error;
            throw error;
        } catch (Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                closeIterator.run();
            } catch (RuntimeException closeFailure) {
                if (failure != null) {
                    failure.addSuppressed(closeFailure);
                } else {
                    throw pageFailure(operation, iteratorType, "close", pageNumber, confirmedPages, confirmedRows, currentPageRows, closeFailure);
                }
            }
        }
        return confirmedRows;
    }

    private static SQLException pageFailure(String operation, String iteratorType, String phase, long pageNumber, long confirmedPages, long confirmedRows, int currentPageRows, Exception cause) {
        SQLException error = MilvusRetry.sqlException(cause);
        // @formatter:off
        String message = "Milvus " + operation + " failed during paged execution: phase=" + phase
                + ", iterator=" + iteratorType
                + ", page=" + pageNumber
                + ", confirmedPages=" + confirmedPages
                + ", confirmedRows=" + confirmedRows
                + ", currentPageRows=" + currentPageRows
                + ", cause=" + error.getMessage();
        // @formatter:on
        if (error instanceof java.sql.SQLTimeoutException) {
            return new java.sql.SQLTimeoutException(message, error.getSQLState(), error.getErrorCode(), error);
        }
        return new SQLException(message, error.getSQLState(), error.getErrorCode(), error);
    }
}
