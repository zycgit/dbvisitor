/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeBasicTypeFixture;
import net.hasor.dbvisitor.test.contract.feature.type.BinaryTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoBinaryTypeJdbcTest extends BinaryTypeJdbcCase {
    private final NativeBasicTypeFixture fixture = new NativeBasicTypeFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env(), "binary_types_explicit_test");
    }

    @Override
    protected String insertCommand(String table, String columns) {
        return this.fixture.insertCommand(table, columns);
    }

    @Override
    protected String insertCommand(String table, String columns, String... parameters) {
        String[] expressions = parameters.clone();
        for (int i = 0; i < expressions.length; i++) {
            if ("NULL".equals(expressions[i])) {
                expressions[i] = "null";
            }
        }
        return this.fixture.insertCommand(table, columns, expressions);
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
