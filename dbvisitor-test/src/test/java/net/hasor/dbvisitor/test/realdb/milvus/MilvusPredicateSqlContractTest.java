/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Arrays;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MilvusPredicateSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FAILED_DELETE_SCOPE)
    public void rejectedConstantPredicateShouldPreserveRowsAndStopFollowingDelete() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,v) VALUES (1,[1,0]),(2,[0,1]),(3,[0.5,0.5])");
        // The 2.6.2 server rejects this constant/field conjunction even through the native SDK.
        String sql = "DELETE FROM " + this.collection + " WHERE id IN ? AND 1=1; DELETE FROM " + this.collection + " WHERE id=?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setObject(1, Arrays.asList(1L, 2L));
            statement.setLong(2, 3L);
            SQLException failure = assertThrows(SQLException.class, statement::execute);
            assertTrue(failure.getMessage().contains("can only be used between boolean expressions"));
        }
        assertEquals(3, countRows(""));
        assertEquals(2, countRows(" WHERE id IN (1,2)"));
        assertEquals(1, countRows(" WHERE id=3"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_NOT_PRECEDENCE)
    public void literalNotShouldKeepSqlComparisonAndLogicalPrecedence() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,age INT32 NULL,v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,age,v) VALUES (1,25,[1,0]),(2,30,[0,1]),(3,35,[0.5,0.5])");
        assertEquals(2, countRows(" WHERE NOT age = 25"));
        assertEquals(1, countRows(" WHERE NOT NOT age = 25"));
        assertEquals(1, countRows(" WHERE NOT age IN (25,30)"));
        assertEquals(2, countRows(" WHERE NOT age + 1 > 31"));
        assertEquals(2, countRows(" WHERE age = 25 OR age = 30 AND id = 2"));
        assertEquals(1, countRows(" WHERE (age = 25 OR age = 30) AND id = 2"));
        assertEquals(2, countRows(" WHERE NOT age = 25 AND age < 35 OR id=1"));
        assertEquals(2L, this.jdbcTemplate.queryForLong("COUNT FROM " + this.collection + " WHERE age <> ?", 25).longValue());
    }
}
