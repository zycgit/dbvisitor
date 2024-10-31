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
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.hasor.test.utils.TestUtils.beanForData1;

/***
 * Lambda 方式执行 Update 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaUpdateTest extends AbstractDbTest {

    //
    //
    //

    @Test
    public void eqBySampleMapTest_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> whereValue = new LinkedHashMap<>();
            whereValue.put("id", 1);
            whereValue.put("name", "mali");
            whereValue.put("abc", "abc");

            Map<String, Object> setValue = new LinkedHashMap<>();
            setValue.put("name", "mali2");
            setValue.put("abc", "abc");
            setValue.put("uid", "the new uid");

            // update auto_id set uid = 'the new uid' , name = 'mali2' where id = 1 and name = 'mali'
            BoundSql boundSql1 = lambdaTemplate.freedomUpdate("auto_id")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateToSample(setValue)   // set ...
                    .getBoundSql();
            assert boundSql1.getSqlString().equals("UPDATE auto_id SET uid = ? , abc = ? , name = ? WHERE ( id = ? AND name = ? AND abc = ? )");
            assert ((SqlArg) boundSql1.getArgs()[0]).getValue().equals("the new uid");
            assert ((SqlArg) boundSql1.getArgs()[1]).getValue().equals("abc");
            assert ((SqlArg) boundSql1.getArgs()[2]).getValue().equals("mali2");
            assert ((SqlArg) boundSql1.getArgs()[3]).getValue().equals(1);
            assert ((SqlArg) boundSql1.getArgs()[4]).getValue().equals("mali");
            assert ((SqlArg) boundSql1.getArgs()[5]).getValue().equals("abc");
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
            BoundSql boundSql1 = lambdaTemplate.updateByTable("not_exist_table")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateToSample(setValue)   // set ...
                    .getBoundSql();
            assert boundSql1.getSqlString().equals("UPDATE not_exist_table SET uid = ? , abc = ? , name = ? WHERE ( abc = ? AND name = ? AND id = ? )");
            assert ((SqlArg) boundSql1.getArgs()[0]).getValue().equals("the new uid");
            assert ((SqlArg) boundSql1.getArgs()[1]).getValue().equals("abc");
            assert ((SqlArg) boundSql1.getArgs()[2]).getValue().equals("mali2");
            assert ((SqlArg) boundSql1.getArgs()[3]).getValue().equals("abc");
            assert ((SqlArg) boundSql1.getArgs()[4]).getValue().equals("mali");
            assert ((SqlArg) boundSql1.getArgs()[5]).getValue().equals(1);

            // delete from user where id = 1 and name = 'mail';
            BoundSql boundSql2 = lambdaTemplate.updateByTable("not_exist_table")//
                    .eqBySampleMap(whereValue)  // where ...
                    .updateToSampleMap(setValue)      // set ...
                    .getBoundSql();

            //UPDATE AUTO_ID SET UID = ? , NAME = ? WHERE ( ID = ? AND NAME = ? )
            assert boundSql2.getSqlString().equals("UPDATE not_exist_table SET uid = ? , abc = ? , name = ? WHERE ( abc = ? AND name = ? AND id = ? )");
            assert ((SqlArg) boundSql2.getArgs()[0]).getValue().equals("the new uid");
            assert ((SqlArg) boundSql2.getArgs()[1]).getValue().equals("abc");
            assert ((SqlArg) boundSql2.getArgs()[2]).getValue().equals("mali2");
            assert ((SqlArg) boundSql2.getArgs()[3]).getValue().equals("abc");
            assert ((SqlArg) boundSql2.getArgs()[4]).getValue().equals("mali");
            assert ((SqlArg) boundSql2.getArgs()[5]).getValue().equals(1);
        }
    }

    @Test
    public void lambda_update_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            EntityQueryOperation<user_info> lambdaQuery = lambdaTemplate.queryByEntity(user_info.class);
            user_info tbUser1 = lambdaQuery.eq(user_info::getLogin_name, beanForData1().getLoginName()).queryForObject();
            assert tbUser1.getUser_name() != null;

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("user_name", null);

            EntityUpdateOperation<user_info> lambdaUpdate = lambdaTemplate.updateByEntity(user_info.class);
            int update = lambdaUpdate.eq(user_info::getLogin_name, "muhammad")//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            user_info tbUser2 = lambdaTemplate.queryByEntity(user_info.class).eq(user_info::getLogin_name, "muhammad").queryForObject();
            assert tbUser2.getUser_name() == null;
        }
    }

    @Test
    public void lambda_update_map_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<UserInfo2> users = lambdaTemplate.queryByEntity(UserInfo2.class).queryForList();
            UserInfo2 info = users.get(0);

            MappingOptions options = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);
            MapUpdateOperation update = lambdaTemplate.updateByTable("user_info").asMap();
            assert update.eq("user_uuid", info.getUid()).updateTo("loginPassword", "newPassword").doUpdate() == 1;

            Map<String, Object> maps = lambdaTemplate.getJdbc().queryForObject("select * from user_info where user_uuid = ?", new Object[] { info.getUid() }, Map.class);
            assert maps.get("login_password").equals("newPassword");
        }
    }

    @Test
    public void lambda_update_pk_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            EntityQueryOperation<user_info> lambdaQuery = lambdaTemplate.queryByEntity(user_info.class);
            user_info tbUser1 = lambdaQuery.eq(user_info::getLogin_name, beanForData1().getLoginName()).queryForObject();
            assert tbUser1.getUser_name() != null;

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("user_name", null);

            EntityUpdateOperation<user_info> lambdaUpdate = lambdaTemplate.updateByEntity(user_info.class);
            int update = lambdaUpdate.eq(user_info::getLogin_name, "muhammad")//
                    .updateToSampleMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            user_info tbUser2 = lambdaTemplate.queryByEntity(user_info.class).eq(user_info::getLogin_name, "muhammad").queryForObject();
            assert tbUser2.getUser_name() == null;
        }
    }
}
