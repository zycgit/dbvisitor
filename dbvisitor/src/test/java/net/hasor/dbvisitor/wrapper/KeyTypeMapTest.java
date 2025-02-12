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
public class KeyTypeMapTest {
    @Test
    public void autoId_overwrite() throws Throwable {
        Map<String, Object> user1 = new LinkedHashMap<>();
        user1.put("uid", newID());
        user1.put("name", "安妮.贝隆");
        user1.put("loginName", "belon");
        user1.put("password", "2");
        user1.put("mail", "belon@hasor.net");
        user1.put("index", 2);
        user1.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            InsertWrapper<AnnoUserInfoDTO> insert = lambdaTemplate.insert(AnnoUserInfoDTO.class);
            insert.applyMap(user1);

            assert insert.getBoundSql() instanceof BatchBoundSql;
            assert insert.executeSumResult() == 1;

            List<UserInfo2> tbUsers = lambdaTemplate.query(UserInfo2.class).queryForList();
            assert tbUsers.size() == 1;
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.get("uid"));
        }
    }

    @Test
    public void autoId_usingUUID32() throws Throwable {
        Map<String, Object> user1 = new LinkedHashMap<>();
        user1.put("name", "安妮.贝隆");
        user1.put("loginName", "belon");
        user1.put("password", "2");
        user1.put("mail", "belon@hasor.net");
        user1.put("seq", 2);
        user1.put("createTime", new Date());

        assert user1.get("uid") == null;

        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambdaTemplate = new WrapperAdapter(c);
            lambdaTemplate.jdbc().execute("delete from user_info");

            InsertWrapper<AnnoUserInfoDTO> lambdaInsert = lambdaTemplate.insert(AnnoUserInfoDTO.class);
            lambdaInsert.applyMap(user1);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            String uidFor1 = (String) user1.get("uid");
            assert uidFor1 != null;

            assert lambdaInsert.executeSumResult() == 1;

            List<AnnoUserInfoDTO> tbUsers = lambdaTemplate.query(AnnoUserInfoDTO.class).queryForList();
            assert tbUsers.size() == 1;
            List<String> ids = tbUsers.stream().map(AnnoUserInfoDTO::getUid).collect(Collectors.toList());
            assert ids.contains(uidFor1);
        }
    }
}
