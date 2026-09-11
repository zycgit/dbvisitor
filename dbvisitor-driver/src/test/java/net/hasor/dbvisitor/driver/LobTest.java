/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import net.hasor.dbvisitor.driver.lob.JdbcBob;
import net.hasor.dbvisitor.driver.lob.JdbcCob;
import org.junit.Test;
import static org.junit.Assert.*;

/** Comprehensive tests for JdbcBob (Blob) and JdbcCob (Clob/NClob). */
public class LobTest {

    @Test
    public void blobFreeDoesNotBreakLateWriterCloseOrRestoreData() throws Exception {
        JdbcBob blob = new JdbcBob(new byte[] { 1, 2, 3 });
        OutputStream writer = blob.setBinaryStream(2);
        blob.free();
        writer.write(9);
        writer.close();
        writer.close();
        blob.free();
        assertThrows(SQLException.class, blob::length);
        assertThrows(SQLException.class, blob::getBinaryStream);
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[] { 4 }));
    }

    @Test
    public void blobRangesRejectNegativeAndOverflowWithoutChangingData() throws Exception {
        JdbcBob blob = new JdbcBob(new byte[] { 1, 2, 3 });
        assertThrows(SQLException.class, () -> blob.getBytes(1, -1));
        assertThrows(SQLException.class, () -> blob.getBytes(3, 2));
        assertThrows(SQLException.class, () -> blob.getBinaryStream(1, -1));
        assertThrows(SQLException.class, () -> blob.getBinaryStream(1, Long.MIN_VALUE));
        assertThrows(SQLException.class, () -> blob.getBinaryStream(4, 1));
        assertThrows(SQLException.class, () -> blob.getBinaryStream(2, Long.MAX_VALUE));
        assertThrows(SQLException.class, () -> blob.getBinaryStream(1, 4294967296L));
        for (long pos : new long[] { 0, -1, 5, 4294967297L, Long.MIN_VALUE, Long.MAX_VALUE }) {
            assertThrows(SQLException.class, () -> blob.getBytes(pos, 0));
            assertThrows(SQLException.class, () -> blob.getBinaryStream(pos, 0));
            assertThrows(SQLException.class, () -> blob.setBinaryStream(pos));
            assertThrows(SQLException.class, () -> blob.setBytes(pos, new byte[] { 8 }));
        }
        assertThrows(SQLException.class, () -> blob.setBytes(1, null));
        assertThrows(SQLException.class, () -> blob.setBytes(1, null, 0, 0));
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[2], -1, 1));
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[2], 0, -1));
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[2], 2, 1));
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[2], 3, 0));
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[2], 1, Integer.MAX_VALUE));
        assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[2], Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0, blob.setBytes(1, new byte[2], 2, 0));
        assertEquals(0, blob.setBytes(4, new byte[2], 2, 0));
        assertArrayEquals(new byte[] { 1, 2, 3 }, blob.getBytes(1, 3));
        assertArrayEquals(new byte[0], blob.getBytes(4, 0));
        try (InputStream empty = blob.getBinaryStream(4, 0)) {
            assertEquals(-1, empty.read());
        }
        assertEquals(2, blob.setBytes(4, new byte[] { 4, 5 }));
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, blob.getBytes(1, 5));
    }

    @Test
    public void clobRangesRejectNegativeAndOverflowWithoutChangingData() throws Exception {
        JdbcCob clob = new JdbcCob("abc");
        assertThrows(SQLException.class, () -> clob.getSubString(1, -1));
        assertThrows(SQLException.class, () -> clob.getCharacterStream(1, -1));
        assertThrows(SQLException.class, () -> clob.getCharacterStream(2, Long.MAX_VALUE));
        assertThrows(SQLException.class, () -> clob.getCharacterStream(1, 4294967296L));
        assertThrows(SQLException.class, () -> clob.truncate(-1));
        assertThrows(SQLException.class, () -> clob.truncate(Long.MAX_VALUE));
        for (long pos : new long[] { 0, -1, 5, 4294967297L, Long.MAX_VALUE }) {
            assertThrows(SQLException.class, () -> clob.getSubString(pos, 0));
            assertThrows(SQLException.class, () -> clob.getCharacterStream(pos, 0));
            assertThrows(SQLException.class, () -> clob.setCharacterStream(pos));
            assertThrows(SQLException.class, () -> clob.setAsciiStream(pos));
            assertThrows(SQLException.class, () -> clob.setString(pos, "z"));
            assertThrows(SQLException.class, () -> clob.setString(pos, "z", 0, 1));
        }
        assertThrows(SQLException.class, () -> clob.setString(1, null));
        assertThrows(SQLException.class, () -> clob.setString(1, null, 0, 0));
        assertThrows(SQLException.class, () -> clob.setString(1, "xy", -1, 1));
        assertThrows(SQLException.class, () -> clob.setString(1, "xy", 0, -1));
        assertThrows(SQLException.class, () -> clob.setString(1, "xy", 2, 1));
        assertThrows(SQLException.class, () -> clob.setString(1, "xy", 3, 0));
        assertThrows(SQLException.class, () -> clob.setString(1, "xy", 1, Integer.MAX_VALUE));
        assertThrows(SQLException.class, () -> clob.setString(1, "xy", Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0, clob.setString(1, "xy", 2, 0));
        assertEquals("abc", clob.getSubString(1, 3));
        assertEquals("", clob.getSubString(4, 0));
        try (Reader empty = clob.getCharacterStream(4, 0)) {
            assertEquals(-1, empty.read());
        }
        assertEquals(2, clob.setString(4, "de"));
        assertEquals("abcde", clob.getSubString(1, 5));
        clob.truncate(0);
        assertEquals(0, clob.length());
    }

    // ==================== JdbcBob ====================
    @Test
    public void bob_length() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 1, 2, 3, 4, 5 });
        assertEquals(5, b.length());
    }

    @Test
    public void bob_getBytes() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 10, 20, 30, 40, 50 });
        byte[] sub = b.getBytes(1, 3);
        assertArrayEquals(new byte[] { 10, 20, 30 }, sub);
    }

    @Test
    public void bob_getBytes_middle() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 10, 20, 30, 40, 50 });
        byte[] sub = b.getBytes(2, 2);
        assertArrayEquals(new byte[] { 20, 30 }, sub);
    }

    @Test
    public void bob_getBinaryStream() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 1, 2, 3 });
        InputStream is = b.getBinaryStream();
        assertNotNull(is);
        assertEquals(1, is.read());
        assertEquals(2, is.read());
        assertEquals(3, is.read());
        assertEquals(-1, is.read());
    }

    @Test
    public void bob_getBinaryStream_offset() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 10, 20, 30, 40 });
        InputStream is = b.getBinaryStream(2, 2);
        assertEquals(20, is.read());
        assertEquals(30, is.read());
        assertEquals(-1, is.read());
    }

    @Test
    public void bob_setBytes() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 0, 0, 0 });
        int written = b.setBytes(1, new byte[] { 10, 20, 30 });
        assertEquals(3, written);
        assertArrayEquals(new byte[] { 10, 20, 30 }, b.getBytes(1, 3));
    }

    @Test
    public void bob_setBytes_offset() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 0, 0, 0, 0 });
        int written = b.setBytes(2, new byte[] { 99, 88, 77, 66 }, 1, 2);
        assertEquals(2, written);
    }

    @Test
    public void bob_setBinaryStream() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 0, 0, 0 });
        OutputStream os = b.setBinaryStream(1);
        assertNotNull(os);
        os.write(new byte[] { 10, 20 });
        os.close();
        assertEquals(10, b.getBytes(1, 1)[0]);
    }

    @Test
    public void bob_truncate() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 1, 2, 3, 4, 5 });
        b.truncate(3);
        assertEquals(3, b.length());
    }

    @Test
    public void bob_free() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 1, 2, 3 });
        b.free();
        try {
            b.length();
            fail();
        } catch (Exception expected) {
        }
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void bob_position_unsupported() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 1, 2, 3 });
        b.position(new byte[] { 1 }, 1);
    }

    @Test(expected = SQLException.class)
    public void bob_position_blob_unsupported() throws Exception {
        JdbcBob b = new JdbcBob(new byte[] { 1, 2, 3 });
        b.position(new JdbcBob(new byte[] { 1 }), 1);
    }

    // ==================== JdbcCob ====================
    @Test
    public void cob_length() throws Exception {
        JdbcCob c = new JdbcCob("hello world");
        assertEquals(11, c.length());
    }

    @Test
    public void cob_getSubString() throws Exception {
        JdbcCob c = new JdbcCob("hello world");
        assertEquals("hello", c.getSubString(1, 5));
    }

    @Test
    public void cob_getSubString_middle() throws Exception {
        JdbcCob c = new JdbcCob("hello world");
        assertEquals("world", c.getSubString(7, 5));
    }

    @Test
    public void cob_getCharacterStream() throws Exception {
        JdbcCob c = new JdbcCob("abc");
        Reader r = c.getCharacterStream();
        assertEquals('a', r.read());
        assertEquals('b', r.read());
        assertEquals('c', r.read());
        assertEquals(-1, r.read());
    }

    @Test
    public void cob_getCharacterStream_offset() throws Exception {
        JdbcCob c = new JdbcCob("abcde");
        Reader r = c.getCharacterStream(2, 3);
        assertEquals('b', r.read());
        assertEquals('c', r.read());
        assertEquals('d', r.read());
        assertEquals(-1, r.read());
    }

    @Test
    public void cob_getAsciiStream() throws Exception {
        JdbcCob c = new JdbcCob("AB");
        InputStream is = c.getAsciiStream();
        assertEquals(65, is.read());
        assertEquals(66, is.read());
    }

    @Test
    public void cob_setString() throws Exception {
        JdbcCob c = new JdbcCob("hello");
        int written = c.setString(1, "world");
        assertEquals(5, written);
        assertEquals("world", c.getSubString(1, 5));
    }

    @Test
    public void cob_setString_middle() throws Exception {
        JdbcCob c = new JdbcCob("hello world");
        int written = c.setString(7, "java!");
        assertEquals(5, written);
        assertEquals("hello java!", c.getSubString(1, (int) c.length()));
    }

    @Test
    public void cob_setString_offset() throws Exception {
        JdbcCob c = new JdbcCob("aaaa");
        int written = c.setString(1, "xyzw", 1, 2);
        assertEquals(2, written);
    }

    @Test
    public void cob_setCharacterStream() throws Exception {
        JdbcCob c = new JdbcCob("hello");
        Writer w = c.setCharacterStream(1);
        assertNotNull(w);
        w.write("AB");
        w.close();
    }

    @Test
    public void cob_setAsciiStream() throws Exception {
        JdbcCob c = new JdbcCob("hello");
        OutputStream os = c.setAsciiStream(1);
        assertNotNull(os);
        os.write(new byte[] { 65, 66 });
        os.close();
    }

    @Test
    public void cob_position_string() throws Exception {
        JdbcCob c = new JdbcCob("hello world hello");
        long pos = c.position("world", 1);
        assertEquals(7, pos);
    }

    @Test
    public void cob_position_clob() throws Exception {
        JdbcCob c = new JdbcCob("hello world");
        JdbcCob search = new JdbcCob("world");
        long pos = c.position(search, 1);
        assertEquals(7, pos);
    }

    @Test
    public void cob_truncate() throws Exception {
        JdbcCob c = new JdbcCob("hello world");
        c.truncate(5);
        assertEquals(5, c.length());
        assertEquals("hello", c.getSubString(1, 5));
    }

    @Test
    public void cob_free() throws Exception {
        JdbcCob c = new JdbcCob("hello");
        c.free();
        assertThrows(SQLException.class, c::length);
    }

    // Test NClob interface
    @Test
    public void cob_as_nclob() throws Exception {
        NClob nc = new JdbcCob("nclob data");
        assertEquals(10, nc.length());
    }
}
