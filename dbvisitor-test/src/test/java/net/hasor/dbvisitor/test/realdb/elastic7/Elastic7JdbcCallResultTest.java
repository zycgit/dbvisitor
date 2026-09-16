/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7JdbcCallResultTest extends JdbcCallResultCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
    }

    @Override
    protected void createCallFixture() throws SQLException {
        setup();
        this.fixture.insert(918001, "ProcAlice", 25, "proc@test.com");
    }

    @Override
    protected String callCommand() {
        return "POST /" + this.fixture.index() + "/_search {\"_source\": [\"name\",\"age\"],\"query\": {\"term\": {\"id\": #{p_id}}}}";
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
