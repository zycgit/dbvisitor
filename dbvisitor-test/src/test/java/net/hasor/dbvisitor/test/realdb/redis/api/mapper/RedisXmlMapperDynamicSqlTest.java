/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperDynamicSqlCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperDynamicSqlTest extends XmlMapperDynamicSqlCase {
    private final RedisEntityFixture fixture = new RedisEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        this.jdbcTemplate = this.fixture.open();
        String[] names = { "DynSqlAlice", "DynSqlBob", "DynSqlCarol", "DynSqlDave", "DynSqlEve" };
        int[] ages = { 22, 28, 35, 42, 50 };
        for (int i = 0; i < names.length; i++) {
            String email = names[i].toLowerCase(Locale.ROOT) + "@test.com";
            Map<String, Object> user = Map.of("id", baseId() + i + 1, "name", names[i], "age", ages[i], "email", email);
            store("users", baseId() + i + 1, user);
            store("ages", ages[i], user);
            store("email-" + email, ages[i], user);
            store("record-" + (baseId() + i + 1), baseId() + i + 1, user);
            store("bound-", baseId() + i + 1, user);
            store("bound-" + names[i].substring(6), baseId() + i + 1, user);
        }
        for (int i = 21; i <= 23; i++) {
            this.fixture.key("record-" + (baseId() + i));
        }
        Configuration configuration = newConfiguration();
        configuration.addMacro("redisEmailIndex", "#{'" + this.fixture.key("email-") + "' + email}");
        configuration.addMacro("redisRecordKey", "#{'" + this.fixture.key("record-") + "' + item}");
        configuration.addMacro("redisBatchRecordKey", "#{'" + this.fixture.key("record-") + "' + item.id}");
        configuration.addMacro("redisBoundIndex", "#{'" + this.fixture.key("bound-") + "' + suffix}");
        this.session = this.fixture.session(configuration, "/mapper/redis/DynamicSqlMapper.xml");
    }

    private void store(String index, int score, Map<String, Object> user) throws SQLException {
        this.jdbcTemplate.queryForLong("ZADD #{arg0} #{arg1} #{arg2,typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}", new Object[] { this.fixture.key(index), score, user });
    }

    @Override
    protected String boundNameParameter() {
        return "DynSqlAlice";
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
