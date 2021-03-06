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
package net.hasor.dbvisitor.jdbc.mapper;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.db.dto.TB_User2;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class SingleColumnRowMapperTest {
    @Test
    public void testSingleColumnRowMapper_1() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String resultData = null;
            //
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_varchar) values ('abc');");
            resultData = jdbcTemplate.queryForObject(//
                    "select c_varchar from tb_h2_types where c_varchar = 'abc';", String.class);
            assert "abc".equals(resultData);
            //
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_int) values (123);");
            resultData = jdbcTemplate.queryForObject(//
                    "select c_int from tb_h2_types where c_int = 123;", String.class);
            assert "123".equals(resultData);
            //
            SingleColumnRowMapper<String> rowMapper = new SingleColumnRowMapper<>(String.class);
            resultData = jdbcTemplate.queryForObject(//
                    "select c_int from tb_h2_types where c_int = 123;", rowMapper);
            assert "123".equals(resultData);
        }
    }

    @Test
    public void testSingleColumnRowMapper_2() throws Throwable {
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_int) values (123);");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_double) values (123.123);");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_float) values (123.123);");
            jdbcTemplate.executeUpdate("insert into tb_h2_types (c_time) values (?)", new Object[] { new Date() });
            //
            int num1 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", Integer.class);
            Number num2 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", Number.class);
            double num3 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", double.class);
            BigDecimal num4 = jdbcTemplate.queryForObject("select c_int from tb_h2_types where c_int = 123;", BigDecimal.class);
            Number num5 = jdbcTemplate.queryForObject("select c_time from tb_h2_types where c_time is not null limit 1;", Number.class);
            //
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
        try (DruidDataSource dataSource = DsUtils.createDs()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            //
            List<TB_User2> tbUser2s = jdbcTemplate.queryForList("select *,'' as futures from tb_user", TB_User2.class);
            assert tbUser2s.size() == 3;
            tbUser2s.forEach(tb_user2 -> {
                assert tb_user2.getFutures() == null;
            });
        }
    }
}
