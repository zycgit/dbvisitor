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
import net.hasor.dbvisitor.session.Session;

/** Same ordered XML result assertions, with native KNN order instead of scalar ORDER BY. */
final class MilvusXmlCapabilityFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private JdbcTemplate jdbc;

    JdbcTemplate open(int baseId) throws SQLException {
        if (this.jdbc != null) {
            return this.jdbc;
        }
        this.jdbc = new JdbcTemplate(this.database.open());
        this.jdbc.execute("""
                CREATE TABLE user_info (id INT64 PRIMARY KEY, name VARCHAR(128), age INT32,
                    email VARCHAR(128), create_time VARCHAR(128), v FLOAT_VECTOR(2))
                WITH (consistency_level=Strong)
                """);
        this.jdbc.execute("CREATE INDEX page_v ON user_info(v) USING FLAT WITH (metric_type=L2)");
        this.jdbc.execute("LOAD TABLE user_info");
        for (int i = 1; i <= 5; i++) {
            this.jdbc.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[] { baseId + i, "XmlCrud" + i, 20 + i, "crud" + i + "@test.com", new Date(), new float[] { i, 0 } });
        }
        return this.jdbc;
    }

    Session session(Configuration configuration) throws Exception {
        configuration.loadMapper("/realdb/milvus/material/XmlCapabilityCrudMapper.xml");
        configuration.loadMapper("/realdb/milvus/material/XmlCapabilityResultMapper.xml");
        return configuration.newSession(this.jdbc.getConnection());
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.jdbc != null) {
                this.jdbc.execute("DROP TABLE IF EXISTS user_info");
            }
        } finally {
            this.database.close();
        }
    }
}
