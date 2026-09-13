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

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class VectorTypeMappingCase extends VectorQuerySupport {
    @Test
    @Capability(CapabilityId.VECTOR_CRUD_ROUND_TRIP)
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

    @Test
    @Capability(CapabilityId.VECTOR_BATCH_QUERY)
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
        } finally {
            cleanupRange(startId, count);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_CRUD_DELETE_AND_SCALAR_UPDATE)
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

    @Test
    @Capability(CapabilityId.VECTOR_PRECISION_BOUNDARY)
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
            assertEquals(1.0f, loaded.getEmbedding().get(2), 0.0001f);
            assertEquals(-1.0f, loaded.getEmbedding().get(3), 0.0001f);
            assertEquals(0.0f, loaded.getEmbedding().get(4), 0.0001f);
            assertEquals(0.12345f, loaded.getEmbedding().get(5), 0.0001f);
        } finally {
            cleanup(id);
        }
    }

}
