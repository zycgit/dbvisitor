/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisParameterUser;
import static org.junit.Assert.assertEquals;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperQueryResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisAnnotationMapperQueryResultTest extends AnnotationMapperQueryResultCase {
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

    private RedisCoverageMapper nativeMapper;
    @Override
    public void createAnnotationMapper() throws Exception {
        fixture.open();
        nativeMapper = fixture.session().createMapper(RedisCoverageMapper.class);
    }
    private RedisParameterUser user(int id, String name) {
        RedisParameterUser user = new RedisParameterUser();
        user.setId(id);
        user.setName(name);
        return user;
    }
    @Override
    protected void prepareQueryRows() throws Exception {
        assertEquals(1, nativeMapper.appendBean(fixture.key("users"), user(1, "first")));
        assertEquals(2, nativeMapper.appendBean(fixture.key("users"), user(2, "second")));
    }
    @Override
    protected List<?> queryObjectRows() throws Exception {
        List<RedisParameterUser> rows = nativeMapper.beans(fixture.key("users"));
        assertEquals(Integer.valueOf(1), rows.get(0).getId());
        assertEquals("first", rows.get(0).getName());
        assertEquals(Integer.valueOf(2), rows.get(1).getId());
        assertEquals("second", rows.get(1).getName());
        return rows;
    }
    @Override
    protected List<?> queryOtherObjectRows() throws Exception {
        return nativeMapper.beans(fixture.key("users"));
    }
    @Override
    protected int queryScalarCount() throws Exception {
        return nativeMapper.count(fixture.key("users"));
    }
}
