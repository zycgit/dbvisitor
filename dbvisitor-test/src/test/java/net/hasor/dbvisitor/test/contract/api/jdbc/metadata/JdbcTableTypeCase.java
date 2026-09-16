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
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract(scope = NxnContract.Scope.JDBC_METADATA)
public abstract class JdbcTableTypeCase extends AbstractNxnContractTest {
    @Override
    @Before
    public void setup() {
        // Metadata discovery must not depend on creating the standard DML fixtures.
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }

    @Test
    @Capability(CapabilityId.JDBC_METADATA_TABLE_TYPES)
    public void tableTypesDescribeAvailableKinds() throws Exception {
        try (Connection connection = OneApiDataSourceManager.getConnection(profile().env()); ResultSet rows = connection.getMetaData().getTableTypes()) {
            Set<String> names = new HashSet<>();
            while (rows.next()) {
                String name = rows.getString("TABLE_TYPE");
                assertNotNull(name);
                assertFalse(name.isEmpty());
                assertTrue("Duplicate table type: " + name, names.add(name));
            }
            assertFalse("Expected native table kinds", names.isEmpty());
        }
    }
}
