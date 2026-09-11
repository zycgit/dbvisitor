/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.vector_query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class VectorKnnOrderingContractTest extends VectorQuerySupport {
    @Test
    @Capability(CapabilityId.VECTOR_KNN_ORDER_L2)
    public void knn_shouldOrderByL2Distance() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 100;
        try {
            for (int i = 0; i < 5; i++) {
                insertVector(startId + i, "L2-" + i, fixedVector(i * 0.2f, 0.01f));
            }

            List<Float> target = fixedVector(0.41f, 0.01f);
            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .orderByL2(ProductVectorForPg::getEmbedding, queryVector(target))//
                    .queryForList();

            assertEquals(5, rows.size());
            assertEquals(Integer.valueOf(startId + 2), rows.get(0).getId());
            assertDistanceOrder(target, rows, MetricType.L2);
        } finally {
            cleanupRange(startId, 5);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_KNN_ORDER_COSINE_IP)
    public void knn_shouldOrderByCosineAndInnerProductDistance() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int cosineId = baseId() + 200;
        int ipId = baseId() + 220;
        try {
            insertVector(cosineId, "Cos-0", sparseVector(0.1f, 0, 1.0f));
            insertVector(cosineId + 1, "Cos-1", sparseVector(0.1f, 1, 1.0f));
            insertVector(cosineId + 2, "Cos-2", sparseVector(0.1f, 2, 1.0f));
            prepareMetric(MetricType.COSINE);
            List<ProductVectorForPg> cosineRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, cosineId)//
                    .le(ProductVectorForPg::getId, cosineId + 2)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, queryVector(sparseVector(0.1f, 1, 0.99f)))//
                    .queryForList();
            assertEquals(3, cosineRows.size());
            assertEquals(Integer.valueOf(cosineId + 1), cosineRows.get(0).getId());

            insertVector(ipId, "IP-small", constantVector(0.1f));
            insertVector(ipId + 1, "IP-mid", constantVector(0.5f));
            insertVector(ipId + 2, "IP-large", constantVector(0.9f));
            prepareMetric(MetricType.IP);
            List<ProductVectorForPg> ipRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, ipId)//
                    .le(ProductVectorForPg::getId, ipId + 2)//
                    .orderByIP(ProductVectorForPg::getEmbedding, queryVector(constantVector(1.0f)))//
                    .queryForList();
            assertEquals(3, ipRows.size());
            assertEquals(Integer.valueOf(ipId + 2), ipRows.get(0).getId());
        } finally {
            cleanupRange(cosineId, 3);
            cleanupRange(ipId, 3);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_KNN_ORDER_BY_METRIC)
    public void knn_shouldSupportMetricDrivenOrderingForMainPgVectorMetrics() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 300;
        try {
            insertVector(startId, "Metric-0", fixedVector(0.1f, 0.01f));
            insertVector(startId + 1, "Metric-1", fixedVector(0.5f, 0.01f));
            insertVector(startId + 2, "Metric-2", fixedVector(0.9f, 0.01f));

            Object target = queryVector(fixedVector(0.5f, 0.01f));
            for (MetricType metric : Arrays.asList(MetricType.L2, MetricType.COSINE, MetricType.IP)) {
                prepareMetric(metric);
                List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                        .ge(ProductVectorForPg::getId, startId)//
                        .le(ProductVectorForPg::getId, startId + 2)//
                        .orderByMetric(metric, ProductVectorForPg::getEmbedding, target)//
                        .queryForList();
                assertEquals("Metric " + metric + " should return all rows", 3, rows.size());
            }
        } finally {
            cleanupRange(startId, 3);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_KNN_TOP_K)
    public void knn_shouldLimitNearestNeighborResultsWithPageSize() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 340;
        int total = 8;
        int topK = 3;
        try {
            for (int i = 0; i < total; i++) {
                insertVector(startId + i, "TopK-" + i, fixedVector(i * 0.1f, 0.005f));
            }

            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + total - 1)//
                    .orderByL2(ProductVectorForPg::getEmbedding, queryVector(fixedVector(0.35f, 0.005f)))//
                    .initPage(topK, 0)//
                    .queryForList();

            assertEquals(topK, rows.size());
            int firstId = rows.get(0).getId();
            assertTrue(firstId == startId + 3 || firstId == startId + 4);
        } finally {
            cleanupRange(startId, total);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_KNN_MATH_ORDERING)
    public void knn_shouldMatchKnownL2CosineAndInnerProductOrdering() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int l2Id = baseId() + 360;
        int cosineId = baseId() + 370;
        int ipId = baseId() + 380;
        try {
            insertVector(l2Id, "L2Math-0", constantVector(0.5f));
            insertVector(l2Id + 1, "L2Math-1", constantVector(0.9f));
            insertVector(l2Id + 2, "L2Math-2", constantVector(0.0f));
            List<ProductVectorForPg> l2Rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, l2Id)//
                    .le(ProductVectorForPg::getId, l2Id + 2)//
                    .orderByL2(ProductVectorForPg::getEmbedding, queryVector(constantVector(1.0f)))//
                    .queryForList();
            assertEquals(Integer.valueOf(l2Id + 1), l2Rows.get(0).getId());
            assertEquals(Integer.valueOf(l2Id), l2Rows.get(1).getId());
            assertEquals(Integer.valueOf(l2Id + 2), l2Rows.get(2).getId());

            List<Float> cosineTarget = sparseVector(0.01f, 0, 1.0f);
            insertVector(cosineId, "CosMath-0", sparseVector(0.01f, 0, 2.0f));
            insertVector(cosineId + 1, "CosMath-1", sparseVector(0.01f, 1, 1.0f));
            insertVector(cosineId + 2, "CosMath-2", sparseVector(0.01f, 60, 5.0f));
            prepareMetric(MetricType.COSINE);
            List<ProductVectorForPg> cosineRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, cosineId)//
                    .le(ProductVectorForPg::getId, cosineId + 2)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, queryVector(cosineTarget))//
                    .queryForList();
            assertEquals(Integer.valueOf(cosineId), cosineRows.get(0).getId());
            assertEquals(Integer.valueOf(cosineId + 2), cosineRows.get(2).getId());
            assertDistanceOrder(cosineTarget, cosineRows, MetricType.COSINE);

            insertVector(ipId, "IPMath-0", constantVector(0.3f));
            insertVector(ipId + 1, "IPMath-1", constantVector(0.6f));
            insertVector(ipId + 2, "IPMath-2", constantVector(0.9f));
            prepareMetric(MetricType.IP);
            List<ProductVectorForPg> ipRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, ipId)//
                    .le(ProductVectorForPg::getId, ipId + 2)//
                    .orderByIP(ProductVectorForPg::getEmbedding, queryVector(constantVector(1.0f)))//
                    .queryForList();
            assertEquals(Integer.valueOf(ipId + 2), ipRows.get(0).getId());
            assertEquals(Integer.valueOf(ipId), ipRows.get(2).getId());
            assertDistanceOrder(constantVector(1.0f), ipRows, MetricType.IP);
        } finally {
            cleanupRange(l2Id, 3);
            cleanupRange(cosineId, 3);
            cleanupRange(ipId, 3);
        }
    }

}
