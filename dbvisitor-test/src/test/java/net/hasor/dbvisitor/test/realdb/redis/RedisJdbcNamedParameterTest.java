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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcNamedParameterCase;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

/** Named values select private sorted-set fixtures using native ZCOUNT/ZCARD. */
public class RedisJdbcNamedParameterTest extends JdbcNamedParameterCase {
    private final String prefix = "nxn_params_" + UUID.randomUUID() + "_";
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
    protected String command(JdbcParameterCommand command) {
        switch (command) {
            case INSERT_COLON:
                return "ZADD #{'" + this.prefix + "' + name + '_' + id} :age #{email + '_' + createTime}";
            case INSERT_BRACE:
                return "ZADD #{'" + this.prefix + "' + name + '_' + id} #{age} #{email + '_' + createTime}";
            case INSERT_POSITIONAL:
                return "ZADD #{'" + this.prefix + "' + arg1 + '_' + arg0} #{arg2} #{arg3 + '_' + arg4}";
            case COUNT_BY_NAME_AGE:
                return "ZCOUNT #{'" + this.prefix + "' + name + '_" + (baseId() + 5) + "'} :age +inf";
            case COUNT_BY_ID_NAME:
                return "ZCARD #{'" + this.prefix + "' + name + '_' + id}";
            case COUNT_BY_NESTED:
                return "ZCOUNT #{'" + this.prefix + "' + names[0] + '_' + ids[0]} :user.info.age :user.info.age";
            default:
                throw new IllegalArgumentException("No native named-parameter fixture for " + command);
        }
    }

    @Override
    protected void writeParameters(String command, Object args) throws SQLException {
        // ZADD exposes its native integer reply as a result set, not a JDBC update count.
        org.junit.Assert.assertEquals(Long.valueOf(1), this.jdbcTemplate.queryForLong(command, args));
    }

    @After
    public void cleanupFixture() throws SQLException {
        // Only these three private fixture keys can be written by this contract.
        List<String> keys = Arrays.asList(this.prefix + "NXN-Param-Colon_" + (baseId() + 5),
                this.prefix + "NXN-Param-Brace_" + (baseId() + 6), this.prefix + "NXN-Param-Ognl_" + (baseId() + 7));
        if (this.connection != null) {
            try (Connection closing = this.connection) {
                for (String key : keys) {
                    this.jdbcTemplate.executeUpdate("DEL ?", new Object[] { key });
                }
            }
        }
    }
}
