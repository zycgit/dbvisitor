/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.type.NativeNamedFieldTypeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

public class RedisNamedFieldTypeTest extends NativeNamedFieldTypeCase {
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

    @Override
    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected <T> void assertRoundTrip(T expected, Class<T> type) throws SQLException {
        int id = Boolean.FALSE.equals(expected) ? 2 : 1;
        this.jdbcTemplate.queryForObject(this.fixture.insertCommand("named_types", "id, typed_value", "?", "?"), new Object[] { id, expected }, Integer.class);
        Object actual = this.jdbcTemplate.queryForObject(this.fixture.selectCommand("named_types", "typed_value"), new Object[] { id }, (rs, row) -> TypeHandlerRegistry.DEFAULT.getTypeHandler(type).getResult(rs, "VALUE"));
        assertEquals(expected, actual);
    }
}
