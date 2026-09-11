/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import net.hasor.dbvisitor.adapter.redis.RedisCustomJedis;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class TestParamTest {

    public Connection redisConnection() throws SQLException {
        Properties prop = new Properties();
        prop.setProperty(JedisKeys.INTERCEPTOR, RedisCommandInterceptor.class.getName());
        prop.setProperty(JedisKeys.CUSTOM_JEDIS, RedisCustomJedis.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:jedis://xxxxxx", prop);
    }

    // resis
}
