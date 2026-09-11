/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.sql.ResultSetMetaData;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for JdbcColumn — covers construction, metadata, equals, hashCode, toString and validation. */
public class JdbcColumnTest {

    @Test
    public void basic_constructor() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertEquals("id", c.name);
        assertEquals("int", c.type);
        assertEquals("tbl", c.table);
        assertEquals("cat", c.catalog);
        assertEquals("sch", c.schema);
        assertEquals(ResultSetMetaData.columnNullableUnknown, c.nullable);
        assertFalse(c.autoIncrement);
        assertEquals(AdapterType.Array, c.elementType);
    }

    @Test
    public void only_full_constructor_is_available() throws Exception {
        assertEquals(1, JdbcColumn.class.getConstructors().length);
        assertNotNull(JdbcColumn.class.getConstructor(String.class, String.class, String.class, String.class, String.class, int.class, boolean.class, String.class));
    }

    @Test
    public void explicit_metadata_is_preserved() {
        JdbcColumn c = new JdbcColumn("values", AdapterType.Array, "tbl", "cat", "sch", ResultSetMetaData.columnNullable, true, AdapterType.Float);
        assertEquals(ResultSetMetaData.columnNullable, c.nullable);
        assertTrue(c.autoIncrement);
        assertEquals(AdapterType.Float, c.elementType);
    }

    @Test
    public void all_jdbc_nullable_states_are_supported() {
        for (int nullable : new int[] { ResultSetMetaData.columnNoNulls, ResultSetMetaData.columnNullable, ResultSetMetaData.columnNullableUnknown }) {
            JdbcColumn c = new JdbcColumn("id", AdapterType.Long, "tbl", "cat", "sch", nullable, false, AdapterType.Array);
            assertEquals(nullable, c.nullable);
        }
    }

    @Test
    public void invalid_nullable_state_throws() {
        for (int nullable : new int[] { ResultSetMetaData.columnNoNulls - 1, ResultSetMetaData.columnNullableUnknown + 1 }) {
            try {
                new JdbcColumn("id", AdapterType.Long, "tbl", "cat", "sch", nullable, false, AdapterType.Array);
                fail("Invalid nullable metadata must be rejected: " + nullable);
            } catch (IllegalArgumentException expected) {
                assertEquals("Invalid nullable metadata: " + nullable, expected.getMessage());
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void null_element_type_throws() {
        new JdbcColumn("id", AdapterType.Long, "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, null);
    }

    @Test
    public void metadata_participates_in_equality() {
        JdbcColumn column = new JdbcColumn("values", AdapterType.Array, "tbl", "cat", "sch", ResultSetMetaData.columnNullable, true, AdapterType.Float);
        JdbcColumn same = new JdbcColumn("values", AdapterType.Array, "tbl", "cat", "sch", ResultSetMetaData.columnNullable, true, AdapterType.Float);
        assertEquals(column, same);
        assertEquals(column.hashCode(), same.hashCode());
        assertNotEquals(column, new JdbcColumn("values", AdapterType.Array, "tbl", "cat", "sch", ResultSetMetaData.columnNoNulls, true, AdapterType.Float));
        assertNotEquals(column, new JdbcColumn("values", AdapterType.Array, "tbl", "cat", "sch", ResultSetMetaData.columnNullable, false, AdapterType.Float));
        assertNotEquals(column, new JdbcColumn("values", AdapterType.Array, "tbl", "cat", "sch", ResultSetMetaData.columnNullable, true, AdapterType.Double));
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_name_throws() {
        new JdbcColumn(null, "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_type_throws() {
        new JdbcColumn("id", null, "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_table_throws() {
        new JdbcColumn("id", "int", null, "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_catalog_throws() {
        new JdbcColumn("id", "int", "tbl", null, "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_schema_throws() {
        new JdbcColumn("id", "int", "tbl", "cat", null, ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    }

    @Test
    public void toString_withTable() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        String s = c.toString();
        assertNotNull(s);
        assertTrue(s.contains("id"));
    }

    @Test
    public void toString_emptyTable() {
        JdbcColumn c = new JdbcColumn("id", "int", "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        String s = c.toString();
        assertNotNull(s);
    }

    @Test
    public void toString_withCatalog() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        String s = c.toString();
        assertTrue(s.contains("cat"));
    }

    @Test
    public void toString_withSchema() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        String s = c.toString();
        assertTrue(s.contains("sch"));
    }

    @Test
    public void equals_sameObject() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertEquals(c, c);
    }

    @Test
    public void equals_equalObjects() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        JdbcColumn c2 = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void equals_null() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertNotEquals(null, c);
    }

    @Test
    public void equals_differentClass() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertNotEquals("not a column", c);
    }

    @Test
    public void equals_differentName() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        JdbcColumn c2 = new JdbcColumn("name", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertNotEquals(c1, c2);
    }

    @Test
    public void equals_differentType() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        JdbcColumn c2 = new JdbcColumn("id", "varchar", "tbl", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertNotEquals(c1, c2);
    }

    @Test
    public void equals_differentTable() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl1", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        JdbcColumn c2 = new JdbcColumn("id", "int", "tbl2", "cat", "sch", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
        assertNotEquals(c1, c2);
    }
}
