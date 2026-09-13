/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperSqlFragmentCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisXmlMapperSqlFragmentTest extends XmlMapperSqlFragmentCase {
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
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> user = Map.of("id", baseId() + i, "name", "SqlFrag" + i,
                    "age", 20 + i * 5, "email", "frag" + i + "@nxn.test", "createTime", timestamp());
            store("users", baseId() + i, user);
            store("ages", 20 + i * 5, user);
            store("name-SqlFrag" + i, 20 + i * 5, user);
        }
        Configuration configuration = newConfiguration();
        configuration.addMacro("redisNameIndex", "#{'" + this.fixture.key("name-") + "' + name}");
        this.session = this.fixture.session(configuration, "/mapper/redis/SqlFragmentMapper.xml");
    }

    private void store(String index, int score, Map<String, Object> user) throws SQLException {
        this.jdbcTemplate.queryForLong("ZADD #{arg0} #{arg1} #{arg2,typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}",
                new Object[] { this.fixture.key(index), score, user });
    }

    @Override
    protected String allNamesParameter() {
        return null;
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
