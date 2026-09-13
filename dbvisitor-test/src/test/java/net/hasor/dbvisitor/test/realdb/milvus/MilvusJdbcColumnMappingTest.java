/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcColumnMappingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Uses a stored uppercase field rather than changing ResultSet metadata. */
public class MilvusJdbcColumnMappingTest extends JdbcColumnMappingCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.create("column_case", "id INT64 PRIMARY KEY, NXN_VALUE VARCHAR(128)");
    }

    @Override
    protected void seedColumnValue() throws SQLException {
        this.jdbcTemplate.executeUpdate("INSERT INTO column_case (id, NXN_VALUE) VALUES (?, ?)",
                new Object[] { 932001, "NXN-Column" });
    }

    @Override
    protected String columnQuery() {
        return "SELECT NXN_VALUE FROM column_case WHERE id = 932001";
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
