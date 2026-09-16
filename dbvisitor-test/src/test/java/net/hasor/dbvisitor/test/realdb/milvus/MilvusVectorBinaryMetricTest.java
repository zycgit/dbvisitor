/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.api.vector_query.VectorBinaryMetricCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Reuses the isolated Milvus database fixture with exact binary-vector indexes. */
public class MilvusVectorBinaryMetricTest extends VectorBinaryMetricCase {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private       boolean               created;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.database.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected String vectorTable() {
        return "binary_vectors";
    }

    @Override
    protected void prepareBinaryStorage(MetricType metric) throws SQLException {
        this.jdbcTemplate.execute("CREATE TABLE binary_vectors (id INT64 PRIMARY KEY, embedding BINARY_VECTOR(8)) WITH (consistency_level=Strong)");
        this.created = true;
        this.jdbcTemplate.execute("CREATE INDEX binary_metric ON binary_vectors(embedding) USING BIN_FLAT WITH (metric_type=" + metric.name() + ")");
        this.jdbcTemplate.execute("LOAD TABLE binary_vectors");
    }

    @Override
    protected Object binaryVector(int bits) {
        return new byte[] { (byte) bits };
    }

    @After
    public void cleanupBinaryStorage() throws SQLException {
        try {
            if (this.created) {
                this.jdbcTemplate.execute("DROP TABLE binary_vectors");
            }
        } finally {
            this.database.close();
        }
    }
}
