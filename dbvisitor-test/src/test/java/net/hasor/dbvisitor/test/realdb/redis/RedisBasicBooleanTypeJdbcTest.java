/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;

import net.hasor.dbvisitor.test.contract.feature.type.BasicBooleanTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisBasicBooleanTypeJdbcTest extends BasicBooleanTypeJdbcCase {
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
    protected Boolean roundTripBooleanValue(boolean input) throws SQLException {
        return fixture.roundTrip(String.valueOf(input), input, Boolean.class);
    }
}
