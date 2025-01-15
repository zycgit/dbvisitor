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
package net.hasor.dbvisitor.template.jdbc.mapper;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import net.hasor.test.dto.UserInfo3;
import net.hasor.test.dto.UserInfo4;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

public class SingleColumnRowMapperTest {

    @Test
    public void testSingleColumnRowMapper_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            String resultData = null;

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('abc');");
            resultData = jdbcTemplate.queryForObject(//
                    "select c_varchar from tb_h2_types where c_varchar = 'abc';", String.class);
            assert "abc".equals(resultData);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_int) values (123);");
            resultData = jdbcTemplate.queryForObject(//
                    "select c_int from tb_h2_types where c_int = 123;", String.class);
            assert "123".equals(resultData);

            SingleColumnRowMapper<String> rowMapper = new SingleColumnRowMapper<>(String.class);
            resultData = jdbcTemplate.queryForObject(//
                    "select c_int from tb_h2_types where c_int = 123;", rowMapper);
            assert "123".equals(resultData);
        }
    }

    @Test
    public void testSingleColumnRowMapper_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_int) values (123);");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_double) values (123.123);");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_double) values (123.123);");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_time) values (?)", new Object[] { new Date() });

            int num1 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", Integer.class);
            Number num2 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", Number.class);
            double num3 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", double.class);
            BigDecimal num4 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", BigDecimal.class);
            Number num5 = jdbcTemplate.queryForObject("select c_time from tb_h2_types where c_time is not null limit 1;", Number.class);

            assert num1 == 123;
            assert num2.intValue() == 123;
            assert num2 instanceof Integer;
            assert num3 == 123d;
            assert num4.intValue() == 123;
            assert num5 != null;
            assert num5.longValue() != 0;
        }
    }

    @Test
    public void testSingleColumnRowMapper_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<UserInfo3> users1 = jdbcTemplate.queryForList("select *,'{''ext1'':''abc'',''ext2'':123,''ext3'':true}' as futures from user_info", UserInfo3.class);
            assert users1.size() == 3;
            users1.forEach(user -> {
                assert user.getFutures() != null;
                assert user.getFutures().getExt1().equals("abc");
                assert user.getFutures().getExt2().equals(123);
                assert user.getFutures().getExt3().equals(true);
                assert user.getFutures().getExt4() == null;
            });

            List<UserInfo4> users2 = jdbcTemplate.queryForList("select *,'{''ext1'':''abc'',''ext2'':123,''ext3'':true}' as futures from user_info", UserInfo4.class);
            assert users2.size() == 3;
            users2.forEach(user -> {
                assert user.getFutures() != null;
                assert user.getFutures().getExt1().equals("abc");
                assert user.getFutures().getExt2().equals(123);
                assert user.getFutures().getExt3().equals(true);
                assert user.getFutures().getExt4() == null;
            });
        }
    }
}
