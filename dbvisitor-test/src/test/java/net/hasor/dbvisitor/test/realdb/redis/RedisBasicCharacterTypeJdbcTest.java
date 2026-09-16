/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.feature.type.BasicCharacterTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisBasicCharacterTypeJdbcTest extends BasicCharacterTypeJdbcCase {
    private final RedisBasicTypeSupport fixture = new RedisBasicTypeSupport();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.openFixture();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    @Override
    protected Map<String, Object> roundTripCharacterValues() throws SQLException {
        Character character = fixture.roundTrip("char", 'A', Character.class);
        return Map.of("char_value", character, "varchar_value", fixture.roundTrip("text", "Hello World!", String.class), "nvarchar_value", fixture.roundTrip("unicode", "你好世界！🌍", String.class));
    }

    @Override
    protected Character characterValue(Map<String, Object> row) {
        return (Character) row.get("char_value");
    }
}
