/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.realdb.redis.api.jdbc.RedisJdbcFixture;

/** Native command material for shared Mapper cases. */
public final class RedisMapperFixture implements AutoCloseable {
    private final RedisJdbcFixture keys = new RedisJdbcFixture();
    private       Session          session;

    public void open() throws SQLException {
        if (session != null) {
            return;
        }
        keys.open();
        session = new Configuration().newSession(keys.connection());
    }

    public Session session() {
        return session;
    }

    public String key(String name) {
        return keys.key(name);
    }

    @Override
    public void close() throws SQLException {
        keys.close();
    }
}
