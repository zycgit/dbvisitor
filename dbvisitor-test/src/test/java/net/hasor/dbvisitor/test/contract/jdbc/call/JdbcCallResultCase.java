/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.jdbc.call;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import static org.junit.Assert.*;

/** Collecting a callable command's result set does not require OUT parameters or stored procedures. */
@NxnContract
public abstract class JdbcCallResultCase extends AbstractNxnContractTest {
    @Before
    public void prepareCallResultFixture() throws SQLException {
        createCallFixture();
    }

    protected abstract void createCallFixture() throws SQLException;

    protected abstract String callCommand();

    protected Map<String, Object> callParameters() {
        return Collections.singletonMap("p_id", 918001);
    }

    protected String nameColumn() {
        return "name";
    }

    protected String ageColumn() {
        return "age";
    }

    @Test
    @Capability(CapabilityId.JDBC_CALL_RESULT_SET)
    public void callShouldCollectReturnedResultSet() throws SQLException {
        Map<String, Object> result = jdbcTemplate.call(callCommand(), callParameters());
        assertNotNull(result);
        assertTrue(result.get("#result-set-1") instanceof List);
        List<?> users = (List<?>) result.get("#result-set-1");
        assertEquals(1, users.size());
        assertTrue(users.get(0) instanceof Map);
        Map<?, ?> user = (Map<?, ?>) users.get(0);
        assertEquals("ProcAlice", rowValue(user, nameColumn()));
        assertTrue(rowValue(user, ageColumn()) instanceof Number);
        assertEquals(25.0, ((Number) rowValue(user, ageColumn())).doubleValue(), 0.0);
    }

    private Object rowValue(Map<?, ?> row, String columnName) {
        for (Map.Entry<?, ?> entry : row.entrySet()) {
            if (entry.getKey() != null && columnName.equalsIgnoreCase(entry.getKey().toString())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
