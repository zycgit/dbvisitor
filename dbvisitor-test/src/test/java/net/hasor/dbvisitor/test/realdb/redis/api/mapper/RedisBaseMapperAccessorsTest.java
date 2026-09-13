/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.Entry;
import net.hasor.dbvisitor.test.contract.api.mapper.basemapper.BaseMapperAccessorsCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisBaseMapperAccessorsTest extends BaseMapperAccessorsCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    private BaseMapper<Entry> nativeMapper;
    @Override
    public void createBaseMapper() throws SQLException {
        fixture.open();
        nativeMapper = fixture.session().createBaseMapper(Entry.class);
    }
    @Override
    protected BaseMapper<?> accessorMapper() {
        return nativeMapper;
    }
    @Override
    protected Class<?> accessorEntityType() {
        return Entry.class;
    }
    @Override
    protected Session expectedAccessorSession() {
        return fixture.session();
    }
}
