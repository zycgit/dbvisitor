/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultMapCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

public class MilvusXmlMapperResultMapTest extends XmlMapperResultMapCase {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
    private Session session;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
        initData();
    }

    @Override
    protected Session openSession(Configuration configuration) throws Exception {
        configuration.loadMapper("/realdb/milvus/material/XmlResultMapLabelsMapper.xml");
        this.session = configuration.newSession(this.fixture.open());
        return this.session;
    }

    @Override
    protected List<Map<String, Object>> queryColumnLabelRows() throws Exception {
        // Milvus supplies labels through native field names, without emulating SQL aliases.
        try {
            this.jdbcTemplate.executeUpdate("""
                    CREATE TABLE result_map_labels (
                        user_id INT64 PRIMARY KEY, user_name VARCHAR(128), user_age INT32,
                        vector_text VARCHAR(128) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            this.jdbcTemplate.executeUpdate("CREATE INDEX labels_v ON result_map_labels(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            this.jdbcTemplate.executeUpdate("LOAD TABLE result_map_labels");
            this.jdbcTemplate.executeUpdate("INSERT INTO result_map_labels (user_id, user_name, user_age) VALUES (?, ?, ?)",
                    new Object[] { baseId() + 3, "RmCfg3", 28 });
            return this.session.queryStatement("milvus.ResultMapLabels.selectLabels", Map.of("id", baseId() + 3));
        } finally {
            this.jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS result_map_labels");
        }
    }

    @After
    public void cleanupFixture() throws SQLException, IOException {
        try {
            this.fixture.close();
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
}
