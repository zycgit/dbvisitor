/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMutationCountCase;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusJdbcMutationCountTest extends JdbcMutationCountCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
    }

    @Override
    protected String command(JdbcCrudCommand command) throws SQLException {
        return command == JdbcCrudCommand.COUNT_BY_ID ? "COUNT FROM user_info WHERE id = ?" : super.command(command);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
