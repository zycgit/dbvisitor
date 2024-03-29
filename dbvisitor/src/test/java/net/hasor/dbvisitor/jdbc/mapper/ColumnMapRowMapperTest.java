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
package net.hasor.dbvisitor.jdbc.mapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.*;

public class ColumnMapRowMapperTest {
    @Test
    public void testColumnMapRowMapper_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from user_info", new ColumnMapRowMapper());

            List<String> collect = mapList.stream().map(stringObjectMap -> {
                return (String) stringObjectMap.get("user_name");
            }).collect(Collectors.toList());

            assert mapList.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void testColumnMapRowMapper_2() throws SQLException {
        try (Connection conn = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(conn);
            Map<String, Object> objectMap1 = jdbcTemplate.queryForObject("select 1 as T, 2 as t", new ColumnMapRowMapper(false, TypeHandlerRegistry.DEFAULT));
            assert objectMap1.size() == 2;

            Map<String, Object> objectMap2 = jdbcTemplate.queryForObject("select 1 as T, 2 as t", new ColumnMapRowMapper(true, TypeHandlerRegistry.DEFAULT));
            assert objectMap2.size() == 1;
        }
    }
}
