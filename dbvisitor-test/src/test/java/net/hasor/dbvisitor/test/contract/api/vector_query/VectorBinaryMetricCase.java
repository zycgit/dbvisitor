/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.vector_query;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Binary distance entry points use binary fields, never FLOAT_VECTOR substitutes. */
@NxnContract
public abstract class VectorBinaryMetricCase extends AbstractNxnContractTest {
    protected abstract String vectorTable();

    protected abstract void prepareBinaryStorage(MetricType metric) throws SQLException;

    protected abstract Object binaryVector(int bits) throws SQLException;

    // 能力归属：向量操作 / KNN 近邻排序。
    @Test
    @Capability(value = CapabilityId.VECTOR_KNN_ORDER_HAMMING, column = "vectors/vectors/knn-ordering")
    public void knn_shouldOrderBinaryVectorsByHammingDistance() throws SQLException {
        prepareRows(MetricType.HAMMING);
        Object target = binaryVector(0xf0);
        List<Map<String, Object>> specialized = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .orderByHamming("embedding", target)//
                .initPage(3, 0)//
                .queryForMapList();
        assertEquals(Arrays.asList(11L, 13L, 14L), ids(specialized));

        List<Map<String, Object>> dynamic = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .orderByMetric(MetricType.HAMMING, "embedding", target)//
                .initPage(4, 0)//
                .queryForMapList();
        assertEquals(Arrays.asList(11L, 13L, 14L, 12L), ids(dynamic));
    }

    // 能力归属：向量操作 / KNN 近邻排序。
    @Test
    @Capability(value = CapabilityId.VECTOR_KNN_ORDER_JACCARD, column = "vectors/vectors/knn-ordering")
    public void knn_shouldOrderBinaryVectorsByJaccardDistance() throws SQLException {
        prepareRows(MetricType.JACCARD);
        Object target = binaryVector(0xf0);
        List<Map<String, Object>> specialized = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .orderByJaccard("embedding", target)//
                .initPage(3, 0)//
                .queryForMapList();
        assertEquals(Arrays.asList(11L, 13L, 14L), ids(specialized));

        List<Map<String, Object>> dynamic = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .orderByMetric(MetricType.JACCARD, "embedding", target)//
                .initPage(4, 0)//
                .queryForMapList();
        assertEquals(Arrays.asList(11L, 13L, 14L, 12L), ids(dynamic));
    }

    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_HAMMING, column = "vectors/vectors/range-filters")
    public void vectorRange_shouldFilterBinaryVectorsByStrictHammingDistance() throws SQLException {
        prepareRows(MetricType.HAMMING);
        List<Map<String, Object>> rows = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .vectorByHamming("embedding", binaryVector(0xf0), 2)//
                .initPage(4, 0)//
                .queryForMapList();
        assertEquals(2, rows.size());
        assertEquals(new HashSet<>(Arrays.asList(11L, 13L)), new HashSet<>(ids(rows)));
    }

    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_JACCARD, column = "vectors/vectors/range-filters")
    public void vectorRange_shouldFilterBinaryVectorsByStrictJaccardDistance() throws SQLException {
        prepareRows(MetricType.JACCARD);
        List<Map<String, Object>> rows = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .vectorByJaccard("embedding", binaryVector(0xf0), 0.5)//
                .initPage(4, 0)//
                .queryForMapList();
        assertEquals(2, rows.size());
        assertEquals(new HashSet<>(Arrays.asList(11L, 13L)), new HashSet<>(ids(rows)));
    }

    private void prepareRows(MetricType metric) throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        prepareBinaryStorage(metric);
        // Distances from 11110000: Hamming = 0, 8, 1, 2; Jaccard = 0, 1, 0.25, 0.5.
        int[] vectors = { 0xf0, 0x0f, 0xe0, 0xc0 };
        for (int index = 0; index < vectors.length; index++) {
            int inserted = lambdaTemplate.insertFreedom(vectorTable())//
                    .applyMap(Map.of("id", 11 + index, "embedding", binaryVector(vectors[index])))//
                    .executeSumResult();
            assertEquals(1, inserted);
        }
    }

    private List<Long> ids(List<Map<String, Object>> rows) {
        List<Long> ids = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Object id = row.get("id");
            assertTrue("Expected numeric binary-vector id", id instanceof Number);
            ids.add(((Number) id).longValue());
        }
        return ids;
    }
}
