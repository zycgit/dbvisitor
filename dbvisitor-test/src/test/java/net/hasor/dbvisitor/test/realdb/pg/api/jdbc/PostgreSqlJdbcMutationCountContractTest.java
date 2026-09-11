/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.pg.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import org.junit.Test;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import static org.junit.Assert.assertEquals;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMutationCountContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlJdbcMutationCountContractTest extends JdbcMutationCountContractTest {
    @Test
    @Capability(CapabilityId.ADAPTER_PG_SQL_UPDATE_COUNT)
    public void jdbcUpsert_shouldInsertAndUpdateRowsWithPostgresOnConflict() throws SQLException {
        requiresNxnFeature(FeatureId.POSTGRES_ON_CONFLICT);

        int firstId = baseId() + 20;
        int secondId = baseId() + 21;
        String upsertSql = """
            INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, age = EXCLUDED.age, email = EXCLUDED.email
            """;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { firstId, "NXN-JDBC-Upsert-Original", 25, "nxn-upsert-original@test.com", new Date() });

        int updated = jdbcTemplate.executeUpdate(upsertSql, //
                new Object[] { firstId, "NXN-JDBC-Upsert-Updated", 26, "nxn-upsert-updated@test.com", new Date() });
        int inserted = jdbcTemplate.executeUpdate(upsertSql, //
                new Object[] { secondId, "NXN-JDBC-Upsert-Inserted", 30, "nxn-upsert-inserted@test.com", new Date() });

        assertEquals(1, updated);
        assertEquals(1, inserted);
        assertEquals("NXN-JDBC-Upsert-Updated", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { firstId }));
        assertEquals(Integer.valueOf(30), jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { secondId }, Integer.class));
    }

    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
