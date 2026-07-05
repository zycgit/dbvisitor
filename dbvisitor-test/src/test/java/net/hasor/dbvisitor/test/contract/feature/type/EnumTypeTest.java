package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnum;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfCode;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfValue;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class EnumTypeTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 660000;
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_NAME)
    public void enumNameFromStringCol() throws SQLException {
        int id = baseId() + 1;
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)", new Object[] { id, StatusEnum.ACTIVE.name() });

        StatusEnum loaded = jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnum.class);

        assertEquals(StatusEnum.ACTIVE, loaded);
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_CODE)
    public void enumCodeFromCustomStringCode() throws SQLException {
        int id = baseId() + 2;
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)", new Object[] { id, "inactive" });

        StatusEnumOfCode loaded = jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnumOfCode.class);

        assertEquals(StatusEnumOfCode.INACTIVE, loaded);
        assertEquals("inactive", loaded.codeName());
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_VALUE)
    public void enumValueFromIntegerCode() throws SQLException {
        int id = baseId() + 3;
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_code) VALUES (?, ?)", new Object[] { id, -1 });

        StatusEnumOfValue loaded = jdbcTemplate.queryForObject("SELECT status_code FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnumOfValue.class);

        assertEquals(StatusEnumOfValue.DELETED, loaded);
        assertEquals(-1, loaded.codeValue());
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_NULL)
    public void enumValuesNullForNullCols() throws SQLException {
        int id = baseId() + 4;
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_string, status_ordinal, status_code) VALUES (?, ?, ?, ?)", //
                new Object[] { id, null, null, null });

        assertNull(jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnum.class));
        assertNull(jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnumOfCode.class));
        assertNull(jdbcTemplate.queryForObject("SELECT status_code FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnumOfValue.class));
        assertNull(jdbcTemplate.queryForObject("SELECT status_ordinal FROM enum_types_explicit_test WHERE id = ?", new Object[] { id }, StatusEnumOfValue.class));
    }

    @Test
    @Capability(CapabilityId.TYPE_ENUM_INVALID)
    public void enumInvalidValuesInvalidMapBehavior() throws SQLException {
        int invalidNameId = baseId() + 5;
        int invalidValueId = baseId() + 6;
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)", new Object[] { invalidNameId, "UNKNOWN" });

        try {
            jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { invalidNameId }, StatusEnum.class);
            fail("Should throw exception for invalid enum name.");
        } catch (Exception e) {
            assertExceptionMentions(e, "UNKNOWN");
        }

        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_code) VALUES (?, ?)", new Object[] { invalidValueId, 999 });

        try {
            StatusEnumOfValue result = jdbcTemplate.queryForObject("SELECT status_code FROM enum_types_explicit_test WHERE id = ?", new Object[] { invalidValueId }, StatusEnumOfValue.class);
            assertNull(result);
        } catch (Exception e) {
            assertExceptionMentions(e, "999");
        }
    }

    private void assertExceptionMentions(Exception e, String expected) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(expected)) {
                return;
            }
            current = current.getCause();
        }
        fail("Expected exception message to mention: " + expected);
    }
}
