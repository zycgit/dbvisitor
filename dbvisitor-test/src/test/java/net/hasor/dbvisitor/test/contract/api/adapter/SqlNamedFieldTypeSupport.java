/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Assume;
import static org.junit.Assert.assertEquals;

/** Relational typed-column fixtures for the shared field-name reading contract. */
public abstract class SqlNamedFieldTypeSupport extends NativeNamedFieldTypeCase {
    @Override
    public void setup() throws SQLException {
        Assume.assumeTrue(profile().env().equals(OneApiDataSourceManager.getDbDialect()));
        try {
            this.dataSource = OneApiDataSourceManager.createDataSource();
        } catch (IOException e) {
            throw new SQLException("Cannot load test connection", e);
        }
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        ensureSchemaExists();
        this.jdbcTemplate.executeUpdate("DELETE FROM time_types_explicit_test");
        this.jdbcTemplate.executeUpdate("DELETE FROM enum_types_explicit_test");
        this.jdbcTemplate.executeUpdate("DELETE FROM basic_types_test");
    }

    @Override
    protected <T> void assertRoundTrip(T expected, Class<T> type) throws SQLException {
        String table;
        String column;
        if (type == Date.class) {
            table = "time_types_explicit_test";
            column = "date_value";
        } else if (type == State.class) {
            table = "enum_types_explicit_test";
            column = "status_string";
        } else if (type == Boolean.class) {
            table = "basic_types_test";
            column = "bool_value";
        } else {
            throw new IllegalArgumentException("No typed fixture column for " + type.getName());
        }
        int id = Boolean.FALSE.equals(expected) ? 2 : 1;
        Object storedValue = expected instanceof State ? ((State) expected).name() : expected;
        this.jdbcTemplate.executeUpdate("INSERT INTO " + table + " (id, " + column + ") VALUES (?, ?)", new Object[] { id, storedValue });
        Object actual = this.jdbcTemplate.queryForObject("SELECT " + column + " AS typed_value FROM " + table + " WHERE id = ?", new Object[] { id },
                (rs, row) -> TypeHandlerRegistry.DEFAULT.getTypeHandler(type).getResult(rs, "typed_value"));
        assertEquals(expected, actual);
    }
}
