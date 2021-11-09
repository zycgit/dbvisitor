/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.types;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.cobble.CommonCodeUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.db.jdbc.SqlParameterUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.types.handler.BytesForWrapTypeHandler;
import net.hasor.db.types.handler.BytesInputStreamTypeHandler;
import net.hasor.db.types.handler.BytesTypeHandler;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BytesTypeTest {
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
    public void testBytesForWrapTypeHandler_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_blob) values (?);", new Object[] { testData });
            List<Byte[]> dat = jdbcTemplate.query("select c_blob from tb_h2_types where c_blob is not null limit 1;", (rs, rowNum) -> {
                return new BytesForWrapTypeHandler().getResult(rs, 1);
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(toPrimitive(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesForWrapTypeHandler_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_blob) values (?);", new Object[] { testData });
            List<Byte[]> dat = jdbcTemplate.query("select c_blob from tb_h2_types where c_blob is not null limit 1;", (rs, rowNum) -> {
                return new BytesForWrapTypeHandler().getResult(rs, "c_blob");
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(toPrimitive(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesForWrapTypeHandler_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            List<Byte[]> dat = jdbcTemplate.query("select ?", ps -> {
                new BytesForWrapTypeHandler().setParameter(ps, 1, toWrapped(testData), JDBCType.BLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BytesForWrapTypeHandler().getNullableResult(rs, 1);
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(toPrimitive(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesForWrapTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.localMySQL()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bytes;");
            jdbcTemplate.execute("create procedure proc_bytes(out p_out varbinary(10)) begin set p_out= b'0111111100001111'; end;");
            //
            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bytes(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARBINARY.getVendorTypeNumber(), new BytesForWrapTypeHandler())));
            //
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
    public void testBytesTypeHandler_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_blob) values (?);", new Object[] { testData });
            List<byte[]> dat = jdbcTemplate.query("select c_blob from tb_h2_types where c_blob is not null limit 1;", (rs, rowNum) -> {
                return new BytesTypeHandler().getResult(rs, 1);
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(dat.get(0));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesTypeHandler_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_blob) values (?);", new Object[] { testData });
            List<byte[]> dat = jdbcTemplate.query("select c_blob from tb_h2_types where c_blob is not null limit 1;", (rs, rowNum) -> {
                return new BytesTypeHandler().getResult(rs, "c_blob");
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(dat.get(0));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesTypeHandler_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            List<byte[]> dat = jdbcTemplate.query("select ?", ps -> {
                new BytesTypeHandler().setParameter(ps, 1, testData, JDBCType.BLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BytesTypeHandler().getNullableResult(rs, 1);
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(dat.get(0));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.localMySQL()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bytes;");
            jdbcTemplate.execute("create procedure proc_bytes(out p_out varbinary(10)) begin set p_out= b'0111111100001111'; end;");
            //
            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bytes(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARBINARY.getVendorTypeNumber(), new BytesTypeHandler())));
            //
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
    public void testBytesInputStreamTypeHandler_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_blob) values (?);", new Object[] { testData });
            List<InputStream> dat = jdbcTemplate.query("select c_blob from tb_h2_types where c_blob is not null limit 1;", (rs, rowNum) -> {
                return new BytesInputStreamTypeHandler().getResult(rs, 1);
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(IOUtils.toByteArray(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesInputStreamTypeHandler_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_blob) values (?);", new Object[] { testData });
            List<InputStream> dat = jdbcTemplate.query("select c_blob from tb_h2_types where c_blob is not null limit 1;", (rs, rowNum) -> {
                return new BytesInputStreamTypeHandler().getResult(rs, "c_blob");
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(IOUtils.toByteArray(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesInputStreamTypeHandler_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            byte[] testData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            List<InputStream> dat = jdbcTemplate.query("select ?", ps -> {
                new BytesInputStreamTypeHandler().setParameter(ps, 1, new ByteArrayInputStream(testData), JDBCType.BLOB.getVendorTypeNumber());
            }, (rs, rowNum) -> {
                return new BytesInputStreamTypeHandler().getNullableResult(rs, 1);
            });
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(testData);
            String s2 = CommonCodeUtils.MD5.encodeMD5(IOUtils.toByteArray(dat.get(0)));
            assert s1.equals(s2);
        }
    }

    @Test
    public void testBytesInputStreamTypeHandler_4() throws Exception {
        try (Connection conn = DsUtils.localMySQL()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_bytes;");
            jdbcTemplate.execute("create procedure proc_bytes(out p_out varbinary(10)) begin set p_out= b'0111111100001111'; end;");
            //
            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_bytes(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARBINARY.getVendorTypeNumber(), new BytesInputStreamTypeHandler())));
            //
            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof InputStream;
            assert objectMap.get("#update-count-1").equals(0);
            //
            byte[] bytes = new byte[2];
            bytes[0] = 0b01111111;
            bytes[1] = 0b00001111;
            //
            String s1 = CommonCodeUtils.MD5.encodeMD5(bytes);
            String s2 = CommonCodeUtils.MD5.encodeMD5(IOUtils.toByteArray((InputStream) objectMap.get("out")));
            assert s1.equals(s2);
        }
    }
}