/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.vector_query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class VectorTypeMappingCase extends VectorQuerySupport {
    // 能力归属：向量操作 / 向量类型映射。
    @Test
    @Capability(value = CapabilityId.VECTOR_CRUD_ROUND_TRIP, column = "vectors/vectors/vector-mapping")
    public void vector_shouldRoundTripAndUpdateEmbedding() throws SQLException {
        requiresNxnFeature(FeatureId.VECTOR);
        int id = baseId() + 1;
        try {
            List<Float> original = fixedVector(0.5f, 0.01f);
            insertVector(id, "VectorCrud", original);

            ProductVectorForPg loaded = loadVector(id);
            assertNotNull(loaded);
            assertEquals("VectorCrud", loaded.getName());
            assertVectorEquals(original, loaded.getEmbedding());

            List<Float> updated = fixedVector(0.9f, -0.005f);
            int rows = lambdaTemplate.update(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id)//
                    .updateTo(ProductVectorForPg::getEmbedding, updated)//
                    .doUpdate();
            assertEquals(1, rows);
            assertVectorEquals(updated, loadVector(id).getEmbedding());
        } finally {
            cleanup(id);
        }
    }

    // 能力归属：向量操作 / 向量类型映射。
    @Test
    @Capability(value = CapabilityId.VECTOR_BATCH_QUERY, column = "vectors/vectors/vector-mapping")
    public void vector_shouldInsertMultipleRowsAndQueryByScalarRange() throws SQLException {
        requiresNxnFeature(FeatureId.VECTOR);
        int startId = baseId() + 20;
        int count = 6;
        try {
            for (int i = 0; i < count; i++) {
                insertVector(startId + i, "VectorBatch-" + i, fixedVector(i * 0.1f, 0.005f));
            }

            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + count - 1)//
                    .queryForList();
            assertEquals(count, rows.size());
            assertVectorIds(rows, startId, startId + 1, startId + 2, startId + 3, startId + 4, startId + 5);
            for (ProductVectorForPg row : rows) {
                int index = row.getId() - startId;
                assertEquals("VectorBatch-" + index, row.getName());
                assertVectorEquals(fixedVector(index * 0.1f, 0.005f), row.getEmbedding());
            }
        } finally {
            cleanupRange(startId, count);
        }
    }

    // 能力归属：向量操作 / 向量类型映射。
    @Test
    @Capability(value = CapabilityId.VECTOR_CRUD_DELETE_AND_SCALAR_UPDATE, column = "vectors/vectors/vector-mapping")
    public void vector_shouldDeleteRowsAndUpdateScalarColumnsWithoutChangingEmbedding() throws SQLException {
        requiresNxnFeature(FeatureId.VECTOR);
        int updateId = baseId() + 40;
        int deleteId = baseId() + 41;
        try {
            List<Float> embedding = fixedVector(0.2f, 0.01f);
            insertVector(updateId, "VectorBeforeRename", embedding);
            insertVector(deleteId, "VectorDelete", fixedVector(0.3f, 0.01f));

            int updated = lambdaTemplate.update(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, updateId)//
                    .updateTo(ProductVectorForPg::getName, "VectorAfterRename")//
                    .doUpdate();
            assertEquals(1, updated);
            ProductVectorForPg renamed = loadVector(updateId);
            assertEquals("VectorAfterRename", renamed.getName());
            assertVectorEquals(embedding, renamed.getEmbedding());

            int deleted = lambdaTemplate.delete(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, deleteId)//
                    .doDelete();
            assertEquals(1, deleted);
            assertNull(loadVector(deleteId));
        } finally {
            cleanup(updateId, deleteId);
        }
    }

    // 能力归属：向量操作 / 向量类型映射。
    @Test
    @Capability(value = CapabilityId.VECTOR_PRECISION_BOUNDARY, column = "vectors/vectors/vector-mapping")
    public void vector_shouldPreserveRepresentativePrecisionBoundaryValues() throws SQLException {
        requiresNxnFeature(FeatureId.VECTOR);
        int id = baseId() + 600;
        try {
            List<Float> vector = new ArrayList<>(VECTOR_DIM);
            vector.add(Float.MIN_VALUE);
            vector.add(-Float.MIN_VALUE);
            vector.add(1.0f);
            vector.add(-1.0f);
            vector.add(0.0f);
            for (int i = 5; i < VECTOR_DIM; i++) {
                vector.add(0.12345f);
            }

            insertVector(id, "VectorPrecision", vector);
            ProductVectorForPg loaded = loadVector(id);
            assertNotNull(loaded);
            assertVectorEquals(vector, loaded.getEmbedding());
            // An absolute tolerance of 0.0001 would incorrectly accept zero for these values.
            assertEquals(Float.valueOf(Float.MIN_VALUE), loaded.getEmbedding().get(0));
            assertEquals(Float.valueOf(-Float.MIN_VALUE), loaded.getEmbedding().get(1));
            assertEquals(1.0f, loaded.getEmbedding().get(2), 0.0001f);
            assertEquals(-1.0f, loaded.getEmbedding().get(3), 0.0001f);
            assertEquals(0.0f, loaded.getEmbedding().get(4), 0.0001f);
            assertEquals(0.12345f, loaded.getEmbedding().get(5), 0.0001f);
        } finally {
            cleanup(id);
        }
    }

    // 能力归属：向量操作 / 向量类型映射。
    @Test
    @Capability(value = CapabilityId.VECTOR_NULL_ROUND_TRIP, column = "vectors/vectors/vector-mapping")
    public void vector_shouldRoundTripNullEmbeddingAndUpdateBetweenNullAndValue() throws SQLException {
        requiresNxnFeature(FeatureId.VECTOR);
        int nullId = baseId() + 620;
        int populatedId = baseId() + 621;
        List<Float> populated = fixedVector(0.25f, 0.005f);
        try {
            insertVector(nullId, "VectorNull", null);
            insertVector(populatedId, "VectorPopulated", populated);

            ProductVectorForPg nullRow = loadVector(nullId);
            assertNotNull(nullRow);
            assertEquals("VectorNull", nullRow.getName());
            assertNull(nullRow.getEmbedding());
            assertVectorEquals(populated, loadVector(populatedId).getEmbedding());

            int populatedRows = lambdaTemplate.update(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, nullId)//
                    .updateTo(ProductVectorForPg::getEmbedding, populated)//
                    .doUpdate();
            assertEquals(1, populatedRows);
            assertVectorEquals(populated, loadVector(nullId).getEmbedding());

            int nullRows = lambdaTemplate.update(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, nullId)//
                    .updateTo(ProductVectorForPg::getEmbedding, null)//
                    .doUpdate();
            assertEquals(1, nullRows);
            ProductVectorForPg restoredNull = loadVector(nullId);
            assertNotNull(restoredNull);
            assertEquals("VectorNull", restoredNull.getName());
            assertNull(restoredNull.getEmbedding());
            assertVectorEquals(populated, loadVector(populatedId).getEmbedding());
        } finally {
            cleanup(nullId, populatedId);
        }
    }

}
