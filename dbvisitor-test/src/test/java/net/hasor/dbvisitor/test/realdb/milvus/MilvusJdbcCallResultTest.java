/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import net.hasor.dbvisitor.test.contract.jdbc.call.JdbcCallResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

public class MilvusJdbcCallResultTest extends JdbcCallResultCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    protected void createCallFixture() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.userTable("call_result", "id INT64 PRIMARY KEY");
        this.jdbcTemplate.executeUpdate("INSERT INTO call_result (id, name, age) VALUES (?, ?, ?)",
                new Object[] { 918001, "ProcAlice", 25 });
    }

    @Override
    protected String callCommand() {
        return "SELECT name, age FROM call_result WHERE id = #{p_id}";
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
