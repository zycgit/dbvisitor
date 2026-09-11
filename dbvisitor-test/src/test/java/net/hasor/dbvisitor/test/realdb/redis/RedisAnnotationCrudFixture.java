/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.realdb.redis.dto1.RedisCrudMapper;

/** Native SET/GET/DEL CRUD; SET replaces a JSON value, not individual fields of a relational row. */
final class RedisAnnotationCrudFixture implements AutoCloseable {
    private final String prefix = "nxn_anno_crud_" + UUID.randomUUID().toString().replace("-", "") + ":";
    private final int baseId;
    private JdbcTemplate jdbc;
    private Session session;

    RedisAnnotationCrudFixture(int baseId) {
        this.baseId = baseId;
    }

    JdbcTemplate open() throws SQLException {
        if (this.jdbc == null) {
            OneApiDataSourceManager.assumeCurrentDataSource("redis");
            this.jdbc = new JdbcTemplate(OneApiDataSourceManager.getConnection("redis"));
        }
        return this.jdbc;
    }

    AnnotationTestMapper createMapper(Configuration configuration) throws Exception {
        configuration.addMacro("nxnCrudInsertBean", set("'id': id, 'name': name, 'age': age, 'email': email, 'createTime': createTime"));
        configuration.addMacro("nxnCrudInsertParams", set("'id': id, 'name': name, 'age': age, 'email': email"));
        configuration.addMacro("nxnCrudUpdateAge", set("'id': id, 'age': age"));
        configuration.addMacro("nxnCrudUpdateInfo", set("'id': id, 'name': name, 'age': age"));
        configuration.addMacro("nxnCrudSelect", "GET #{'" + this.prefix + "' + id}");
        configuration.addMacro("nxnCrudDelete", "DEL #{'" + this.prefix + "' + id}");
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session.createMapper(RedisCrudMapper.class);
    }

    private String set(String fields) {
        return "SET #{'" + this.prefix + "' + id} "
                + "#{#{" + fields + "}, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}";
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.jdbc != null) {
                // Only the four private keys written by the shared CRUD scenarios.
                for (int offset : new int[] { 1, 4, 5, 8 }) {
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
