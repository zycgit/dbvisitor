/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.*;

/** 对照 pymilvus datatypes / sparse 示例，验证 SQL 的读写、搜索及结果解码。 */
public class MilvusVectorSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FLOAT_VECTOR)
    public void floatVector_shouldAcceptPrimitiveArraysAndReturnSearchProjection() throws SQLException {
        prepareVector("FLOAT_VECTOR(2)", "FLAT", "L2");
        insertVector(1, new float[] { 1, 0 });
        insertVector(2, new double[] { 0, 1 });
        try (PreparedStatement search = search("<->")) {
            search.setObject(1, new int[] { 1, 0 });
            try (ResultSet result = search.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
                assertEquals(Arrays.asList(1.0f, 0.0f), result.getObject("v"));
                assertEquals(0.0, result.getDouble("score"), 0.0001);
                assertFalse(result.next());
            }
        }
        assertEquals(Arrays.asList(0.0f, 1.0f), this.jdbcTemplate.queryForMap("SELECT v FROM " + this.collection + " WHERE id = 2").get("v"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_BINARY_VECTOR)
    public void binaryVector_shouldRoundTripBytesAndUseHammingSearch() throws SQLException {
        prepareVector("BINARY_VECTOR(16)", "BIN_FLAT", "HAMMING");
        byte[] bits = { 0x0f, (byte) 0xf0 };
        insertVector(1, bits);
        insertVector(2, new byte[] { 0, 0 });
        ByteBuffer query = ByteBuffer.wrap(new byte[] { 99, 0x0f, (byte) 0xf0, 88 });
        query.position(1);
        query.limit(3);
        assertPackedVector(query, bits, "~=");
        assertEquals(1, query.position());
        assertEquals(3, query.limit());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FP16_VECTOR)
    public void float16Vector_shouldEncodeNumbersAndAcceptLittleEndianBytes() throws SQLException {
        assertHalfVector("FLOAT16_VECTOR(2)", new byte[] { 0, 0x3c, 0, 0 });
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_INT8_VECTOR)
    public void int8Vector_shouldPreserveSignedBytesThroughSearchAndMutations() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v INT8_VECTOR(2)");
        this.jdbcTemplate.execute("CREATE INDEX idx_v ON " + this.collection
                + "(v) USING HNSW WITH (metric_type=L2, M=8, efConstruction=200)");
        loadCollection();
        byte[] target = { -128, 127 };
        insertVector(1, target);
        insertVector(2, new int[] { 0, 1 });
        assertPackedVector(target, target, "<->");
        try (PreparedStatement range = this.connection.prepareStatement("SELECT id, v FROM " + this.collection + " WHERE v <-> ? < ?")) {
            range.setFetchSize(1);
            range.setBytes(1, target);
            range.setInt(2, 1);
            try (ResultSet result = range.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
                assertArrayEquals(target, result.getBytes("v"));
                assertFalse(result.next());
            }
        }
        String hybridSql = "SELECT id, v FROM " + this.collection
                + " ORDER BY HYBRID (v <-> ? LIMIT 2, v <-> ? LIMIT 2) LIMIT 1 WITH (reranker='rrf', k=60)";
        try (PreparedStatement hybrid = this.connection.prepareStatement(hybridSql)) {
            hybrid.setBytes(1, target);
            hybrid.setObject(2, new int[] { -127, 126 });
            try (ResultSet result = hybrid.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
                assertArrayEquals(target, result.getBytes("v"));
                assertFalse(result.next());
            }
        }

        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + this.collection + " SET v = ? WHERE id = 1")) {
            update.setObject(1, new short[] { -127, 126 });
            assertEquals(1, update.executeUpdate());
        }
        byte[] updated = { -127, 126 };
        assertPackedVector(updated, updated, "<->");
        try (PreparedStatement upsert = this.connection.prepareStatement("UPSERT INTO " + this.collection + " (id, v) VALUES (?, ?)")) {
            upsert.setLong(1, 1);
            upsert.setBytes(2, target);
            assertEquals(1, upsert.executeUpdate());
        }
        assertPackedVector(target, target, "<->");
        try (PreparedStatement delete = this.connection.prepareStatement("DELETE FROM " + this.collection + " ORDER BY v <-> ? LIMIT 1")) {
            delete.setBytes(1, target);
            assertEquals(1, delete.executeUpdate());
        }
        assertEquals(0, countRows(" WHERE id = 1"));
        assertEquals(1, countRows(" WHERE id = 2"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_BF16_VECTOR)
    public void bfloat16Vector_shouldEncodeNumbersAndAcceptLittleEndianBytes() throws SQLException {
        assertHalfVector("BFLOAT16_VECTOR(2)", new byte[] { (byte) 0x80, 0x3f, 0, 0 });
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_SPARSE_VECTOR)
    public void sparseVector_shouldPreserveIndicesAndSearchByInnerProduct() throws SQLException {
        prepareVector("SPARSE_FLOAT_VECTOR", "SPARSE_INVERTED_INDEX", "IP");
        Map<Long, Float> target = Map.of(2L, 1.0f, 1000L, 0.5f);
        insertVector(1, target);
        insertVector(2, Map.of(3, 0.5f));
        try (PreparedStatement search = search("<#>")) {
            search.setObject(1, target);
            try (ResultSet result = search.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
                assertEquals(target, result.getObject("v"));
                assertEquals(1.25, result.getDouble("score"), 0.0001);
                assertFalse(result.next());
            }
        }
        assertEquals(target, this.jdbcTemplate.queryForMap("SELECT v FROM " + this.collection + " WHERE id = 1").get("v"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_VECTOR_INVALID_DIMENSION)
    public void dimensionMismatch_shouldFailWithoutWritingAnEntity() throws SQLException {
        prepareVector("FLOAT_VECTOR(2)", "FLAT", "L2");
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, v) VALUES (?, ?)")) {
            insert.setLong(1, 1);
            insert.setObject(2, new float[] { 1 });
            assertThrows(SQLException.class, insert::executeUpdate);
        }
        assertEquals(0, countRows(""));
    }

    private void prepareVector(String type, String index, String metric) throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v " + type);
        createIndex("v", index, metric);
        loadCollection();
    }

    private void insertVector(long id, Object vector) throws SQLException {
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, v) VALUES (?, ?)")) {
            insert.setLong(1, id);
            insert.setObject(2, vector);
            assertEquals(1, insert.executeUpdate());
        }
    }

    private PreparedStatement search(String operator) throws SQLException {
        return this.connection.prepareStatement("SELECT id, v, score FROM " + this.collection + " ORDER BY v " + operator + " ? LIMIT 1");
    }

    private void assertHalfVector(String type, byte[] encoded) throws SQLException {
        prepareVector(type, "FLAT", "L2");
        insertVector(1, new float[] { 1, 0 });
        insertVector(2, new float[] { 0, 1 });
        assertPackedVector(encoded, encoded, "<->");
    }

    private void assertPackedVector(Object query, byte[] expected, String operator) throws SQLException {
        try (PreparedStatement search = search(operator)) {
            search.setObject(1, query);
            try (ResultSet result = search.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1, result.getLong("id"));
                assertArrayEquals(expected, result.getBytes("v"));
                assertEquals(0.0, result.getDouble("score"), 0.0001);
                assertFalse(result.next());
            }
        }
        try (PreparedStatement select = this.connection.prepareStatement("SELECT v FROM " + this.collection + " WHERE id = ?")) {
            select.setLong(1, 1);
            try (ResultSet result = select.executeQuery()) {
                assertTrue(result.next());
                assertArrayEquals(expected, result.getBytes("v"));
                assertFalse(result.next());
            }
        }
    }
}
