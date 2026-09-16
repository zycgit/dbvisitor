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
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
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
import net.hasor.dbvisitor.test.contract.material.handler.UserNameMapExtractor;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusAnnotationMapperResultHandlerTest extends AnnotationMapperResultHandlerCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private       Session               session;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
    }

    @Override
    @Before
    public void createResultHandlerMapper() throws Exception {
        this.session = new Configuration().newSession(this.fixture.open());
        this.mapper = this.session.createMapper(NativeResultMapper.class);
        prepareRows();
    }

    @Override
    protected long timestamp() {
        return 1700000000123L;
    }

    @After
    public void cleanupFixture() throws Exception {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    // Fixture names AnnoHandler1..10 lie between the shared pattern token and AnnoHandles.
    // This tests result handlers without requiring the server to parameterize LIKE.
    @SimpleMapper
    public interface NativeResultMapper extends ResultHandlerMapper {
        @Override
        @Query("SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'")
        List<UserInfo> selectDefault(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultSetExtractor = CustomResultSetExtractor.class)
        List<UserInfo> selectWithExtractor(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultSetExtractor = UserNameMapExtractor.class)
        Map<Integer, String> selectMapWithExtractor(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultSetExtractor = CustomResultSetExtractor.class, fetchSize = 1, timeout = 30)
        List<UserInfo> selectWithExtractorAndFetchSize(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultRowMapper = CustomRowMapper.class)
        List<UserInfo> selectWithRowMapper(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultRowMapper = CustomRowMapper.class, fetchSize = 1, timeout = 30)
        List<UserInfo> selectWithRowMapperAndOptions(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultRowCallback = RecordingRowCallbackHandler.class)
        void selectWithRowCallback(@Param("pattern") String pattern);

        @Override
        @Query(value = "SELECT * FROM user_info WHERE name >= #{pattern} AND name < 'AnnoHandles'", resultRowCallback = RecordingRowCallbackHandler.class, timeout = 30)
        void selectWithRowCallbackAndTimeout(@Param("pattern") String pattern);
    }
}
