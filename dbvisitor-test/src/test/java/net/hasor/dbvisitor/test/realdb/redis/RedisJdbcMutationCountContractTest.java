/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMutationCountContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

/** Single-key SET/DEL replies provide the same one-item update-count assertions. */
public class RedisJdbcMutationCountContractTest extends JdbcMutationCountContractTest {
    private final String prefix = "nxn:mutation:" + UUID.randomUUID() + ":";
    private final Set<String> keys = new LinkedHashSet<>();
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
        this.connection = OneApiDataSourceManager.getConnection("redis");
        this.jdbcTemplate = new JdbcTemplate(this.connection);
    }

    @Override
    protected int insertUser(int id, String name, int age, String email) throws SQLException {
        String key = this.prefix + id;
        this.keys.add(key);
        return this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, name });
    }

    @Override
    protected String command(JdbcCrudCommand command) {
        switch (command) {
            case UPDATE_AGE:
                return "SET #{'" + this.prefix + "' + arg1} #{arg0}";
            case DELETE:
                return "DEL #{'" + this.prefix + "' + arg0}";
            default:
                throw new IllegalArgumentException("Unsupported single-key mutation fixture: " + command);
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            for (String key : this.keys) {
                this.jdbcTemplate.executeUpdate("DEL ?", new Object[] { key });
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
