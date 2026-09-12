/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.write;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ClickHouseMutationAndVersionTest extends AbstractNxnContractTest {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Test
    public void synchronousMutation_returnsUpdatedValueAndObservableProgress() throws Exception {
        dropTableIfExists("doc_event_log");
        try {
            jdbcTemplate.executeUpdate("CREATE TABLE doc_event_log (id String, event_name String) ENGINE = MergeTree ORDER BY id");
            jdbcTemplate.executeUpdate("INSERT INTO doc_event_log VALUES ('E1', 'login')");
            jdbcTemplate.executeUpdate(
                    "ALTER TABLE doc_event_log UPDATE event_name = ? WHERE id = ? SETTINGS mutations_sync = 0",
                    new Object[] { "archived", "E1" });
            jdbcTemplate.executeUpdate(
                    "ALTER TABLE doc_event_log UPDATE event_name = ? WHERE id = ? SETTINGS mutations_sync = 1",
                    new Object[] { "archived", "E1" });
            assertEquals("archived", jdbcTemplate.queryForObject("SELECT event_name FROM doc_event_log WHERE id = ?",
                    new Object[] { "E1" }, String.class));
            List<Map<String, Object>> tasks = jdbcTemplate.queryForList("""
                    SELECT mutation_id, command, is_done, parts_to_do, latest_fail_reason
                    FROM system.mutations
                    WHERE database = currentDatabase() AND table = ?
                    ORDER BY create_time DESC, mutation_id DESC
                    """, new Object[] { "doc_event_log" });
            assertFalse(tasks.isEmpty());
            for (Map<String, Object> task : tasks) {
                assertNotNull(task.get("mutation_id"));
                assertNotNull(task.get("command"));
                assertEquals(0, ((Number) task.get("parts_to_do")).intValue());
                Object done = task.get("is_done");
                assertEquals(true, done instanceof Boolean ? done : ((Number) done).intValue() == 1);
                assertEquals("", task.get("latest_fail_reason"));
            }
        } finally {
            dropTableIfExists("doc_event_log");
        }
    }

    @Test
    public void duplicateBusinessId_keepsHistoryAndArgMaxReturnsLatest() throws Exception {
        dropTableIfExists("doc_event_version");
        try {
            jdbcTemplate.executeUpdate("CREATE TABLE doc_event_version (id String, event_name String, version UInt64) ENGINE = MergeTree ORDER BY id");
            jdbcTemplate.executeUpdate("INSERT INTO doc_event_version VALUES ('E1', 'login', 1), ('E1', 'archived', 2)");
            List<Map<String, Object>> records = jdbcTemplate.queryForList(
                    "SELECT id, event_name, version FROM doc_event_version WHERE id = ? ORDER BY version", new Object[] { "E1" });
            assertEquals(2, records.size());
            assertEquals("login", records.get(0).get("event_name"));
            assertEquals("archived", records.get(1).get("event_name"));

            List<Map<String, Object>> latest = jdbcTemplate.queryForList("""
                    SELECT id, argMax(event_name, version) AS event_name
                    FROM doc_event_version
                    GROUP BY id
                    ORDER BY id
                    """);
            assertEquals(1, latest.size());
            assertEquals("E1", latest.get(0).get("id"));
            assertEquals("archived", latest.get(0).get("event_name"));
        } finally {
            dropTableIfExists("doc_event_version");
        }
    }
}
