/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;

/** Native schema and commands only; assertions belong to the shared Session cases. */
public final class MilvusSessionMapperSupport implements AutoCloseable {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private Session session;

    void open() throws SQLException {
        this.fixture.open();
    }

    Session session() throws Exception {
        if (this.session == null) {
            Configuration configuration = new Configuration();
            configuration.loadMapper(XmlMapper.class);
            this.session = configuration.newSession(this.fixture.open());
        }
        return this.session;
    }

    @Override
    public void close() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    @SimpleMapper
    public interface AnnotationMapper extends SessionUserMapper {
        @Override
        @Insert("INSERT INTO user_info (id, name, age, email) VALUES (#{id}, #{name}, #{age}, #{email})")
        int insertUser(UserInfo user);

        @Override
        @Query("SELECT * FROM user_info")
        List<UserInfo> selectAll();
    }

    @RefMapper("/milvus/session-shared.xml")
    public interface XmlMapper extends SessionRefUserMapper {
        @Override
        default UserOrderDTO queryOrderWithUser(Integer orderId) {
            throw new UnsupportedOperationException("Milvus does not support joins");
        }
    }
}
