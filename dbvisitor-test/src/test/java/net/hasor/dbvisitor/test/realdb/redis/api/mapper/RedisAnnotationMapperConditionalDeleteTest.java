/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperConditionalDeleteCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisAnnotationMapperConditionalDeleteTest extends AnnotationMapperConditionalDeleteCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        fixture.open();
        jdbcTemplate = fixture.session().jdbc();
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        fixture.open();
        Configuration configuration = newConfiguration();
        configuration.addMacro("redisConditionalUsers", "'" + fixture.key("conditional-users") + "'");
        Session session = configuration.newSession(fixture.session().jdbc().getConnection());
        mapper = session.createMapper(ConditionalMapper.class);
    }

    @After
    public void closeFixture() throws Exception {
        fixture.close();
    }

    @SimpleMapper
    public interface ConditionalMapper extends AnnotationTestMapper {
        @Override
        @Query("ZADD @{macro, redisConditionalUsers} #{age} "
                + "#{#{'id': id, 'name': name, 'age': age, 'email': email}, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}")
        int insertUserWithParams(@Param("id") Integer id, @Param("name") String name,
                @Param("age") Integer age, @Param("email") String email);

        @Override
        @Delete("ZREMRANGEBYSCORE @{macro, redisConditionalUsers} #{age} #{age}")
        int deleteByAge(@Param("age") Integer age);

        @Override
        @Query("ZCOUNT @{macro, redisConditionalUsers} #{age} #{age}")
        int countByAge(@Param("age") Integer age);
    }
}
