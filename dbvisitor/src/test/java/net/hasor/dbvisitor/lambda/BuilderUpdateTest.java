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
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.scene.singletable.dto.User;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.TB_User;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BuilderUpdateTest extends AbstractDbTest {
    @Test
    public void updateBuilder_1() {
        try {
            EntityUpdateOperation<TB_User> lambdaUpdate = new LambdaTemplate().lambdaUpdate(TB_User.class);
            SqlDialect dialect = new MySqlDialect();
            assert lambdaUpdate.getBoundSql(dialect) == null;
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("Nothing to update.");
        }
        //
        try {
            new LambdaTemplate().lambdaUpdate(TB_User.class).updateBySample(null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("newValue is null.");
        }
        //
        try {
            EntityUpdateOperation<TB_User> lambdaUpdate = new LambdaTemplate().lambdaUpdate(TB_User.class).updateTo(new TB_User());
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous UPDATE operation,");
        }
    }

    @Test
    public void updateBuilder_2() {
        EntityUpdateOperation<TB_User> lambdaUpdate = new LambdaTemplate().lambdaUpdate(TB_User.class);
        lambdaUpdate.allowEmptyWhere();
        //
        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaUpdate.getBoundSql(dialect);
        assert boundSql1 == null;
        //
        BoundSql boundSql2 = lambdaUpdate.useQualifier().getBoundSql(dialect);
        assert boundSql2 == null;
    }

    @Test
    public void updateBuilder_3() {
        TB_User data = new TB_User();
        data.setLoginName("acc");
        data.setLoginPassword("pwd");
        EntityUpdateOperation<TB_User> lambdaUpdate = new LambdaTemplate().lambdaUpdate(TB_User.class);
        lambdaUpdate.and(queryBuilder -> {
            queryBuilder.eq(TB_User::getIndex, 123);
        }).updateBySample(data);
        //
        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaUpdate.getBoundSql(dialect);
        assert !(boundSql1 instanceof BatchBoundSql);
        //                                      UPDATE TB_User SET registerTime = ? , loginName = ? , name = ? , loginPassword = ? , `index` = ? , userUUID = ? , email = ? WHERE ( `index` = ? )
        assert boundSql1.getSqlString().equals("UPDATE TB_User SET loginName = ? , loginPassword = ? WHERE ( `index` = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);
        //
        BoundSql boundSql2 = lambdaUpdate.useQualifier().getBoundSql(dialect);
        assert !(boundSql2 instanceof BatchBoundSql);
        assert boundSql2.getSqlString().equals("UPDATE `TB_User` SET `loginName` = ? , `loginPassword` = ? WHERE ( `index` = ? )");
        assert boundSql2.getArgs()[0].equals("acc");
        assert boundSql2.getArgs()[1].equals("pwd");
        assert boundSql2.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_4() {
        TB_User data = new TB_User();
        data.setLoginName("acc");
        data.setLoginPassword("pwd");
        //
        EntityUpdateOperation<TB_User> lambdaUpdate = new LambdaTemplate().lambdaUpdate(TB_User.class);
        lambdaUpdate.eq(TB_User::getLoginName, "admin").and().eq(TB_User::getLoginPassword, "pass").allowReplaceRow().updateTo(data);
        //
        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaUpdate.getBoundSql(dialect);
        assert boundSql1.getSqlString().equals("UPDATE TB_User SET registerTime = ? , loginName = ? , name = ? , loginPassword = ? , `index` = ? , userUUID = ? , email = ? WHERE loginName = ? AND loginPassword = ?");
        //
        BoundSql boundSql2 = lambdaUpdate.useQualifier().getBoundSql(dialect);
        assert boundSql2.getSqlString().equals("UPDATE `TB_User` SET `registerTime` = ? , `loginName` = ? , `name` = ? , `loginPassword` = ? , `index` = ? , `userUUID` = ? , `email` = ? WHERE `loginName` = ? AND `loginPassword` = ?");
    }

    @Test
    public void updateBuilder_5() {
        TB_User data = new TB_User();
        data.setLoginName("acc");
        data.setLoginPassword("pwd");
        //
        try {
            EntityUpdateOperation<TB_User> lambdaUpdate = new LambdaTemplate().lambdaUpdate(TB_User.class);
            lambdaUpdate.eq(TB_User::getLoginName, "admin").and().eq(TB_User::getLoginPassword, "pass").updateTo(data);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("The dangerous UPDATE operation, You must call `allowReplaceRow()` to enable REPLACE row.");
        }
    }

    @Test
    public void eqBySampleMapTest_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> whereValue = new HashMap<>();
            whereValue.put("id", 1);
            whereValue.put("name", "mali");
            whereValue.put("abc", "abc");

            Map<String, Object> setValue = new HashMap<>();
            setValue.put("name", "mali");
            setValue.put("abc", "abc");
            setValue.put("create_time", new Date());

            // delete from user where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaUpdate("user")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateBySample(setValue)   // set ...
                    .getBoundSql();

            assert boundSql.getSqlString().equals("UPDATE USER SET CREATE_TIME = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )");
        }
    }

    @Test
    public void eqBySampleMapTest_1() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> whereValue = new HashMap<>();
            whereValue.put("id", 1);
            whereValue.put("name", "mali");
            whereValue.put("abc", "abc");

            Map<String, Object> setValue = new HashMap<>();
            setValue.put("name", "mali");
            setValue.put("abc", "abc");
            setValue.put("create_time", new Date());

            // delete from user where id = 1 and name = 'mail';
            BoundSql boundSql = lambdaTemplate.lambdaUpdate("user")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateByMap(setValue)      // set ...
                    .getBoundSql();

            assert boundSql.getSqlString().equals("UPDATE USER SET CREATE_TIME = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )");
        }
    }

    @Test
    public void eqBySampleTest_0() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        Map<String, Object> whereValue = new HashMap<>();
        whereValue.put("id", 1);
        whereValue.put("name", "mali");
        whereValue.put("abc", "abc");

        Map<String, Object> setValue = new HashMap<>();
        setValue.put("name", "mali");
        setValue.put("abc", "abc");
        setValue.put("create_time", new Date());

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.lambdaUpdate(TB_User.class).eqBySampleMap(whereValue).updateByMap(setValue).getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE TB_User SET name = ? WHERE ( name = ? )");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql2 = lambdaTemplate.lambdaUpdate(User.class).eqBySampleMap(whereValue).updateByMap(setValue).getBoundSql();
        assert boundSql2.getSqlString().equals("UPDATE User SET create_time = ? , name = ? WHERE ( id = ? AND name = ? )");
    }
}
