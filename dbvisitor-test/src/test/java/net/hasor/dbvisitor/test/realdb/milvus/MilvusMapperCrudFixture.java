/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;

/** Native UserInfo schema for the same annotation/XML CRUD assertions as other sources. */
final class MilvusMapperCrudFixture implements AutoCloseable {
    private final MilvusUserInfoFixture collection = new MilvusUserInfoFixture();
    private JdbcTemplate jdbc;
    private Session session;

    JdbcTemplate open() throws SQLException {
        if (this.jdbc == null) {
            this.jdbc = new JdbcTemplate(this.collection.open());
        }
        return this.jdbc;
    }

    AnnotationTestMapper createMapper(Configuration configuration) throws Exception {
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session.createMapper(AnnotationTestMapper.class);
    }

    Session createXmlSession(Configuration configuration) throws Exception {
        configuration.addMacro("nxnCrudInsertXml", """
                INSERT INTO user_info (id, name, age, email, create_time)
                VALUES (#{id}, #{name}, #{age}, #{email}, '2026-01-02 03:04:05')
                """);
        configuration.addMacro("nxnCrudSelect", "SELECT * FROM user_info WHERE id = #{id}");
        configuration.addMacro("nxnCrudUpdateEmail", "UPDATE user_info SET email = #{email} WHERE id = #{id}");
        configuration.addMacro("nxnCrudDelete", "DELETE FROM user_info WHERE id = #{id}");
        configuration.loadMapper("/mapper/NativeCrudMapper.xml");
        this.session = configuration.newSession(this.jdbc.getConnection());
        return this.session;
    }

    void seedXmlUsers(int baseId) throws SQLException {
        for (int i = 1; i <= 5; i++) {
            this.jdbc.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)",
                    new Object[] { baseId + i, "XmlCrud" + i, 20 + i, "crud" + i + "@test.com", new Date() });
        }
    }

    @Override
    public void close() throws Exception {
        try {
            this.collection.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
