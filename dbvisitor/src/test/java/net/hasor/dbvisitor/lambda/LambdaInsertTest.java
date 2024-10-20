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
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.DateFormatType;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.AutoId;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.newID;

/**
 * Lambda 方式执行 Insert 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaInsertTest extends AbstractDbTest {
    @Test
    public void lambda_insert_1() throws Throwable {
        UserInfo2 user1 = new UserInfo2();
        user1.setUid(newID());
        user1.setName("默罕默德");
        user1.setLoginName("muhammad");
        user1.setPassword("1");
        user1.setEmail("muhammad@hasor.net");
        user1.setSeq(1);
        user1.setCreateTime(new Date());
        Map<String, Object> user2 = new HashMap<>();
        user2.put("uid", newID());
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("index", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from user_info");

            InsertOperation<UserInfo2> lambdaInsert = lambdaTemplate.lambdaInsert(UserInfo2.class);
            lambdaInsert.applyEntity(user1);
            lambdaInsert.applyMap(user2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;

            int i = lambdaInsert.executeSumResult();
            assert i == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.getUid());
            assert ids.contains(user2.get("uid"));
        }
    }

    @Test
    public void lambda_insert_2() throws Throwable {
        UserInfo2 user1 = new UserInfo2();
        user1.setName("默罕默德");
        user1.setLoginName("muhammad");
        user1.setPassword("1");
        user1.setEmail("muhammad@hasor.net");
        user1.setSeq(1);
        user1.setCreateTime(new Date());
        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("seq", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from user_info");

            InsertOperation<UserInfo2> lambdaInsert = lambdaTemplate.lambdaInsert(UserInfo2.class);
            lambdaInsert.applyEntity(user1);
            lambdaInsert.applyMap(user2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;

            int i = lambdaInsert.executeSumResult();
            assert i == 2;

            List<user_info> tbUsers = lambdaTemplate.lambdaQuery(user_info.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(user_info::getUser_uuid).collect(Collectors.toList());
            assert ids.contains(user1.getUid());
            assert ids.contains(user2.get("uid"));
        }
    }

    @Test
    public void lambda_insert_3() throws Throwable {
        AutoId autoId1 = new AutoId();
        autoId1.setName("默罕默德");
        Map<String, Object> autoId2 = CollectionUtils.asMap("name", "安妮.贝隆");

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from auto_id");

            InsertOperation<AutoId> lambdaInsert = lambdaTemplate.lambdaInsert(AutoId.class);
            lambdaInsert.applyEntity(autoId1);
            lambdaInsert.applyMap(autoId2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;

            int i = lambdaInsert.executeSumResult();
            assert i == 2;

            List<AutoId> tbUsers = lambdaTemplate.lambdaQuery(AutoId.class).queryForList();
            assert tbUsers.size() == 2;

            List<String> uids = tbUsers.stream().map(AutoId::getUid).collect(Collectors.toList());
            assert StringUtils.isNotBlank(uids.get(0));
            assert StringUtils.isNotBlank(uids.get(1));
            assert uids.contains(autoId1.getUid());
            assert uids.contains(autoId2.get("uid"));

            List<Integer> ids = tbUsers.stream().map(AutoId::getId).collect(Collectors.toList());
            assert ids.contains(autoId1.getId());
            assert ids.contains(autoId2.get("id"));
        }
    }

    @Test
    public void lambda_insert_4() throws Throwable {
        Map<String, Object> tbUser1 = new LinkedCaseInsensitiveMap<>(CollectionUtils.asMap("name", "默罕默德", "uid", "uuid-1"));
        Map<String, Object> tbUser2 = new LinkedCaseInsensitiveMap<>(CollectionUtils.asMap("name", "安妮.贝隆", "uid", "uuid-2"));

        try (Connection c = DsUtils.mysqlConn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from auto_id");

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.lambdaInsert("auto_id");
            lambdaInsert.applyMap(tbUser1);
            lambdaInsert.applyMap(tbUser2);

            try {
                lambdaInsert.getBoundSql();
                assert false;
            } catch (IllegalStateException e) {
                assert e.getMessage().equals("require single record.");
            }

            int i = lambdaInsert.executeSumResult();
            assert i == 2;

            List<AutoId> tbUsers = lambdaTemplate.lambdaQuery(AutoId.class).queryForList();
            assert tbUsers.size() == 2;

            List<String> uids = tbUsers.stream().map(AutoId::getUid).collect(Collectors.toList());
            assert StringUtils.isNotBlank(uids.get(0));
            assert StringUtils.isNotBlank(uids.get(1));
            assert uids.contains(tbUser1.get("uid"));
            assert uids.contains(tbUser2.get("uid"));

            assert tbUser1.get("id") == null;
            assert tbUser2.get("id") == null;
        }
    }

    @Test
    public void lambda_insert_5() throws Throwable {
        Map<String, Object> tbUser1 = new LinkedCaseInsensitiveMap<>(CollectionUtils.asMap("name", "默罕默德", "uid", "uuid-1"));

        try (Connection c = DsUtils.mysqlConn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from auto_id");

            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.lambdaInsert("auto_id");
            lambdaInsert.applyMap(tbUser1);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;

            int i = lambdaInsert.executeSumResult();
            assert i == 1;

            List<AutoId> tbUsers = lambdaTemplate.lambdaQuery(AutoId.class).queryForList();
            assert tbUsers.size() == 1;

            List<String> uids = tbUsers.stream().map(AutoId::getUid).collect(Collectors.toList());
            assert StringUtils.isNotBlank(uids.get(0));
            assert uids.contains(tbUser1.get("uid"));

            assert tbUser1.get("id") == null;
        }
    }

    @Test
    public void lambda_insert_6() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from user_info");

            Map<String, Object> newValue = new HashMap<>();
            newValue.put("user_uuid", "uuiduuiduuid");
            newValue.put("userName", "yongchun.zyc"); // use CamelCase convert -> user_name
            newValue.put("loginName", "abc");// use CamelCase convert -> login_name
            newValue.put("loginPassword", "my password");// use CamelCase convert -> login_password
            newValue.put("email", "zyc@hasor.net");
            newValue.put("seq", 111);
            newValue.put("registerTime", DateFormatType.s_yyyyMMdd_HHmmss.toDate("2000-01-01 12:34:56"));

            MappingOptions options = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);
            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.lambdaInsert("user_info", options);
            assert lambdaInsert.applyMap(newValue).executeSumResult() == 1;

            List<UserInfo2> users = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForList();
            assert users.size() == 1;

            assert users.get(0).getUid().equals("uuiduuiduuid");
            assert users.get(0).getName().equals("yongchun.zyc");
            assert users.get(0).getLoginName().equals("abc");
            assert users.get(0).getPassword().equals("my password");
            assert users.get(0).getEmail().equals("zyc@hasor.net");
            assert users.get(0).getSeq().equals(111);
        }
    }
}
