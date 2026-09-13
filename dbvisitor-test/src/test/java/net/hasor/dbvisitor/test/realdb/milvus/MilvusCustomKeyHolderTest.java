/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import net.hasor.dbvisitor.test.contract.feature.keygen.NumericKeyModel;
import net.hasor.dbvisitor.test.contract.material.handler.keygen.AfterInsertKeyHolder;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandler;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerContext;
import net.hasor.dbvisitor.mapping.GeneratedKeyHandlerFactory;
import net.hasor.dbvisitor.mapping.KeyHolder;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.test.contract.feature.keygen.CustomKeyHolderCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusCustomKeyHolderTest extends CustomKeyHolderCase {
    private final MilvusCapabilityFixture fixture = new MilvusCapabilityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.fixture.userTable("user_info", "id INT64 PRIMARY KEY");
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void ensureAfterTable() throws SQLException {
        this.fixture.userTable("user_keygen_after", "id INT64 PRIMARY KEY AUTO_ID");
    }

    @Override
    protected NumericKeyModel<?> connectionKeyModel() {
        return new NumericKeyModel<>(ConnectionKeyUser.class, (name, age) -> {
            ConnectionKeyUser user = new ConnectionKeyUser();
            user.setName(name);
            user.setAge(age);
            user.setCreateTime(new Date());
            return user;
        }, ConnectionKeyUser::getId, ConnectionKeyUser::setId);
    }

    @Override
    protected Class<?> afterKeyEntityType() {
        return AfterKeyUser.class;
    }

    @Override
    protected Object newAfterKeyEntity() {
        AfterKeyUser user = new AfterKeyUser();
        user.setName("After Key User");
        user.setAge(45);
        user.setCreateTime(new Date());
        return user;
    }

    @Override
    protected Object afterKeyValue(Object entity) {
        return ((AfterKeyUser) entity).getId();
    }

    @Table("user_keygen_after")
    public static class AfterKeyUser {
        @Column(primary = true, keyType = KeyType.Holder)
        @KeyHolder(AfterInsertKeyHolder.class)
        private Long id;
        private String name;
        private Integer age;
        @Column(name = "create_time")
        private Date createTime;

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return this.age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Date getCreateTime() {
            return this.createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
    }

    @Table("user_info")
    public static class ConnectionKeyUser {
        @Column(primary = true, keyType = KeyType.Holder)
        @KeyHolder(CountKeyHolder.class)
        private Long id;
        private String name;
        private Integer age;
        @Column(name = "create_time")
        private Date createTime;

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return this.age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Date getCreateTime() {
            return this.createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
    }

    /** A fixture-only allocator: proves callback connection access, not concurrent key allocation. */
    public static class CountKeyHolder implements GeneratedKeyHandlerFactory {
        @Override
        public GeneratedKeyHandler createHolder(GeneratedKeyHandlerContext context) {
            return new GeneratedKeyHandler() {
                @Override
                public boolean onBefore() {
                    return true;
                }

                @Override
                public Object beforeApply(Connection connection, Object entity, ColumnMapping mapping) throws SQLException {
                    // COUNT is native to Milvus; MAX is not needed to prove the JDBC callback contract.
                    try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("COUNT FROM user_info")) {
                        if (!rows.next()) {
                            throw new SQLException("COUNT did not return a row");
                        }
                        long key = rows.getLong(1) + 1;
                        mapping.getHandler().set(entity, key);
                        return key;
                    }
                }
            };
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
