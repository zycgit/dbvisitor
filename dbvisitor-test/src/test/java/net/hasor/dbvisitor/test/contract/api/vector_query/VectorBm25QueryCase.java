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

/** BM25 uses the datasource's analyzer and sparse-vector function, not Java-side scoring. */
@NxnContract
public abstract class VectorBm25QueryCase extends AbstractNxnContractTest {
    protected abstract String vectorTable();

    protected abstract void prepareBm25Documents() throws SQLException;

    // 能力归属：向量操作 / KNN 近邻排序。
    @Test
    @Capability(value = CapabilityId.VECTOR_KNN_ORDER_BM25, column = "vectors/vectors/knn-ordering")
    public void knn_shouldRankBm25MatchesThroughSpecializedAndMetricEntryPoints() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        prepareBm25Documents();
        List<Map<String, Object>> specialized = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .orderByBM25("embedding", "milvus")//
                .initPage(2, 0)//
                .queryForMapList();
        assertEquals(Arrays.asList(11L, 13L), ids(specialized));

        List<Map<String, Object>> dynamic = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .orderByMetric(MetricType.BM25, "embedding", "milvus")//
                .initPage(1, 0)//
                .queryForMapList();
        assertEquals(List.of(11L), ids(dynamic));
    }

    // 能力归属：向量操作 / 距离范围过滤。
    @Test
    @Capability(value = CapabilityId.VECTOR_RANGE_BM25, column = "vectors/vectors/range-filters")
    public void vectorRange_shouldKeepPositiveBm25MatchesAndHonorDisabledFilter() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        prepareBm25Documents();
        List<Map<String, Object>> rows = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .vectorByBM25("embedding", "milvus", 0.0)//
                .initPage(4, 0)//
                .queryForMapList();
        assertEquals(2, rows.size());
        assertEquals(new HashSet<>(Arrays.asList(11L, 13L)), new HashSet<>(ids(rows)));

        // A zero threshold alone could still pass if the radius was silently ignored.
        List<Map<String, Object>> aboveAllScores = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .vectorByBM25("embedding", "milvus", 1000000.0)//
                .initPage(4, 0)//
                .queryForMapList();
        assertTrue("No score from the four short fixture documents can reach this threshold", aboveAllScores.isEmpty());

        List<Map<String, Object>> disabled = lambdaTemplate.queryFreedom(vectorTable())//
                .select("id")//
                .vectorByBM25(false, "embedding", null, null)//
                .queryForMapList();
        assertEquals(4, disabled.size());
        assertEquals(new HashSet<>(Arrays.asList(11L, 12L, 13L, 14L)), new HashSet<>(ids(disabled)));
    }

    private List<Long> ids(List<Map<String, Object>> rows) {
        List<Long> ids = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Object id = row.get("id");
            assertTrue("Expected numeric BM25 result id", id instanceof Number);
            ids.add(((Number) id).longValue());
        }
        return ids;
    }
}
