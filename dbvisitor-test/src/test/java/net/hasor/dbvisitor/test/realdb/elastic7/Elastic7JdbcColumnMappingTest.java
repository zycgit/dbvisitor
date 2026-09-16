/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcColumnMappingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7JdbcColumnMappingTest extends JdbcColumnMappingCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());

    }

    @Override
    protected void seedColumnValue() throws SQLException {
        jdbcTemplate.executeUpdate("POST /" + fixture.index() + "/_doc {\"NXN_VALUE\": ?}", new Object[] { "NXN-Column" });
    }

    @Override
    protected String columnQuery() {
        return "POST /" + fixture.index() + "/_search {\"_source\": [\"NXN_VALUE\"]}";
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
