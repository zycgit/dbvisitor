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
import net.hasor.dbvisitor.lambda.dto.AnnoUserInfoDTO;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.newID;

/**
 * Lambda 方式执行 Insert 操作
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-3-22
 */
public class KeyTypeEntTest {
    @Test
    public void autoId_overwrite() throws Throwable {
        AnnoUserInfoDTO user1 = new AnnoUserInfoDTO();
        user1.setUid(newID());
        user1.setName("默罕默德");
        user1.setLoginName("muhammad");
        user1.setPassword("1");
        user1.setEmail("muhammad@hasor.net");
        user1.setSeq(1);
        user1.setCreateTime(new Date());

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.getJdbc().execute("delete from user_info");

            InsertOperation<AnnoUserInfoDTO> insert = lambdaTemplate.insertByEntity(AnnoUserInfoDTO.class);
            insert.applyEntity(user1);

            assert insert.getBoundSql() instanceof BatchBoundSql;

            int i = insert.executeSumResult();
            assert i == 1;

            List<UserInfo2> tbUsers = lambdaTemplate.queryByEntity(UserInfo2.class).queryForList();
            assert tbUsers.size() == 1;
            List<String> ids = tbUsers.stream().map(UserInfo2::getUid).collect(Collectors.toList());
            assert ids.contains(user1.getUid());
        }
    }

    @Test
    public void autoId_usingUUID32() throws Throwable {
        AnnoUserInfoDTO user1 = new AnnoUserInfoDTO();
        user1.setName("默罕默德");
        user1.setLoginName("muhammad");
        user1.setPassword("1");
        user1.setEmail("muhammad@hasor.net");
        user1.setSeq(1);
        user1.setCreateTime(new Date());

        assert user1.getUid() == null;

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.getJdbc().execute("delete from user_info");

            InsertOperation<AnnoUserInfoDTO> lambdaInsert = lambdaTemplate.insertByEntity(AnnoUserInfoDTO.class);
            lambdaInsert.applyEntity(user1);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            String uidFor1 = user1.getUid();
            assert uidFor1 != null;

            int i = lambdaInsert.executeSumResult();
            assert i == 1;

            List<user_info> tbUsers = lambdaTemplate.queryByEntity(user_info.class).queryForList();
            assert tbUsers.size() == 1;
            List<String> ids = tbUsers.stream().map(user_info::getUser_uuid).collect(Collectors.toList());
            assert ids.contains(uidFor1);
        }
    }
}
