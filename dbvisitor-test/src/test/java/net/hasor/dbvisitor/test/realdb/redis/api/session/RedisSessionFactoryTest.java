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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.session.SessionFactoryCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisMapperFixture;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.NativeMapper;
import org.junit.After;
import org.junit.Before;

public class RedisSessionFactoryTest extends SessionFactoryCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();
    private HikariDataSource sessionSource;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
        Properties properties = OneApiDataSourceManager.loadAdapterProperties("redis");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("jdbc.url"));
        config.setDriverClassName(properties.getProperty("jdbc.driver"));
        config.setUsername(properties.getProperty("jdbc.username"));
        config.setPassword(properties.getProperty("jdbc.password"));
        config.setConnectionTestQuery("PING");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(0);
        sessionSource = new HikariDataSource(config);
        dataSource = sessionSource;
    }

    @After
    public void closeFixture() throws SQLException {
        try {
            fixture.close();
        } finally {
            if (sessionSource != null) {
                sessionSource.close();
            }
        }
    }

    @Override
    protected String jdbcInsertCommand() { return "SET ? ?"; }
    @Override
    protected Object[] jdbcInsertParameters() { return new Object[] { fixture.key("factory"), "ConfigJdbc" }; }
    @Override
    protected String jdbcCountCommand() { return "EXISTS ?"; }
    @Override
    protected Object[] jdbcCountParameters() { return new Object[] { fixture.key("factory") }; }
    @Override
    protected String jdbcNameCommand() { return "GET ?"; }
    @Override
    protected Object[] jdbcNameParameters() { return new Object[] { fixture.key("factory") }; }

    @Override
    protected int insertFactoryEntity(Session local, int id, String name, int age) throws Exception {
        return local.createMapper(NativeMapper.class).replace(fixture.key("factory"), name);
    }

    @Override
    protected String readFactoryName(Session local, int id) throws Exception {
        return local.createMapper(NativeMapper.class).get(fixture.key("factory"));
    }
}
