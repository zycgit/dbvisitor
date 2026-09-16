/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

public class MongoJdbcMultipleResultSetTest extends JdbcMultipleResultSetCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String insertSql() {
        return this.fixture.command("insert({id: ?, name: ?, age: ?, email: ?})");
    }

    private String find(String filter) {
        return this.fixture.command("find(" + filter + ", {_id: 0, id: 1, name: 1, age: 1})");
    }

    @Override
    protected String literalMultipleCommand() {
        return find("{id: " + (baseId() + 1) + "}") + ";" + find("{id: {$gte: " + (baseId() + 2) + ", $lte: " + (baseId() + 3) + "}}");
    }

    @Override
    protected String positionalMultipleCommand() {
        return find("{age: {$gt: ?}}") + ";" + find("{name: ?}");
    }

    @Override
    protected String namedMultipleCommand() {
        return find("{age: {$lt: :ageLimit}}") + ";" + find("{age: {$gte: :ageLimit}}");
    }

    @Override
    protected String ruleMultipleCommand() {
        String type = "net.hasor.dbvisitor.test.contract.material.model.UserInfo";
        return find("{id: " + (baseId() + 1) + "}") + "; @{resultSet,name=youngUsers,javaType=" + type + "}\n" + find("{id: " + (baseId() + 3) + "}") + "; @{resultSet,name=seniorUsers,javaType=" + type + "}";
    }
}
