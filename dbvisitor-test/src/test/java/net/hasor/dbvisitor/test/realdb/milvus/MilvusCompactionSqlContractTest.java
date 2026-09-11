/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Set;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

/** Verifies SQL submission and observation, not compaction scheduling or storage savings. */
public class MilvusCompactionSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_COMPACTION)
    public void compaction_shouldReturnTaskIdAndExposeStateAndPlans() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id, v) VALUES (1, [1, 0]), (2, [0, 1])");
        this.jdbcTemplate.execute("FLUSH " + this.collection);
        long taskId;
        try (Statement statement = this.connection.createStatement();
                ResultSet result = statement.executeQuery("COMPACT TABLE " + this.collection)) {
            assertEquals(Types.BIGINT, result.getMetaData().getColumnType(1));
            assertTrue(result.next());
            taskId = result.getLong("COMPACTION_ID");
            assertFalse(result.wasNull());
            assertTrue(taskId > 0);
            assertFalse(result.next());
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SHOW COMPACTION ?")) {
            statement.setLong(1, taskId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(taskId, result.getLong("COMPACTION_ID"));
                assertTrue(Set.of("Executing", "Completed").contains(result.getString("STATE")));
                assertTrue(result.getLong("EXECUTING_PLANS") >= 0);
                assertTrue(result.getLong("COMPLETED_PLANS") >= 0);
                assertTrue(result.getLong("TIMEOUT_PLANS") >= 0);
                assertFalse(result.next());
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SHOW COMPACTION PLANS ?")) {
            statement.setLong(1, taskId);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(taskId, result.getLong("COMPACTION_ID"));
                assertTrue(Set.of("Executing", "Completed").contains(result.getString("STATE")));
                assertTrue(JsonParser.parseString(result.getString("PLANS")).isJsonArray());
                assertFalse(result.next());
            }
        }
    }
}
