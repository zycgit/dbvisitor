package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class JdbcQueryTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 630000;
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_MAP)
    public void jdbcQueryForMapOneRowAsMap() throws SQLException {
        seedUsers();

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT id, name, age FROM user_info WHERE id = ?", new Object[] { baseId() + 1 });

        assertEquals(baseId() + 1, ((Number) value(row, "id")).intValue());
        assertEquals("NXN-JDBC-Query-1", value(row, "name"));
        assertEquals(61, ((Number) value(row, "age")).intValue());
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_LIST)
    public void jdbcQueryForListRowsAsMapsAndScalars() throws SQLException {
        seedUsers();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, name, age FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 3 });
        List<String> names = jdbcTemplate.queryForList("SELECT name FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 3 }, String.class);

        assertEquals(3, rows.size());
        assertEquals(3, names.size());
        assertEquals("NXN-JDBC-Query-1", names.get(0));
        assertEquals("NXN-JDBC-Query-3", names.get(2));
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_BEAN)
    public void jdbcQueryForListBeans() throws SQLException {
        seedUsers();

        List<UserInfo> rows = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 3 }, UserInfo.class);

        assertEquals(3, rows.size());
        assertEquals(Integer.valueOf(baseId() + 2), rows.get(1).getId());
        assertEquals("NXN-JDBC-Query-2", rows.get(1).getName());
        assertEquals(Integer.valueOf(62), rows.get(1).getAge());
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_SCALAR)
    public void jdbcQueryForObjectAndCountScalarValues() throws SQLException {
        seedUsers();

        Integer age = jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { baseId() + 3 }, Integer.class);
        int count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM user_info WHERE id BETWEEN ? AND ?", //
                new Object[] { baseId() + 1, baseId() + 3 });

        assertEquals(Integer.valueOf(63), age);
        assertEquals(3, count);
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_PAIRS)
    public void jdbcQueryForPairsTypedKeyValueMaps() throws SQLException {
        seedUsers();

        Map<Integer, String> idToName = jdbcTemplate.queryForPairs(//
                "SELECT id, name FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                Integer.class, String.class, new Object[] { baseId() + 1, baseId() + 3 });
        Map<String, Integer> nameToAge = jdbcTemplate.queryForPairs(//
                "SELECT name, age FROM user_info WHERE id >= :minId AND id <= :maxId ORDER BY id", //
                String.class, Integer.class, params(baseId() + 1, baseId() + 3));
        Map<Long, Date> idToDate = jdbcTemplate.queryForPairs(//
                "SELECT " + profile().castToBigInt("id") + ", create_time FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                Long.class, Date.class, new Object[] { baseId() + 1, baseId() + 2 });

        assertEquals(3, idToName.size());
        assertEquals("NXN-JDBC-Query-1", idToName.get(baseId() + 1));
        assertEquals(Integer.valueOf(62), nameToAge.get("NXN-JDBC-Query-2"));
        assertEquals(2, idToDate.size());
        assertNotNull(idToDate.get((long) baseId() + 1));
        assertNotNull(idToDate.get((long) baseId() + 2));
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_SCALAR_SHORTCUTS)
    public void jdbcScalarShortcutsLongIntAndStringQueries() throws SQLException {
        seedUsers();

        Long count = jdbcTemplate.queryForLong("SELECT COUNT(*) FROM user_info WHERE id BETWEEN ? AND ?", //
                new Object[] { baseId() + 1, baseId() + 3 });
        Long ageSum = jdbcTemplate.queryForLong("SELECT SUM(age) FROM user_info WHERE id >= :minId AND id <= :maxId", //
                params(baseId() + 1, baseId() + 3));
        Integer minAge = jdbcTemplate.queryForInt("SELECT MIN(age) FROM user_info WHERE id BETWEEN ? AND ?", //
                new Object[] { baseId() + 1, baseId() + 3 });
        Integer maxAge = jdbcTemplate.queryForInt("SELECT MAX(age) FROM user_info WHERE id >= :minId AND id <= :maxId", //
                params(baseId() + 1, baseId() + 3));
        String name = jdbcTemplate.queryForString("SELECT name FROM user_info WHERE id = ?", new Object[] { baseId() + 2 });
        String maxName = jdbcTemplate.queryForString("SELECT MAX(name) FROM user_info WHERE id BETWEEN ? AND ?", //
                new Object[] { baseId() + 1, baseId() + 3 });

        assertEquals(Long.valueOf(3), count);
        assertEquals(Long.valueOf(186), ageSum);
        assertEquals(Integer.valueOf(61), minAge);
        assertEquals(Integer.valueOf(63), maxAge);
        assertEquals("NXN-JDBC-Query-2", name);
        assertEquals("NXN-JDBC-Query-3", maxName);
    }

    private void seedUsers() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + i, "NXN-JDBC-Query-" + i, 60 + i, "nxn-jdbc-query-" + i + "@test.com", new Date() });
        }
    }

    private Map<String, Object> params(int minId, int maxId) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("minId", minId);
        params.put("maxId", maxId);
        return params;
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
