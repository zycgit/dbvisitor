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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultHandlerCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusXmlMapperResultHandlerTest extends XmlMapperResultHandlerCase {
    private final MilvusLambdaResultFixture fixture = new MilvusLambdaResultFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    protected long timestamp() {
        return 1700000000123L;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        initData();
    }

    @Override
    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration configuration = new Configuration();
        configuration.loadMapper("/realdb/milvus/mapper/SharedResultHandlerMapper.xml");
        this.session = configuration.newSession(this.fixture.open());
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            this.jdbcTemplate.executeUpdate(
                    "INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[] { baseId() + i, "ResHdl" + i, 20 + i * 5, "hdl" + i + "@nxn.test", new Date(1700000000123L), new float[] { i, 0 } });
        }
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
}
