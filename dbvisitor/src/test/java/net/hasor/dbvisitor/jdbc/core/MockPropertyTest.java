/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.StatementCallback;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.transaction.ConnectionProxy;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-1-13
 */
public class MockPropertyTest extends AbstractDbTest {
    @Test
    public void jdbcConnectionTest_1() {
        JdbcConnection jdbcTemplate = new JdbcConnection();
        jdbcTemplate.setFetchSize(123);
        assert jdbcTemplate.getFetchSize() == 123;
        jdbcTemplate.setMaxRows(321);
        assert jdbcTemplate.getMaxRows() == 321;
        jdbcTemplate.setQueryTimeout(11111);
        assert jdbcTemplate.getQueryTimeout() == 11111;
    }

    @Test
    public void jdbcConnectionTest_2() throws SQLException {
        Connection connection = PowerMockito.mock(Connection.class);
        JdbcConnection jdbcTemplate = new JdbcConnection(connection);
        jdbcTemplate.setIgnoreWarnings(false);

        Statement statement = PowerMockito.mock(Statement.class);
        SQLWarning warning = new SQLWarning("abc");
        PowerMockito.when(statement.getWarnings()).thenReturn(warning);

        try {
            jdbcTemplate.handleWarnings(statement);
            assert false;
        } catch (Exception e) {
            assert e instanceof SQLException;
            assert e.getMessage().equals("Warning not ignored");
            assert e.getCause() == warning;
        }
    }

    @Test
    public void jdbcConnectionTest_3() {
        try {
            JdbcConnection jdbcTemplate = new JdbcConnection();
            jdbcTemplate.execute((ConnectionCallback<Object>) con -> null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("Connection unavailable, any of (Connection/DynamicConnection/DataSource) is required.");
        }
    }

    @Test
    public void jdbcConnectionTest_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            try {
                new JdbcConnection(c).execute((ConnectionCallback<Object>) con -> {
                    con.createStatement().execute("xxxxx");
                    return null;
                });
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().contains("Syntax error in SQL");
                assert e.getMessage().contains("xxxxx");
            }

            try {
                new JdbcConnection(c).execute((StatementCallback<Object>) stat -> {
                    stat.execute("xxxxx");
                    return null;
                });
                assert false;
            } catch (SQLException e) {
                assert e.getMessage().contains("Syntax error in SQL");
                assert e.getMessage().contains("xxxxx");
            }
        }
    }

    @Test
    public void jdbcConnectionTest_5() throws Throwable {
        try (Connection dataSource = DsUtils.h2Conn()) {
            JdbcConnection jdbcConnection = new JdbcConnection(dataSource);
            jdbcConnection.setMaxRows(123);
            jdbcConnection.setFetchSize(10);
            jdbcConnection.setQueryTimeout(1234567);

            jdbcConnection.execute((StatementCallback<Object>) stat -> {
                assert stat.getMaxRows() == 123;
                assert stat.getFetchSize() == 10;
                assert stat.getQueryTimeout() == 1234567;
                return null;
            });
        }
    }

    @Test
    public void jdbcConnectionTest_6() throws SQLException {
        Connection connection = PowerMockito.mock(Connection.class);
        JdbcConnection jdbcConnection = new JdbcConnection(connection);

        jdbcConnection.execute((ConnectionCallback<Object>) con -> {
            assert con instanceof ConnectionProxy;
            assert ((ConnectionProxy) con).getTargetConnection() == connection;
            assert con != connection;
            assert con.equals(con);
            assert !con.equals(connection);

            con.hashCode();
            con.close();
            return null;
        });
    }

    @Test
    public void jdbcTemplateTest_6() {
        DataSource dataSource = PowerMockito.mock(DataSource.class);
        TypeHandlerRegistry typeRegistry = new TypeHandlerRegistry();
        MappingRegistry mappingRegistry = new MappingRegistry(null, typeRegistry, Options.of());
        JdbcQueryContext context = new JdbcQueryContext();
        context.setTypeRegistry(typeRegistry);

        assert new JdbcTemplate(dataSource, mappingRegistry, context).getRegistry().getTypeRegistry() == typeRegistry;

        assert new JdbcTemplate().getRegistry().getTypeRegistry() == TypeHandlerRegistry.DEFAULT;

        assert new JdbcTemplate().isResultsCaseInsensitive();
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        assert jdbcTemplate.isResultsCaseInsensitive();
        jdbcTemplate.setResultsCaseInsensitive(false);
        assert !jdbcTemplate.isResultsCaseInsensitive();
    }
}
