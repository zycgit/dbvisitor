package net.hasor.dbvisitor.test.contract.feature.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBasicTypeJdbcContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 650000;
    }

    @Test
    @Capability(CapabilityId.TYPE_BASIC_NUMERIC)
    public void basicNumericTypes_shouldRoundTripThroughJdbcTemplate() throws SQLException {
        int id = baseId() + 1;
        jdbcTemplate.executeUpdate(//
                "INSERT INTO basic_types_test (id, byte_value, short_value, int_value, long_value, float_value, double_value, decimal_value, big_int_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", //
                new Object[] { id, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, 3.14f, 2.718281828d, new BigDecimal("12345.67"), new BigInteger("9223372036854775807") });

        BasicTypesModel loaded = jdbcTemplate.queryForObject("SELECT * FROM basic_types_test WHERE id = ?", new Object[] { id }, BasicTypesModel.class);

        assertNotNull(loaded);
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), loaded.getByteValue());
        assertEquals(Short.valueOf(Short.MAX_VALUE), loaded.getShortValue());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), loaded.getIntValue());
        assertEquals(Long.valueOf(Long.MAX_VALUE), loaded.getLongValue());
        assertEquals(3.14f, loaded.getFloatValue(), 0.001f);
        assertEquals(2.718281828d, loaded.getDoubleValue(), 0.000001d);
        assertEquals(0, new BigDecimal("12345.67").compareTo(loaded.getDecimalValue()));
        assertEquals(new BigInteger("9223372036854775807"), loaded.getBigIntValue());
    }

    @Test
    @Capability(CapabilityId.TYPE_BASIC_BOOLEAN)
    public void basicBooleanTypes_shouldRoundTripTrueFalseAndNull() throws SQLException {
        int trueId = baseId() + 2;
        int nullId = baseId() + 3;
        String insertSql = "INSERT INTO basic_types_test (id, bool_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { trueId, true });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { nullId, null });

        Boolean trueValue = jdbcTemplate.queryForObject("SELECT bool_value FROM basic_types_test WHERE id = ?", new Object[] { trueId }, Boolean.class);
        Boolean nullValue = jdbcTemplate.queryForObject("SELECT bool_value FROM basic_types_test WHERE id = ?", new Object[] { nullId }, Boolean.class);

        assertTrue(trueValue);
        assertNull(nullValue);
    }

    @Test
    @Capability(CapabilityId.TYPE_BASIC_BIT_CAST_NULL)
    public void explicitBitAndBooleanTypes_shouldExposeDatabaseValues() throws SQLException {
        requiresNxnFeature(FeatureId.BIT_CAST_NULL_VALUE);

        int id = baseId() + 4;
        int nullId = baseId() + 5;
        String insertSql = isDataSource("mysql") || isOracle() //
                ? "INSERT INTO basic_types_explicit_test (id, bool_bit, bool_boolean) VALUES (?, ?, ?)" //
                : "INSERT INTO basic_types_explicit_test (id, bool_bit, bool_boolean) VALUES (?, CAST(? AS BIT), ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { id, 1, isOracle() ? 0 : false });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { nullId, null, null });

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?", new Object[] { id });
        Map<String, Object> nullRow = jdbcTemplate.queryForMap("SELECT bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?", new Object[] { nullId });

        assertTrue(booleanValue(value(row, "bool_bit")));
        assertFalse(booleanValue(value(row, "bool_boolean")));
        assertNull(value(nullRow, "bool_bit"));
        assertNull(value(nullRow, "bool_boolean"));
    }

    @Test
    @Capability(CapabilityId.TYPE_BASIC_CHARACTER)
    public void basicCharacterTypes_shouldRoundTripAsciiUnicodeEmptyAndNull() throws SQLException {
        int id = baseId() + 6;
        int emptyId = baseId() + 7;
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_explicit_test (id, char_value, varchar_value, nvarchar_value) VALUES (?, ?, ?, ?)", //
                new Object[] { id, 'A', "Hello World!", "你好世界！🌍" });
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_explicit_test (id, char_value, varchar_value, nvarchar_value) VALUES (?, ?, ?, ?)", //
                new Object[] { emptyId, null, "", null });

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT char_value, varchar_value, nvarchar_value FROM basic_types_explicit_test WHERE id = ?", new Object[] { id });
        Map<String, Object> emptyRow = jdbcTemplate.queryForMap("SELECT char_value, varchar_value, nvarchar_value FROM basic_types_explicit_test WHERE id = ?", new Object[] { emptyId });

        assertEquals('A', ((String) value(row, "char_value")).charAt(0));
        assertEquals("Hello World!", value(row, "varchar_value"));
        assertEquals("你好世界！🌍", value(row, "nvarchar_value"));
        assertNull(value(emptyRow, "char_value"));
        if (isOracle()) {
            assertNull(value(emptyRow, "varchar_value"));
        } else {
            assertEquals("", value(emptyRow, "varchar_value"));
        }
        assertNull(value(emptyRow, "nvarchar_value"));
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
