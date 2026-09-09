package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcBatchTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 640000;
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_POSITIONAL_INSERT)
    public void jdbcBatchPosInsertRows() throws SQLException {
        Object[][] args = new Object[3][];
        for (int i = 0; i < args.length; i++) {
            args[i] = new Object[] { baseId() + i + 1, "NXN-Batch-Pos-" + i };
        }

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)", args);

        assertEquals(3, rows.length);
        assertEquals(3, (int) jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?", new Object[] { baseId() + 1, baseId() + 3 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_NAMED_INSERT)
    public void jdbcBatchNamedInsertRows() throws SQLException {
        Map<String, Object>[] args = new Map[3];
        for (int i = 0; i < args.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", baseId() + 11 + i);
            row.put("val", "NXN-Batch-Named-" + i);
            args[i] = row;
        }

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)", args);

        assertEquals(3, rows.length);
        assertEquals("NXN-Batch-Named-1", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 12 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_UPDATE)
    public void jdbcBatchUpdateChangeRows() throws SQLException {
        jdbcBatchPosInsertRows();

        Object[][] args = new Object[][] { //
                new Object[] { "NXN-Batch-Updated-1", baseId() + 1 }, //
                new Object[] { "NXN-Batch-Updated-2", baseId() + 2 } };

        int[] rows = jdbcTemplate.executeBatch("UPDATE basic_types_test SET string_value = ? WHERE id = ?", args);

        assertEquals(2, rows.length);
        assertEquals("NXN-Batch-Updated-1", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 1 }));
        assertEquals("NXN-Batch-Pos-2", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 3 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_DELETE)
    public void jdbcBatchDeleteRemoveRows() throws SQLException {
        jdbcBatchPosInsertRows();

        Object[][] args = new Object[][] { //
                new Object[] { baseId() + 1 }, //
                new Object[] { baseId() + 2 } };

        int[] rows = jdbcTemplate.executeBatch("DELETE FROM basic_types_test WHERE id = ?", args);

        assertEquals(2, rows.length);
        assertEquals(1, (int) jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?", new Object[] { baseId() + 1, baseId() + 3 }));
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_PARTIAL_FAILURE)
    public void jdbcBatchPartialFailureDupKeyError() throws SQLException {
        requiresNxnFeature(FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED);

        // @formatter:off
        Object[][] args = new Object[][] {
            new Object[] { baseId() + 31, "NXN-Batch-Valid-1" },
            new Object[] { baseId() + 32, "NXN-Batch-Valid-2" },
            new Object[] { baseId() + 31, "NXN-Batch-Duplicate" },
            new Object[] { baseId() + 33, "NXN-Batch-Valid-3" }
        };
        // @formatter:on

        boolean caught = false;
        try {
            jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)", args);
        } catch (SQLException e) {
            caught = true;
            assertTrue(lowerMessage(e), isDuplicateKeyMessage(e));
        }

        assertTrue("Expected SQLException for duplicate primary key in batch", caught);
        assertTrue(jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?", new Object[] { baseId() + 31, baseId() + 33 }) >= 0);
    }

    @Test
    @Capability(CapabilityId.JDBC_BATCH_LARGE_INSERT)
    public void jdbcBatchLargeInsertManyNamedRows() throws SQLException {
        Map<String, Object>[] args = new Map[200];
        for (int i = 0; i < args.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", baseId() + 1000 + i);
            row.put("val", "NXN-Batch-Large-" + i);
            args[i] = row;
        }

        int[] rows = jdbcTemplate.executeBatch("INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)", args);

        assertEquals(200, rows.length);
        assertEquals(200, (int) jdbcTemplate.queryForInt("SELECT COUNT(*) FROM basic_types_test WHERE id >= ? AND id < ?", new Object[] { baseId() + 1000, baseId() + 1200 }));
        assertEquals("NXN-Batch-Large-199", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = ?", new Object[] { baseId() + 1199 }));
    }
}
