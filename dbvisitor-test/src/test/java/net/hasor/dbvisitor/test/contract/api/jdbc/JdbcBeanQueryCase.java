/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JdbcBeanQueryCase extends JdbcQuerySupport {
    @Test
    @Capability(CapabilityId.JDBC_QUERY_BEAN)
    public void jdbcQueryForList_shouldReturnBeans() throws SQLException {
        seedUsers();

        List<UserInfo> rows = jdbcTemplate.queryForList(selectRange("id, name, age, email, create_time", "?", "?", true), //
                new Object[] { baseId() + 1, baseId() + 3 }, UserInfo.class);

        assertEquals(3, rows.size());
        assertEquals(Integer.valueOf(baseId() + 2), rows.get(1).getId());
        assertEquals("NXN-JDBC-Query-2", rows.get(1).getName());
        assertEquals(Integer.valueOf(62), rows.get(1).getAge());
        for (int i = 0; i < rows.size(); i++) {
            UserInfo row = rows.get(i);
            assertEquals(Integer.valueOf(baseId() + i + 1), row.getId());
            assertEquals("NXN-JDBC-Query-" + (i + 1), row.getName());
            assertEquals(Integer.valueOf(61 + i), row.getAge());
            assertEquals("nxn-jdbc-query-" + (i + 1) + "@test.com", row.getEmail());
            assertNotNull(row.getCreateTime());
        }
    }
}
