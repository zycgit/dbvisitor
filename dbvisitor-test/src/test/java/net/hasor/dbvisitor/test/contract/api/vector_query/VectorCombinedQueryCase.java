/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.vector_query;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class VectorCombinedQueryCase extends VectorQuerySupport {
    // 能力归属：向量操作 / 组合查询。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_WITH_SCALAR_FILTER, column = "vectors/vectors/vector--scalar")
    public void knn_shouldComposeVectorRangeWithScalarPredicates() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 460;
        try {
            insertVector(startId, "Range-A-0", constantVector(0.1f));
            insertVector(startId + 1, "Range-B-1", constantVector(0.2f));
            insertVector(startId + 2, "Range-A-2", constantVector(0.5f));
            insertVector(startId + 3, "Range-A-3", constantVector(0.9f));

            List<Float> target = constantVector(0.0f);
            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 3)//
                    .in(ProductVectorForPg::getName, List.of("Range-A-0", "Range-A-2", "Range-A-3"))//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, rangeBound(MetricType.L2, 6.0))//
                    .queryForList();

            assertFalse(rows.isEmpty());
            assertVectorIds(rows, startId, startId + 2);
            for (ProductVectorForPg row : rows) {
                assertTrue(row.getName().startsWith("Range-A"));
                assertTrue(l2Distance(target, row.getEmbedding()) < 6.0);
            }
        } finally {
            cleanupRange(startId, 4);
        }
    }

    // 能力归属：向量操作 / 组合查询。
    @Test
    @Capability(value = CapabilityId.VECTOR_KNN_WITH_SCALAR_FILTER, column = "vectors/vectors/vector--scalar")
    public void knn_shouldComposeVectorOrderingWithScalarPredicates() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 500;
        try {
            insertVector(startId, "Cat-A-0", fixedVector(0.1f, 0.01f));
            insertVector(startId + 1, "Cat-B-1", fixedVector(0.2f, 0.01f));
            insertVector(startId + 2, "Cat-A-2", fixedVector(0.3f, 0.01f));
            insertVector(startId + 3, "Cat-B-3", fixedVector(0.4f, 0.01f));
            insertVector(startId + 4, "Cat-A-4", fixedVector(0.5f, 0.01f));

            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .in(ProductVectorForPg::getName, List.of("Cat-A-0", "Cat-A-2", "Cat-A-4"))//
                    .orderByL2(ProductVectorForPg::getEmbedding, queryVector(fixedVector(0.45f, 0.01f)))//
                    .queryForList();

            assertEquals(3, rows.size());
            for (ProductVectorForPg row : rows) {
                assertTrue(row.getName().startsWith("Cat-A"));
            }
            assertEquals(Integer.valueOf(startId + 4), rows.get(0).getId());
            assertOrderedVectorIds(rows, startId + 4, startId + 2, startId);
            assertDistanceOrder(fixedVector(0.45f, 0.01f), rows, MetricType.L2);
        } finally {
            cleanupRange(startId, 5);
        }
    }

    // 能力归属：向量操作 / 组合查询。
    @Test
    @Capability(value = CapabilityId.VECTOR_KNN_SCALAR_TOP_K, column = "vectors/vectors/vector--scalar")
    public void knn_shouldApplyScalarFilteringBeforeSelectingTopK() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 680;
        List<Float> target = constantVector(0.0f);
        try {
            insertVector(startId, "TopK-Exclude", constantVector(0.01f));
            insertVector(startId + 1, "TopK-Keep", constantVector(0.5f));
            insertVector(startId + 2, "TopK-Keep", constantVector(0.2f));
            insertVector(startId + 3, "TopK-Exclude", constantVector(0.02f));
            insertVector(startId + 4, "TopK-Keep", constantVector(0.4f));

            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .eq(ProductVectorForPg::getName, "TopK-Keep")//
                    .orderByL2(ProductVectorForPg::getEmbedding, queryVector(target))//
                    .initPage(2, 0)//
                    .queryForList();

            assertOrderedVectorIds(rows, startId + 2, startId + 4);
            for (ProductVectorForPg row : rows) {
                assertEquals("TopK-Keep", row.getName());
            }
            assertDistanceOrder(target, rows, MetricType.L2);

            List<ProductVectorForPg> noCandidates = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .eq(ProductVectorForPg::getName, "TopK-Missing")//
                    .orderByL2(ProductVectorForPg::getEmbedding, queryVector(target))//
                    .initPage(2, 0)//
                    .queryForList();
            assertTrue(noCandidates.isEmpty());
        } finally {
            cleanupRange(startId, 5);
        }
    }

}
