/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.contract.feature.type.BasicNumericTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisBasicNumericTypeJdbcTest extends BasicNumericTypeJdbcCase {
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
    protected BasicTypesModel roundTripNumericValues() throws SQLException {
        BasicTypesModel loaded = new BasicTypesModel();
        loaded.setByteValue(fixture.roundTrip("byte", Byte.MAX_VALUE, Byte.class));
        loaded.setShortValue(fixture.roundTrip("short", Short.MAX_VALUE, Short.class));
        loaded.setIntValue(fixture.roundTrip("int", Integer.MAX_VALUE, Integer.class));
        loaded.setLongValue(fixture.roundTrip("long", Long.MAX_VALUE, Long.class));
        loaded.setFloatValue(fixture.roundTrip("float", 3.14f, Float.class));
        loaded.setDoubleValue(fixture.roundTrip("double", 2.718281828d, Double.class));
        return loaded;
    }
}
