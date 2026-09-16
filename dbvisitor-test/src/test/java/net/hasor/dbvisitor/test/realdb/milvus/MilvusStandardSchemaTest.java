/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.metadata.StandardSchemaCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Binds the common scenarios to the explicitly declared Milvus boundary. */
public class MilvusStandardSchemaTest extends StandardSchemaCase {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private       boolean               created;

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = new JdbcTemplate(database.open());
        jdbcTemplate.execute("CREATE TABLE nxn_metadata_user (id INT64 PRIMARY KEY, name VARCHAR(128) NULL, age INT32, v FLOAT_VECTOR(2))");
        created = true;
    }

    @Override
    protected Connection schemaConnection() throws SQLException {
        return database.newConnection();
    }

    @Override
    protected Map<String, List<String>> standardSchema() {
        return Map.of("nxn_metadata_user", List.of("id", "name", "age", "v"));
    }

    @After
    public void cleanupMetadata() throws SQLException {
        try {
            if (created) {
                jdbcTemplate.execute("DROP TABLE nxn_metadata_user");
            }
        } finally {
            database.close();
        }
    }

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }
}
