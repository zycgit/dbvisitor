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
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.wrapper.dto.AnnoUserInfoDTO;
import net.hasor.dbvisitor.wrapper.dto.UserInfo;
import net.hasor.test.dto.UserInfo2;
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
public class DoEntQueryTest {
    @Test
    public void selectAll_forList_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<AnnoUserInfoDTO> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).queryForList();
            List<String> collect = users.stream().map(AnnoUserInfoDTO::getName).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forList_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).asMap()//
                    .queryForList();
            List<String> collect = users.stream().map(m -> m.get("name").toString()).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forList_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<UserInfo> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).queryForList(UserInfo.class);
            List<String> collect = users.stream().map(UserInfo::getEmail).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getEmail());
            assert collect.contains(beanForData2().getEmail());
            assert collect.contains(beanForData3().getEmail());
        }
    }

    @Test
    public void selectAll_forList_2_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<UserInfo> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).asMap()//
                    .queryForList(UserInfo.class);
            List<String> collect = users.stream().map(UserInfo::getEmail).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getEmail());
            assert collect.contains(beanForData2().getEmail());
            assert collect.contains(beanForData3().getEmail());
        }
    }

    @Test
    public void selectAll_forMapList_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).queryForMapList();
            List<String> collect2 = users.stream().map(tbUser -> (String) tbUser.get("name")).collect(Collectors.toList());

            assert collect2.size() == 3;
            assert collect2.contains(beanForData1().getName());
            assert collect2.contains(beanForData2().getName());
            assert collect2.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forMapList_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).asMap()//
                    .queryForMapList();
            List<String> collect2 = users.stream().map(tbUser -> (String) tbUser.get("name")).collect(Collectors.toList());

            assert collect2.size() == 3;
            assert collect2.contains(beanForData1().getName());
            assert collect2.contains(beanForData2().getName());
            assert collect2.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forCallBack_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<String> users = new ArrayList<>();
            new WrapperAdapter(c).query(AnnoUserInfoDTO.class).query((rs, rowNum) -> {
                users.add(rs.getString("user_name"));
            });

            assert users.size() == 3;
            assert users.contains(beanForData1().getName());
            assert users.contains(beanForData2().getName());
            assert users.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forCallBack_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<String> users = new ArrayList<>();
            new WrapperAdapter(c).query(AnnoUserInfoDTO.class).asMap()//
                    .query((rs, rowNum) -> {
                        users.add(rs.getString("user_name"));
                    });

            assert users.size() == 3;
            assert users.contains(beanForData1().getName());
            assert users.contains(beanForData2().getName());
            assert users.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forExtractor_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> list = new WrapperAdapter(c).query(AnnoUserInfoDTO.class)//
                    .query(new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()));

            List<String> collect = list.stream().map(tbUser -> {
                return (String) tbUser.get("user_name");
            }).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forExtractor_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> list = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).asMap()//
                    .query(new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()));

            List<String> collect = list.stream().map(tbUser -> {
                return (String) tbUser.get("user_name");
            }).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forRowMapper_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> list = new WrapperAdapter(c).query(AnnoUserInfoDTO.class)//
                    .queryForList(new ColumnMapRowMapper());

            List<String> collect = list.stream().map(tbUser -> {
                return (String) tbUser.get("user_name");
            }).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_forRowMapper_1_2map() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            List<Map<String, Object>> list = new WrapperAdapter(c).query(AnnoUserInfoDTO.class).asMap()//
                    .queryForList(new ColumnMapRowMapper());

            List<String> collect = list.stream().map(tbUser -> {
                return (String) tbUser.get("user_name");
            }).collect(Collectors.toList());

            assert collect.size() == 3;
            assert collect.contains(beanForData1().getName());
            assert collect.contains(beanForData2().getName());
            assert collect.contains(beanForData3().getName());
        }
    }

    @Test
    public void selectAll_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            List<AnnoUserInfoDTO> users1 = lambda.query(AnnoUserInfoDTO.class).selectAll().queryForList();
            List<AnnoUserInfoDTO> users2 = lambda.query(AnnoUserInfoDTO.class).queryForList();
            assert users1.size() == 3;
            assert users2.size() == 3;
        }
    }

    @Test
    public void selectList_condition_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            Map<String, Object> forData1 = mapForData1();

            List<AnnoUserInfoDTO> users = new WrapperAdapter(c).query(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, forData1.get("loginName")).queryForList();

            assert users.size() == 1;
            assert users.get(0).getLoginName().equals("muhammad");
            assert users.get(0).getLoginName().equals(forData1.get("loginName"));
            assert users.get(0).getUid().equals(forData1.get("userUUID"));
        }
    }

    @Test
    public void selectObject_condition_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            AnnoUserInfoDTO tbUser = new WrapperAdapter(c).query(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1").queryForObject();

            assert tbUser.getName().equals("默罕默德");
        }
    }

    @Test
    public void selectMap_condition_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            Map<String, Object> tbUser = new WrapperAdapter(c).query(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad").apply("limit 1")//
                    .queryForMap();

            assert tbUser.get("name").equals("默罕默德");
            assert tbUser.get("loginName").equals("muhammad");
        }
    }

    @Test
    public void selectCount_condition_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter lambda = new WrapperAdapter(c);

            int lambdaCount1 = lambda.query(AnnoUserInfoDTO.class)//
                    .eq(AnnoUserInfoDTO::getLoginName, "muhammad")//
                    .queryForCount();

            assert lambdaCount1 == 1;
            assert lambda.query(UserInfo2.class).queryForCount() == 3;
            assert lambda.query(UserInfo2.class).queryForLargeCount() == 3L;
        }
    }
}
