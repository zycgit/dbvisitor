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
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.annotation.AnnotationMapperPaginationResultCase;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Shared PageObject assertions with native vector ordering as the page fixture. */
public class MilvusAnnotationMapperPaginationResultTest extends AnnotationMapperPaginationResultCase {
    private final MilvusDatabaseFixture fixture = new MilvusDatabaseFixture();
    private Session session;
    private NativePageMapper pageMapper;

    @SimpleMapper
    public interface NativePageMapper {
        @Query("""
                SELECT id, name, age, email, create_time FROM user_info
                WHERE id >= #{minId} AND id <= #{maxId} ORDER BY v <-> #{vector}
                """)
        List<UserInfo> selectPage(@Param("minId") int minId, @Param("maxId") int maxId,
                @Param("vector") float[] vector, PageObject page);
    }

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.jdbcTemplate.execute("""
                CREATE TABLE user_info (
                    id INT64 PRIMARY KEY, name VARCHAR(128) NULL, age INT32 NULL,
                    email VARCHAR(128) NULL, create_time VARCHAR(128) NULL, v FLOAT_VECTOR(2)
                ) WITH (consistency_level=Strong)
                """);
        this.jdbcTemplate.execute("CREATE INDEX page_v ON user_info(v) USING FLAT WITH (metric_type=L2)");
        this.jdbcTemplate.execute("LOAD TABLE user_info");
        initData();
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            this.jdbcTemplate.executeUpdate("""
                    INSERT INTO user_info (id, name, age, email, create_time, v)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, new Object[] { baseId() + i, "AnnoResult" + i, 20 + i,
                    "anno-result" + i + "@nxn.test", new Date(), new float[] { i, 0 } });
        }
    }

    @Override
    @Before
    public void createAnnotationMapper() throws Exception {
        this.session = newConfiguration().newSession(this.fixture.open());
        this.pageMapper = this.session.createMapper(NativePageMapper.class);
    }

    @Override
    protected List<UserInfo> queryPage(PageObject page) {
        return this.pageMapper.selectPage(baseId() + 1, baseId() + 10, new float[] { 0, 0 }, page);
    }

    @After
    public void cleanupFixture() throws Exception {
        try {
            if (this.jdbcTemplate != null) {
                this.jdbcTemplate.execute("DROP TABLE IF EXISTS user_info");
            }
        } finally {
            try {
                this.fixture.close();
            } finally {
                if (this.session != null) {
                    this.session.close();
                }
            }
        }
    }
}
