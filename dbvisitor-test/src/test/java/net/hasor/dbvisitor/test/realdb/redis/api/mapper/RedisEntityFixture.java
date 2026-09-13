/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

/** Redis JSON records with explicit sorted-set indexes owned by each test. */
public final class RedisEntityFixture implements AutoCloseable {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    public String key(String suffix) {
        return this.fixture.key(suffix);
    }

    public JdbcTemplate open() throws SQLException {
        this.fixture.open();
        return this.fixture.session().jdbc();
    }

    public Session session(Configuration configuration, String resource) throws Exception {
        open();
        configuration.addMacro("redisUsers", "'" + this.fixture.key("users") + "'");
        configuration.addMacro("redisAges", "'" + this.fixture.key("ages") + "'");
        configuration.addMacro("redisNames", "'" + this.fixture.key("names") + "'");
        configuration.addMacro("redisOrders", "'" + this.fixture.key("orders") + "'");
        configuration.addMacro("redisOwners", "'" + this.fixture.key("owners") + "'");
        configuration.addMacro("redisCounter", "'" + this.fixture.key("counter") + "'");
        configuration.loadMapper(resource);
        return configuration.newSession(this.fixture.session().jdbc().getConnection());
    }

    @Override
    public void close() throws SQLException {
        this.fixture.close();
    }
}
