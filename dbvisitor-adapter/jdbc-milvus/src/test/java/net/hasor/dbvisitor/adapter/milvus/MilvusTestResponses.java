/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.service.vector.response.*;
import io.milvus.v2.utils.ConvertUtils;
import org.mockito.Mockito;

/** Reuse wire-level result fixtures through the SDK's own V2 converters. */
public final class MilvusTestResponses {
    public static Object v2Response(String method, Object value) {
        ConvertUtils converter = new ConvertUtils();
        if (value instanceof DescribeCollectionResponse) {
            return converter.convertDescCollectionResp((DescribeCollectionResponse) value);
        }
        if (value instanceof QueryResults) {
            if ("queryIterator".equals(method)) {
                QueryIterator iterator = Mockito.mock(QueryIterator.class);
                Mockito.when(iterator.next()).thenReturn(new QueryResultsWrapper((QueryResults) value).getRowRecords(), Collections.emptyList());
                return iterator;
            }
            return QueryResp.builder().queryResults(converter.getEntities((QueryResults) value)).build();
        }
        if (value instanceof SearchResults result) {
            if ("searchIteratorV2".equals(method)) {
                SearchIteratorV2 iterator = Mockito.mock(SearchIteratorV2.class);
                Mockito.when(iterator.next()).thenReturn(result.getResults().getNumQueries() == 0 ? Collections.emptyList() : converter.getEntities(result).get(0), Collections.emptyList());
                return iterator;
            }
            return SearchResp.builder().searchResults(converter.getEntities((SearchResults) value)).build();
        }
        if (value instanceof MutationResult mutation) {
            switch (method) {
                case "insert":
                    return InsertResp.builder().InsertCnt(mutation.getInsertCnt()).build();
                case "upsert":
                    return UpsertResp.builder().upsertCnt(mutation.getUpsertCnt()).build();
                case "delete":
                    return DeleteResp.builder().deleteCnt(mutation.getDeleteCnt()).build();
                default:
                    throw new AssertionError("Unexpected mutation call: " + method);
            }
        }
        if (value instanceof Status) {
            return null;
        }
        return value;
    }

    public static List<SearchResp.SearchResult> searchHits(String primaryKey, List<QueryResultsWrapper.RowRecord> rows) {
        return rows.stream().map(row -> SearchResp.SearchResult.builder().primaryKey(primaryKey).id(row.get(primaryKey))
                .score(row.get("score") instanceof Number score ? score.floatValue() : 0F).entity(row.getFieldValues()).build()).collect(Collectors.toList());
    }
}
