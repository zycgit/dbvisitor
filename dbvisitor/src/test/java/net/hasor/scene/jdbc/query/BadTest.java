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
package net.hasor.scene.jdbc.query;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.args.BeanSqlArgSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import net.hasor.test.utils.TestUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/***
 *
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class BadTest extends AbstractDbTest {
    @Test
    public void badTest_1() {
        try {
            new JdbcTemplate().loadSplitSQL(";", StandardCharsets.UTF_8, "abc");
        } catch (Exception e) {
            assert e.getMessage().equals("can't find resource 'abc'");
        }
    }

    @Test
    public void badTest_2() {
        try {
            new JdbcTemplate().executeBatch(new String[0]);
        } catch (NullPointerException | SQLException e) {
            assert e.getMessage().equals("SQL array must not be empty");
        }
    }

    @Test
    public void badTest_3() throws SQLException {
        int[] ints1 = new JdbcTemplate().executeBatch("insert abc(id) values (?)", new SqlArgSource[0]);
        assert ints1.length == 0;

        int[] ints2 = new JdbcTemplate().executeBatch("insert abc(id) values (:id)", new SqlArgSource[0]);
        assert ints2.length == 0;

        int[] ints3 = new JdbcTemplate().executeBatch("insert abc(id,name) values (:id,?)", new SqlArgSource[0]);
        assert ints3.length == 0;
    }

    @Test
    public void badTest_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<user_info> tbUsers1 = jdbcTemplate.queryForList("select * from user_info", user_info.class);
            Set<String> collect1 = tbUsers1.stream().map(user_info::getUser_name).collect(Collectors.toSet());
            assert collect1.size() == 3;
            assert collect1.contains(TestUtils.beanForData1().getName());
            assert collect1.contains(TestUtils.beanForData2().getName());
            assert collect1.contains(TestUtils.beanForData3().getName());

            SqlArgSource[] ids = new SqlArgSource[] {//
                    new BeanSqlArgSource(TestUtils.beanForData1()),//
                    new BeanSqlArgSource(TestUtils.beanForData2()),//
                    new BeanSqlArgSource(TestUtils.beanForData3()) //
            };

            try {
                jdbcTemplate.executeBatch("update user_info set user_name = CONCAT(user_name, '~' ) where user_uuid = ${\"'\" + userUuid + \"'\"}", ids);
            } catch (SQLException e) {
                assert e.getMessage().startsWith("executeBatch, each set of parameters must be able to derive the same SQL.");
            }
        }
    }
}
