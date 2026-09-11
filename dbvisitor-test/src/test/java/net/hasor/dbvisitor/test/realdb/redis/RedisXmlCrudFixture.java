/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import net.hasor.dbvisitor.adapter.redis.JedisKeys;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** One JSON entity per private list; native LRANGE preserves an empty result for a missing key. */
final class RedisXmlCrudFixture implements AutoCloseable {
    private final String prefix = "nxn_xml_crud_" + UUID.randomUUID().toString().replace("-", "") + ":";
    private final int baseId;
    private JdbcTemplate jdbc;
    private Session session;

    RedisXmlCrudFixture(int baseId) {
        this.baseId = baseId;
    }

    JdbcTemplate open() throws SQLException {
        if (this.jdbc == null) {
            OneApiDataSourceManager.assumeCurrentDataSource("redis");
            Properties properties = new Properties();
            properties.setProperty(JedisKeys.SEPARATOR_CHAR, ";");
            this.jdbc = new JdbcTemplate(OneApiDataSourceManager.getConnection("redis", properties));
        }
        return this.jdbc;
    }

    void seedUsers() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(this.baseId + i);
            user.setName("XmlCrud" + i);
            user.setAge(20 + i);
            user.setEmail("crud" + i + "@test.com");
            user.setCreateTime(new Date());
            this.jdbc.executeUpdate("RPUSH #{arg0} #{arg1, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}",
                    new Object[] { this.prefix + user.getId(), user });
        }
    }

    Session createSession(Configuration configuration) throws Exception {
        String key = "#{'" + this.prefix + "' + id}";
        String fields = "'id': id, 'name': name, 'age': age, 'email': email, 'createTime': " + System.currentTimeMillis() + "L";
        configuration.addMacro("nxnRedisCrudInsert", "RPUSH " + key + " " + json(fields));
        configuration.addMacro("nxnRedisCrudSelect", "LRANGE " + key + " 0 -1");
        // LSET replaces the entity value; field-level partial update is a separate capability.
        configuration.addMacro("nxnRedisCrudUpdate", "LSET " + key + " 0 " + json("'id': id, 'email': email"));
        configuration.addMacro("nxnRedisCrudDelete", "DEL " + key);
        configuration.loadMapper("/mapper/RedisCrudMapper.xml");
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session;
    }

    private String json(String fields) {
        return "#{#{" + fields + "}, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}";
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.jdbc != null) {
                for (int offset : new int[] { 1, 2, 3, 4, 5, 10 }) {
                    this.jdbc.executeUpdate("DEL ?", new Object[] { this.prefix + (this.baseId + offset) });
                }
            }
        } finally {
            if (this.session != null) {
                this.session.close();
            } else if (this.jdbc != null) {
                this.jdbc.getConnection().close();
            }
        }
    }
}
