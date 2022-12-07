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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dialect.BatchBoundSql;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.AutoId;
import net.hasor.test.dto.TB_User;
import net.hasor.test.dto.TbUser;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.newID;

/***
 * Lambda 方式执行 Insert 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaInsertTest extends AbstractDbTest {
    @Test
    public void lambda_insert_1() throws Throwable {
        TbUser tbUser1 = new TbUser();
        tbUser1.setUid(newID());
        tbUser1.setName("默罕默德");
        tbUser1.setAccount("muhammad");
        tbUser1.setPassword("1");
        tbUser1.setMail("muhammad@hasor.net");
        tbUser1.setIndex(1);
        tbUser1.setCreateTime(new Date());
        Map<String, Object> tbUser2 = new HashMap<>();
        tbUser2.put("uid", newID());
        tbUser2.put("name", "安妮.贝隆");
        tbUser2.put("account", "belon");
        tbUser2.put("password", "2");
        tbUser2.put("mail", "belon@hasor.net");
        tbUser2.put("index", 2);
        tbUser2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from tb_user");
            //
            InsertOperation<TbUser> lambdaInsert = lambdaTemplate.lambdaInsert(TbUser.class);
            lambdaInsert.applyEntity(tbUser1);
            lambdaInsert.applyMap(tbUser2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            //
            int i = lambdaInsert.executeSumResult();
            assert i == 2;
            //
            List<TB_User> tbUsers = lambdaTemplate.lambdaQuery(TB_User.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(TB_User::getUserUUID).collect(Collectors.toList());
            assert ids.contains(tbUser1.getUid());
            assert ids.contains(tbUser2.get("uid"));
        }
    }

    @Test
    public void lambda_insert_2() throws Throwable {
        TbUser tbUser1 = new TbUser();
        tbUser1.setName("默罕默德");
        tbUser1.setAccount("muhammad");
        tbUser1.setPassword("1");
        tbUser1.setMail("muhammad@hasor.net");
        tbUser1.setIndex(1);
        tbUser1.setCreateTime(new Date());
        Map<String, Object> tbUser2 = new HashMap<>();
        tbUser2.put("name", "安妮.贝隆");
        tbUser2.put("account", "belon");
        tbUser2.put("password", "2");
        tbUser2.put("mail", "belon@hasor.net");
        tbUser2.put("index", 2);
        tbUser2.put("createTime", new Date());

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from tb_user");
            //
            InsertOperation<TbUser> lambdaInsert = lambdaTemplate.lambdaInsert(TbUser.class);
            lambdaInsert.applyEntity(tbUser1);
            lambdaInsert.applyMap(tbUser2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            //
            int i = lambdaInsert.executeSumResult();
            assert i == 2;
            //
            List<TB_User> tbUsers = lambdaTemplate.lambdaQuery(TB_User.class).queryForList();
            assert tbUsers.size() == 2;
            List<String> ids = tbUsers.stream().map(TB_User::getUserUUID).collect(Collectors.toList());
            assert ids.contains(tbUser1.getUid());
            assert ids.contains(tbUser2.get("uid"));
        }
    }

    @Test
    public void lambda_insert_3() throws Throwable {
        AutoId tbUser1 = new AutoId();
        tbUser1.setName("默罕默德");
        Map<String, Object> tbUser2 = CollectionUtils.asMap("name", "安妮.贝隆");

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from auto_id");
            //
            InsertOperation<AutoId> lambdaInsert = lambdaTemplate.lambdaInsert(AutoId.class);
            lambdaInsert.applyEntity(tbUser1);
            lambdaInsert.applyMap(tbUser2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            //
            int i = lambdaInsert.executeSumResult();
            assert i == 2;
            //
            List<AutoId> tbUsers = lambdaTemplate.lambdaQuery(AutoId.class).queryForList();
            assert tbUsers.size() == 2;

            List<String> uids = tbUsers.stream().map(AutoId::getUid).collect(Collectors.toList());
            assert StringUtils.isNotBlank(uids.get(0));
            assert StringUtils.isNotBlank(uids.get(1));
            assert uids.contains(tbUser1.getUid());
            assert uids.contains(tbUser2.get("uid"));

            List<Integer> ids = tbUsers.stream().map(AutoId::getId).collect(Collectors.toList());
            assert ids.contains(tbUser1.getId());
            assert ids.contains(tbUser2.get("id"));
        }
    }

    @Test
    public void lambda_insert_4() throws Throwable {
        Map<String, Object> tbUser1 = new LinkedCaseInsensitiveMap<>(CollectionUtils.asMap("name", "默罕默德", "uid", "uuid-1"));
        Map<String, Object> tbUser2 = new LinkedCaseInsensitiveMap<>(CollectionUtils.asMap("name", "安妮.贝隆", "uid", "uuid-2"));

        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.execute("delete from auto_id");
            //
            InsertOperation<Map<String, Object>> lambdaInsert = lambdaTemplate.lambdaInsert("auto_id");
            lambdaInsert.applyMap(tbUser1);
            lambdaInsert.applyMap(tbUser2);

            assert lambdaInsert.getBoundSql() instanceof BatchBoundSql;
            //
            int i = lambdaInsert.executeSumResult();
            assert i == 2;
            //
            List<AutoId> tbUsers = lambdaTemplate.lambdaQuery(AutoId.class).queryForList();
            assert tbUsers.size() == 2;

            List<String> uids = tbUsers.stream().map(AutoId::getUid).collect(Collectors.toList());
            assert StringUtils.isNotBlank(uids.get(0));
            assert StringUtils.isNotBlank(uids.get(1));
            assert uids.contains(tbUser1.get("uid"));
            assert uids.contains(tbUser2.get("uid"));

            List<Integer> ids = tbUsers.stream().map(AutoId::getId).collect(Collectors.toList());
            assert ids.contains(tbUser1.get("id"));
            assert ids.contains(tbUser2.get("id"));
        }
    }
}
