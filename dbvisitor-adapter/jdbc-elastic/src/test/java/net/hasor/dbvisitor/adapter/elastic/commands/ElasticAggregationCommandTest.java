/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;
import static org.junit.Assert.*;

/** The real JDBC pipeline with only HTTP responses intercepted. */
public class ElasticAggregationCommandTest extends AbstractElasticCommandTest {
    private static final String COMPOSITE = """
            POST /books/_search {
              "aggs": {"rows": {"composite": {"sources": [{"age": {"terms": {"field": "age","missing_bucket": true}}}]}}}
            }
            """;

    @Test
    public void metricAliasesPreserveNumericAndNullValues() throws Exception {
        respondWith("""
                {"timed_out":false,"_shards":{"failed":0},"aggregations":{"total":{"value":30.0},"maximum":{"value":null},"cnt":{"doc_count":0}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                        POST /books/_search {"aggs":{"total":{"sum":{"field":"age"}},"maximum":{"max":{"field":"age"}},"cnt":{"filter":{"match_all":{}}}}}
                        """)) {
            assertEquals(3, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals(30L, result.getLong("total"));
            assertNull(result.getObject("maximum"));
            assertEquals(0L, result.getLong("cnt"));
            assertFalse(result.next());
        }
        assertEquals(0, requestBody(0).get("size").intValue());
        assertEquals("/books/_search", requests.get(0).getEndpoint());
    }

