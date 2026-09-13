/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import java.sql.Date;
import java.sql.SQLException;
import java.io.IOException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeNamedFieldTypeContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ClickHouseNamedFieldTypeContractTest extends NativeNamedFieldTypeContractTest {
    private enum State { ACTIVE, INACTIVE }

    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    public void setup() throws SQLException {
        // The shared contract uses document-store fixtures; ClickHouse uses native typed columns.
        try {
            this.dataSource = OneApiDataSourceManager.createDataSource();
        } catch (IOException e) {
            throw new SQLException("Cannot load ClickHouse test connection", e);
        }
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        cleanTestData();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_DATE)
    public void date_shouldRoundTripByFieldName() throws SQLException {
        assertRoundTrip("time_types_explicit_test", "date_value", Date.valueOf("2024-02-29"), Date.class, 1);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_ENUM)
    public void enum_shouldRoundTripByFieldName() throws SQLException {
        assertRoundTrip("enum_types_explicit_test", "status_string", State.INACTIVE, State.class, 1);
    }

    @Test
    @Capability(CapabilityId.ADAPTER_NAMED_FIELD_BOOLEAN)
    public void boolean_shouldRoundTripByFieldName() throws SQLException {
        assertRoundTrip("basic_types_test", "bool_value", true, Boolean.class, 1);
        assertRoundTrip("basic_types_test", "bool_value", false, Boolean.class, 2);
    }

    private <T> void assertRoundTrip(String table, String column, T expected, Class<T> type, int id) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO " + table + " (id, " + column + ") VALUES (?, ?)", new Object[] { id, expected });
        Object actual = jdbcTemplate.queryForObject("SELECT " + column + " AS typed_value FROM " + table + " WHERE id = ?", new Object[] { id },
                (rs, row) -> TypeHandlerRegistry.DEFAULT.getTypeHandler(type).getResult(rs, "typed_value"));
        assertEquals(expected, actual);
    }
}
