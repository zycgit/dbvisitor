/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeBasicTypeFixture;
import net.hasor.dbvisitor.test.contract.feature.type.BasicCharacterNullJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.After;
import org.junit.Before;

public class Elastic7BasicCharacterNullJdbcTest extends BasicCharacterNullJdbcCase {
    private final NativeBasicTypeFixture fixture = new NativeBasicTypeFixture();

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
    protected String insertCommand(String table, String columns) {
        return this.fixture.insertCommand(table, columns);
    }

    @Override
    protected String selectCommand(String table, String columns) throws SQLException {
        return this.fixture.selectCommand(table, columns);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
