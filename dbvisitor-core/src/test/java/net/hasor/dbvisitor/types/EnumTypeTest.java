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
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.dbvisitor.jdbc.SqlParameterUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.types.handler.EnumTypeHandler;
import net.hasor.test.db.dto.CharacterSensitiveEnum;
import net.hasor.test.db.dto.LicenseOfCodeEnum;
import net.hasor.test.db.dto.LicenseOfValueEnum;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EnumTypeTest {
    @Test
    public void testEnumTypeHandler_CharacterSensitive_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('READ_UNCOMMITTED');");
            Isolation dat1 = jdbcTemplate.queryForObject("select c_varchar from tb_h2_types where c_varchar is not null limit 1;", Isolation.class);
            assert dat1 == Isolation.READ_UNCOMMITTED;

            CharacterSensitiveEnum dat2 = jdbcTemplate.queryForObject("select 'a';", CharacterSensitiveEnum.class);
            assert dat2 == CharacterSensitiveEnum.a;
            CharacterSensitiveEnum dat3 = jdbcTemplate.queryForObject("select 'A';", CharacterSensitiveEnum.class);
            assert dat3 == CharacterSensitiveEnum.a;
        }
    }

    @Test
    public void testEnumTypeHandler_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('READ_UNCOMMITTED');");
            List<Isolation> dat = jdbcTemplate.query("select c_varchar from tb_h2_types where c_varchar is not null limit 1;", (rs, rowNum) -> {
                return new EnumTypeHandler<>(Isolation.class).getResult(rs, 1);
            });

            assert dat.get(0) == Isolation.READ_UNCOMMITTED;
        }
    }

    @Test
    public void testEnumTypeHandler_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('READ_UNCOMMITTED');");
            List<Isolation> dat = jdbcTemplate.query("select c_varchar from tb_h2_types where c_varchar is not null limit 1;", (rs, rowNum) -> {
                return new EnumTypeHandler<>(Isolation.class).getResult(rs, "c_varchar");
            });

            assert dat.get(0) == Isolation.READ_UNCOMMITTED;
        }
    }

    @Test
    public void testEnumTypeHandler_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            List<Isolation> dat = jdbcTemplate.query("select ?", ps -> {
                new EnumTypeHandler<>(Isolation.class).setParameter(ps, 1, Isolation.READ_UNCOMMITTED, null);
            }, (rs, rowNum) -> {
                return new EnumTypeHandler<>(Isolation.class).getNullableResult(rs, 1);
            });

            assert dat.get(0) == Isolation.READ_UNCOMMITTED;
        }
    }

    @Test
    public void testEnumTypeHandler_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(50)) begin set p_out='READ_UNCOMMITTED'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARCHAR.getVendorTypeNumber(), new EnumTypeHandler<>(Isolation.class))));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof Isolation;
            assert objectMap.get("out").equals(Isolation.READ_UNCOMMITTED);
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testEnumTypeHandler_ofCode_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('Apache 2.0');");
            List<LicenseOfCodeEnum> dat = jdbcTemplate.query("select c_varchar from tb_h2_types where c_varchar is not null limit 1;", (rs, rowNum) -> {
                return new EnumTypeHandler<>(LicenseOfCodeEnum.class).getResult(rs, 1);
            });

            assert dat.get(0) == LicenseOfCodeEnum.Apache2;
        }
    }

    @Test
    public void testEnumTypeHandler_ofCode_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('Apache 2.0');");
            List<LicenseOfCodeEnum> dat = jdbcTemplate.query("select c_varchar from tb_h2_types where c_varchar is not null limit 1;", (rs, rowNum) -> {
                return new EnumTypeHandler<>(LicenseOfCodeEnum.class).getResult(rs, "c_varchar");
            });

            assert dat.get(0) == LicenseOfCodeEnum.Apache2;
        }
    }

    @Test
    public void testEnumTypeHandler_ofCode_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            List<LicenseOfCodeEnum> dat = jdbcTemplate.query("select ?", ps -> {
                new EnumTypeHandler<>(LicenseOfCodeEnum.class).setParameter(ps, 1, LicenseOfCodeEnum.Apache2, null);
            }, (rs, rowNum) -> {
                return new EnumTypeHandler<>(LicenseOfCodeEnum.class).getNullableResult(rs, 1);
            });

            assert dat.get(0) == LicenseOfCodeEnum.Apache2;
        }
    }

    @Test
    public void testEnumTypeHandler_ofCode_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_varchar;");
            jdbcTemplate.execute("create procedure proc_varchar(out p_out varchar(50)) begin set p_out='Apache 2.0'; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_varchar(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.VARCHAR.getVendorTypeNumber(), new EnumTypeHandler<>(LicenseOfCodeEnum.class))));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof LicenseOfCodeEnum;
            assert objectMap.get("out").equals(LicenseOfCodeEnum.Apache2);
            assert objectMap.get("#update-count-1").equals(0);
        }
    }

    @Test
    public void testEnumTypeHandler_ofValue_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_integer) values (4);");
            List<LicenseOfValueEnum> dat = jdbcTemplate.query("select c_integer from tb_h2_types where c_integer is not null limit 1;", (rs, rowNum) -> {
                return new EnumTypeHandler<>(LicenseOfValueEnum.class).getResult(rs, 1);
            });

            assert dat.get(0) == LicenseOfValueEnum.Apache2;
        }
    }

    @Test
    public void testEnumTypeHandler_ofValue_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_integer) values (4);");
            List<LicenseOfValueEnum> dat = jdbcTemplate.query("select c_integer from tb_h2_types where c_integer is not null limit 1;", (rs, rowNum) -> {
                return new EnumTypeHandler<>(LicenseOfValueEnum.class).getResult(rs, "c_integer");
            });

            assert dat.get(0) == LicenseOfValueEnum.Apache2;
        }
    }

    @Test
    public void testEnumTypeHandler_ofValue_3() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            List<LicenseOfValueEnum> dat = jdbcTemplate.query("select ?", ps -> {
                new EnumTypeHandler<>(LicenseOfValueEnum.class).setParameter(ps, 1, LicenseOfValueEnum.Apache2, null);
            }, (rs, rowNum) -> {
                return new EnumTypeHandler<>(LicenseOfValueEnum.class).getNullableResult(rs, 1);
            });

            assert dat.get(0) == LicenseOfValueEnum.Apache2;
        }
    }

    @Test
    public void testEnumTypeHandler_ofValue_4() throws SQLException {
        try (Connection conn = DsUtils.mysqlConnection()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            jdbcTemplate.execute("drop procedure if exists proc_integer;");
            jdbcTemplate.execute("create procedure proc_integer(out p_out int) begin set p_out=4; end;");

            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_integer(?)}",//
                    Collections.singletonList(SqlParameterUtils.withOutputName("out", JDBCType.INTEGER.getVendorTypeNumber(), new EnumTypeHandler<>(LicenseOfValueEnum.class))));

            assert objectMap.size() == 2;
            assert objectMap.get("out") instanceof LicenseOfValueEnum;
            assert objectMap.get("out").equals(LicenseOfValueEnum.Apache2);
            assert objectMap.get("#update-count-1").equals(0);
        }
    }
}
