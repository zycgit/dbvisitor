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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusImportSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_IMPORT_FAILURE)
    public void failedImportsShouldRemainInspectableWithoutResubmission() throws Exception {
        createCollection("id INT64 PRIMARY KEY,v FLOAT_VECTOR(2)");
        String missingFile = "/tmp/" + this.collection + "_missing.json";
        String asyncJob;
        try (PreparedStatement submit = this.connection.prepareStatement(
                "/*+ sync=false */ IMPORT FROM ? INTO " + this.collection + " RETURNING JOB_ID")) {
            submit.setString(1, missingFile);
            try (ResultSet result = submit.executeQuery()) {
                assertTrue(result.next());
                asyncJob = result.getString("JOB_ID");
                assertNotNull(asyncJob);
                assertFalse(result.next());
            }
        }
        awaitFailure(asyncJob, missingFile);

        String syncJob;
        try (PreparedStatement submit = this.connection.prepareStatement(
                "/*+ timeout=30000 */ IMPORT FROM ? INTO " + this.collection)) {
            submit.setString(1, missingFile);
            SQLException failure = assertThrows(SQLException.class, submit::executeUpdate);
            String message = failure.getMessage();
            assertTrue(message, message.contains("state=Failed"));
            assertTrue(message, message.contains(missingFile));
            Matcher id = Pattern.compile("jobId=(\\d+)").matcher(message);
            assertTrue(message, id.find());
            syncJob = id.group(1);
        }
        assertNotEquals(asyncJob, syncJob);
        awaitFailure(syncJob, missingFile);

        try (Statement statement = this.connection.createStatement();
             ResultSet jobs = statement.executeQuery("SHOW IMPORTS FROM " + this.collection)) {
            Set<String> ids = new HashSet<>();
            while (jobs.next()) {
                assertTrue(ids.add(jobs.getString("JOB_ID")));
                assertEquals("Failed", jobs.getString("STATE"));
            }
            assertEquals(Set.of(asyncJob, syncJob), ids);
        }
        createIndex("v", "FLAT", "L2");
        loadCollection();
        assertEquals(0, countRows(""));
        assertEquals(1, this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,v) VALUES (1,[1,0])"));
        assertEquals(1, countRows(""));
    }

    private void awaitFailure(String jobId, String missingFile) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        try (PreparedStatement progress = this.connection.prepareStatement("SHOW IMPORT ?")) {
            progress.setString(1, jobId);
            progress.setQueryTimeout(5);
            String state = null;
            while (System.nanoTime() < deadline) {
                try (ResultSet result = progress.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(jobId, result.getString("JOB_ID"));
                    state = result.getString("STATE");
                    if ("Failed".equals(state)) {
                        String reason = result.getString("REASON");
                        assertNotNull(reason);
                        assertTrue(reason, reason.contains(missingFile));
                        assertFalse(result.next());
                        return;
                    }
                    assertNotEquals("Completed", state);
                }
                Thread.sleep(100);
            }
            fail("Import did not reach Failed within 30 seconds: job=" + jobId + ", state=" + state);
        }
    }
}
