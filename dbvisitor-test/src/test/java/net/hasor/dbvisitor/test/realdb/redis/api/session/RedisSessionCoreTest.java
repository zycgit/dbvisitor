/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionCoreCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisMapperFixture;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.NativeMapper;

public class RedisSessionCoreTest extends SessionCoreCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();
    private HikariDataSource sessionSource;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.fixture.open();
        this.jdbcTemplate = this.fixture.session().jdbc();
        Properties properties = OneApiDataSourceManager.loadAdapterProperties("redis");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("jdbc.url"));
        config.setDriverClassName(properties.getProperty("jdbc.driver"));
        config.setUsername(properties.getProperty("jdbc.username"));
        config.setPassword(properties.getProperty("jdbc.password"));
        config.setConnectionTestQuery("PING");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        this.sessionSource = new HikariDataSource(config);
        this.dataSource = this.sessionSource;
    }

    @After
    public void closeFixture() throws SQLException {
        try {
            this.fixture.close();
        } finally {
            if (this.sessionSource != null) {
                this.sessionSource.close();
            }
        }
    }

    @Override
    protected String sessionInsertCommand() {
        return "SET ? ?";
    }

    @Override
    protected Object[] sessionInsertArgs(int id, String name, int age) {
        return new Object[] { this.fixture.key("user:" + id), name };
    }

    @Override
    protected String sessionCountCommand() {
        return "EXISTS ?";
    }

    @Override
    protected Object[] sessionCountArgs(int id) {
        return new Object[] { this.fixture.key("user:" + id) };
    }

    @Override
    protected int insertSessionUser(Session session, UserInfo user) throws Exception {
        return session.createMapper(NativeMapper.class).put(this.fixture.key("user:" + user.getId()), user.getName());
    }

    @Override
    protected String readSessionUserName(Session session, int id) throws Exception {
        return session.createMapper(NativeMapper.class).get(this.fixture.key("user:" + id));
    }
}
