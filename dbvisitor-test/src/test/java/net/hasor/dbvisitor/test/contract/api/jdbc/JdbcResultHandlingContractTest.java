package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.extractor.BeanMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.MapMappingResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.PairsResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class JdbcResultHandlingContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 670000;
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_MAPPER_CUSTOM)
    public void rowMapper_shouldSupportCustomMappingAndDtoProjection() throws SQLException {
        seedUsers();

        RowMapper<UserNameAge> mapper = (rs, rowNum) -> new UserNameAge(rs.getString("name").toUpperCase() + ":" + rs.getInt("age"));

        List<UserNameAge> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 3 }, mapper);

        assertEquals(3, results.size());
        assertEquals("NXN-RESULT-1:21", results.get(0).nameAge);
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_MAPPER_BUILTIN)
    public void rowMapper_shouldSupportBuiltInMappers() throws SQLException {
        seedUsers();

        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT id, name, age FROM user_info WHERE id = ?", //
                new Object[] { baseId() + 2 }, new ColumnMapRowMapper());
        List<String> names = jdbcTemplate.queryForList("SELECT name FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 2 }, new SingleColumnRowMapper<>(String.class));
        List<UserInfo> beans = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id = ?", //
                new Object[] { baseId() + 3 }, new BeanMappingRowMapper<>(UserInfo.class));

        assertEquals(1, maps.size());
        assertEquals("NXN-Result-2", value(maps.get(0), "name"));
        assertEquals(2, names.size());
        assertEquals("NXN-Result-1", names.get(0));
        assertEquals(1, beans.size());
        assertEquals(Integer.valueOf(baseId() + 3), beans.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_MAP_AND_SCALAR)
    public void resultShortcuts_shouldReturnMapListAndScalarValues() throws SQLException {
        seedUsers();

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT id, name, age FROM user_info WHERE id = ?", new Object[] { baseId() + 4 });
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, name, age FROM user_info WHERE age BETWEEN ? AND ? ORDER BY id", new Object[] { 23, 26 });
        Integer age = jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?", new Object[] { baseId() + 5 }, Integer.class);
        List<Integer> ages = jdbcTemplate.queryForList("SELECT age FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", new Object[] { baseId() + 6, baseId() + 8 }, Integer.class);

        assertEquals(baseId() + 4, ((Number) value(row, "id")).intValue());
        assertEquals("NXN-Result-4", value(row, "name"));
        assertEquals(4, rows.size());
        assertEquals(Integer.valueOf(25), age);
        assertEquals(3, ages.size());
        assertEquals(Integer.valueOf(26), ages.get(0));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_CALLBACK)
    public void rowCallbackHandler_shouldStreamRows() throws SQLException {
        seedUsers();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        List<String> names = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            names.add(rs.getString("name"));
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        jdbcTemplate.query("SELECT * FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", new Object[] { baseId() + 1, baseId() + 5 }, handler);

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("NXN-Result-1", names.get(0));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_CUSTOM)
    public void resultSetExtractor_shouldSupportCustomAggregation() throws SQLException {
        seedUsers();

        ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
            Map<Integer, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
            return map;
        };

        Map<Integer, String> result = jdbcTemplate.query("SELECT id, name FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 3 }, extractor);

        assertEquals(3, result.size());
        assertEquals("NXN-Result-1", result.get(baseId() + 1));
        assertEquals("NXN-Result-3", result.get(baseId() + 3));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_BUILTIN)
    public void resultSetExtractor_shouldSupportBuiltInListExtractors() throws SQLException {
        seedUsers();

        List<Map<String, Object>> columnMaps = jdbcTemplate.query("SELECT id, name, age FROM user_info WHERE age > ? ORDER BY id", //
                new Object[] { 26 }, new ColumnMapResultSetExtractor());
        RowMapper<UserInfo> rowMapper = new BeanMappingRowMapper<>(UserInfo.class);
        List<UserInfo> mappedRows = jdbcTemplate.query("SELECT * FROM user_info WHERE id = ?", //
                new Object[] { baseId() + 6 }, new RowMapperResultSetExtractor<>(rowMapper));
        List<UserInfo> filteredRows = jdbcTemplate.query("SELECT * FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 1, baseId() + 10 }, new FilterResultSetExtractor<>(rowMapper, user -> user.getAge() > 24));

        assertFalse(columnMaps.isEmpty());
        assertEquals(1, mappedRows.size());
        assertEquals("NXN-Result-6", mappedRows.get(0).getName());
        assertEquals(6, filteredRows.size());
        assertTrue(filteredRows.stream().allMatch(user -> user.getAge() > 24));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_PAIRS)
    public void resultSetExtractor_shouldSupportPairsExtractor() throws SQLException {
        seedUsers();

        ResultSetExtractor<Map<Integer, String>> extractor = new PairsResultSetExtractor<>(TypeHandlerRegistry.DEFAULT, Integer.class, String.class);
        Map<Integer, String> result = jdbcTemplate.query("SELECT id, name FROM user_info WHERE id BETWEEN ? AND ? ORDER BY id", //
                new Object[] { baseId() + 7, baseId() + 9 }, extractor);

        assertEquals(3, result.size());
        assertEquals("NXN-Result-7", result.get(baseId() + 7));
        assertEquals("NXN-Result-9", result.get(baseId() + 9));
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_EXTRACTOR_MAPPING)
    public void resultSetExtractor_shouldSupportMappingExtractors() throws SQLException {
        seedUsers();

        List<UserInfo> beans = jdbcTemplate.query("SELECT * FROM user_info WHERE age = ?", //
                new Object[] { 25 }, new BeanMappingResultSetExtractor<>(UserInfo.class, MappingRegistry.DEFAULT));
        List<Map<String, Object>> maps = jdbcTemplate.query("SELECT * FROM user_info WHERE id = ?", //
                new Object[] { baseId() + 10 }, new MapMappingResultSetExtractor(UserInfo.class, MappingRegistry.DEFAULT));

        assertEquals(1, beans.size());
        assertEquals("NXN-Result-5", beans.get(0).getName());
        assertEquals(1, maps.size());
        assertEquals(baseId() + 10, ((Number) value(maps.get(0), "id")).intValue());
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_IGNORE_FIELD_MAPPING)
    public void beanMapping_shouldHonorIgnoredFields() throws SQLException {
        int id = baseId() + 31;
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, "NXN-Ignore-Result", 30, "ignored-result@nxn.test", new Date() });

        IgnoredEmailUser user = jdbcTemplate.queryForObject("SELECT * FROM user_info WHERE id = ?", //
                new Object[] { id }, IgnoredEmailUser.class);

        assertNotNull(user);
        assertEquals(Integer.valueOf(id), user.getId());
        assertEquals("NXN-Ignore-Result", user.getName());
        assertEquals(Integer.valueOf(30), user.getAge());
        assertEquals(null, user.getEmail());
    }

    private void seedUsers() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { baseId() + i, "NXN-Result-" + i, 20 + i, "nxn-result-" + i + "@test.com", new Date() });
        }
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

    private static class UserNameAge {
        private final String nameAge;

        private UserNameAge(String nameAge) {
            this.nameAge = nameAge;
        }
    }
}
