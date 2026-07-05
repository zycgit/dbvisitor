package net.hasor.dbvisitor.test.contract.api.vector_query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class VecQSupport extends AbstractNxnContractTest {
    protected static final int VECTOR_DIM = 128;

    protected int baseId() {
        return 800000;
    }

    protected ProductVectorForPg insertVector(int id, String name, List<Float> embedding) throws SQLException {
        ProductVectorForPg row = new ProductVectorForPg();
        row.setId(id);
        row.setName(name);
        row.setEmbedding(embedding);
        lambdaTemplate.insert(ProductVectorForPg.class).applyEntity(row).executeSumResult();
        return row;
    }

    protected ProductVectorForPg loadVector(int id) throws SQLException {
        return lambdaTemplate.query(ProductVectorForPg.class)//
                .eq(ProductVectorForPg::getId, id)//
                .queryForObject();
    }

    protected void cleanup(int... ids) throws SQLException {
        for (int id : ids) {
            lambdaTemplate.delete(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id)//
                    .allowEmptyWhere()//
                    .doDelete();
        }
    }

    protected void cleanupRange(int startId, int count) throws SQLException {
        for (int i = 0; i < count; i++) {
            cleanup(startId + i);
        }
    }

    protected List<Float> fixedVector(float base, float step) {
        List<Float> vector = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            vector.add(base + i * step);
        }
        return vector;
    }

    protected List<Float> sparseVector(float defaultValue, int specialIndex, float specialValue) {
        List<Float> vector = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            vector.add(i == specialIndex ? specialValue : defaultValue);
        }
        return vector;
    }

    protected List<Float> constantVector(float value) {
        List<Float> vector = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            vector.add(value);
        }
        return vector;
    }

    protected Object pgVector(List<Float> vector) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(vectorLiteral(vector));
        return pgObject;
    }

    protected String vectorLiteral(List<Float> vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector.get(i));
        }
        return sb.append(']').toString();
    }

    protected void assertVectorEquals(List<Float> expected, List<Float> actual) {
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Vector component " + i, expected.get(i), actual.get(i), 0.0001f);
        }
    }

    protected void assertDistanceOrder(List<Float> target, List<ProductVectorForPg> rows, MetricType metricType) {
        double previous = Double.NEGATIVE_INFINITY;
        for (ProductVectorForPg row : rows) {
            double current = distance(target, row.getEmbedding(), metricType);
            assertTrue("Distance should be monotonic for " + metricType + ": previous=" + previous + ", current=" + current, current >= previous - 1e-6);
            previous = current;
        }
    }

    protected double distance(List<Float> left, List<Float> right, MetricType metricType) {
        if (metricType == MetricType.COSINE) {
            return cosineDistance(left, right);
        }
        if (metricType == MetricType.IP) {
            return ipDistance(left, right);
        }
        return l2Distance(left, right);
    }

    protected double l2Distance(List<Float> left, List<Float> right) {
        double sum = 0;
        for (int i = 0; i < left.size(); i++) {
            double diff = left.get(i) - right.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    protected double cosineDistance(List<Float> left, List<Float> right) {
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

    protected double ipDistance(List<Float> left, List<Float> right) {
        double dot = 0;
        for (int i = 0; i < left.size(); i++) {
            dot += left.get(i) * (double) right.get(i);
        }
        return -dot;
    }
}
