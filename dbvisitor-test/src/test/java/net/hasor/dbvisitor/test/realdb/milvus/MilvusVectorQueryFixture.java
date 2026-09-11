/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.io.IOException;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.mapping.MappingRegistry;

/** Shared 128-dimensional data and native metric indexes for the common vector contracts. */
final class MilvusVectorQueryFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private JdbcTemplate jdbc;
    private MetricType metric;

    LambdaTemplate open() throws IOException, SQLException {
        this.jdbc = new JdbcTemplate(this.database.open());
        this.jdbc.execute("""
                CREATE TABLE product_vector (id INT64 PRIMARY KEY, name VARCHAR(128), embedding FLOAT_VECTOR(128))
                WITH (consistency_level=Strong)
                """);
        prepareMetric(MetricType.L2);
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/mapping/milvus_product_vector.xml");
        return new LambdaTemplate(this.jdbc.getConnection(), registry, null);
    }

    void prepareMetric(MetricType requested) throws SQLException {
        if (requested == this.metric) {
            return;
        }
        if (this.metric != null) {
            this.jdbc.execute("RELEASE TABLE product_vector");
            this.jdbc.execute("DROP INDEX vector_metric ON product_vector");
        }
        this.jdbc.execute("CREATE INDEX vector_metric ON product_vector(embedding) USING FLAT WITH (metric_type=" + requested.name() + ")");
        this.jdbc.execute("LOAD TABLE product_vector");
        this.metric = requested;
    }

    double rangeBound(MetricType metric, double distance) {
        // Preserve the common mathematical predicate using Milvus's native score convention.
        switch (metric) {
            case L2:
                return distance * distance;
            case COSINE:
                return 1.0 - distance;
            case IP:
                return -distance;
            default:
                throw new IllegalArgumentException("No fixture distance conversion for " + metric);
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.jdbc != null) {
                this.jdbc.execute("DROP TABLE IF EXISTS product_vector");
            }
        } finally {
            this.database.close();
        }
    }
}
