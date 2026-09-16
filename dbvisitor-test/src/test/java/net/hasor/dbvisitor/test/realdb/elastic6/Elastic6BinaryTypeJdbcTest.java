/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeBasicTypeFixture;
import net.hasor.dbvisitor.test.contract.feature.type.BinaryTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticTypeMappings;
import org.junit.After;
import org.junit.Before;

public class Elastic6BinaryTypeJdbcTest extends BinaryTypeJdbcCase {
    private final NativeBasicTypeFixture fixture = new NativeBasicTypeFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
        jdbcTemplate = fixture.openWithMapping(profile().env(), "binary_types_explicit_test", ElasticTypeMappings.binary());
    }

    @Override
    protected String insertCommand(String table, String columns) throws SQLException {
        return fixture.insertCommand(table, columns);
    }

    @Override
    protected String insertCommand(String table, String columns, String... values) throws SQLException {
        String[] expressions = values.clone();
        for (int i = 0; i < expressions.length; i++) {
            if ("NULL".equalsIgnoreCase(expressions[i])) {
                expressions[i] = "null";
            }
        }
        return fixture.insertCommand(table, columns, expressions);
    }

    @Override
    protected String selectCommand(String table, String columns) throws SQLException {
        return fixture.selectCommand(table, columns);
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
