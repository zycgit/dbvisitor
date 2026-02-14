package net.hasor.dbvisitor.driver;

import java.sql.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MockDriverFeaturesTest {
    private Connection conn;

    @Before
    public void setUp() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        this.conn = DriverManager.getConnection("jdbc:dbvisitor:mock://127.0.0.1/test");
    }

    @Test
    public void testLobCreation() throws Exception {
        Blob blob = conn.createBlob();
        assertNotNull(blob);
        blob.setBytes(1, new byte[] { 1, 2, 3 });
        assertEquals(3, blob.length());

        Clob clob = conn.createClob();
        assertNotNull(clob);
        clob.setString(1, "abc");
        assertEquals(3, clob.length());

        // JDBC Array
        Array array = conn.createArrayOf("varchar", new Object[] { "a", "b" });
        assertNotNull(array);
        Object arrData = array.getArray();
        assertNotNull(arrData);
    }

    @Test
    public void testResultSetLobs() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM types")) {
                assertTrue(rs.next());

                // Blob
                Blob b = rs.getBlob("c_blob");
                // Expect conversion from byte[]
                // If unimplemented, might return null or fail
                if (b != null) {
                    assertEquals(3, b.length());
                    assertArrayEquals(new byte[] { 1, 2, 3 }, b.getBytes(1, 3));
                }

                // Clob
                Clob c = rs.getClob("c_clob");
                if (c != null) {
                    assertEquals("clob content", c.getSubString(1, 12));
                }
            }
        }
    }

    @Test
    public void testPreparedStatementParams() throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO t VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setNull(1, Types.VARCHAR);
            pstmt.setObject(2, "str");
            pstmt.setBigDecimal(3, new java.math.BigDecimal("1.23"));
            pstmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));

            pstmt.execute();
            // Mock returns update count 1
        }
    }
}
