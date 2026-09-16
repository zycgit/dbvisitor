/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus_cloud;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudVectorTest extends CloudTestSupport {
    @Test
    public void floatCosineShouldRoundTrip() throws Exception {
        assertVector("FLOAT_VECTOR(2)", "COSINE", "<=>", new float[] { 1, 0 }, List.of(1f, 0f));
    }

    @Test
    public void floatInnerProductShouldRoundTrip() throws Exception {
        assertVector("FLOAT_VECTOR(2)", "IP", "<#>", new double[] { 1, 0 }, List.of(1f, 0f));
    }

    @Test
    public void binaryHammingShouldRoundTrip() throws Exception {
        byte[] vector = { 15, (byte) 240 };
        assertVector("BINARY_VECTOR(16)", "HAMMING", "~=", vector, vector);
    }

    @Test
    public void float16ShouldRoundTripEncodedValues() throws Exception {
        assertVector("FLOAT16_VECTOR(2)", "L2", "<->", new float[] { 1, 0 }, new byte[] { 0, 0x3c, 0, 0 });
    }

    @Test
    public void bfloat16ShouldRoundTripEncodedValues() throws Exception {
        assertVector("BFLOAT16_VECTOR(2)", "L2", "<->", new float[] { 1, 0 }, new byte[] { (byte) 128, 0x3f, 0, 0 });
    }

    @Test
    public void int8ShouldPreserveSignedBytes() throws Exception {
        byte[] vector = { -128, 127 };
        assertVector("INT8_VECTOR(2)", "L2", "<->", vector, vector);
    }

    @Test
    public void sparseShouldPreserveDimensionsAndWeights() throws Exception {
        Map<Long, Float> vector = Map.of(2L, 1f, 1000L, 0.5f);
        assertVector("SPARSE_FLOAT_VECTOR", "IP", "<#>", vector, vector);
    }

    @Test
    public void rangeAndHybridShouldBindVectorsAndReturnOneResultSet() throws Exception {
        String table = createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)", "L2");
        this.jdbc.executeUpdate("INSERT INTO " + table + " (id,v) VALUES (1,[1,0]),(2,[2,0]),(3,[3,0])");
        try (PreparedStatement range = this.connection.prepareStatement("SELECT id FROM " + table + " WHERE v <-> ? < ?")) {
            range.setFetchSize(1);
            range.setObject(1, new float[] { 0, 0 });
            range.setDouble(2, 5);
            try (ResultSet rows = range.executeQuery()) {
                assertEquals(List.of(1L, 2L), readIds(rows));
            }
        }
        String sql = "SELECT id,score FROM " + table + " ORDER BY HYBRID (v <-> ? LIMIT 3,v <-> ? LIMIT 3) LIMIT 2 WITH(reranker='rrf',k=60)";
        try (PreparedStatement hybrid = this.connection.prepareStatement(sql)) {
            hybrid.setObject(1, new float[] { 1, 0 });
            hybrid.setObject(2, new float[] { 1, 0 });
            try (ResultSet rows = hybrid.executeQuery()) {
                assertEquals(List.of(1L, 2L), readIds(rows));
            }
            assertFalse(hybrid.getMoreResults());
        }
    }

    @Test
    public void bm25ShouldAcceptBoundTextThroughJdbcAndJdbcTemplate() throws Exception {
        String table = createCollection("""
                id INT64 PRIMARY KEY, body VARCHAR(128) WITH(enable_analyzer=true),
                v SPARSE_FLOAT_VECTOR, FUNCTION text_vector USING BM25(body) INTO(v)
                """, "BM25");
        this.jdbc.executeUpdate("INSERT INTO " + table + " (id,body) VALUES (1,'milvus vector search'),(2,'relational tables')");
        try (PreparedStatement search = this.connection.prepareStatement("SELECT id FROM " + table + " ORDER BY v <?> ? LIMIT 1")) {
            search.setString(1, "milvus");
            try (ResultSet rows = search.executeQuery()) {
                assertEquals(List.of(1L), readIds(rows));
            }
        }
        // JdbcTemplate parses positional parameters before the native driver sees the command.
        assertEquals(Long.valueOf(1), this.jdbc.queryForLong("SELECT id FROM " + table + " ORDER BY v <\\?> ? LIMIT 1", "milvus"));
    }

    private void assertVector(String type, String metric, String operator, Object input, Object expected) throws Exception {
        String table = createCollection("id INT64 PRIMARY KEY, v " + type, metric);
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + table + " (id,v) VALUES (1,?)")) {
            insert.setObject(1, input);
            assertEquals(1, insert.executeUpdate());
        }
        try (PreparedStatement search = this.connection.prepareStatement("SELECT id,v,score FROM " + table + " ORDER BY v " + operator + " ? LIMIT 1")) {
            search.setObject(1, input);
            try (ResultSet rows = search.executeQuery()) {
                assertTrue(rows.next());
                assertEquals(1, rows.getLong("id"));
                if (expected instanceof byte[] bytes) {
                    assertArrayEquals(bytes, rows.getBytes("v"));
                } else {
                    assertEquals(expected, rows.getObject("v"));
                }
                assertTrue(Double.isFinite(rows.getDouble("score")));
                assertFalse(rows.next());
            }
        }
    }
}
