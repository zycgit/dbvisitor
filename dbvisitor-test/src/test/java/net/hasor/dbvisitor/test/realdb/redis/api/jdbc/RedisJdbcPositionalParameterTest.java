/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.feature.parameter.JdbcPositionalParameterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcPositionalParameterTest extends JdbcPositionalParameterCase {

    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @After
    public void closeRedisFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected void insert(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.queryForLong("ZADD ? ? ?", new Object[] { fixture.key(name), age, id });
        jdbcTemplate.executeUpdate("HSET ? ? ?", new Object[] { fixture.key("emails"), id, email });
    }

    @Override
    protected String command(JdbcParameterCommand command) {
        switch (command) {
            case SELECT_USER:
                return "ZRANGEBYSCORE ? ? +inf WITHSCORES";
            case SELECT_EMAIL_BY_ID:
                return "HGET '" + fixture.key("emails") + "' ?";
            default:
                throw new IllegalArgumentException("Unexpected positional fixture command: " + command);
        }
    }

    @Override
    protected String positionalEmailColumn() {
        return "VALUE";
    }

    @Override
    protected Object[] positionalQueryArguments() {
        return new Object[] { fixture.key(positionalName()), 20 };
    }

    @Override
    protected String positionalName() {
        return "NXN-Param-Array ' \" ; 世界";
    }

    @Override
    protected Class<?> positionalBeanType() {
        return ScoredId.class;
    }

    @Override
    protected Integer positionalId(Object bean) {
        return ((ScoredId) bean).getId();
    }

    @Override
    protected Integer positionalAge(Object bean) {
        return ((ScoredId) bean).getAge();
    }

    public static class ScoredId {
        @Column("ELEMENT")
        private Integer id;
        @Column("SCORE")
        private Integer age;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
