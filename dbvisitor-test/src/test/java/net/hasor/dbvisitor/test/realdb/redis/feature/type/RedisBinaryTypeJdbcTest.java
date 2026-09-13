/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.SQLException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.test.contract.feature.type.BinaryTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

import static org.junit.Assert.assertArrayEquals;

public class RedisBinaryTypeJdbcTest extends BinaryTypeJdbcCase {
    private final RedisTypeCommandFixture fixture = new RedisTypeCommandFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String insertCommand(String table, String columns, String... parameters) {
        return this.fixture.insertCommand(table, columns, parameters);
    }

    @Override
    protected int executeInsert(String command, Object[] parameters) throws SQLException {
        return this.jdbcTemplate.queryForObject(command, parameters, Integer.class);
    }

    @Override
    protected String selectCommand(String table, String columns) {
        return this.fixture.selectCommand(table, columns);
    }

    @Override
    protected Object[] selectParameters(int id) {
        return new Object[] { Integer.toString(id).getBytes(StandardCharsets.UTF_8) };
    }

    @Override
    protected void verifyAdditionalBinaryValues() throws SQLException {
        int id = baseId() + 6;
        byte[] empty = new byte[0];
        executeInsert(insertCommand("binary_types_explicit_test", "id, blob_value", "?", "?"), new Object[] { id, empty });
        byte[] loaded = this.jdbcTemplate.queryForObject(selectCommand("binary_types_explicit_test", "blob_value"),
                selectParameters(id), byte[].class);
        assertArrayEquals(empty, loaded);
    }
}
