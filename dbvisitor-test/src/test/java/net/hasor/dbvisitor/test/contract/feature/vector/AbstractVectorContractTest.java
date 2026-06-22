package net.hasor.dbvisitor.test.contract.feature.vector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.postgresql.util.PGobject;

import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractVectorContractTest extends AbstractNxnContractTest {
    private static final int VECTOR_DIM = 128;

    protected int baseId() {
        return 800000;
    }

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
                    .orderByL2(ProductVectorForPg::getEmbedding, pgVector(target))//
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
            List<ProductVectorForPg> cosineRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, cosineId)//
                    .le(ProductVectorForPg::getId, cosineId + 2)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, pgVector(sparseVector(0.1f, 1, 0.99f)))//
                    .queryForList();
            assertEquals(3, cosineRows.size());
            assertEquals(Integer.valueOf(cosineId + 1), cosineRows.get(0).getId());

            insertVector(ipId, "IP-small", constantVector(0.1f));
            insertVector(ipId + 1, "IP-mid", constantVector(0.5f));
            insertVector(ipId + 2, "IP-large", constantVector(0.9f));
            List<ProductVectorForPg> ipRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, ipId)//
                    .le(ProductVectorForPg::getId, ipId + 2)//
                    .orderByIP(ProductVectorForPg::getEmbedding, pgVector(constantVector(1.0f)))//
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

            Object target = pgVector(fixedVector(0.5f, 0.01f));
            for (MetricType metric : Arrays.asList(MetricType.L2, MetricType.COSINE, MetricType.IP)) {
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
                    .orderByL2(ProductVectorForPg::getEmbedding, pgVector(fixedVector(0.35f, 0.005f)))//
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
                    .orderByL2(ProductVectorForPg::getEmbedding, pgVector(constantVector(1.0f)))//
                    .queryForList();
            assertEquals(Integer.valueOf(l2Id + 1), l2Rows.get(0).getId());
            assertEquals(Integer.valueOf(l2Id), l2Rows.get(1).getId());
            assertEquals(Integer.valueOf(l2Id + 2), l2Rows.get(2).getId());

            List<Float> cosineTarget = sparseVector(0.01f, 0, 1.0f);
            insertVector(cosineId, "CosMath-0", sparseVector(0.01f, 0, 2.0f));
            insertVector(cosineId + 1, "CosMath-1", sparseVector(0.01f, 1, 1.0f));
            insertVector(cosineId + 2, "CosMath-2", sparseVector(0.01f, 60, 5.0f));
            List<ProductVectorForPg> cosineRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, cosineId)//
                    .le(ProductVectorForPg::getId, cosineId + 2)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, pgVector(cosineTarget))//
                    .queryForList();
            assertEquals(Integer.valueOf(cosineId), cosineRows.get(0).getId());
            assertEquals(Integer.valueOf(cosineId + 2), cosineRows.get(2).getId());
            assertDistanceOrder(cosineTarget, cosineRows, MetricType.COSINE);

            insertVector(ipId, "IPMath-0", constantVector(0.3f));
            insertVector(ipId + 1, "IPMath-1", constantVector(0.6f));
            insertVector(ipId + 2, "IPMath-2", constantVector(0.9f));
            List<ProductVectorForPg> ipRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, ipId)//
                    .le(ProductVectorForPg::getId, ipId + 2)//
                    .orderByIP(ProductVectorForPg::getEmbedding, pgVector(constantVector(1.0f)))//
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
                    .vectorByL2(ProductVectorForPg::getEmbedding, zero, 7.0)//
                    .queryForList();
            assertTrue("L2 range should keep a strict subset", nearRows.size() >= 1 && nearRows.size() < 5);
            for (ProductVectorForPg row : nearRows) {
                assertTrue(l2Distance(zero, row.getEmbedding()) < 7.0);
            }

            List<ProductVectorForPg> disabledRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .vectorByL2(false, ProductVectorForPg::getEmbedding, zero, 0.001)//
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
            List<ProductVectorForPg> cosineRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, cosineId)//
                    .le(ProductVectorForPg::getId, cosineId + 2)//
                    .vectorByCosine(ProductVectorForPg::getEmbedding, cosineTarget, 0.1)//
                    .queryForList();
            assertTrue(cosineRows.size() >= 1);
            for (ProductVectorForPg row : cosineRows) {
                assertTrue(cosineDistance(cosineTarget, row.getEmbedding()) < 0.1);
            }

            insertVector(ipId, "RIP-small", constantVector(0.01f));
            insertVector(ipId + 1, "RIP-mid", constantVector(0.5f));
            insertVector(ipId + 2, "RIP-large", constantVector(1.0f));
            List<ProductVectorForPg> ipRows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, ipId)//
                    .le(ProductVectorForPg::getId, ipId + 2)//
                    .vectorByIP(ProductVectorForPg::getEmbedding, constantVector(1.0f), -50.0)//
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
    @Capability(CapabilityId.VECTOR_RANGE_WITH_SCALAR_FILTER)
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
                    .likeRight(ProductVectorForPg::getName, "Range-A")//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, 6.0)//
                    .queryForList();

            assertTrue(rows.size() >= 1);
            for (ProductVectorForPg row : rows) {
                assertTrue(row.getName().startsWith("Range-A"));
                assertTrue(l2Distance(target, row.getEmbedding()) < 6.0);
            }
        } finally {
            cleanupRange(startId, 4);
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
                    .vectorByL2(ProductVectorForPg::getEmbedding, constantVector(0.0f), 0.001)//
                    .queryForList();

            assertEquals(0, rows.size());
        } finally {
            cleanupRange(startId, 2);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_KNN_WITH_SCALAR_FILTER)
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
                    .likeRight(ProductVectorForPg::getName, "Cat-A")//
                    .orderByL2(ProductVectorForPg::getEmbedding, pgVector(fixedVector(0.45f, 0.01f)))//
                    .queryForList();

            assertEquals(3, rows.size());
            for (ProductVectorForPg row : rows) {
                assertTrue(row.getName().startsWith("Cat-A"));
            }
            assertEquals(Integer.valueOf(startId + 4), rows.get(0).getId());
        } finally {
            cleanupRange(startId, 5);
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

    private ProductVectorForPg insertVector(int id, String name, List<Float> embedding) throws SQLException {
        ProductVectorForPg row = new ProductVectorForPg();
        row.setId(id);
        row.setName(name);
        row.setEmbedding(embedding);
        lambdaTemplate.insert(ProductVectorForPg.class).applyEntity(row).executeSumResult();
        return row;
    }

    private ProductVectorForPg loadVector(int id) throws SQLException {
        return lambdaTemplate.query(ProductVectorForPg.class)//
                .eq(ProductVectorForPg::getId, id)//
                .queryForObject();
    }

    private void cleanup(int... ids) throws SQLException {
        for (int id : ids) {
            lambdaTemplate.delete(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id)//
                    .allowEmptyWhere()//
                    .doDelete();
        }
    }

    private void cleanupRange(int startId, int count) throws SQLException {
        for (int i = 0; i < count; i++) {
            cleanup(startId + i);
        }
    }

    private List<Float> fixedVector(float base, float step) {
        List<Float> vector = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            vector.add(base + i * step);
        }
        return vector;
    }

    private List<Float> sparseVector(float defaultValue, int specialIndex, float specialValue) {
        List<Float> vector = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            vector.add(i == specialIndex ? specialValue : defaultValue);
        }
        return vector;
    }

    private List<Float> constantVector(float value) {
        List<Float> vector = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            vector.add(value);
        }
        return vector;
    }

    private Object pgVector(List<Float> vector) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(vectorLiteral(vector));
        return pgObject;
    }

    private String vectorLiteral(List<Float> vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector.get(i));
        }
        return sb.append(']').toString();
    }

    private void assertVectorEquals(List<Float> expected, List<Float> actual) {
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Vector component " + i, expected.get(i), actual.get(i), 0.0001f);
        }
    }

    private void assertDistanceOrder(List<Float> target, List<ProductVectorForPg> rows, MetricType metricType) {
        double previous = Double.NEGATIVE_INFINITY;
        for (ProductVectorForPg row : rows) {
            double current = distance(target, row.getEmbedding(), metricType);
            assertTrue("Distance should be monotonic for " + metricType + ": previous=" + previous + ", current=" + current, current >= previous - 1e-6);
            previous = current;
        }
    }

    private double distance(List<Float> left, List<Float> right, MetricType metricType) {
        if (metricType == MetricType.COSINE) {
            return cosineDistance(left, right);
        }
        if (metricType == MetricType.IP) {
            return ipDistance(left, right);
        }
        return l2Distance(left, right);
    }

    private double l2Distance(List<Float> left, List<Float> right) {
        double sum = 0;
        for (int i = 0; i < left.size(); i++) {
            double diff = left.get(i) - right.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private double cosineDistance(List<Float> left, List<Float> right) {
        double dot = 0;
        double normLeft = 0;
        double normRight = 0;
        for (int i = 0; i < left.size(); i++) {
            dot += left.get(i) * (double) right.get(i);
            normLeft += left.get(i) * (double) left.get(i);
            normRight += right.get(i) * (double) right.get(i);
        }
        return 1.0 - dot / (Math.sqrt(normLeft) * Math.sqrt(normRight));
    }

    private double ipDistance(List<Float> left, List<Float> right) {
        double dot = 0;
        for (int i = 0; i < left.size(); i++) {
            dot += left.get(i) * (double) right.get(i);
        }
        return -dot;
    }
}
