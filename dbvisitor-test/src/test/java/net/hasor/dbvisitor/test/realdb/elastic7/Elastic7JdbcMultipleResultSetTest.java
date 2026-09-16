/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7JdbcMultipleResultSetTest extends JdbcMultipleResultSetCase {
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

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    private String search(String query) {
        return "POST /" + fixture.index() + "/_search {\"_source\": [\"id\",\"name\",\"age\"],\"query\": " + query + ",\"sort\": [{\"id\": \"asc\"}]}";
    }

    @Override
    protected void seedUsers() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            fixture.insert(baseId() + i, "NXN-Multi-" + i, 30 + i, "nxn-multi-" + i + "@test.com");
        }
    }

    @Override
    protected String literalMultipleCommand() {
        return search("{\"term\": {\"id\": " + (baseId() + 1) + "}}") + ";" + search("{\"range\": {\"id\": {\"gte\": " + (baseId() + 2) + ",\"lte\": " + (baseId() + 3) + "}}}");
    }

    @Override
    protected String positionalMultipleCommand() {
        return search("{\"range\": {\"age\": {\"gt\": ?}}}") + ";" + search("{\"term\": {\"name\": ?}}");
    }

    @Override
    protected String namedMultipleCommand() {
        return search("{\"range\": {\"age\": {\"lt\": :ageLimit}}}") + ";" + search("{\"range\": {\"age\": {\"gte\": :ageLimit}}}");
    }

    @Override
    protected String ruleMultipleCommand() {
        String type = UserInfo.class.getName();
        return search("{\"term\": {\"id\": " + (baseId() + 1) + "}}") + "; @{resultSet,name=youngUsers,javaType=" + type + "}\n" + search("{\"term\": {\"id\": " + (baseId() + 3) + "}}") + "; @{resultSet,name=seniorUsers,javaType=" + type + "}";
    }
}
