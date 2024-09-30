/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.types;
import net.hasor.cobble.codec.MD5;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.handler.BlobAsBytesTypeHandler;
import net.hasor.dbvisitor.types.handler.BlobAsBytesWrapTypeHandler;
import net.hasor.dbvisitor.types.handler.BlobAsInputStreamTypeHandler;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class BlobBytesTypeTest {
    private byte[] toPrimitive(Byte[] bytes) {
        byte[] dat = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            dat[i] = bytes[i];
        }
        return dat;
    }

    private Byte[] toWrapped(byte[] bytes) {
        Byte[] dat = new Byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            dat[i] = bytes[i];
        }
        return dat;
    }

    @Test
    public void testBlobBytesForWrapTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_binary_lage) values (?);", new Object[] { testData });
            List<Byte[]> dat = jdbcTemplate.queryForList("select c_binary_lage from tb_h2_types where c_binary_lage is not null limit 1;", (rs, rowNum) -> {
                return new BlobAsBytesWrapTypeHandler().getResult(rs, 1);
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(toPrimitive(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobBytesForWrapTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_binary_lage) values (?);", new Object[] { testData });
            List<Byte[]> dat = jdbcTemplate.queryForList("select c_binary_lage from tb_h2_types where c_binary_lage is not null limit 1;", (rs, rowNum) -> {
                return new BlobAsBytesWrapTypeHandler().getResult(rs, "c_binary_lage");
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(toPrimitive(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobBytesForWrapTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            List<Byte[]> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new BlobAsBytesWrapTypeHandler().setParameter(ps, 1, toWrapped(testData), JDBCType.BLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BlobAsBytesWrapTypeHandler().getNullableResult(rs, 1);
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(toPrimitive(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobBytesForWrapTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_blob;");
            jdbcTemplate.execute("create procedure proc_blob(out p_out blob) begin set p_out= b'0111111100001111'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_blob(?)}",//
                    SqlArg.asOut("out", JDBCType.BLOB.getVendorTypeNumber(), new BlobAsBytesWrapTypeHandler()));

            assert objectMap.size() == 2;
            assert !(objectMap.get("out") instanceof byte[]);
            assert objectMap.get("out") instanceof Byte[];
            Byte[] bytes = (Byte[]) objectMap.get("out");
            assert bytes[0] == 0b01111111;
            assert bytes[1] == 0b00001111;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testBlobBytesTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_binary_lage) values (?);", new Object[] { testData });
            List<byte[]> dat = jdbcTemplate.queryForList("select c_binary_lage from tb_h2_types where c_binary_lage is not null limit 1;", (rs, rowNum) -> {
                return new BlobAsBytesTypeHandler().getResult(rs, 1);
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(dat.get(0));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobBytesTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_binary_lage) values (?);", new Object[] { testData });
            List<byte[]> dat = jdbcTemplate.queryForList("select c_binary_lage from tb_h2_types where c_binary_lage is not null limit 1;", (rs, rowNum) -> {
                return new BlobAsBytesTypeHandler().getResult(rs, "c_binary_lage");
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(dat.get(0));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobBytesTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            List<byte[]> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new BlobAsBytesTypeHandler().setParameter(ps, 1, testData, JDBCType.BLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BlobAsBytesTypeHandler().getNullableResult(rs, 1);
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(dat.get(0));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobBytesTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_blob;");
            jdbcTemplate.execute("create procedure proc_blob(out p_out blob) begin set p_out= b'0111111100001111'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_blob(?)}",//
                    SqlArg.asOut("out", JDBCType.BLOB.getVendorTypeNumber(), new BlobAsBytesTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof byte[];
            assert !(objectMap.get("out") instanceof Byte[]);
            byte[] bytes = (byte[]) objectMap.get("out");
            assert bytes[0] == 0b01111111;
            assert bytes[1] == 0b00001111;
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testBlobInputStreamTypeHandler_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_binary_lage) values (?);", new Object[] { testData });
            List<InputStream> dat = jdbcTemplate.queryForList("select c_binary_lage from tb_h2_types where c_binary_lage is not null limit 1;", (rs, rowNum) -> {
                return new BlobAsInputStreamTypeHandler().getResult(rs, 1);
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(IOUtils.toByteArray(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobInputStreamTypeHandler_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_binary_lage) values (?);", new Object[] { testData });
            List<InputStream> dat = jdbcTemplate.queryForList("select c_binary_lage from tb_h2_types where c_binary_lage is not null limit 1;", (rs, rowNum) -> {
                return new BlobAsInputStreamTypeHandler().getResult(rs, "c_binary_lage");
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(IOUtils.toByteArray(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobInputStreamTypeHandler_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            List<InputStream> dat = jdbcTemplate.queryForList("select ?", ps -> {
                new BlobAsInputStreamTypeHandler().setParameter(ps, 1, new ByteArrayInputStream(testData), JDBCType.BLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BlobAsInputStreamTypeHandler().getNullableResult(rs, 1);
            });

            String s1 = MD5.encodeMD5(testData);
            String s2 = MD5.encodeMD5(IOUtils.toByteArray(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBlobInputStreamTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_blob;");
            jdbcTemplate.execute("create procedure proc_blob(out p_out blob) begin set p_out= b'0111111100001111'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_blob(?)}",//
                    SqlArg.asOut("out", JDBCType.BLOB.getVendorTypeNumber(), new BlobAsInputStreamTypeHandler()));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof InputStream;
            assert objectMap.get("#update-count-1").equals(0);

            byte[] bytes = new byte[2];
            bytes[0] = 0b01111111;
            bytes[1] = 0b00001111;

            String s1 = MD5.encodeMD5(bytes);
            String s2 = MD5.encodeMD5(IOUtils.toByteArray((InputStream) objectMap.get("out")));
            assert s1.equals(s2);
        }
    }
}
