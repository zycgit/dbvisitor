/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.StatementType;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperExecutionCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

public class RedisAnnotationMapperExecutionTest extends AnnotationMapperExecutionCase {
    private final RedisMapperFixture fixture = new RedisMapperFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.fixture.open();
        this.jdbcTemplate = this.fixture.session().jdbc();
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.fixture.open();
        Configuration configuration = newConfiguration();
        configuration.addMacro("redisAttributeUsers", "'" + this.fixture.key("attribute-users") + "'");
        configuration.addMacro("redisAttributeFetch", "'" + this.fixture.key("attribute-fetch") + "'");
        Session session = configuration.newSession(this.fixture.session().jdbc().getConnection());
        this.mapper = session.createMapper(NativeAttributesMapper.class);
        initData();
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            UserInfo user = user(baseId() + i, "AttrNxn" + i, 20 + i, "attr-nxn" + i + "@nxn.test");
            this.mapper.insertUserBasic(user);
            this.jdbcTemplate.queryForLong("ZADD #{arg0} #{arg1} #{arg2,typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}",
                    new Object[] { this.fixture.key("attribute-fetch"), user.getId(), user });
        }
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @SimpleMapper
    public interface NativeAttributesMapper extends AnnotationAttributesMapper {
        @Override
        @Query(value = ALL, resultSetType = ResultSetType.DEFAULT, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithDefaultResultSetType(@Param("pattern") String pattern);

        @Override
        @Query(value = ALL, resultSetType = ResultSetType.FORWARD_ONLY, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithForwardOnly(@Param("pattern") String pattern);

        @Override
        @Query(value = ALL, resultSetType = ResultSetType.SCROLL_INSENSITIVE, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithScrollInsensitive(@Param("pattern") String pattern);

        @Override
        @Query(value = ALL, resultSetType = ResultSetType.SCROLL_SENSITIVE, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithScrollSensitive(@Param("pattern") String pattern);

        String ENTITY = "#{#{'id':id,'name':name,'age':age,'email':email,'createTime':createTime},typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}";
        String INSERT = "HSET @{macro, redisAttributeUsers} #{id} " + ENTITY;
        String BY_ID = "HGET @{macro, redisAttributeUsers} #{id}";
        String ALL = "ZRANGE @{macro, redisAttributeFetch} 0 -1";

        @Override
        @Insert(INSERT)
        int insertUserBasic(UserInfo user);

        @Override
        @Query(value = BY_ID, statementType = StatementType.Prepared, resultTypeHandler = JsonTypeHandler.class)
        UserInfo selectByIdPrepared(@Param("id") Integer id);

        @Override
        @Query(value = "HGET @{macro, redisAttributeUsers} ${id}",
                statementType = StatementType.Statement, resultTypeHandler = JsonTypeHandler.class)
        UserInfo selectByIdStatement(@Param("id") Integer id);

        @Override
        @Query(value = BY_ID, timeout = -1, resultTypeHandler = JsonTypeHandler.class)
        UserInfo selectByIdDefaultTimeout(@Param("id") Integer id);

        @Override
        @Query(value = BY_ID, timeout = 30, resultTypeHandler = JsonTypeHandler.class)
        UserInfo selectByIdWithTimeout(@Param("id") Integer id);

        @Override
        @Query(value = BY_ID, timeout = 3600, resultTypeHandler = JsonTypeHandler.class)
        UserInfo selectWithMaxTimeout(@Param("id") Integer id);

        @Override
        @Query(value = "EVAL \"local value=redis.call('HGET',KEYS[1],ARGV[1]); if not value then return 0 end; local user=cjson.decode(value); user.age=tonumber(ARGV[2]); redis.call('HSET',KEYS[1],ARGV[1],cjson.encode(user)); return 1\" 1 @{macro, redisAttributeUsers} #{id} #{age}",
                timeout = 30)
        int updateWithTimeout(@Param("id") Integer id, @Param("age") Integer age);

        @Override
        @Delete(value = "HDEL @{macro, redisAttributeUsers} #{id}", timeout = 30)
        int deleteWithTimeout(@Param("id") Integer id);

        @Override
        @Insert(value = INSERT, timeout = 30)
        int insertWithTimeout(UserInfo user);

        @Override
        @Query(value = ALL, fetchSize = 256, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithDefaultFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = ALL, fetchSize = 10, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithSmallFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = ALL, fetchSize = 1000, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithLargeFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = ALL, fetchSize = 1, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithFetchSizeOne(@Param("pattern") String pattern);

        @Override
        @Insert({ "HSET", "@{macro, redisAttributeUsers}", "#{id}", ENTITY })
        int insertMultiLine(UserInfo user);

        @Override
        @Query(value = ALL, statementType = StatementType.Prepared, timeout = 60, fetchSize = 100,
                resultSetType = ResultSetType.FORWARD_ONLY, resultTypeHandler = JsonTypeHandler.class)
        List<UserInfo> selectWithCombinedAttributes(@Param("pattern") String pattern);

        @Override
        @Query(value = BY_ID, resultTypeHandler = JsonTypeHandler.class)
        UserInfo selectWithAllDefaults(@Param("id") Integer id);
    }
}
