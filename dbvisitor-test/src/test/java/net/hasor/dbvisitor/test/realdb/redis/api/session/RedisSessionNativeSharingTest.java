/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionNativeSharingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisMapperFixture;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.NativeMapper;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.RefNativeMapper;
import org.junit.After;
import org.junit.Before;

public class RedisSessionNativeSharingTest extends SessionNativeSharingCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.fixture.open();
        this.jdbcTemplate = this.fixture.session().jdbc();
    }

    @Override
    protected Session createSession() {
        return this.fixture.session();
    }

    @Override
    protected void prepareSharing(Session session) throws Exception {
        session.getConfiguration().loadMapper("/mapper/redis/NativeMapper.xml");
    }

    @Override
    protected int annotationPut(Session session, int id, String value) throws Exception {
        return session.createMapper(NativeMapper.class).put(key(id), value);
    }

    @Override
    protected String annotationGet(Session session, int id) throws Exception {
        return session.createMapper(NativeMapper.class).get(key(id));
    }

    @Override
    protected int annotationRemove(Session session, int id) throws Exception {
        return session.createMapper(NativeMapper.class).remove(key(id));
    }

    @Override
    protected int xmlPut(Session session, int id, String value) throws Exception {
        return ((Number) session.executeStatement("redis.Native.put", Map.of("key", key(id), "value", value))).intValue();
    }

    @Override
    protected List<String> xmlGet(Session session, int id) throws Exception {
        return session.queryStatement("redis.Native.get", Map.of("key", key(id)));
    }

    @Override
    protected int xmlReplace(Session session, int id, String value) throws Exception {
        return ((Number) session.executeStatement("redis.Native.replace", Map.of("key", key(id), "value", value))).intValue();
    }

    @Override
    protected String referenceGet(Session session, int id) throws Exception {
        return session.createMapper(RefNativeMapper.class).get(key(id));
    }

    @Override
    protected String jdbcGet(Session session, int id) throws SQLException {
        return session.jdbc().queryForString("GET ?", key(id));
    }

    private String key(int id) {
        return this.fixture.key("sharing:" + id);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
