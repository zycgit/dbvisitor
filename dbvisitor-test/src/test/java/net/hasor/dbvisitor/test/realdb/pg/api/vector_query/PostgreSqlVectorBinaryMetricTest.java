/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.pg.api.vector_query;

import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.api.vector_query.VectorBinaryMetricCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;
import org.junit.After;
import org.postgresql.util.PGobject;

/** pgvector >= 0.7 exposes Hamming/Jaccard operators for PostgreSQL BIT fields. */
public class PostgreSqlVectorBinaryMetricTest extends VectorBinaryMetricCase {
    private final String  table = "nxn_binary_vector_" + UUID.randomUUID().toString().replace("-", "");
    private       boolean created;

    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }

    @Override
    protected String vectorTable() {
        return this.table;
    }

    @Override
    protected void prepareBinaryStorage(MetricType metric) throws SQLException {
        jdbcTemplate.execute("CREATE TABLE " + this.table + " (id INTEGER PRIMARY KEY, embedding BIT(8))");
        this.created = true;
    }

    @Override
    protected Object binaryVector(int bits) throws SQLException {
        PGobject value = new PGobject();
        value.setType("bit");
        value.setValue(String.format("%8s", Integer.toBinaryString(bits)).replace(' ', '0'));
        return value;
    }

    @After
    public void cleanupBinaryStorage() throws SQLException {
        if (this.created) {
            jdbcTemplate.execute("DROP TABLE " + this.table);
        }
    }
}
