/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Map;
import java.util.Locale;
import org.junit.Test;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import static org.junit.Assert.*;

/** JDBC result-column matching, independent of Lambda field generation. */
@NxnContract
public abstract class JdbcColumnMappingCase extends AbstractNxnContractTest {
    protected void seedColumnValue() throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (?, ?, ?)",
                new Object[] { 932001, "NXN-Column", 21 });
    }

    protected String columnQuery() {
        return "SELECT name AS " + profile().leftQualifier() + "NXN_VALUE" + profile().rightQualifier()
                + " FROM user_info WHERE id = 932001";
    }

    protected String resultColumn() {
        return "NXN_VALUE";
    }

    protected Class<? extends ColumnValue> strictType() {
        return StrictValue.class;
    }

    protected Class<? extends ColumnValue> insensitiveType() {
        return InsensitiveValue.class;
    }

    @Test
    @Capability(CapabilityId.JDBC_COLUMN_STRICT_BEAN)
    public void strictBean_shouldRequireMatchingColumnCase() throws SQLException {
        seedColumnValue();
        ColumnValue strict = jdbcTemplate.queryForObject(columnQuery(), strictType());
        ColumnValue insensitive = jdbcTemplate.queryForObject(columnQuery(), insensitiveType());
        assertNotNull(strict);
        assertNotNull(insensitive);
        assertNull(strict.getValue());
        assertEquals("NXN-Column", insensitive.getValue());
    }

    @Test
    @Capability(CapabilityId.JDBC_COLUMN_INSENSITIVE_MAP)
    public void insensitiveMap_shouldAcceptDifferentColumnCase() throws SQLException {
        seedColumnValue();
        jdbcTemplate.setResultsCaseInsensitive(true);
        Map<String, Object> row = jdbcTemplate.queryForMap(columnQuery());
        String lower = resultColumn().toLowerCase(Locale.ROOT);
        assertEquals("NXN-Column", row.get(resultColumn()));
        assertEquals("NXN-Column", row.get(lower));
        assertEquals("NXN-Column", row.get(Character.toUpperCase(lower.charAt(0)) + lower.substring(1)));
    }

    @Test
    @Capability(CapabilityId.JDBC_COLUMN_SENSITIVE_MAP)
    public void sensitiveMap_shouldRequireExactColumnCase() throws SQLException {
        seedColumnValue();
        jdbcTemplate.setResultsCaseInsensitive(false);
        Map<String, Object> row = jdbcTemplate.queryForMap(columnQuery());
        String lower = resultColumn().toLowerCase(Locale.ROOT);
        assertEquals("NXN-Column", row.get(resultColumn()));
        assertNull(row.get(lower));
        assertNull(row.get(Character.toUpperCase(lower.charAt(0)) + lower.substring(1)));
    }

    public interface ColumnValue {
        String getValue();
    }

    @Table(caseInsensitive = false)
    public static class StrictValue implements ColumnValue {
        @Column("nxn_value")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Table(caseInsensitive = true)
    public static class InsensitiveValue extends StrictValue {
    }
}
