package net.hasor.dbvisitor.driver;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for JdbcColumn — covers constructors, equals, hashCode, toString, null validation. */
public class JdbcColumnTest {

    @Test
    public void basic_constructor() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        assertEquals("id", c.name);
        assertEquals("int", c.type);
        assertEquals("tbl", c.table);
        assertEquals("cat", c.catalog);
        assertEquals("sch", c.schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_name_throws() {
        new JdbcColumn(null, "int", "tbl", "cat", "sch");
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_type_throws() {
        new JdbcColumn("id", null, "tbl", "cat", "sch");
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_table_throws() {
        new JdbcColumn("id", "int", null, "cat", "sch");
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_catalog_throws() {
        new JdbcColumn("id", "int", "tbl", null, "sch");
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_schema_throws() {
        new JdbcColumn("id", "int", "tbl", "cat", null);
    }

    @Test
    public void toString_withTable() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "", "");
        String s = c.toString();
        assertNotNull(s);
        assertTrue(s.contains("id"));
    }

    @Test
    public void toString_emptyTable() {
        JdbcColumn c = new JdbcColumn("id", "int", "", "", "");
        String s = c.toString();
        assertNotNull(s);
    }

    @Test
    public void toString_withCatalog() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "");
        String s = c.toString();
        assertTrue(s.contains("cat"));
    }

    @Test
    public void toString_withSchema() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "", "sch");
        String s = c.toString();
        assertTrue(s.contains("sch"));
    }

    @Test
    public void equals_sameObject() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        assertTrue(c.equals(c));
    }

    @Test
    public void equals_equalObjects() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        JdbcColumn c2 = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        assertTrue(c1.equals(c2));
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void equals_null() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        assertFalse(c.equals(null));
    }

    @Test
    public void equals_differentClass() {
        JdbcColumn c = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        assertFalse(c.equals("not a column"));
    }

    @Test
    public void equals_differentName() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        JdbcColumn c2 = new JdbcColumn("name", "int", "tbl", "cat", "sch");
        assertFalse(c1.equals(c2));
    }

    @Test
    public void equals_differentType() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl", "cat", "sch");
        JdbcColumn c2 = new JdbcColumn("id", "varchar", "tbl", "cat", "sch");
        assertFalse(c1.equals(c2));
    }

    @Test
    public void equals_differentTable() {
        JdbcColumn c1 = new JdbcColumn("id", "int", "tbl1", "cat", "sch");
        JdbcColumn c2 = new JdbcColumn("id", "int", "tbl2", "cat", "sch");
        assertFalse(c1.equals(c2));
    }
}
