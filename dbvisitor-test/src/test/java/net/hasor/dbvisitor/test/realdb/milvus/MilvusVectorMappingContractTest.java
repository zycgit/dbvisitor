/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Entity vector mapping scenarios, using Milvus fields instead of pgvector-specific material. */
public class MilvusVectorMappingContractTest extends AdapterContractTest {
    private static final int VECTOR_DIM = 128;
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private Connection connection;
    private LambdaTemplate lambda;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Before
    public void createVectorFixture() throws SQLException {
        this.connection = this.database.open();
        JdbcTemplate jdbc = new JdbcTemplate(this.connection);
        jdbc.execute("CREATE TABLE vector_mapping (id INT64 PRIMARY KEY, name VARCHAR(100), embedding FLOAT_VECTOR(128)) WITH (consistency_level=Strong)");
        jdbc.execute("CREATE INDEX vector_mapping_idx ON vector_mapping(embedding) USING FLAT WITH (metric_type=L2)");
        jdbc.execute("LOAD TABLE vector_mapping");
        this.lambda = new LambdaTemplate(this.connection);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_VECTOR_MAPPING_ROUND_TRIP)
    public void entityVectorShouldRoundTripAndUpdateEmbedding() throws SQLException {
        List<Float> original = vector(0.5f, 0.01f);
        insert(1, "before", original);
        VectorRow loaded = load(1);
        assertNotNull(loaded);
        assertEquals("before", loaded.getName());
        assertVector(original, loaded.getEmbedding());

        List<Float> updated = vector(0.9f, -0.005f);
        assertEquals(1, this.lambda.update(VectorRow.class).eq(VectorRow::getId, 1L)
                .updateTo(VectorRow::getEmbedding, updated).doUpdate());
        assertVector(updated, load(1).getEmbedding());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_VECTOR_MAPPING_MULTIPLE)
    public void scalarRangeShouldReturnEveryMappedVector() throws SQLException {
        for (int i = 0; i < 6; i++) {
            insert(i + 1, "row-" + i, vector(i * 0.1f, 0.005f));
        }
        List<VectorRow> rows = this.lambda.query(VectorRow.class).ge(VectorRow::getId, 1L)
                .le(VectorRow::getId, 6L).queryForList();
        assertEquals(6, rows.size());
        Set<Long> ids = new HashSet<>();
        for (VectorRow row : rows) {
            int i = row.getId().intValue() - 1;
            assertTrue(i >= 0 && i < 6);
            assertTrue(ids.add(row.getId()));
            assertEquals("row-" + i, row.getName());
            assertVector(vector(i * 0.1f, 0.005f), row.getEmbedding());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_VECTOR_MAPPING_SCALAR_UPDATE_DELETE)
    public void scalarUpdateAndDeleteShouldPreserveOtherVectors() throws SQLException {
        List<Float> original = vector(0.2f, 0.01f);
        insert(1, "before", original);
        insert(2, "delete", vector(0.3f, 0.01f));
        assertEquals(1, this.lambda.update(VectorRow.class).eq(VectorRow::getId, 1L)
                .updateTo(VectorRow::getName, "after").doUpdate());
        assertEquals("after", load(1).getName());
        assertVector(original, load(1).getEmbedding());
        assertEquals(1, this.lambda.delete(VectorRow.class).eq(VectorRow::getId, 2L).doDelete());
        assertNull(load(2));
        assertVector(original, load(1).getEmbedding());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_VECTOR_MAPPING_PRECISION)
    public void entityVectorShouldPreserveRepresentativeBoundaryValues() throws SQLException {
        List<Float> values = vector(0.12345f, 0);
        values.set(0, Float.MIN_VALUE);
        values.set(1, -Float.MIN_VALUE);
        values.set(2, 1.0f);
        values.set(3, -1.0f);
        values.set(4, 0.0f);
        insert(1, "precision", values);
        VectorRow loaded = load(1);
        assertNotNull(loaded);
        assertVector(values, loaded.getEmbedding());
    }

    private void insert(long id, String name, List<Float> embedding) throws SQLException {
        VectorRow row = new VectorRow();
        row.setId(id);
        row.setName(name);
        row.setEmbedding(embedding);
        assertEquals(1, this.lambda.insert(VectorRow.class).applyEntity(row).executeSumResult());
    }

    private VectorRow load(long id) throws SQLException {
        return this.lambda.query(VectorRow.class).eq(VectorRow::getId, id).queryForObject();
    }

    private List<Float> vector(float start, float step) {
        List<Float> values = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            values.add(start + i * step);
        }
        return values;
    }

    private void assertVector(List<Float> expected, List<Float> actual) {
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Vector component " + i, expected.get(i), actual.get(i), 0.0001f);
        }
    }

    @After
    public void cleanupVectorFixture() throws SQLException {
        try {
            if (this.connection != null) {
                new JdbcTemplate(this.connection).execute("DROP TABLE IF EXISTS vector_mapping");
            }
        } finally {
            this.database.close();
        }
    }

    @Table("vector_mapping")
    public static class VectorRow {
        @Column(primary = true)
        private Long id;
        private String name;
        private List<Float> embedding;

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Float> getEmbedding() {
            return this.embedding;
        }

        public void setEmbedding(List<Float> embedding) {
            this.embedding = embedding;
        }
    }
}
