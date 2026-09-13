/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusLoadSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_LOAD_FIELDS)
    public void selectedFieldsShouldPermitSelectedQueriesAndExplicitReload() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, note VARCHAR(128), v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,note,v) VALUES (1,'stored',[1,0])");
        this.jdbcTemplate.executeUpdate("FLUSH " + this.collection);
        try (PreparedStatement load = this.connection.prepareStatement("LOAD TABLE " + this.collection
                + " WITH (num_replicas=?, load_fields=?, skip_load_dynamic_field=?, resource_groups=?)")) {
            load.setInt(1, 1);
            load.setObject(2, new String[] { "id", "v" });
            load.setBoolean(3, true);
            load.setObject(4, Collections.emptyList());
            assertEquals(0, load.executeUpdate());
        }
        assertEquals(Long.valueOf(1), this.jdbcTemplate.queryForLong("SELECT id FROM " + this.collection + " WHERE id=1"));
        // The native SDK on 2.6.2 also allows reading an omitted field. Do not invent column access control.
        // Change the loaded field set explicitly; no automatic release/reload belongs in query handling.
        this.jdbcTemplate.executeUpdate("RELEASE TABLE " + this.collection);
        loadCollection();
        assertEquals("stored", this.jdbcTemplate.queryForString("SELECT note FROM " + this.collection + " WHERE id=1"));
        assertEquals(0, this.jdbcTemplate.executeUpdate("LOAD TABLE " + this.collection + " WITH (refresh=true)"));
        assertEquals("stored", this.jdbcTemplate.queryForString("SELECT note FROM " + this.collection + " WHERE id=1"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_LOAD_PARTITION_REFRESH)
    public void partitionLoadAndRefreshShouldKeepTheSelectedPartitionQueryable() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, note VARCHAR(128), v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        this.jdbcTemplate.executeUpdate("CREATE PARTITION p ON " + this.collection);
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " PARTITION p (id,note,v) VALUES (1,'partition',[1,0])");
        this.jdbcTemplate.executeUpdate("FLUSH " + this.collection);
        try (PreparedStatement load = this.connection.prepareStatement("LOAD TABLE " + this.collection
                + " PARTITION p WITH (num_replicas=1, load_fields=?, refresh=?)")) {
            load.setObject(1, Arrays.asList("id", "note", "v"));
            load.setBoolean(2, false);
            assertEquals(0, load.executeUpdate());
            assertEquals("partition", this.jdbcTemplate.queryForString("SELECT note FROM " + this.collection + " PARTITION p WHERE id=1"));
            load.setBoolean(2, true);
            assertEquals(0, load.executeUpdate());
        }
        assertEquals("partition", this.jdbcTemplate.queryForString("SELECT note FROM " + this.collection + " PARTITION p WHERE id=1"));
    }
}
