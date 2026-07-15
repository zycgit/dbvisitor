package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NxnContract
public abstract class JdbcCrudTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 610000;
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_INSERT)
    public void jdbcInsertPersistOneUser() throws SQLException {
        int id = baseId() + 1;
        int rows = jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-JDBC-Insert", 31, "nxn-insert@test.com", new Date() });

        assertEquals(1, rows);
        assertEquals("NXN-JDBC-Insert", jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_QUERY)
    public void jdbcQueryUserById() throws SQLException {
        int id = baseId() + 2;
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-JDBC-Query", 32, "nxn-query@test.com", new Date() });

        Integer age = jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { id }, Integer.class);
        assertEquals(Integer.valueOf(32), age);
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_UPDATE)
    public void jdbcUpdateChangeUser() throws SQLException {
        int id = baseId() + 3;
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-JDBC-Update", 33, "nxn-update@test.com", new Date() });

        int rows = jdbcTemplate.executeUpdate("UPDATE user_info SET age = ? WHERE id = ?", new Object[] { 34, id });
        assertEquals(1, rows);
        assertEquals(Integer.valueOf(34), jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_DELETE)
    public void jdbcDeleteRemoveUser() throws SQLException {
        int id = baseId() + 4;
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-JDBC-Delete", 35, "nxn-delete@test.com", new Date() });

        int rows = jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = ?", new Object[] { id });
        assertEquals(1, rows);
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { id }, Integer.class));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_EXECUTE_DDL)
    public void jdbcExecuteDdlAndDmlTableOps() throws SQLException {
        String tableName = "nxn_jdbc_execute_temp";
        dropTableIfExists(tableName);
        jdbcTemplate.execute("CREATE TABLE " + tableName + " (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, create_time " + profile().datetimeColumnType() + ")");

        int inserted = jdbcTemplate.executeUpdate("INSERT INTO " + tableName + " (id, name, age, create_time) VALUES (?, ?, ?, ?)", //
                new Object[] { 1, "NXN-JDBC-DDL", 25, new Date() });
        jdbcTemplate.execute(addColumnSql(tableName, "email VARCHAR(100)"));
        int updated = jdbcTemplate.executeUpdate("UPDATE " + tableName + " SET email = ? WHERE id = ?", //
                new Object[] { "nxn-jdbc-ddl@test.com", 1 });
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT id, name, email FROM " + tableName + " WHERE id = ?", new Object[] { 1 });

        assertEquals(1, inserted);
        assertEquals(1, updated);
        assertEquals("NXN-JDBC-DDL", value(row, "name"));
        assertEquals("nxn-jdbc-ddl@test.com", value(row, "email"));

        jdbcTemplate.execute("DROP TABLE " + tableName);
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
            fail("Temporary DDL table should be dropped");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_UPSERT_ON_CONFLICT)
    public void jdbcUpsertAndUpdateRowsWithPostgresOnConflict() throws SQLException {
        requiresNxnFeature(FeatureId.POSTGRES_ON_CONFLICT);

        int firstId = baseId() + 20;
        int secondId = baseId() + 21;
        String upsertSql = "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?) " //
                + "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, age = EXCLUDED.age, email = EXCLUDED.email";
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

    private Object value(Map<String, Object> row, String key) {
        assertNotNull(row);
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
