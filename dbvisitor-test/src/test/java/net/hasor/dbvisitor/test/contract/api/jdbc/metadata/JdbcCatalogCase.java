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
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import static org.junit.Assert.*;

@NxnContract
public abstract class JdbcCatalogCase extends AbstractNxnContractTest {
    @Override
    @Before
    public void setup() {
        // Metadata discovery must not depend on creating the standard DML fixtures.
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }

    @Test
    @Capability(CapabilityId.JDBC_METADATA_CATALOGS)
    public void catalogsExposeDatabaseNames() throws Exception {
        try (Connection connection = OneApiDataSourceManager.getConnection(profile().env());
                ResultSet rows = connection.getMetaData().getCatalogs()) {
            Set<String> names = new HashSet<>();
            while (rows.next()) {
                String name = rows.getString("TABLE_CAT");
                assertNotNull(name);
                assertFalse(name.isEmpty());
                assertTrue("Duplicate catalog: " + name, names.add(name));
            }
            assertFalse("Expected native databases", names.isEmpty());
            String current = connection.getCatalog();
            if (current != null && !current.isEmpty()) {
                assertTrue("Current catalog missing: " + current + ", actual=" + names, names.contains(current));
            }
        }
    }
}
