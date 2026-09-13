/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

public class MilvusHybridSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_HYBRID_RRF)
    public void rrf_shouldFuseCandidatesIntoOneResultAndApplySharedFilter() throws SQLException {
        prepareDenseFields();
        String sql = "SELECT id, score FROM " + this.collection + """
                 WHERE category = ? ORDER BY HYBRID (
                    left_v <-> ? LIMIT 3,
                    right_v <-> ? LIMIT 3
                 ) LIMIT 3 WITH (reranker='rrf', k=60)
                """;
        try (PreparedStatement search = this.connection.prepareStatement(sql)) {
            search.setString(1, "keep");
            search.setObject(2, new float[] { 0, 0 });
            search.setObject(3, new float[] { 0, 0 });
            assertTrue(search.execute());
            try (ResultSet result = search.getResultSet()) {
                List<Long> ids = readIds(result);
                assertEquals(3, ids.size());
                assertEquals(3, new HashSet<>(ids).size());
                assertEquals(Long.valueOf(1), ids.get(0));
                assertFalse(ids.contains(4L));
            }
            assertFalse(search.getMoreResults());
            assertEquals(-1, search.getUpdateCount());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_HYBRID_WEIGHTED)
    public void weightedRanker_shouldHonorBranchWeightsAndFinalOffset() throws SQLException {
        prepareDenseFields();
        String sql = "SELECT id FROM " + this.collection + """
                 WHERE category = 'keep' ORDER BY HYBRID (
                    left_v <-> ? LIMIT 3,
                    right_v <-> ? LIMIT 3
                 ) LIMIT 2 OFFSET 1 WITH (reranker='weighted', weights='[1.0,0.0]')
                """;
        try (PreparedStatement search = this.connection.prepareStatement(sql)) {
            search.setObject(1, new float[] { 0, 0 });
            search.setObject(2, new float[] { 0, 0 });
            try (ResultSet result = search.executeQuery()) {
                assertEquals(List.of(2L, 3L), readIds(result));
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_HYBRID_LOCAL_FILTERS)
    public void perCandidateFiltersShouldIntersectSharedScopeAndRoundFusedScores() throws SQLException {
        prepareDenseFields();
        String sql = "SELECT id,score FROM " + collection + """
                 WHERE category = ? ORDER BY HYBRID (
                    left_v <-> ? WHERE id = ? OR id = ? LIMIT 3 WITH(timezone=?),
                    right_v <-> ? WHERE id IN ? LIMIT 3
                 ) LIMIT 3 WITH(reranker='rrf',k=60,round_decimal=?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "keep");
            statement.setObject(2, new float[] { 0, 0 });
            statement.setLong(3, 1L);
            statement.setLong(4, 4L);
            statement.setString(5, "UTC");
            statement.setObject(6, new float[] { 0, 0 });
            statement.setObject(7, new long[] { 2, 4 });
            statement.setInt(8, 2);
            HashSet<Long> ids = new HashSet<>();
            int count = 0;
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    count++;
                    ids.add(rows.getLong("id"));
                    // Each hit is first in exactly one branch: round(1 / (60 + 1), 2).
                    assertEquals(0.02F, rows.getFloat("score"), 0.000001F);
                }
            }
            assertEquals(2, count);
            assertEquals(new HashSet<>(java.util.Arrays.asList(1L, 2L)), ids);
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_HYBRID_LOCAL_ONLY)
    public void localFiltersWithoutSharedWhereShouldStillProduceOneFusedResult() throws SQLException {
        prepareDenseFields();
        String sql = "SELECT id,score FROM " + collection + """
                 ORDER BY HYBRID (
                    left_v <-> ? WHERE id = ? LIMIT 3,
                    right_v <-> ? WHERE id = ? LIMIT 3 WITH(timezone='')
                 ) LIMIT 3 WITH(reranker='rrf',k=60,round_decimal=3)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, new float[] { 0, 0 });
            statement.setLong(2, 1L);
            statement.setObject(3, new float[] { 0, 0 });
            statement.setLong(4, 1L);
            try (ResultSet rows = statement.executeQuery()) {
                assertTrue(rows.next());
                assertEquals(1L, rows.getLong("id"));
                assertEquals(0.033F, rows.getFloat("score"), 0.000001F);
                assertFalse(rows.next());
            }
            assertFalse(statement.getMoreResults());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_BM25)
    public void bm25_shouldGenerateSparseValuesAndSearchBoundText() throws SQLException {
        createCollection("""
                id INT64 PRIMARY KEY,
                body VARCHAR(512) WITH (enable_analyzer=true),
                sparse SPARSE_FLOAT_VECTOR,
                FUNCTION bm25_fn USING BM25 (body) INTO (sparse)
                """);
        createIndex("sparse", "SPARSE_INVERTED_INDEX", "BM25");
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + """
                 (id, body) VALUES (1, 'milvus vector search'), (2, 'relational database tables'), (3, 'zilliz cloud service')
                """);
        this.jdbcTemplate.execute("FLUSH " + this.collection);
        loadCollection();
        try (PreparedStatement search = this.connection.prepareStatement("SELECT id, body, score FROM " + this.collection + " ORDER BY sparse <?> ? LIMIT 1")) {
            search.setString(1, "milvus");
            try (ResultSet result = search.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
                assertEquals("milvus vector search", result.getString("body"));
                assertTrue(result.getDouble("score") > 0);
                assertFalse(result.next());
            }
        }
    }

    private void prepareDenseFields() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, category VARCHAR(16), left_v FLOAT_VECTOR(2), right_v FLOAT_VECTOR(2)");
        createIndex("left_v", "FLAT", "L2");
        createIndex("right_v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + """
                 (id, category, left_v, right_v) VALUES
                 (1, 'keep', [1,0], [1,0]), (2, 'keep', [2,0], [3,0]),
                 (3, 'keep', [3,0], [2,0]), (4, 'excluded', [0,0], [0,0])
                """);
    }
}
