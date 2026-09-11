/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Milvus native scores: L2 is squared distance; COSINE and IP are similarities. */
public class MilvusLambdaVectorSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_VECTOR_COSINE)
    public void cosineShouldFilterAboveThresholdAndOrderBySimilarity() throws SQLException {
        prepareVectors("COSINE");
        LambdaTemplate lambda = new LambdaTemplate(this.connection);
        List<Map<String, Object>> rows = lambda.queryFreedom(this.collection).eq("category", "keep")
                .vectorByCosine("v", new float[] { 1, 0 }, 0.8).queryForMapList();
        assertEquals(List.of(1L), ids(rows));
        assertEquals(1.0, ((Number) rows.get(0).get("score")).doubleValue(), 0.0001);
        assertOrdering(lambda, MetricType.COSINE);
        assertEquals(3, lambda.queryFreedom(this.collection).eq("category", "keep")
                .vectorByCosine(false, "v", new float[] { 1, 0 }, 0.8).queryForMapList().size());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_VECTOR_IP)
    public void innerProductShouldFilterAboveThresholdAndOrderBySimilarity() throws SQLException {
        prepareVectors("IP");
        LambdaTemplate lambda = new LambdaTemplate(this.connection);
        List<Map<String, Object>> rows = lambda.queryFreedom(this.collection).eq("category", "keep")
                .vectorByIP("v", new float[] { 1, 0 }, 0.5).queryForMapList();
        assertEquals(List.of(1L), ids(rows));
        assertEquals(1.0, ((Number) rows.get(0).get("score")).doubleValue(), 0.0001);
        assertOrdering(lambda, MetricType.IP);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_VECTOR_L2)
    public void l2ShouldFilterBelowSquaredDistanceAndHonorTopK() throws SQLException {
        prepareVectors("L2");
        LambdaTemplate lambda = new LambdaTemplate(this.connection);
        List<Map<String, Object>> rows = lambda.queryFreedom(this.collection).eq("category", "keep")
                .vectorByL2("v", new float[] { 1, 0 }, 0.5).queryForMapList();
        assertEquals(List.of(1L), ids(rows));
        assertEquals(0.0, ((Number) rows.get(0).get("score")).doubleValue(), 0.0001);
        assertOrdering(lambda, MetricType.L2);
        assertEquals(3, lambda.queryFreedom(this.collection).eq("category", "keep")
                .vectorByL2(false, "v", new float[] { 1, 0 }, 0.5).queryForMapList().size());
        assertEquals(List.of(), lambda.queryFreedom(this.collection).eq("category", "keep")
                .vectorByL2("v", new float[] { 10, 10 }, 0.01).queryForMapList());
    }

    private void assertOrdering(LambdaTemplate lambda, MetricType metric) throws SQLException {
        List<Map<String, Object>> all = lambda.queryFreedom(this.collection).eq("category", "keep")
                .orderByMetric(metric, "v", new float[] { 1, 0 }).queryForMapList();
        assertEquals(List.of(1L, 2L, 3L), ids(all));
        // Verify the metric-specific entry points as well as orderByMetric.
        List<Map<String, Object>> specialized;
        if (metric == MetricType.L2) {
            specialized = lambda.queryFreedom(this.collection).eq("category", "keep")
                    .orderByL2("v", new float[] { 1, 0 }).queryForMapList();
        } else if (metric == MetricType.COSINE) {
            specialized = lambda.queryFreedom(this.collection).eq("category", "keep")
                    .orderByCosine("v", new float[] { 1, 0 }).queryForMapList();
        } else {
            specialized = lambda.queryFreedom(this.collection).eq("category", "keep")
                    .orderByIP("v", new float[] { 1, 0 }).queryForMapList();
        }
        assertEquals(List.of(1L, 2L, 3L), ids(specialized));
        List<Map<String, Object>> top = lambda.queryFreedom(this.collection).eq("category", "keep")
                .orderByMetric(metric, "v", new float[] { 1, 0 }).initPage(2, 0).queryForMapList();
        assertEquals(List.of(1L, 2L), ids(top));
    }

    private void prepareVectors(String metric) throws SQLException {
        createCollection("id INT64 PRIMARY KEY, category VARCHAR(16), v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", metric);
        loadCollection();
        float[][] vectors = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
        try (PreparedStatement insert = this.connection.prepareStatement("INSERT INTO " + this.collection + " (id, category, v) VALUES (?, ?, ?)")) {
            for (int i = 0; i < vectors.length; i++) {
                insert.setLong(1, i + 1);
                insert.setString(2, i == 3 ? "exclude" : "keep");
                insert.setObject(3, vectors[i]);
                assertEquals(1, insert.executeUpdate());
            }
        }
    }

    private List<Long> ids(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> ((Number) row.get("id")).longValue()).collect(Collectors.toList());
    }
}
