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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.Map;

/***
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class BaseLambdaTest extends AbstractDbTest {
    @Test
    public void base_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> userInfo = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo.get("name").equals("默罕默德");
            assert userInfo.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate1 = new LambdaTemplate(c);
            Map<String, Object> userInfo1 = lambdaTemplate1.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo1.get("name").equals("默罕默德");
            assert userInfo1.get("loginName").equals("muhammad");

            LambdaTemplate lambdaTemplate2 = new LambdaTemplate(c);
            Map<String, Object> userInfo2 = lambdaTemplate2.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo2.get("name").equals("默罕默德");
            assert userInfo2.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            Map<String, Object> userInfo = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo.get("name").equals("默罕默德");
            assert userInfo.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            Map<String, Object> tbUser = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert tbUser.get("name").equals("默罕默德");
            assert tbUser.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(new JdbcTemplate(c));

            Map<String, Object> tbUser = lambdaTemplate.lambdaQuery(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert tbUser.get("name").equals("默罕默德");
            assert tbUser.get("loginName").equals("muhammad");
        }
    }
}
