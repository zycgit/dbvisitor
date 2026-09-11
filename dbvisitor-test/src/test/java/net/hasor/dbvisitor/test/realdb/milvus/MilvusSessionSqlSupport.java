/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3Mapper;
import org.junit.After;
import org.junit.Before;

/** Isolated collection and mapper configuration shared by Milvus Session contracts. */
public abstract class MilvusSessionSqlSupport extends AdapterContractTest {
    protected static final String NAMESPACE = UserInfoMilvus3Mapper.class.getName();
    protected final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    protected Connection connection;
    protected Session session;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Before
    public void openSession() throws Exception {
        this.connection = this.database.open();
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE tb_mapper_user_milvus (
                        uid VARCHAR(64) PRIMARY KEY, name VARCHAR(64),
                        loginName VARCHAR(64), loginPassword VARCHAR(64), v FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX session_v ON tb_mapper_user_milvus(v) USING FLAT WITH (metric_type=L2)");
            statement.executeUpdate("LOAD TABLE tb_mapper_user_milvus");
        }
        Configuration configuration = new Configuration();
        configuration.loadMapper(UserInfoMilvus3Mapper.class);
        this.session = configuration.newSession(this.connection);
    }

    @After
    public void closeFixture() throws Exception {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS tb_mapper_user_milvus");
                }
            }
        } finally {
            try {
                if (this.session != null) {
                    this.session.close();
                }
            } finally {
                this.database.close();
            }
        }
    }

    protected Map<String, Object> parameters(String id) {
        return Map.of("uid", id, "name", "name", "loginName", "login", "loginPassword", "password", "v", List.of(1F, 0F));
    }
}
