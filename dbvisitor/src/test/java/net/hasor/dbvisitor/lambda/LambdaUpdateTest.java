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
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.hasor.test.utils.TestUtils.beanForData1;

/***
 * Lambda 方式执行 Update 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaUpdateTest extends AbstractDbTest {
    @Test
    public void lambda_update_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            EntityQueryOperation<user_info> lambdaQuery = lambdaTemplate.lambdaQuery(user_info.class);
            user_info tbUser1 = lambdaQuery.eq(user_info::getLogin_name, beanForData1().getLoginName()).queryForObject();
            assert tbUser1.getUser_name() != null;

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("user_name", null);

            EntityUpdateOperation<user_info> lambdaUpdate = lambdaTemplate.lambdaUpdate(user_info.class);
            int update = lambdaUpdate.eq(user_info::getLogin_name, "muhammad")//
                    .updateToMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            user_info tbUser2 = lambdaTemplate.lambdaQuery(user_info.class).eq(user_info::getLogin_name, "muhammad").queryForObject();
            assert tbUser2.getUser_name() == null;
        }
    }

    @Test
    public void lambda_update_map_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<UserInfo2> users = lambdaTemplate.lambdaQuery(UserInfo2.class).queryForList();
            UserInfo2 info = users.get(0);

            MappingOptions options = MappingOptions.buildNew().mapUnderscoreToCamelCase(true);
            MapUpdateOperation update = lambdaTemplate.lambdaUpdate("user_info", options);
            assert update.eq("user_uuid", info.getUid()).updateTo("loginPassword", "newPassword").doUpdate() == 1;

            Map<String, Object> maps = lambdaTemplate.queryForObject("select * from user_info where user_uuid = ?", new Object[] { info.getUid() }, Map.class);
            assert maps.get("login_password").equals("newPassword");
        }
    }

    @Test
    public void lambda_update_pk_0() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            EntityQueryOperation<user_info> lambdaQuery = lambdaTemplate.lambdaQuery(user_info.class);
            user_info tbUser1 = lambdaQuery.eq(user_info::getLogin_name, beanForData1().getLoginName()).queryForObject();
            assert tbUser1.getUser_name() != null;

            HashMap<String, Object> valueMap = new HashMap<>();
            valueMap.put("user_name", null);

            EntityUpdateOperation<user_info> lambdaUpdate = lambdaTemplate.lambdaUpdate(user_info.class);
            int update = lambdaUpdate.eq(user_info::getLogin_name, "muhammad")//
                    .updateToMap(valueMap)//
                    .doUpdate();
            assert update == 1;

            user_info tbUser2 = lambdaTemplate.lambdaQuery(user_info.class).eq(user_info::getLogin_name, "muhammad").queryForObject();
            assert tbUser2.getUser_name() == null;
        }
    }
}
