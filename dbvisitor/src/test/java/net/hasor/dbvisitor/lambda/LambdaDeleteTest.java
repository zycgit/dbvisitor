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
import net.hasor.scene.singletable.dto.User;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.TB_User;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            //
            EntityDeleteOperation<TB_User> lambdaDelete = lambdaTemplate.lambdaDelete(TB_User.class);
            int delete = lambdaDelete.allowEmptyWhere().doDelete();
            assert delete == 3;
        }
    }

    @Test
    public void lambda_delete_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            //
            EntityDeleteOperation<TB_User> lambdaDelete = lambdaTemplate.lambdaDelete(TB_User.class);
            int delete = lambdaDelete.eq(TB_User::getLoginName, beanForData1().getLoginName()).doDelete();
            assert delete == 1;
            //
            List<TB_User> tbUsers = lambdaTemplate.lambdaQuery(TB_User.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> collect = tbUsers.stream().map(TB_User::getUserUUID).collect(Collectors.toList());
            assert collect.contains(beanForData2().getUserUUID());
            assert collect.contains(beanForData3().getUserUUID());
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

            // delete from user where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaDelete("user").eqBySampleMap(newValue).getBoundSql();

            assert boundSql.getSqlString().equals("DELETE FROM USER WHERE ( ID = ? AND NAME = ? )");
        }
    }

    @Test
    public void eqBySampleMapTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("id", 1);
            newValue.put("name", "mali");

            // delete from user where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaDelete("user").eqBySampleMap(newValue).getBoundSql();

            assert boundSql.getSqlString().equals("DELETE FROM USER WHERE ( ID = ? AND NAME = ? )");
        }
    }

    @Test
    public void eqBySampleTest_0() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 1);
        newValue.put("name", "mali");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.lambdaDelete(TB_User.class).eqBySampleMap(newValue).getBoundSql();
        assert boundSql1.getSqlString().equals("DELETE FROM TB_User WHERE ( name = ? )");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql2 = lambdaTemplate.lambdaDelete(User.class).eqBySampleMap(newValue).getBoundSql();
        assert boundSql2.getSqlString().equals("DELETE FROM User WHERE ( id = ? AND name = ? )");
    }
}
