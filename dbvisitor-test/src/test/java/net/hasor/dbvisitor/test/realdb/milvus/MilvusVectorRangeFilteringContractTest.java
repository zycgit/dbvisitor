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
import java.util.List;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.contract.api.vector_query.VectorRangeFilteringContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Common vector assertions executed with Milvus-native values and metric indexes. */
public class MilvusVectorRangeFilteringContractTest extends VectorRangeFilteringContractTest {
    private final MilvusVectorQueryFixture fixture = new MilvusVectorQueryFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        this.lambdaTemplate = this.fixture.open();
    }

    @Override
    protected Object queryVector(List<Float> vector) {
        return vector;
    }

    @Override
    protected void prepareMetric(MetricType metric) throws SQLException {
        this.fixture.prepareMetric(metric);
    }

    @Override
    protected double rangeBound(MetricType metric, double distance) {
        return this.fixture.rangeBound(metric, distance);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
