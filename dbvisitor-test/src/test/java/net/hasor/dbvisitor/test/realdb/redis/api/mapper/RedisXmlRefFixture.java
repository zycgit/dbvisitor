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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

/** Native index material shared by the XML-reference cases. */
public final class RedisXmlRefFixture implements AutoCloseable {
    private final RedisEntityFixture entities = new RedisEntityFixture();

    public JdbcTemplate open() throws SQLException {
        return this.entities.open();
    }

    public String index(String name) {
        return this.entities.key(name);
    }

    public RedisXmlRefMapperDao createMapper(Configuration configuration, int baseId) throws Exception {
        configuration.addMacro("redisRecordKey", "#{'" + this.entities.key("record-") + "' + item}");
        Session session = this.entities.session(configuration, "/session/RedisUserSessionMapper.xml");
        String[] names = { "RefMapA", "RefMapB", "RefMapC", "RefMapD" };
        int[] ages = { 22, 28, 35, 28 };
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> user = Map.of("id", baseId + i + 1, "name", names[i], "age", ages[i], "email", "ref" + (char) ('a' + i) + "@nxn.test", "createTime", 1L);
            session.executeStatement("session.UserSessionMapper.insertUser", user);
            session.jdbc().queryForLong("ZADD ? ? ?", new Object[] { index("owners"), baseId + i + 1, names[i] });
            session.jdbc().queryForLong("ZADD #{arg0} #{arg1} #{arg2,typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}", new Object[] { index("record-" + (baseId + i + 1)), baseId + i + 1, user });
        }
        return session.createMapper(RedisXmlRefMapperDao.class);
    }

    @Override
    public void close() throws SQLException {
        this.entities.close();
    }
}
