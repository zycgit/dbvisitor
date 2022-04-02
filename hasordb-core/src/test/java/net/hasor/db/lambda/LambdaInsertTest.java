/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.lambda;
import com.alibaba.druid.pool.DruidDataSource;
import net.hasor.db.dialect.BatchBoundSql;
import net.hasor.test.db.AbstractDbTest;
import net.hasor.test.db.dto.TB_User;
import net.hasor.test.db.dto.TbUser;
import net.hasor.test.db.utils.DsUtils;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.db.utils.TestUtils.newID;

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

        try (DruidDataSource dataSource = DsUtils.createDs()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
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
}
