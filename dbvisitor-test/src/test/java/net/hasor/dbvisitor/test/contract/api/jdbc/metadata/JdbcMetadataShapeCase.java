/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc.metadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Set;
import java.util.UUID;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@NxnContract(scope = NxnContract.Scope.JDBC_METADATA)
public abstract class JdbcMetadataShapeCase extends AbstractNxnContractTest {
    @Override
    @Before
    public void setup() {
        // Metadata discovery must not depend on creating the standard DML fixtures.
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }

    @Test
    @Capability(CapabilityId.JDBC_METADATA_EMPTY_RESULTS)
    public void emptyResultsPreserveMetadataColumns() throws Exception {
        try (Connection connection = OneApiDataSourceManager.getConnection(profile().env())) {
            try (ResultSet catalogs = connection.getMetaData().getCatalogs()) {
                verifyNames(catalogs, "TABLE_CAT", CapabilityId.JDBC_METADATA_CATALOGS);
            }
            try (ResultSet schemas = connection.getMetaData().getSchemas()) {
                verifyNames(schemas, "TABLE_SCHEM", CapabilityId.JDBC_METADATA_SCHEMAS);
            }
            try (ResultSet types = connection.getMetaData().getTableTypes()) {
                verifyNames(types, "TABLE_TYPE", CapabilityId.JDBC_METADATA_TABLE_TYPES);
            }
            String missing = "nxnmissing" + UUID.randomUUID().toString().replace("-", "");
            try (ResultSet tables = connection.getMetaData().getTables(null, null, missing, null)) {
                assertTrue(tables.findColumn("TABLE_CAT") > 0);
                assertTrue(tables.findColumn("TABLE_SCHEM") > 0);
                assertTrue(tables.findColumn("TABLE_NAME") > 0);
                assertTrue(tables.findColumn("TABLE_TYPE") > 0);
                assertFalse(tables.next());
            }
            try (ResultSet columns = connection.getMetaData().getColumns(null, null, missing, "%")) {
                assertTrue(columns.findColumn("COLUMN_NAME") > 0);
                assertTrue(columns.findColumn("TYPE_NAME") > 0);
                // Vendors represent this numeric descriptor as SMALLINT, INTEGER or NUMBER.
                int type = columns.getMetaData().getColumnType(columns.findColumn("DATA_TYPE"));
                assertTrue("Expected numeric DATA_TYPE descriptor: " + type, Set.of(Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.NUMERIC, Types.DECIMAL).contains(type));
                assertFalse(columns.next());
            }
        }
    }

    private void verifyNames(ResultSet rows, String column, String capability) throws Exception {
        assertTrue(rows.findColumn(column) > 0);
        if (profile().support(capability) != SupportStatus.SUPPORTED) {
            while (rows.next()) {
                String value = rows.getString(column);
                assertTrue("Unexpected named namespace: " + value, value == null || value.isEmpty());
            }
        }
    }
}
