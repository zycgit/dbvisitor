/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.test.contract.api.vector_query.VectorCombinedQueryCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticVectorFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7VectorCombinedQueryTest extends VectorCombinedQueryCase {
    private final ElasticVectorFixture fixture = new ElasticVectorFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        lambdaTemplate = fixture.open();
    }

    @Override
    protected Object queryVector(List<Float> vector) {
        return vector;
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
