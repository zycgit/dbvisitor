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
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.lambda.dto.AnnoUserInfoDTO;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.dto.user_info;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.hasor.test.utils.TestUtils.*;

/***
 * Lambda 方式执行 Select 操作
 * @version : 2021-3-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaQueryTest extends AbstractDbTest {

    @Test
    public void lambda_select_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<UserInfo2> users1 = lambdaTemplate.queryBySpace(UserInfo2.class).queryForList();
            List<String> collect1 = users1.stream().map(UserInfo2::getName).collect(Collectors.toList());
            assert collect1.size() == 3;
            assert collect1.contains(beanForData1().getName());
            assert collect1.contains(beanForData2().getName());
            assert collect1.contains(beanForData3().getName());

            List<Map<String, Object>> users2 = lambdaTemplate.queryBySpace(UserInfo2.class).queryForMapList();
            List<String> collect2 = users2.stream().map(tbUser -> {
                return (String) tbUser.get("name");
            }).collect(Collectors.toList());
            assert collect2.size() == 3;
            assert collect2.contains(beanForData1().getName());
            assert collect2.contains(beanForData2().getName());
            assert collect2.contains(beanForData3().getName());

            List<String> users3 = new ArrayList<>();
            lambdaTemplate.queryBySpace(UserInfo2.class).query((rs, rowNum) -> {
                users3.add(rs.getString("user_name"));
            });
            assert users3.size() == 3;
            assert users3.contains(beanForData1().getName());
            assert users3.contains(beanForData2().getName());
            assert users3.contains(beanForData3().getName());

            List<Map<String, Object>> users4 = lambdaTemplate.queryBySpace(UserInfo2.class)//
                    .query(new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()));
            List<String> collect4 = users4.stream().map(tbUser -> {
                return (String) tbUser.get("user_name");
            }).collect(Collectors.toList());
            assert collect4.size() == 3;
            assert collect4.contains(beanForData1().getName());
            assert collect4.contains(beanForData2().getName());
            assert collect4.contains(beanForData3().getName());

            List<Map<String, Object>> users5 = lambdaTemplate.queryBySpace(UserInfo2.class).query(new ColumnMapRowMapper());
            List<String> collect5 = users5.stream().map(tbUser -> {
                return (String) tbUser.get("user_name");
            }).collect(Collectors.toList());
            assert collect5.size() == 3;
            assert collect5.contains(beanForData1().getName());
            assert collect5.contains(beanForData2().getName());
            assert collect5.contains(beanForData3().getName());
        }
    }

    @Test
    public void lambdaQuery_select_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<UserInfo2> users1 = lambdaTemplate.queryBySpace(UserInfo2.class).selectAll().queryForList();
            List<UserInfo2> users2 = lambdaTemplate.queryBySpace(UserInfo2.class).queryForList();
            assert users1.size() == 3;
            assert users2.size() == 3;

            Map<String, Object> forData1 = mapForData1();
            List<UserInfo2> users3 = lambdaTemplate.queryBySpace(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, forData1.get("loginName")).queryForList();
            assert users3.size() == 1;
            assert users3.get(0).getLoginName().equals("muhammad");
            assert users3.get(0).getLoginName().equals(forData1.get("loginName"));
            assert users3.get(0).getUid().equals(forData1.get("userUUID"));
        }
    }

    @Test
    public void lambdaQuery_select_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            List<user_info> users1 = lambdaTemplate.queryBySpace(user_info.class).selectAll().queryForList();
            List<user_info> users2 = lambdaTemplate.queryBySpace(user_info.class).queryForList();
            assert users1.size() == 3;
            assert users2.size() == 3;

            Map<String, Object> forData1 = mapForData1();
            List<user_info> users3 = lambdaTemplate.queryBySpace(user_info.class)//
                    .eq(user_info::getLogin_name, forData1.get("loginName")).queryForList();
            assert users3.size() == 1;
            assert users3.get(0).getLogin_name().equals("muhammad");
            assert users3.get(0).getLogin_name().equals(forData1.get("loginName"));
            assert users3.get(0).getUser_uuid().equals(forData1.get("userUUID"));
        }
    }

    @Test
    public void lambdaQuery_select_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserInfo2 tbUser = lambdaTemplate.queryBySpace(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1").queryForObject();

            assert tbUser.getName().equals("默罕默德");
        }
    }

    @Test
    public void lambdaQuery_select_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            Map<String, Object> tbUser = lambdaTemplate.queryBySpace(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();

            assert tbUser.get("name").equals("默罕默德");
            assert tbUser.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void lambdaQuery_lambdaCount_6() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            int lambdaCount1 = lambdaTemplate.queryBySpace(UserInfo2.class)//
                    .eq(UserInfo2::getLoginName, "muhammad")//
                    .queryForCount();
            assert lambdaCount1 == 1;
            assert lambdaTemplate.queryBySpace(UserInfo2.class).queryForCount() == 3;
            assert lambdaTemplate.queryBySpace(UserInfo2.class).queryForLargeCount() == 3L;
        }
    }

    @Test
    public void base_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            Map<String, Object> userInfo = lambda.queryBySpace(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo.get("name").equals("默罕默德");
            assert userInfo.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate1 = new LambdaTemplate(c);
            Map<String, Object> userInfo1 = lambdaTemplate1.queryBySpace(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo1.get("name").equals("默罕默德");
            assert userInfo1.get("loginName").equals("muhammad");

            LambdaTemplate lambdaTemplate2 = new LambdaTemplate(c);
            Map<String, Object> res = lambdaTemplate2.queryBySpace(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1").queryForMap();
            assert res.get("name").equals("默罕默德");
            assert res.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_3() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            Map<String, Object> userInfo = lambdaTemplate.queryBySpace(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert userInfo.get("name").equals("默罕默德");
            assert userInfo.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_4() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            Map<String, Object> tbUser = lambdaTemplate.queryBySpace(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert tbUser.get("name").equals("默罕默德");
            assert tbUser.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void base_5() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(new JdbcTemplate(c));

            Map<String, Object> tbUser = lambdaTemplate.queryBySpace(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();
            assert tbUser.get("name").equals("默罕默德");
            assert tbUser.get("loginName").equals("muhammad");
        }
    }
}
