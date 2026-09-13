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

import org.junit.Test;

import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class VectorRangeFilteringCase extends VectorQuerySupport {
    @Test
    @Capability(CapabilityId.VECTOR_RANGE_FILTER)
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
            for (ProductVectorForPg row : nearRows) {
                assertTrue(l2Distance(zero, row.getEmbedding()) < 7.0);
            }

            List<ProductVectorForPg> disabledRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .vectorByL2(false, ProductVectorForPg::getEmbedding, zero, rangeBound(MetricType.L2, 0.001))//
                    .queryForList();
            assertEquals(5, disabledRows.size());
        } finally {
            cleanupRange(startId, 5);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_RANGE_METRIC_VARIANTS)
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
            for (ProductVectorForPg row : ipRows) {
                assertTrue(ipDistance(constantVector(1.0f), row.getEmbedding()) < -50.0);
            }
        } finally {
            cleanupRange(cosineId, 3);
            cleanupRange(ipId, 3);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_RANGE_EMPTY_RESULT)
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

}
