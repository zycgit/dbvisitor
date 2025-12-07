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
package net.hasor.dbvisitor.jdbc.extractor;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static net.hasor.test.utils.TestUtils.*;

/***
 * @version 2020-11-12
 * @author 赵永春 (zyc@hasor.net)
 */
public class FilterNullResultSetExtractorTest extends AbstractDbTest {
    @Test
    public void testFilterNullResultSetExtractor_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData6());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData7());

            ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
            FilterResultSetExtractor<Map<String, Object>> fullExtractor = new FilterResultSetExtractor<>(rowMapper);
            FilterResultSetExtractor<Map<String, Object>> nonullExtractor = new FilterResultSetExtractor<>(rowMapper);
            nonullExtractor.setRowTester(data -> data.get("login_password") != null);

            List<Map<String, Object>> mapList1 = jdbcTemplate.query("select * from user_info", nonullExtractor);
            List<Map<String, Object>> mapList2 = jdbcTemplate.query("select * from user_info", fullExtractor);

            assert mapList1.size() == 3;
            assert mapList2.size() == 5;
        }
    }

    @Test
    public void testFilterNullResultSetExtractor_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData6());
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData7());

            FilterResultSetExtractor<Map<String, Object>> nonullExtractor = new FilterResultSetExtractor<>(//
                    new ColumnMapRowMapper(),//
                    data -> data.get("login_password") != null//
            );

            List<Map<String, Object>> mapList = jdbcTemplate.query("select * from user_info", nonullExtractor);
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
    public void testFilterNullResultSetExtractor_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData6()); // loginPassword is null
            jdbcTemplate.executeUpdate(INSERT_ARRAY, arrayForData7()); // loginPassword is null

            FilterResultSetExtractor<Map<String, Object>> nonullExtractor = new FilterResultSetExtractor<>(//
                    new ColumnMapRowMapper(), s -> true, 2//
            );
            nonullExtractor.setRowTester(data -> data.get("login_password") != null);

            List<Map<String, Object>> mapList = jdbcTemplate.query("select * from user_info order by seq asc", nonullExtractor);
            List<String> collect = mapList.stream().map(stringObjectMap -> {
                return (String) stringObjectMap.get("user_name");
            }).collect(Collectors.toList());

            assert mapList.size() == 2;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
        }
    }

    @Test
    public void testFilterNullResultSetExtractor_4() {
        Predicate<Map<String, Object>> test = data -> data.get("login_password") != null;
        FilterResultSetExtractor<Map<String, Object>> nonullExtractor = new FilterResultSetExtractor<>(new ColumnMapRowMapper(), s -> true, 2);
        nonullExtractor.setRowTester(test);

        assert nonullExtractor.getRowTester() == test;
    }
}
