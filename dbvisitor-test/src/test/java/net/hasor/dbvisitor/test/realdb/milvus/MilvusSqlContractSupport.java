/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** 所有集合、索引和数据操作均通过驱动 SQL；每个用例只清理自己创建的集合。 */
public abstract class MilvusSqlContractSupport extends AdapterContractTest {
    protected final String collection = "dbv_sql_" + UUID.randomUUID().toString().replace("-", "");
    protected Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Before
    public void openSqlConnection() throws SQLException {
        this.connection = newAdapterConnection();
        this.jdbcTemplate = new JdbcTemplate(this.connection);
    }

    @After
    public void closeSqlConnection() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection; Statement statement = closing.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS " + this.collection);
            }
        }
    }

    protected void createCollection(String fields) throws SQLException {
        // QueryIterator uses the collection's consistency level, including read-after-write tests.
        this.jdbcTemplate.execute("CREATE TABLE " + this.collection + " (" + fields + ") WITH (consistency_level='Strong')");
    }

    protected void createIndex(String field, String indexType, String metric) throws SQLException {
        this.jdbcTemplate.execute("CREATE INDEX idx_" + field + " ON " + this.collection + "(" + field + ") USING " + indexType + " WITH (metric_type=" + metric + ")");
    }

    protected void loadCollection() throws SQLException {
        this.jdbcTemplate.execute("LOAD TABLE " + this.collection);
    }

    protected List<Long> readIds(ResultSet result) throws SQLException {
        List<Long> ids = new ArrayList<>();
        while (result.next()) {
            ids.add(result.getLong("id"));
        }
        return ids;
    }

    protected long countRows(String filter) throws SQLException {
        return this.jdbcTemplate.queryForLong("COUNT FROM " + this.collection + filter);
    }
}
