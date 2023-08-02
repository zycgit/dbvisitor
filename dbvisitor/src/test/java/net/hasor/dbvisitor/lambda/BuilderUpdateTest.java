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
import net.hasor.dbvisitor.types.MappedArg;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
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
            EntityUpdateOperation<user_info> lambdaUpdate = new LambdaTemplate().lambdaUpdate(user_info.class);
            SqlDialect dialect = new MySqlDialect();
            assert lambdaUpdate.getBoundSql(dialect) == null;
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("nothing to update.");
        }

        try {
            new LambdaTemplate().lambdaUpdate(user_info.class).updateBySample(null);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().contains("newValue is null.");
        }

        try {
            EntityUpdateOperation<user_info> lambdaUpdate = new LambdaTemplate().lambdaUpdate(user_info.class).updateTo(new user_info());
            lambdaUpdate.doUpdate();
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("The dangerous UPDATE operation,");
        }
    }

    @Test
    public void updateBuilder_2() {
        EntityUpdateOperation<user_info> lambdaUpdate = new LambdaTemplate().lambdaUpdate(user_info.class);
        lambdaUpdate.allowEmptyWhere();
        SqlDialect dialect = new MySqlDialect();

        try {
            lambdaUpdate.getBoundSql(dialect);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("nothing to update.");
        }

        try {
            lambdaUpdate.useQualifier().getBoundSql(dialect);
            assert false;
        } catch (Exception e) {
            assert e.getMessage().startsWith("nothing to update.");
        }
    }

    @Test
    public void updateBuilder_3() {
        user_info data = new user_info();
        data.setLogin_name("acc");
        data.setLogin_password("pwd");
        EntityUpdateOperation<user_info> lambdaUpdate = new LambdaTemplate().lambdaUpdate(user_info.class);
        lambdaUpdate.and(queryBuilder -> {
            queryBuilder.eq(user_info::getSeq, 123);
        }).updateBySample(data);

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaUpdate.getBoundSql(dialect);
        assert !(boundSql1 instanceof BatchBoundSql);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET login_name = ? , login_password = ? WHERE ( seq = ? )");
        assert boundSql1.getArgs()[0].equals("acc");
        assert boundSql1.getArgs()[1].equals("pwd");
        assert boundSql1.getArgs()[2].equals(123);

        BoundSql boundSql2 = lambdaUpdate.useQualifier().getBoundSql(dialect);
        assert !(boundSql2 instanceof BatchBoundSql);
        assert boundSql2.getSqlString().equals("UPDATE `user_info` SET `login_name` = ? , `login_password` = ? WHERE ( `seq` = ? )");
        assert boundSql2.getArgs()[0].equals("acc");
        assert boundSql2.getArgs()[1].equals("pwd");
        assert boundSql2.getArgs()[2].equals(123);
    }

    @Test
    public void updateBuilder_4() {
        user_info data = new user_info();
        data.setLogin_name("acc");
        data.setLogin_password("pwd");

        EntityUpdateOperation<user_info> lambdaUpdate = new LambdaTemplate().lambdaUpdate(user_info.class);
        lambdaUpdate.eq(user_info::getLogin_name, "admin").and().eq(user_info::getLogin_password, "pass").allowReplaceRow().updateTo(data);

        SqlDialect dialect = new MySqlDialect();
        BoundSql boundSql1 = lambdaUpdate.getBoundSql(dialect);
        assert boundSql1.getSqlString().equals("UPDATE user_info SET user_uuid = ? , login_name = ? , login_password = ? , user_name = ? , email = ? , seq = ? , register_time = ? WHERE login_name = ? AND login_password = ?");

        BoundSql boundSql2 = lambdaUpdate.useQualifier().getBoundSql(dialect);
        assert boundSql2.getSqlString().equals("UPDATE `user_info` SET `user_uuid` = ? , `login_name` = ? , `login_password` = ? , `user_name` = ? , `email` = ? , `seq` = ? , `register_time` = ? WHERE `login_name` = ? AND `login_password` = ?");
    }

    @Test
    public void updateBuilder_5() {
        user_info data = new user_info();
        data.setLogin_name("acc");
        data.setLogin_password("pwd");

        try {
            EntityUpdateOperation<user_info> lambdaUpdate = new LambdaTemplate().lambdaUpdate(user_info.class);
            lambdaUpdate.eq(user_info::getLogin_name, "admin").and().eq(user_info::getLogin_password, "pass").updateTo(data);
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
            setValue.put("name", "mali2");
            setValue.put("abc", "abc");
            setValue.put("uid", "the new uid");

            // update auto_id set uid = 'the new uid' , name = 'mali2' where id = 1 and name = 'mali'
            BoundSql boundSql1 = lambdaTemplate.lambdaUpdate("auto_id")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateBySample(setValue)   // set ...
                    .getBoundSql();
            assert boundSql1.getSqlString().equals("UPDATE AUTO_ID SET UID = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )");
            assert ((MappedArg) boundSql1.getArgs()[0]).getValue().equals("the new uid");
            assert ((MappedArg) boundSql1.getArgs()[1]).getValue().equals("mali2");
            assert ((MappedArg) boundSql1.getArgs()[2]).getValue().equals(1);
            assert ((MappedArg) boundSql1.getArgs()[3]).getValue().equals("mali");

            // update auto_id set uid = 'the new uid' , name = 'mali2' where id = 1 and name = 'mali'
            BoundSql boundSql2 = lambdaTemplate.lambdaUpdate("auto_id")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateByMap(setValue)      // set ...
                    .getBoundSql();

            // UPDATE AUTO_ID SET UID = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )
            assert boundSql2.getSqlString().equals("UPDATE AUTO_ID SET UID = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )");
            assert ((MappedArg) boundSql2.getArgs()[0]).getValue().equals("the new uid");
            assert ((MappedArg) boundSql2.getArgs()[1]).getValue().equals("mali2");
            assert ((MappedArg) boundSql2.getArgs()[2]).getValue().equals(1);
            assert ((MappedArg) boundSql2.getArgs()[3]).getValue().equals("mali");
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
            setValue.put("name", "mali2");
            setValue.put("abc", "abc");
            setValue.put("uid", "the new uid");

            // update not_exist_table set uid = 'the new uid', name = 'mali2' where id = 1 and name = 'mali' and abc = 'abc'
            BoundSql boundSql1 = lambdaTemplate.lambdaUpdate("not_exist_table")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateBySample(setValue)   // set ...
                    .getBoundSql();
            assert boundSql1.getSqlString().equals("UPDATE NOT_EXIST_TABLE SET uid = ? , abc = ? , name = ? WHERE ( abc = ? AND name = ? AND id = ? )");
            assert ((MappedArg) boundSql1.getArgs()[0]).getValue().equals("the new uid");
            assert ((MappedArg) boundSql1.getArgs()[1]).getValue().equals("abc");
            assert ((MappedArg) boundSql1.getArgs()[2]).getValue().equals("mali2");
            assert ((MappedArg) boundSql1.getArgs()[3]).getValue().equals("abc");
            assert ((MappedArg) boundSql1.getArgs()[4]).getValue().equals("mali");
            assert ((MappedArg) boundSql1.getArgs()[5]).getValue().equals(1);

            // delete from user where id = 1 and name = 'mail';
            BoundSql boundSql2 = lambdaTemplate.lambdaUpdate("not_exist_table")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateByMap(setValue)      // set ...
                    .getBoundSql();

            //UPDATE AUTO_ID SET UID = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )
            assert boundSql2.getSqlString().equals("UPDATE NOT_EXIST_TABLE SET uid = ? , abc = ? , name = ? WHERE ( abc = ? AND name = ? AND id = ? )");
            assert ((MappedArg) boundSql2.getArgs()[0]).getValue().equals("the new uid");
            assert ((MappedArg) boundSql2.getArgs()[1]).getValue().equals("abc");
            assert ((MappedArg) boundSql2.getArgs()[2]).getValue().equals("mali2");
            assert ((MappedArg) boundSql2.getArgs()[3]).getValue().equals("abc");
            assert ((MappedArg) boundSql2.getArgs()[4]).getValue().equals("mali");
            assert ((MappedArg) boundSql2.getArgs()[5]).getValue().equals(1);
        }
    }

    @Test
    public void eqBySampleTest_0() {
        LambdaTemplate lambdaTemplate = new LambdaTemplate();

        Map<String, Object> whereValue = new HashMap<>();
        whereValue.put("id", 1);
        whereValue.put("user_name", "mali1");
        whereValue.put("name", "123");
        whereValue.put("abc", "abc");

        Map<String, Object> setValue = new HashMap<>();
        setValue.put("user_name", "mali2");
        setValue.put("name", "321");
        setValue.put("abc", "abc");
        setValue.put("create_time", new Date());

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql1 = lambdaTemplate.lambdaUpdate(user_info.class).eqBySampleMap(whereValue).updateByMap(setValue).getBoundSql();
        assert boundSql1.getSqlString().equals("UPDATE user_info SET user_name = ? WHERE ( user_name = ? )");
        assert ((MappedArg) boundSql1.getArgs()[0]).getValue().equals("mali2");
        assert ((MappedArg) boundSql1.getArgs()[1]).getValue().equals("mali1");

        // delete from user where id = 1 and name = 'mail';
        BoundSql boundSql2 = lambdaTemplate.lambdaUpdate(UserInfo2.class).eqBySampleMap(whereValue).updateByMap(setValue).getBoundSql();
        assert boundSql2.getSqlString().equals("UPDATE user_info SET user_name = ? WHERE ( user_name = ? )");
        assert ((MappedArg) boundSql2.getArgs()[0]).getValue().equals("321");
        assert ((MappedArg) boundSql2.getArgs()[1]).getValue().equals("123");
    }
}
