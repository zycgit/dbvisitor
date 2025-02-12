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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.newID;

/**
 * Lambda 方式执行 Insert 操作
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-3-22
 */
public class DoEntInsertTest {

    @Test
    public void batchInsert_ent() throws Throwable {
        AnnoUserInfoDTO user1 = new AnnoUserInfoDTO();
        user1.setUid(newID());
        user1.setName("默罕默德");
        user1.setLoginName("muhammad");
        user1.setPassword("1");
        user1.setEmail("muhammad@hasor.net");
        user1.setSeq(1);
        user1.setCreateTime(new Date());

        AnnoUserInfoDTO user2 = new AnnoUserInfoDTO();
        user2.setUid(newID());
        user2.setName("安妮.贝隆");
        user2.setLoginName("belon");
        user2.setPassword("2");
        user2.setEmail("belon@hasor.net");
        user2.setSeq(2);
        user2.setCreateTime(new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            InsertWrapper<AnnoUserInfoDTO> insert = lambdaTemplate.insert(AnnoUserInfoDTO.class);
            insert.applyEntity(user1, user2);

            assert insert.executeSumResult() == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.getUid());
            assert ids.contains(user2.getUid());
        }
    }

    @Test
    public void batchInsert_ent_2map() throws Throwable {
        Map<String, Object> user1 = new LinkedHashMap<>();
        user1.put("uid", newID());
        user1.put("name", "默罕默德");
        user1.put("loginName", "muhammad");
        user1.put("password", "1");
        user1.put("mail", "muhammad@hasor.net");
        user1.put("index", 1);
        user1.put("createTime", new Date());

        Map<String, Object> user2 = new LinkedHashMap<>();
        user2.put("uid", newID());
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("index", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            MapInsertWrapper insert = lambdaTemplate.insert(AnnoUserInfoDTO.class).asMap();
            insert.applyEntity(user1, user2);

            assert insert.executeSumResult() == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.get("uid"));
            assert ids.contains(user2.get("uid"));
        }
    }

    @Test
    public void batchInsert_map() throws Throwable {
        Map<String, Object> user1 = new LinkedHashMap<>();
        user1.put("uid", newID());
        user1.put("name", "默罕默德");
        user1.put("loginName", "muhammad");
        user1.put("password", "1");
        user1.put("mail", "muhammad@hasor.net");
        user1.put("index", 1);
        user1.put("createTime", new Date());

        Map<String, Object> user2 = new LinkedHashMap<>();
        user2.put("uid", newID());
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("index", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            InsertWrapper<AnnoUserInfoDTO> insert = lambdaTemplate.insert(AnnoUserInfoDTO.class);
            insert.applyMap(user1, user2);

            assert insert.executeSumResult() == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.get("uid"));
            assert ids.contains(user2.get("uid"));
        }
    }

    @Test
    public void batchInsert_map_2map() throws Throwable {
        Map<String, Object> user1 = new LinkedHashMap<>();
        user1.put("uid", newID());
        user1.put("name", "默罕默德");
        user1.put("loginName", "muhammad");
        user1.put("password", "1");
        user1.put("mail", "muhammad@hasor.net");
        user1.put("index", 1);
        user1.put("createTime", new Date());

        Map<String, Object> user2 = new LinkedHashMap<>();
        user2.put("uid", newID());
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("index", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            MapInsertWrapper insert = lambdaTemplate.insert(AnnoUserInfoDTO.class).asMap();
            insert.applyMap(user1, user2);

            assert insert.executeSumResult() == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.get("uid"));
            assert ids.contains(user2.get("uid"));
        }
    }

    @Test
    public void batchInsert_mix() throws Throwable {
        AnnoUserInfoDTO user1 = new AnnoUserInfoDTO();
        user1.setUid(newID());
        user1.setName("默罕默德");
        user1.setLoginName("muhammad");
        user1.setPassword("1");
        user1.setEmail("muhammad@hasor.net");
        user1.setSeq(1);
        user1.setCreateTime(new Date());

        Map<String, Object> user2 = new LinkedHashMap<>();
        user2.put("uid", newID());
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("index", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            InsertWrapper<AnnoUserInfoDTO> insert = lambdaTemplate.insert(AnnoUserInfoDTO.class);
            insert.applyEntity(user1);
            insert.applyMap(user2);

            assert insert.getBoundSql() instanceof BatchBoundSql;
            assert insert.executeSumResult() == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.getUid());
            assert ids.contains(user2.get("uid"));
        }
    }

    @Test
    public void batchInsert_mix_2map() throws Throwable {
        Map<String, Object> user1 = new LinkedHashMap<>();
        user1.put("uid", newID());
        user1.put("name", "默罕默德");
        user1.put("loginName", "muhammad");
        user1.put("password", "1");
        user1.put("mail", "muhammad@hasor.net");
        user1.put("index", 1);
        user1.put("createTime", new Date());

        Map<String, Object> user2 = new LinkedHashMap<>();
        user2.put("uid", newID());
        user2.put("name", "安妮.贝隆");
        user2.put("loginName", "belon");
        user2.put("password", "2");
        user2.put("mail", "belon@hasor.net");
        user2.put("index", 2);
        user2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            MapInsertWrapper insert = lambdaTemplate.insert(AnnoUserInfoDTO.class).asMap();
            insert.applyEntity(user1);
            insert.applyMap(user2);

            assert insert.getBoundSql() instanceof BatchBoundSql;
            assert insert.executeSumResult() == 2;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.get("uid"));
            assert ids.contains(user2.get("uid"));
        }
    }
}
