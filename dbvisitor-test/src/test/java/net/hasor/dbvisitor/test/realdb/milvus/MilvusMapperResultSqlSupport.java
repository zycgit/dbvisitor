/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterCase;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.contract.material.handler.CustomResultSetExtractor;
import net.hasor.dbvisitor.test.contract.material.handler.CustomRowMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

public abstract class MilvusMapperResultSqlSupport extends AdapterCase {
    protected final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    protected Session session;
    protected ResultHandlerMapper shared;
    protected static final ThreadLocal<List<Integer>> CALLBACK_IDS = ThreadLocal.withInitial(ArrayList::new);

    /** Captures the callback output on the invoking test thread. */
    public static class RecordingRowCallback implements RowCallbackHandler {
        @Override
        public void processRow(ResultSet result, int rowNum) throws SQLException {
            List<Integer> ids = CALLBACK_IDS.get();
            assertEquals(ids.size(), rowNum);
            ids.add(result.getInt("id"));
        }
    }

    @SimpleMapper
    public interface NativeResultMapper {
        @Query("SELECT * FROM user_info WHERE id >= #{min}")
        List<UserInfo> defaults(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultRowMapper = CustomRowMapper.class)
        List<UserInfo> plainRows(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultSetExtractor = CustomResultSetExtractor.class)
        List<UserInfo> plainExtracted(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultRowMapper = CustomRowMapper.class, fetchSize = 1, timeout = 30)
        List<UserInfo> rows(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultSetExtractor = CustomResultSetExtractor.class, fetchSize = 1, timeout = 30)
        List<UserInfo> extracted(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultRowCallback = RecordingRowCallback.class)
        void callback(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultRowCallback = RecordingRowCallback.class, timeout = 30)
        void callbackWithTimeout(@Param("min") int min) throws SQLException;

        @Query(value = "SELECT * FROM user_info WHERE id >= #{min}", resultSetExtractor = CustomResultSetExtractor.class,
                fetchSize = 1, resultSetType = ResultSetType.SCROLL_INSENSITIVE)
        List<UserInfo> scrollExtracted(@Param("min") int min) throws SQLException;
    }

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Before
    public void openMapper() throws Exception {
        this.session = new Configuration().newSession(this.fixture.open());
        this.shared = this.session.createMapper(ResultHandlerMapper.class);
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setId(i);
            user.setName("row" + i);
            user.setAge(20 + i);
            user.setEmail("row" + i + "@test.com");
            user.setCreateTime(new Date(1700000000123L));
            assertEquals(1, this.shared.insertUser(user));
        }
    }

    @After
    public void closeMapper() throws Exception {
        try {
            this.fixture.close();
        } finally {
            CALLBACK_IDS.remove();
            if (this.session != null) {
                this.session.close();
            }
        }
    }

}
