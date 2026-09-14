/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperResultHandlerCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.contract.material.handler.CustomResultSetExtractor;
import net.hasor.dbvisitor.test.contract.material.handler.CustomRowMapper;
import net.hasor.dbvisitor.test.contract.material.handler.RecordingRowCallbackHandler;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7AnnotationMapperResultHandlerTest extends AnnotationMapperResultHandlerCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = fixture.open(profile().env());
    }

    @Override
    @Before
    public void createResultHandlerMapper() throws Exception {
        this.jdbcTemplate = fixture.open(profile().env());
        Session session = fixture.session();
        Configuration configuration = session.getConfiguration();
        String path = "POST /" + fixture.index();
        configuration.addMacro("esHandlerInsert", path + "/_doc "
                + "{\"id\": #{id},\"name\": #{name},\"age\": #{age},\"email\": #{email},\"create_time\": #{createTime}}");
        configuration.addMacro("esHandlerAll", path + "/_search {\"_source\": [\"id\",\"name\",\"age\",\"email\",\"create_time\"],\"size\": 100,\"sort\": [{\"id\": \"asc\"}],"
                + "\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}}}");
        configuration.addMacro("esHandlerOne", path + "/_search {\"_source\": [\"id\",\"name\",\"age\",\"email\",\"create_time\"],\"query\": {\"term\": {\"id\": #{id}}}}");
        this.mapper = session.createMapper(NativeResultMapper.class);
        prepareRows();
    }

    @After
    public void closeResultFixture() throws Exception {
        fixture.close();
    }

    @SimpleMapper
    public interface NativeResultMapper extends ResultHandlerMapper {
        @Override
        @Insert("@{macro, esHandlerInsert}")
        int insertUser(UserInfo user);

        @Override
        @Query("@{macro, esHandlerAll}")
        List<UserInfo> selectDefault(@Param("pattern") String pattern);

        @Override
        @Query(value = "@{macro, esHandlerAll}", resultSetExtractor = CustomResultSetExtractor.class)
        List<UserInfo> selectWithExtractor(@Param("pattern") String pattern);

        @Override
        @Query(value = "@{macro, esHandlerAll}", resultSetExtractor = CustomResultSetExtractor.class, fetchSize = 1, timeout = 30)
        List<UserInfo> selectWithExtractorAndFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = "@{macro, esHandlerAll}", resultRowMapper = CustomRowMapper.class)
        List<UserInfo> selectWithRowMapper(@Param("pattern") String pattern);

        @Override
        @Query(value = "@{macro, esHandlerOne}", resultRowMapper = CustomRowMapper.class)
        UserInfo selectSingleWithRowMapper(@Param("id") Integer id);

        @Override
        @Query(value = "@{macro, esHandlerAll}", resultRowMapper = CustomRowMapper.class, fetchSize = 100, timeout = 30)
        List<UserInfo> selectWithRowMapperAndOptions(@Param("pattern") String pattern);

        @Override
        @Query(value = "@{macro, esHandlerAll}", resultRowCallback = RecordingRowCallbackHandler.class)
        void selectWithRowCallback(@Param("pattern") String pattern);

        @Override
        @Query(value = "@{macro, esHandlerAll}", resultRowCallback = RecordingRowCallbackHandler.class, timeout = 30)
        void selectWithRowCallbackAndTimeout(@Param("pattern") String pattern);

        @Override
        @Query("@{macro, esHandlerOne}")
        UserInfo selectById(@Param("id") Integer id);
    }
}
