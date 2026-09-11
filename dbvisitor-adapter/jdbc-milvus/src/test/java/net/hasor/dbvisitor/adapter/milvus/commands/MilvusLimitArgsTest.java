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
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import io.milvus.grpc.*;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.orm.iterator.SearchIteratorV2;
import io.milvus.param.Constant;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.v2Response;
import static org.junit.Assert.*;

public class MilvusLimitArgsTest extends AbstractJdbcTest {
    private final List<QueryReq>            queryParams           = new ArrayList<>();
    private final List<SearchReq>           searchParams          = new ArrayList<>();
    private final List<DeleteReq>           deleteParams          = new ArrayList<>();
    private final List<QueryIteratorReq>    queryIteratorParams   = new ArrayList<>();
    private final List<SearchIteratorReqV2> searchIteratorParams  = new ArrayList<>();
    private final List<UpsertReq>           partialUpsertRequests = new ArrayList<>();
    private       QueryResults              queryResult;
    private       QueryIterator             queryIterator;
    private       SearchIteratorV2          searchIterator;
    private       int                       upsertFailuresRemaining;
    private       int                       deleteFailuresRemaining;
    private       DataType                  primaryKeyType;

    private Connection getConnection() throws SQLException {
        return this.getConnection(null);
    }

    private Connection getConnection(Integer maxRetry) throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        prop.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        String url = "jdbc:dbvisitor:milvus://xxxxxx:19530";
        if (maxRetry != null) {
            url += "?" + MilvusKeys.MAX_RETRY + "=" + maxRetry;
        }
        return new JdbcDriver().connect(url, prop);
    }

    private void installInterceptor() {
        MilvusCommandInterceptor.resetInterceptor();
        this.queryParams.clear();
        this.searchParams.clear();
        this.deleteParams.clear();
        this.queryIteratorParams.clear();
        this.searchIteratorParams.clear();
        this.partialUpsertRequests.clear();
        this.queryResult = QueryResults.newBuilder()//
                .setStatus(Status.newBuilder().setErrorCode(ErrorCode.Success))//
                .build();
        this.queryIterator = PowerMockito.mock(QueryIterator.class);
        this.searchIterator = PowerMockito.mock(SearchIteratorV2.class);
        this.upsertFailuresRemaining = 0;
        this.deleteFailuresRemaining = 0;
        this.primaryKeyType = DataType.Int64;
        PowerMockito.when(this.queryIterator.next()).thenReturn(new ArrayList<>());
        PowerMockito.when(this.searchIterator.next()).thenReturn(new ArrayList<>());
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("query".equals(method.getName())) {
                this.queryParams.add((QueryReq) args[0]);
                return v2Response(method.getName(), this.queryResult);
            }
            if ("search".equals(method.getName())) {
                this.searchParams.add((SearchReq) args[0]);
                SearchResultData resultData = SearchResultData.newBuilder().setNumQueries(1).setTopK(0).build();
                return v2Response(method.getName(), SearchResults.newBuilder()//
                        .setStatus(Status.newBuilder().setErrorCode(ErrorCode.Success))//
                        .setResults(resultData)//
                        .build());
            }
            if ("queryIterator".equals(method.getName())) {
                this.queryIteratorParams.add((QueryIteratorReq) args[0]);
                return v2Response(method.getName(), this.queryIterator);
            }
            if ("searchIteratorV2".equals(method.getName())) {
                this.searchIteratorParams.add((SearchIteratorReqV2) args[0]);
                return v2Response(method.getName(), this.searchIterator);
            }
            if ("describeCollection".equals(method.getName())) {
                CollectionSchema schema = CollectionSchema.newBuilder()//
                        .setName("book_vectors")//
                        .addFields(FieldSchema.newBuilder().setName("book_id").setDataType(this.primaryKeyType).setIsPrimaryKey(true))//
                        .addFields(FieldSchema.newBuilder().setName("word_count").setDataType(DataType.Int32))//
                        .addFields(FieldSchema.newBuilder().setName("book_intro").setDataType(DataType.FloatVector))//
                        .build();
                return v2Response(method.getName(), DescribeCollectionResponse.newBuilder()//
                        .setStatus(Status.newBuilder().setErrorCode(ErrorCode.Success))//
                        .setSchema(schema)//
                        .build());
            }
            if ("delete".equals(method.getName())) {
                DeleteReq deleteParam = (DeleteReq) args[0];
                this.deleteParams.add(deleteParam);
                if (this.deleteFailuresRemaining > 0) {
                    this.deleteFailuresRemaining--;
                    throw new java.sql.SQLTransientConnectionException("retryable delete failure", "08006");
                }
                String expr = deleteParam.getFilter();
                long deleteCount = 1;
                if (expr.indexOf('[') >= 0 && expr.lastIndexOf(']') > expr.indexOf('[')) {
                    String idList = expr.substring(expr.indexOf('[') + 1, expr.lastIndexOf(']')).trim();
                    deleteCount = idList.isEmpty() ? 0 : idList.split(",").length;
                }
                return v2Response(method.getName(), MutationResult.newBuilder().setDeleteCnt(deleteCount).build());
            }
            if ("close".equals(method.getName())) {
                return null;
            }
            if ("upsert".equals(method.getName())) {
                UpsertReq request = (UpsertReq) args[0];
                this.partialUpsertRequests.add(request);
                if (this.upsertFailuresRemaining > 0) {
                    this.upsertFailuresRemaining--;
                    throw new java.sql.SQLTransientConnectionException("retryable partial upsert failure", "08006");
                }
                return UpsertResp.builder().upsertCnt(request.getData().size()).build();
            }
            return null;
        });
    }

    @After
    public void cleanupInterceptor() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void testDeleteWithoutWhereUsesActualPrimaryKeyAndNativeDelete() throws Exception {
        for (DataType type : Arrays.asList(DataType.Int64, DataType.VarChar)) {
            this.installInterceptor();
            this.primaryKeyType = type;
            try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM book_vectors PARTITION selected_partition")) {
                assertEquals(1, ps.executeUpdate());
            }
            assertEquals(1, this.deleteParams.size());
            DeleteReq request = this.deleteParams.get(0);
            assertEquals("book_id is not null", request.getFilter());
            assertTrue(request.getFilterTemplateValues().isEmpty());
            assertEquals("selected_partition", request.getPartitionName());
            assertTrue(this.queryParams.isEmpty());
            assertTrue(this.queryIteratorParams.isEmpty());
            assertTrue(this.searchIteratorParams.isEmpty());
        }
    }

    @Test
    public void testDeleteWithoutWhereKeepsLimitFetchSizeAndBoundPrimaryKeys() throws Exception {
        for (Object key : Arrays.asList(-1L, "quoted\"key")) {
            this.installInterceptor();
            this.primaryKeyType = key instanceof String ? DataType.VarChar : DataType.Int64;
            QueryResultsWrapper.RowRecord row = new QueryResultsWrapper.RowRecord();
            row.put("book_id", key);
            PowerMockito.when(this.queryIterator.next()).thenReturn(List.of(row), new ArrayList<>());
            try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM book_vectors PARTITION selected_partition LIMIT ?")) {
                ps.setFetchSize(1);
                ps.setInt(1, 1);
                assertEquals(1, ps.executeUpdate());
            }
            assertEquals(1, this.queryIteratorParams.size());
            QueryIteratorReq selection = this.queryIteratorParams.get(0);
            assertEquals("book_id is not null", selection.getExpr());
            assertEquals(1L, selection.getLimit());
            assertEquals(1L, selection.getBatchSize());
            assertEquals(List.of("selected_partition"), selection.getPartitionNames());
            assertEquals(1, this.deleteParams.size());
            DeleteReq deletion = this.deleteParams.get(0);
            assertEquals("selected_partition", deletion.getPartitionName());
            assertEquals("book_id in {ids}", deletion.getFilter());
            assertEquals(java.util.Map.of("ids", List.of(key)), deletion.getFilterTemplateValues());
        }
    }

    @Test
    public void testExplicitConstantWhereIsNotReplacedByDeleteAllFilter() throws Exception {
        this.installInterceptor();
        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM book_vectors WHERE 1=1")) {
            ps.executeUpdate();
        }
        assertEquals(1, this.deleteParams.size());
        String filter = this.deleteParams.get(0).getFilter();
        assertTrue(filter.contains("1"));
        assertFalse(filter.contains("is not null"));
        assertTrue(this.queryIteratorParams.isEmpty());
    }

    @Test
    public void testScalarDeletePassesLimitToSelectionQuery() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE word_count > ? LIMIT ?")) {
            ps.setInt(1, 100);
            ps.setInt(2, 7);
            ps.executeUpdate();
        }

        assertEquals(0, this.queryParams.size());
        assertEquals(1, this.queryIteratorParams.size());
        assertEquals(7L, this.queryIteratorParams.get(0).getLimit());
        assertEquals("word_count > {arg1}", this.queryIteratorParams.get(0).getExpr());
        assertEquals(java.util.Map.of("arg1", 100), this.queryIteratorParams.get(0).getFilterTemplateValues());
    }

    @Test
    public void testScalarDeleteOnlyDeletesPrimaryKeysSelectedByLimit() throws Exception {
        this.installInterceptor();
        QueryResultsWrapper.RowRecord first = new QueryResultsWrapper.RowRecord();
        first.put("book_id", 101L);
        QueryResultsWrapper.RowRecord second = new QueryResultsWrapper.RowRecord();
        second.put("book_id", 102L);
        PowerMockito.when(this.queryIterator.next())//
                .thenReturn(List.of(first), List.of(second), new ArrayList<>());

        int affected;
        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE word_count > ? LIMIT ?")) {
            ps.setInt(1, 100);
            ps.setInt(2, 2);
            affected = ps.executeUpdate();
        }

        assertEquals(2, affected);
        assertEquals(0, this.queryParams.size());
        assertEquals(1, this.queryIteratorParams.size());
        assertEquals(2L, this.queryIteratorParams.get(0).getLimit());
        assertEquals(2, this.deleteParams.size());
        assertEquals("book_id in {ids}", this.deleteParams.get(0).getFilter());
        assertEquals(java.util.Map.of("ids", List.of(101L)), this.deleteParams.get(0).getFilterTemplateValues());
        assertEquals("book_id in {ids}", this.deleteParams.get(1).getFilter());
        assertEquals(java.util.Map.of("ids", List.of(102L)), this.deleteParams.get(1).getFilterTemplateValues());
    }

    @Test
    public void testRangeDeletePassesParameterizedLimitToTopK() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?) LIMIT ?")) {
            ps.setObject(1, Arrays.asList(0.1f, 0.2f));
            ps.setDouble(2, 0.5D);
            ps.setInt(3, 11);
            ps.executeUpdate();
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(11L, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testRangeUpdatePassesParameterizedLimitToTopK() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE vector_range(book_intro, ?, ?) LIMIT ?")) {
            ps.setInt(1, 999);
            ps.setObject(2, Arrays.asList(0.1f, 0.2f));
            ps.setDouble(3, 0.5D);
            ps.setInt(4, 13);
            ps.executeUpdate();
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(13L, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testScalarUpdateWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE book_id = ?")) {
            ps.setInt(1, 999);
            ps.setLong(2, 101L);
            assertEquals(0, ps.executeUpdate());
        }

        assertEquals(0, this.queryParams.size());
        assertEquals(1, this.queryIteratorParams.size());
        assertEquals(Constant.UNLIMITED, this.queryIteratorParams.get(0).getLimit());
        assertEquals("book_id == {arg2}", this.queryIteratorParams.get(0).getExpr());
        assertEquals(java.util.Map.of("arg2", 101L), this.queryIteratorParams.get(0).getFilterTemplateValues());
    }

    @Test
    public void testScalarUpdateUsesFetchSizeAndUpsertsEachPage() throws Exception {
        this.installInterceptor();
        QueryResultsWrapper.RowRecord first = new QueryResultsWrapper.RowRecord();
        first.put("book_id", 101L);
        first.put("word_count", 100);
        QueryResultsWrapper.RowRecord second = new QueryResultsWrapper.RowRecord();
        second.put("book_id", 102L);
        second.put("word_count", 200);
        PowerMockito.when(this.queryIterator.next())//
                .thenReturn(List.of(first), List.of(second), new ArrayList<>());

        int affected;
        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE book_id > ? LIMIT ?")) {
            ps.setFetchSize(1);
            ps.setInt(1, 999);
            ps.setLong(2, 100L);
            ps.setInt(3, 2);
            affected = ps.executeUpdate();
        }

        assertEquals(2, affected);
        assertEquals(1, this.queryIteratorParams.size());
        assertEquals(1L, this.queryIteratorParams.get(0).getBatchSize());
        assertEquals(2L, this.queryIteratorParams.get(0).getLimit());
        assertEquals(List.of("book_id"), this.queryIteratorParams.get(0).getOutputFields());
        assertEquals(2, this.partialUpsertRequests.size());
        assertTrue(this.partialUpsertRequests.get(0).isPartialUpdate());
        assertTrue(this.partialUpsertRequests.get(1).isPartialUpdate());
        assertEquals(101L, this.partialUpsertRequests.get(0).getData().get(0).get("book_id").getAsLong());
        assertEquals(102L, this.partialUpsertRequests.get(1).getData().get(0).get("book_id").getAsLong());
        assertEquals(999, this.partialUpsertRequests.get(0).getData().get(0).get("word_count").getAsInt());
        assertEquals(999, this.partialUpsertRequests.get(1).getData().get(0).get("word_count").getAsInt());
        assertEquals(2, this.partialUpsertRequests.get(0).getData().get(0).size());
    }

    @Test
    public void testFetchSizeIsCappedAtSdkBatchMaximum() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE book_id > ?")) {
            ps.setFetchSize(Constant.MAX_BATCH_SIZE + 1);
            ps.setInt(1, 999);
            ps.setLong(2, 100L);
            assertEquals(0, ps.executeUpdate());
        }

        assertEquals(1, this.queryIteratorParams.size());
        assertEquals(Constant.MAX_BATCH_SIZE, this.queryIteratorParams.get(0).getBatchSize());
    }

    @Test
    public void testUpdateRejectsPrimaryKeyModificationBeforeSelection() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET book_id = ? WHERE book_id = ?")) {
            ps.setLong(1, 102L);
            ps.setLong(2, 101L);
            ps.executeUpdate();
            fail("Expected primary key modification to be rejected.");
        } catch (SQLException e) {
            assertEquals("Milvus UPDATE cannot modify primary key field 'book_id'.", e.getMessage());
        }

        assertEquals(0, this.queryIteratorParams.size());
        assertEquals(0, this.partialUpsertRequests.size());
    }

    @Test
    public void testPageUpdateRetriesUpToConfiguredMaximum() throws Exception {
        this.installInterceptor();
        QueryResultsWrapper.RowRecord row = new QueryResultsWrapper.RowRecord();
        row.put("book_id", 101L);
        row.put("word_count", 100);
        PowerMockito.when(this.queryIterator.next()).thenReturn(List.of(row), new ArrayList<>());
        this.upsertFailuresRemaining = 2;

        int affected;
        try (Connection conn = this.getConnection(2); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE book_id = ?")) {
            ps.setInt(1, 999);
            ps.setLong(2, 101L);
            affected = ps.executeUpdate();
        }

        assertEquals(1, affected);
        assertEquals(3, this.partialUpsertRequests.size());
    }

    @Test
    public void testPageUpdateReportsProgressAfterRetryExhausted() throws Exception {
        this.installInterceptor();
        QueryResultsWrapper.RowRecord row = new QueryResultsWrapper.RowRecord();
        row.put("book_id", 101L);
        row.put("word_count", 100);
        PowerMockito.when(this.queryIterator.next()).thenReturn(List.of(row));
        this.upsertFailuresRemaining = 2;

        try (Connection conn = this.getConnection(1); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE book_id = ?")) {
            ps.setInt(1, 999);
            ps.setLong(2, 101L);
            ps.executeUpdate();
            fail("Expected page update retries to be exhausted.");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("page=1"));
            assertTrue(e.getMessage().contains("confirmedPages=0"));
            assertTrue(e.getMessage().contains("confirmedRows=0"));
            assertTrue(e.getMessage().contains("currentPageRows=1"));
            assertTrue(e.getMessage().contains("failed after 2 attempt(s)"));
        }

        assertEquals(2, this.partialUpsertRequests.size());
    }

    @Test
    public void testAllPagedWritesReportConfirmedProgressAfterRetryExhaustion() throws Exception {
        for (boolean update : new boolean[] { true, false }) {
            for (boolean search : new boolean[] { true, false }) {
                this.installInterceptor();
                QueryResultsWrapper.RowRecord first = new QueryResultsWrapper.RowRecord();
                first.put("book_id", 101L);
                QueryResultsWrapper.RowRecord second = new QueryResultsWrapper.RowRecord();
                second.put("book_id", 102L);
                int[] pagesRead = { 0 };
                org.mockito.stubbing.Answer<List<QueryResultsWrapper.RowRecord>> nextPage = invocation -> {
                    pagesRead[0]++;
                    if (pagesRead[0] == 1) {
                        return List.of(first);
                    }
                    if (pagesRead[0] == 2) {
                        this.upsertFailuresRemaining = 2;
                        this.deleteFailuresRemaining = 2;
                        return List.of(second);
                    }
                    throw new AssertionError("Do not read another page after a write failure.");
                };
                PowerMockito.when(this.queryIterator.next()).thenAnswer(nextPage);
                PowerMockito.when(this.searchIterator.next()).thenAnswer(invocation -> net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("book_id", nextPage.answer(invocation)));

                String operation = update ? "UPDATE" : "DELETE";
                String sql = update ? "UPDATE book_vectors SET word_count = 999" : "DELETE FROM book_vectors";
                sql += " WHERE book_id > 0";
                if (search) {
                    sql += " ORDER BY book_intro <-> [1, 2]";
                }
                sql += " LIMIT 3";
                try (Connection conn = this.getConnection(1); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.executeUpdate();
                    fail("Expected retries on the second page to be exhausted.");
                } catch (SQLException e) {
                    assertTrue(e.getMessage(), e.getMessage().contains("Milvus " + operation + " failed during paged execution"));
                    assertTrue(e.getMessage(), e.getMessage().contains("page=2"));
                    assertTrue(e.getMessage(), e.getMessage().contains("confirmedPages=1"));
                    assertTrue(e.getMessage(), e.getMessage().contains("confirmedRows=1"));
                    assertTrue(e.getMessage(), e.getMessage().contains("currentPageRows=1"));
                    assertTrue(e.getMessage(), e.getMessage().contains("failed after 2 attempt(s)"));
                }

                assertEquals(2, pagesRead[0]);
                assertEquals(3, update ? this.partialUpsertRequests.size() : this.deleteParams.size());
                if (search) {
                    org.mockito.Mockito.verify(this.searchIterator).close();
                    assertEquals(1, this.searchIteratorParams.size());
                } else {
                    org.mockito.Mockito.verify(this.queryIterator).close();
                    assertEquals(1, this.queryIteratorParams.size());
                }
            }
        }
    }

    @Test
    public void testAllPagedWritesCloseIteratorAfterReadFailure() throws Exception {
        for (boolean update : new boolean[] { true, false }) {
            for (boolean search : new boolean[] { true, false }) {
                this.installInterceptor();
                QueryResultsWrapper.RowRecord first = new QueryResultsWrapper.RowRecord();
                first.put("book_id", 101L);
                PowerMockito.when(this.queryIterator.next()).thenReturn(List.of(first)).thenThrow(new IllegalStateException("read page failed"));
                PowerMockito.when(this.searchIterator.next()).thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("book_id", List.of(first))).thenThrow(new IllegalStateException("read page failed"));

                String sql = update ? "UPDATE book_vectors SET word_count = 999" : "DELETE FROM book_vectors";
                sql += " WHERE book_id > 0";
                if (search) {
                    sql += " ORDER BY book_intro <-> [1, 2]";
                }
                sql += " LIMIT 3";
                try (Connection conn = this.getConnection(2); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.executeUpdate();
                    fail("Expected an iterator read failure.");
                } catch (SQLException e) {
                    assertTrue(e.getMessage(), e.getMessage().contains("phase=read"));
                    assertTrue(e.getMessage(), e.getMessage().contains("confirmedPages=1"));
                    assertTrue(e.getMessage(), e.getMessage().contains("confirmedRows=1"));
                    assertTrue(e.getMessage(), e.getMessage().contains("read page failed"));
                }

                assertEquals(1, update ? this.partialUpsertRequests.size() : this.deleteParams.size());
                if (search) {
                    org.mockito.Mockito.verify(this.searchIterator, org.mockito.Mockito.times(2)).next();
                    org.mockito.Mockito.verify(this.searchIterator).close();
                } else {
                    org.mockito.Mockito.verify(this.queryIterator, org.mockito.Mockito.times(2)).next();
                    org.mockito.Mockito.verify(this.queryIterator).close();
                }
            }
        }
    }

    @Test
    public void testNativeDeleteSharesConnectionRetryConfiguration() throws Exception {
        this.installInterceptor();
        this.deleteFailuresRemaining = 1;

        int affected;
        try (Connection conn = this.getConnection(1); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE book_id = ?")) {
            ps.setLong(1, 101L);
            affected = ps.executeUpdate();
        }

        assertEquals(1, affected);
        assertEquals(2, this.deleteParams.size());
        assertEquals("book_id == {arg1}", this.deleteParams.get(0).getFilter());
        assertEquals(java.util.Map.of("arg1", 101L), this.deleteParams.get(0).getFilterTemplateValues());
        assertEquals("book_id == {arg1}", this.deleteParams.get(1).getFilter());
        assertEquals(java.util.Map.of("arg1", 101L), this.deleteParams.get(1).getFilterTemplateValues());
    }

    @Test
    public void testNegativeMaxRetryIsRejectedAtConnectionTime() throws Exception {
        this.installInterceptor();

        try (Connection ignored = this.getConnection(-1)) {
            fail("Expected negative maxRetry to be rejected.");
        } catch (SQLException e) {
            assertEquals("Milvus connection property 'maxRetry' must be greater than or equal to 0.", e.getMessage());
        }
    }

    @Test
    public void testRangeUpdateWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE vector_range(book_intro, ?, ?)")) {
            ps.setInt(1, 999);
            ps.setObject(2, Arrays.asList(0.1f, 0.2f));
            ps.setDouble(3, 0.5D);
            assertEquals(0, ps.executeUpdate());
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(Constant.UNLIMITED, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testKnnUpdateWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? ORDER BY book_intro <-> ?")) {
            ps.setInt(1, 999);
            ps.setObject(2, Arrays.asList(0.1f, 0.2f));
            assertEquals(0, ps.executeUpdate());
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(Constant.UNLIMITED, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testRangeDeleteWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();
        QueryResultsWrapper.RowRecord first = new QueryResultsWrapper.RowRecord();
        first.put("book_id", 101L);
        QueryResultsWrapper.RowRecord second = new QueryResultsWrapper.RowRecord();
        second.put("book_id", 102L);
        PowerMockito.when(this.searchIterator.next())//
                .thenReturn(net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("book_id", List.of(first)), net.hasor.dbvisitor.adapter.milvus.MilvusTestResponses.searchHits("book_id", List.of(second)), new ArrayList<>());

        int affected;
        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?)")) {
            ps.setFetchSize(1);
            ps.setObject(1, Arrays.asList(0.1f, 0.2f));
            ps.setDouble(2, 0.5D);
            affected = ps.executeUpdate();
        }

        assertEquals(2, affected);
        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(Constant.UNLIMITED, this.searchIteratorParams.get(0).getLimit());
        assertEquals(1L, this.searchIteratorParams.get(0).getBatchSize());
        assertEquals(2, this.deleteParams.size());
        assertEquals("book_id in {ids}", this.deleteParams.get(0).getFilter());
        assertEquals(java.util.Map.of("ids", List.of(101L)), this.deleteParams.get(0).getFilterTemplateValues());
        assertEquals("book_id in {ids}", this.deleteParams.get(1).getFilter());
        assertEquals(java.util.Map.of("ids", List.of(102L)), this.deleteParams.get(1).getFilterTemplateValues());
    }

    @Test
    public void testKnnDeleteWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors ORDER BY book_intro <-> ?")) {
            ps.setObject(1, Arrays.asList(0.1f, 0.2f));
            assertEquals(0, ps.executeUpdate());
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(Constant.UNLIMITED, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testRangeSelectWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "SELECT * FROM book_vectors WHERE vector_range(book_intro, ?, ?)")) {
            ps.setObject(1, Arrays.asList(0.1f, 0.2f));
            ps.setDouble(2, 0.5D);
            ps.executeQuery();
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(Constant.UNLIMITED, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testKnnSelectWithoutLimitUsesUnlimitedIterator() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "SELECT * FROM book_vectors ORDER BY book_intro <-> ?")) {
            ps.setObject(1, Arrays.asList(0.1f, 0.2f));
            ps.executeQuery();
        }

        assertEquals(0, this.searchParams.size());
        assertEquals(1, this.searchIteratorParams.size());
        assertEquals(-1L, this.searchIteratorParams.get(0).getLimit());
    }

    @Test
    public void testNonPositiveLimitIsRejected() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "UPDATE book_vectors SET word_count = ? WHERE book_id = ? LIMIT ?")) {
            ps.setInt(1, 999);
            ps.setLong(2, 101L);
            ps.setInt(3, 0);
            ps.executeUpdate();
            fail("Expected zero LIMIT to be rejected.");
        } catch (SQLException e) {
            assertEquals("LIMIT must be greater than 0.", e.getMessage());
        }

        assertEquals(0, this.queryParams.size());
    }

    @Test
    public void testFractionalLimitIsRejected() throws Exception {
        this.installInterceptor();

        try (Connection conn = this.getConnection(); PreparedStatement ps = conn.prepareStatement(//
                "DELETE FROM book_vectors WHERE vector_range(book_intro, ?, ?) LIMIT ?")) {
            ps.setObject(1, Arrays.asList(0.1f, 0.2f));
            ps.setDouble(2, 0.5D);
            ps.setDouble(3, 1.5D);
            ps.executeUpdate();
            fail("Expected fractional LIMIT to be rejected.");
        } catch (SQLException e) {
            assertEquals("LIMIT must be an integer within the BIGINT range.", e.getMessage());
        }

        assertEquals(0, this.searchParams.size());
    }
}
