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
package net.hasor.dbvisitor.lambda;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.types.MappedArg;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.*;

/***
 * Lambda 方式执行 Delete 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaDeleteTest extends AbstractDbTest {
    @Test
    public void lambda_delete_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            EntityDeleteOperation<user_info> lambdaDelete = lambdaTemplate.lambdaDelete(user_info.class);
            int delete = lambdaDelete.allowEmptyWhere().doDelete();
            assert delete == 3;
        }
    }

    @Test
    public void lambda_delete_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            EntityDeleteOperation<user_info> lambdaDelete = lambdaTemplate.lambdaDelete(user_info.class);
            int delete = lambdaDelete.eq(user_info::getLogin_name, beanForData1().getLoginName()).doDelete();
            assert delete == 1;

            List<user_info> tbUsers = lambdaTemplate.lambdaQuery(user_info.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> collect = tbUsers.stream().map(user_info::getUser_uuid).collect(Collectors.toList());
            assert collect.contains(beanForData2().getUserUuid());
            assert collect.contains(beanForData3().getUserUuid());
        }
    }

    @Test
    public void eqBySampleMapTest_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");
            newValue.put("abc", "abc");

            // delete from auto_id where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaDelete("auto_id").eqBySampleMap(newValue).getBoundSql();

            assert boundSql.getSqlString().equals("DELETE FROM AUTO_ID WHERE ( ID = ? AND NAME = ? )");
            assert ((MappedArg) boundSql.getArgs()[0]).getValue().equals(1);
            assert ((MappedArg) boundSql.getArgs()[1]).getValue().equals("mali");
        }
    }

    @Test
    public void eqBySampleMapTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");

            // delete from auto_id where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaDelete("auto_id").eqBySampleMap(newValue).getBoundSql();

            assert boundSql.getSqlString().equals("DELETE FROM AUTO_ID WHERE ( ID = ? AND NAME = ? )");
            assert ((MappedArg) boundSql.getArgs()[0]).getValue().equals(1);
            assert ((MappedArg) boundSql.getArgs()[1]).getValue().equals("mali");
        }
    }

    @Test
    public void eqBySampleMapTest_3() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new LinkedHashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");

            // delete from not_exist_table where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaDelete("not_exist_table").eqBySampleMap(newValue).getBoundSql();

            assert boundSql.getSqlString().equals("DELETE FROM NOT_EXIST_TABLE WHERE ( id = ? AND name = ? )");
            assert ((MappedArg) boundSql.getArgs()[0]).getValue().equals(1);
            assert ((MappedArg) boundSql.getArgs()[1]).getValue().equals("mali");
        }
    }

    @Test
    public void eqBySampleMapTest_4() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new LinkedHashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");
            newValue.put("abc", "abc");

            // delete from not_exist_table where id = 1 and name = 'mail' and abc = 'abc';
            BoundSql boundSql = lambdaTemplate.lambdaDelete("not_exist_table").eqBySampleMap(newValue).getBoundSql();

            assert boundSql.getSqlString().equals("DELETE FROM NOT_EXIST_TABLE WHERE ( id = ? AND name = ? AND abc = ? )");
            assert ((MappedArg) boundSql.getArgs()[0]).getValue().equals(1);
            assert ((MappedArg) boundSql.getArgs()[1]).getValue().equals("mali");
            assert ((MappedArg) boundSql.getArgs()[2]).getValue().equals("abc");
        }
    }

    @Test
    public void eqBySampleTest_0() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 1);
        newValue.put("user_name", "mali");
        newValue.put("name", "mali2");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.lambdaDelete(user_info.class).eqBySampleMap(newValue).getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE ( user_name = ? )");
        assert ((MappedArg) boundSql1.getArgs()[0]).getValue().equals("mali");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql2 = lambdaTemplate.lambdaDelete(UserInfo.class).eqBySampleMap(newValue).getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM UserInfo WHERE ( name = ? )");
        assert ((MappedArg) boundSql2.getArgs()[0]).getValue().equals("mali2");
    }

    @Test
    public void eqBySampleTest_1() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 1);
        newValue.put("user_name", Arrays.asList("mali", "mali1"));
        newValue.put("name", Arrays.asList("mali2", "mali3"));

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.lambdaDelete(user_info.class).eqBySampleMap(newValue).getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM user_info WHERE ( user_name = ? )");
        assert ((List) ((MappedArg) boundSql1.getArgs()[0]).getValue()).get(0).equals("mali");
        assert ((List) ((MappedArg) boundSql1.getArgs()[0]).getValue()).get(1).equals("mali1");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql2 = lambdaTemplate.lambdaDelete(UserInfo.class).eqBySampleMap(newValue).getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM UserInfo WHERE ( name = ? )");
        assert ((List) ((MappedArg) boundSql2.getArgs()[0]).getValue()).get(0).equals("mali2");
        assert ((List) ((MappedArg) boundSql2.getArgs()[0]).getValue()).get(1).equals("mali3");
    }
}