    @Test
    public void compositeLoadsNextPageOnlyWhenConsumed() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"after_key":{"age":20},"buckets":[{"key":{"age":10},"doc_count":1},{"key":{"age":20},"doc_count":2}]}}}
                """);
        respondWith("""
                {"aggregations":{"rows":{"buckets":[{"key":{"age":30},"doc_count":1}]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            statement.setFetchSize(2);
            try (ResultSet result = statement.executeQuery(COMPOSITE)) {
                assertEquals(1, requests.size());
                assertTrue(result.next());
                assertEquals(10, result.getInt("age"));
                assertTrue(result.next());
                assertEquals(20, result.getInt("age"));
                assertEquals(1, requests.size());
                assertTrue(result.next());
                assertEquals(30, result.getInt("age"));
                assertFalse(result.next());
            }
        }
        assertEquals(2, requestBody(0).at("/aggs/rows/composite/size").intValue());
        assertEquals(20, requestBody(1).at("/aggs/rows/composite/after/age").intValue());
    }

    @Test
    public void skipLimitAndMaxRowsApplyToBucketsNotInputDocuments() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"after_key":{"age":20},"buckets":[{"key":{"age":10},"doc_count":100},{"key":{"age":20},"doc_count":200}]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            statement.setFetchSize(2);
            try (ResultSet result = statement.executeQuery("/*+ overwrite_find_skip=1,overwrite_find_limit=5 */" + COMPOSITE)) {
                assertTrue(result.next());
                assertEquals(20, result.getInt("age"));
                assertFalse(result.next());
            }
        }
        assertEquals(1, requests.size());
        assertEquals(0, requestBody(0).get("size").intValue());
        assertFalse(requestBody(0).has("from"));
    }

    @Test
    public void exactDistinctCountVisitsAllBucketsAndExcludesNull() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"after_key":{"age":10},"buckets":[{"key":{"age":null},"doc_count":4},{"key":{"age":10},"doc_count":100}]}}}
                """);
        respondWith("""
                {"aggregations":{"rows":{"after_key":{"age":30},"buckets":[{"key":{"age":20},"doc_count":100},{"key":{"age":30},"doc_count":100}]}}}
                """);
        respondWith("""
                {"aggregations":{"rows":{"buckets":[]}}}
                """);
        String query = """
                /*+ overwrite_find_limit=1 */ POST /books/_search {
                  "aggs":{"rows":{"composite":{"sources":[{"age":{"terms":{"field":"age","missing_bucket":true}}}]},
                  "meta":{"dbvisitor":{"mode":"count","column":"distinct_count"}}}}
                }
                """;
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            statement.setFetchSize(2);
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery(query)) {
                assertTrue(result.next());
                assertEquals(3L, result.getLong("distinct_count"));
                assertFalse(result.next());
            }
        }
        assertEquals(3, requests.size());
        assertEquals(30, requestBody(2).at("/aggs/rows/composite/after/age").intValue());
    }

    @Test
    public void distinctRowsRetainNullAndEmptyResultsRetainColumns() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"buckets":[{"key":{"age":null},"doc_count":3}]}}}
                """);
        respondWith("""
                {"aggregations":{"rows":{"buckets":[]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery(COMPOSITE)) {
                assertTrue(result.next());
                assertNull(result.getObject("age"));
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery(COMPOSITE)) {
                assertEquals("age", result.getMetaData().getColumnLabel(1));
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void groupedProjectionBecomesServerCompositeWithGroupKeyOrder() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"buckets":[{"key":{"age":20},"cnt":{"doc_count":2}}]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("""
                        /*+ aggregation_group="age" */ POST /books/_search {
                          "aggs":{"cnt":{"filter":{"match_all":{}}}},"sort":[{"age":{"order":"desc"}}]
                        }
                        """)) {
            assertTrue(result.next());
            assertEquals(20, result.getInt("age"));
            assertEquals(2L, result.getLong("cnt"));
            assertFalse(result.next());
        }
        assertEquals("desc", requestBody(0).at("/aggs/rows/composite/sources/0/age/terms/order").textValue());
        assertFalse(requestBody(0).has("sort"));
    }

    @Test
    public void pageCountCountsGroupsInsteadOfDocuments() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"buckets":[{"key":{"age":10},"doc_count":100},{"key":{"age":20},"doc_count":200}]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("/*+ overwrite_find_as_count */" + COMPOSITE)) {
            assertTrue(result.next());
            assertEquals(2L, result.getLong(1));
            assertFalse(result.next());
        }
        assertEquals("/books/_search", requests.get(0).getEndpoint());
    }

    @Test
    public void partialResponsesDoNotBecomeSuccessfulCounts() throws Exception {
        respondWith("""
                {"timed_out":true,"aggregations":{"rows":{"buckets":[{"key":{"age":10},"doc_count":1}]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeQuery("/*+ overwrite_find_as_count */" + COMPOSITE);
                fail("Partial server results must fail");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("incomplete"));
            }
        }
    }

    @Test
    public void repeatedAfterKeyFailsRatherThanLooping() throws Exception {
        respondWith("""
                {"aggregations":{"rows":{"after_key":{"age":10},"buckets":[{"key":{"age":10},"doc_count":1}]}}}
                """);
        respondWith("""
                {"aggregations":{"rows":{"after_key":{"age":10},"buckets":[{"key":{"age":10},"doc_count":1}]}}}
                """);
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(COMPOSITE)) {
            assertTrue(result.next());
            try {
                result.next();
                fail("A repeated cursor key must fail");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("after_key"));
            }
        }
    }

    @Test
    public void globalMetricSortingIsNotSilentlyReducedToPerPageSorting() throws Exception {
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeQuery("""
                        POST /books/_search {"aggs":{"rows":{"composite":{"sources":[{"age":{"terms":{"field":"age"}}}]},"aggs":{"total":{"sum":{"field":"age"}}}}},"sort":[{"total":"desc"}]}
                        """);
                fail("Composite pages cannot guarantee global metric order");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("Global ordering"));
            }
        }
        assertTrue(requests.isEmpty());
    }

    @Test
    public void closingOrCancellingStopsFurtherCompositePages() throws Exception {
        respondWith("{\"aggregations\":{\"rows\":{\"after_key\":{\"age\":10},\"buckets\":[{\"key\":{\"age\":10},\"doc_count\":1}]}}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            ResultSet result = statement.executeQuery(COMPOSITE);
            result.close();
            assertEquals(1, requests.size());
            respondWith("{\"aggregations\":{\"rows\":{\"after_key\":{\"age\":10},\"buckets\":[{\"key\":{\"age\":10},\"doc_count\":1}]}}}");
            try (ResultSet cancelled = statement.executeQuery(COMPOSITE)) {
                statement.cancel();
                try {
                    cancelled.next();
                    fail("Cancellation must stop lazy aggregation fetching");
                } catch (SQLException expected) {
                    assertTrue(expected.getMessage().contains("cancel"));
                }
            }
            assertEquals(2, requests.size());
        }
    }

    @Test
    public void shardFailuresAreNotReportedAsCompleteResults() throws Exception {
        respondWith("{\"_shards\":{\"failed\":1},\"aggregations\":{\"rows\":{\"buckets\":[]}}}");
        try (Connection connection = elasticConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeQuery(COMPOSITE);
                fail("Failed shards must not produce a successful empty result");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage().contains("incomplete"));
            }
        }
    }
}
