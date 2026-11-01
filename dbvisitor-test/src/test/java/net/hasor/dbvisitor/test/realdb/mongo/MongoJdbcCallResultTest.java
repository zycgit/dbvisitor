/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class MongoJdbcCallResultTest extends JdbcCallResultCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = this.fixture.open();
        }
    }

    @Override
    protected void createCallFixture() throws SQLException {
        setup();
        this.jdbcTemplate.executeUpdate(this.fixture.command("insertOne({id: 918001, name: 'ProcAlice', age: 25})"));
    }

    @Override
    protected String callCommand() {
        return this.fixture.command("find({id: :p_id}, {_id: 0, name: 1, age: 1})");
    }

    @AfterEach
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
