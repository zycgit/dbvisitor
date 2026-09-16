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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class VectorRangeFilteringCase extends VectorQuerySupport {
    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_FILTER, column = "vectors/vectors/range-filters")
    public void knn_shouldFilterByVectorRangeAndHonorDisabledPredicate() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 400;
        try {
            for (int i = 0; i < 5; i++) {
                insertVector(startId + i, "Range-" + i, fixedVector(i * 0.5f, 0.0f));
            }

            List<Float> zero = constantVector(0.0f);
            List<ProductVectorForPg> nearRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .vectorByL2(ProductVectorForPg::getEmbedding, zero, rangeBound(MetricType.L2, 7.0))//
                    .queryForList();
            assertTrue("L2 range should keep a strict subset", nearRows.size() >= 1 && nearRows.size() < 5);
            assertVectorIds(nearRows, startId, startId + 1);
            for (ProductVectorForPg row : nearRows) {
                assertTrue(l2Distance(zero, row.getEmbedding()) < 7.0);
            }

            List<ProductVectorForPg> disabledRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .vectorByL2(false, ProductVectorForPg::getEmbedding, zero, rangeBound(MetricType.L2, 0.001))//
                    .queryForList();
            assertEquals(5, disabledRows.size());
            assertVectorIds(disabledRows, startId, startId + 1, startId + 2, startId + 3, startId + 4);

            List<ProductVectorForPg> absentVectorRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .vectorByL2(false, ProductVectorForPg::getEmbedding, null, null)//
                    .vectorByCosine(false, ProductVectorForPg::getEmbedding, null, null)//
                    .vectorByIP(false, ProductVectorForPg::getEmbedding, null, null)//
                    .vectorByHamming(false, ProductVectorForPg::getEmbedding, null, null)//
                    .vectorByJaccard(false, ProductVectorForPg::getEmbedding, null, null)//
                    .vectorByBM25(false, ProductVectorForPg::getEmbedding, null, null)//
                    .queryForList();
            assertVectorIds(absentVectorRows, startId, startId + 1, startId + 2, startId + 3, startId + 4);
        } finally {
            cleanupRange(startId, 5);
        }
    }

    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_METRIC_VARIANTS, column = "vectors/vectors/range-filters")
    public void knn_shouldFilterVectorRangesForCosineAndInnerProductMetrics() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int cosineId = baseId() + 430;
        int ipId = baseId() + 440;
        try {
            insertVector(cosineId, "RCos-0", sparseVector(0.01f, 0, 1.0f));
            insertVector(cosineId + 1, "RCos-1", sparseVector(0.01f, 1, 1.0f));
            insertVector(cosineId + 2, "RCos-2", sparseVector(0.01f, 50, 1.0f));
            List<Float> cosineTarget = sparseVector(0.01f, 0, 0.9f);
            prepareMetric(MetricType.COSINE);
            List<ProductVectorForPg> cosineRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, cosineId)//
                    .le(ProductVectorForPg::getId, cosineId + 2)//
                    .vectorByCosine(ProductVectorForPg::getEmbedding, cosineTarget, rangeBound(MetricType.COSINE, 0.1))//
                    .queryForList();
            assertTrue(cosineRows.size() >= 1);
            assertVectorIds(cosineRows, cosineId);
            for (ProductVectorForPg row : cosineRows) {
                assertTrue(cosineDistance(cosineTarget, row.getEmbedding()) < 0.1);
            }

            insertVector(ipId, "RIP-small", constantVector(0.01f));
            insertVector(ipId + 1, "RIP-mid", constantVector(0.5f));
            insertVector(ipId + 2, "RIP-large", constantVector(1.0f));
            prepareMetric(MetricType.IP);
            List<ProductVectorForPg> ipRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, ipId)//
                    .le(ProductVectorForPg::getId, ipId + 2)//
                    .vectorByIP(ProductVectorForPg::getEmbedding, constantVector(1.0f), rangeBound(MetricType.IP, -50.0))//
                    .queryForList();
            assertTrue(ipRows.size() >= 1);
            assertVectorIds(ipRows, ipId + 1, ipId + 2);
            for (ProductVectorForPg row : ipRows) {
                assertTrue(ipDistance(constantVector(1.0f), row.getEmbedding()) < -50.0);
            }
        } finally {
            cleanupRange(cosineId, 3);
            cleanupRange(ipId, 3);
        }
    }

    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_EMPTY_RESULT, column = "vectors/vectors/range-filters")
    public void knn_shouldReturnEmptyListWhenVectorRangeHasNoMatches() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 480;
        try {
            insertVector(startId, "RangeEmpty-0", constantVector(1.0f));
            insertVector(startId + 1, "RangeEmpty-1", constantVector(0.5f));

            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 1)//
                    .vectorByL2(ProductVectorForPg::getEmbedding, constantVector(0.0f), rangeBound(MetricType.L2, 0.001))//
                    .queryForList();

            assertEquals(0, rows.size());
        } finally {
            cleanupRange(startId, 2);
        }
    }

    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_STRICT_BOUNDARY, column = "vectors/vectors/range-filters")
    public void knn_shouldExcludeVectorsExactlyOnTheL2RangeThreshold() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 650;
        try {
            // One nonzero component gives exact distances 0.5, 1.0 and 1.5.
            insertVector(startId, "RangeInside", sparseVector(0.0f, 0, 0.5f));
            insertVector(startId + 1, "RangeBoundary", sparseVector(0.0f, 0, 1.0f));
            insertVector(startId + 2, "RangeOutside", sparseVector(0.0f, 0, 1.5f));

            List<Float> target = constantVector(0.0f);
            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 2)//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, rangeBound(MetricType.L2, 1.0))//
                    .queryForList();

            assertVectorIds(rows, startId);
            assertVectorEquals(sparseVector(0.0f, 0, 0.5f), rows.get(0).getEmbedding());
        } finally {
            cleanupRange(startId, 3);
        }
    }

}
