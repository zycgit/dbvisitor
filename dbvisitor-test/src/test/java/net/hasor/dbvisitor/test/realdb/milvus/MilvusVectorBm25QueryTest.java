/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.api.vector_query.VectorBm25QueryCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Same analyzer/function/index combination as the existing native Milvus BM25 SQL test. */
public class MilvusVectorBm25QueryTest extends VectorBm25QueryCase {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private       boolean               created;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.database.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected String vectorTable() {
        return "bm25_vectors";
    }

    @Override
    protected void prepareBm25Documents() throws SQLException {
        this.jdbcTemplate.execute("""
                CREATE TABLE bm25_vectors (
                    id INT64 PRIMARY KEY,
                    body VARCHAR(512) WITH (enable_analyzer=true),
                    embedding SPARSE_FLOAT_VECTOR,
                    FUNCTION bm25_fn USING BM25 (body) INTO (embedding)
                ) WITH (consistency_level=Strong)
                """);
        this.created = true;
        this.jdbcTemplate.execute("CREATE INDEX bm25_metric ON bm25_vectors(embedding) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
        this.jdbcTemplate.executeUpdate("""
                INSERT INTO bm25_vectors (id, body) VALUES
                    (11, 'milvus'), (12, 'relational database tables'),
                    (13, 'milvus vector database'), (14, 'cloud service')
                """);
        this.jdbcTemplate.execute("FLUSH bm25_vectors");
        this.jdbcTemplate.execute("LOAD TABLE bm25_vectors");
    }

    @After
    public void cleanupBm25Storage() throws SQLException {
        try {
            if (this.created) {
                this.jdbcTemplate.execute("DROP TABLE bm25_vectors");
            }
        } finally {
            this.database.close();
        }
    }
}
