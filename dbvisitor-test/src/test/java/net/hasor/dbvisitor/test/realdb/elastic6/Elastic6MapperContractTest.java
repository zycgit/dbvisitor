/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo1BaseMapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo1Mapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo1a;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo1b;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo2;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo2Mapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo3;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo3Mapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo4Mapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo5;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo5Mapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo6;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo6Mapper;
import static org.junit.Assert.*;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class Elastic6MapperContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_ANNOTATION_CRUD)
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo1Mapper infoMapper = s.createMapper(UserInfo1Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo1b user = new UserInfo1b();
            user.setUid("1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertTrue(saveStatus >= 0);

            // load
            UserInfo1b info = infoMapper.loadUser("1111");
            assertNotNull(info);
            assertNotSame(user, info);
            assertEquals("1111", info.getUid());
            assertEquals("username", info.getName());
            assertEquals("login_123", info.getLoginName());
            assertEquals("password", info.getLoginPassword());

            // verify with raw jdbc
            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"1111\"}}}");
            assertEquals(1, list.size());

            // Update through the annotated native command, then read persisted values.
            assertTrue(infoMapper.updateName("1111", "updated") >= 0);
            assertEquals("updated", infoMapper.loadUser("1111").getName());
            assertEquals("login_123", infoMapper.loadUser("1111").getLoginName());

            assertTrue(infoMapper.updateUser("1111", "updated-again", "new-login") >= 0);
            assertEquals("updated-again", infoMapper.loadUser("1111").getName());
            assertEquals("new-login", infoMapper.loadUser("1111").getLoginName());
            assertEquals("password", infoMapper.loadUser("1111").getLoginPassword());
            assertNull(infoMapper.loadUser("missing-user"));

            // delete
            int delStatus = infoMapper.deleteUser("1111");
            assertTrue(delStatus >= 0);

            list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"1111\"}}}");
            assertTrue(list.isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_ANNOTATION_CRUD)
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo2Mapper infoMapper = s.createMapper(UserInfo2Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo2 user = new UserInfo2();
            user.setUid("2222");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertTrue(saveStatus >= 0);

            // load
            UserInfo2 info = infoMapper.loadUser("2222");
            assertNotNull(info);
            assertNotSame(user, info);
            assertEquals("2222", info.getUid());
            assertEquals("username", info.getName());
            assertEquals("login_123", info.getLoginName());
            assertEquals("password", info.getLoginPassword());

            // delete
            int delStatus = infoMapper.deleteUser("2222");
            assertTrue(delStatus >= 0);

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"match\": {\"uid\": \"2222\"}}}");
            assertTrue(list.isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_XML_CRUD)
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo3Mapper infoMapper = s.createMapper(UserInfo3Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo3 user = new UserInfo3();
            user.setUid("3333");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertTrue(saveStatus >= 0);

            // load1
            UserInfo3 info1 = infoMapper.loadUser1("3333");
            assertNotNull(info1);
            assertEquals("3333", info1.getUid());
            assertEquals("username", info1.getName());
            assertEquals("login_123", info1.getLoginName());
            assertEquals("password", info1.getLoginPassword());

            // load2
            UserInfo1a info2 = infoMapper.loadUser2("3333");
            assertNotNull(info2);
            assertEquals("3333", info2.getUid());
            assertEquals("username", info2.getName());
            assertEquals("login_123", info2.getLoginName());
            assertEquals("password", info2.getLoginPassword());

            // Update through the XML native command and verify the stored entity.
            assertTrue(infoMapper.updateName("3333", "xml-updated") >= 0);
            assertEquals("xml-updated", infoMapper.loadUser1("3333").getName());
            assertEquals("login_123", infoMapper.loadUser1("3333").getLoginName());
            assertNull(infoMapper.loadUser1("missing-user"));

            // delete
            int delStatus = infoMapper.deleteUser("3333");
            assertTrue(delStatus >= 0);

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"3333\"}}}");
            assertTrue(list.isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_XML_CRUD)
    public void using_mapper_file_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo4Mapper infoMapper = s.createMapper(UserInfo4Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo1a user = new UserInfo1a();
            user.setUid("4444");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertTrue(saveStatus >= 0);

            // load1
            UserInfo1a info1 = infoMapper.loadUser("4444");
            assertNotNull(info1);
            assertEquals("4444", info1.getUid());
            assertEquals("username", info1.getName());
            assertEquals("login_123", info1.getLoginName());
            assertEquals("password", info1.getLoginPassword());

            // delete
            int delStatus = infoMapper.deleteUser("4444");
            assertTrue(delStatus >= 0);

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"match\": {\"uid\": \"4444\"}}}");
            assertTrue(list.isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_XML_RESULT_MAP)
    public void using_mapper_result_map() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo5Mapper infoMapper = s.createMapper(UserInfo5Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo5 user = new UserInfo5();
            user.setUserId("5555");
            user.setUserName("username");
            user.setAccount("login_123");
            user.setPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertTrue(saveStatus >= 0);

            // load
            UserInfo5 info = infoMapper.loadUser("5555");
            assertNotNull(info);
            assertNotSame(user, info);
            assertEquals("5555", info.getUserId());
            assertEquals("username", info.getUserName());
            assertEquals("login_123", info.getAccount());
            assertEquals("password", info.getPassword());

            // delete
            int delStatus = infoMapper.deleteUser("5555");
            assertTrue(delStatus >= 0);

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"5555\"}}}");
            assertTrue(list.isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_ANNOTATION_MAP)
    public void using_mapper_annotation_map() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo6Mapper infoMapper = s.createMapper(UserInfo6Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo6 user = new UserInfo6();
            user.setUserId("6666");
            user.setUserName("username");
            user.setAccount("login_123");
            user.setPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assertTrue(saveStatus >= 0);

            // load
            UserInfo6 info = infoMapper.loadUser("6666");
            assertNotNull(info);
            assertNotSame(user, info);
            assertEquals("6666", info.getUserId());
            assertEquals("username", info.getUserName());
            assertEquals("login_123", info.getAccount());
            assertEquals("password", info.getPassword());

            // delete
            int delStatus = infoMapper.deleteUser("6666");
            assertTrue(delStatus >= 0);

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"6666\"}}}");
            assertTrue(list.isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_BASEMAPPER_PAGE_BY_SAMPLE)
    public void using_base_mapper_pageBySample() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo1BaseMapper mapper = s.createMapper(UserInfo1BaseMapper.class);
            String loginName = "page_login";
            for (int i = 0; i < 5; i++) {
                UserInfo1a user = new UserInfo1a();
                user.setUid("p_" + i);
                user.setName("username_" + i);
                user.setLoginName(loginName);
                user.setLoginPassword("password_" + i);
                assertEquals(1, mapper.insert(user));
            }

            UserInfo1a sample = new UserInfo1a();
            sample.setLoginName(loginName);

            PageObject pageInfo = new PageObject(0, 2);
            PageResult<UserInfo1a> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uidKeyword", OrderType.ASC));
            assertEquals(2, page1.getData().size());
            assertEquals("p_0", page1.getData().get(0).getUid());
            assertEquals("p_1", page1.getData().get(1).getUid());

            pageInfo.nextPage();
            PageResult<UserInfo1a> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uidKeyword", OrderType.ASC));
            assertEquals(2, page2.getData().size());
            assertEquals("p_2", page2.getData().get(0).getUid());
            assertEquals("p_3", page2.getData().get(1).getUid());

            pageInfo.nextPage();
            PageResult<UserInfo1a> page3 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uidKeyword", OrderType.ASC));
            assertEquals(1, page3.getData().size());
            assertEquals("p_4", page3.getData().get(0).getUid());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_MAPPER_XML_PAGE_BY_PARAM)
    public void using_mapper_result_map_pageByParam() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            UserInfo5Mapper infoMapper = s.createMapper(UserInfo5Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            String userName = "paged_name";
            for (int i = 0; i < 5; i++) {
                UserInfo5 user = new UserInfo5();
                user.setUserId("u_" + i);
                user.setUserName(userName);
                user.setAccount("acc_" + i);
                user.setPassword("pwd_" + i);
                int saveStatus = infoMapper.saveUser(user);
                assertTrue(saveStatus >= 0);
            }

            PageObject pageInfo = new PageObject(0, 2);
            List<UserInfo5> page1 = infoMapper.listByUserName(userName, pageInfo);
            assertEquals(2, page1.size());
            assertEquals("u_0", page1.get(0).getUserId());
            assertEquals("u_1", page1.get(1).getUserId());

            pageInfo.nextPage();
            List<UserInfo5> page2 = infoMapper.listByUserName(userName, pageInfo);
            assertEquals(2, page2.size());
            assertEquals("u_2", page2.get(0).getUserId());
            assertEquals("u_3", page2.get(1).getUserId());

            pageInfo.nextPage();
            List<UserInfo5> page3 = infoMapper.listByUserName(userName, pageInfo);
            assertEquals(1, page3.size());
            assertEquals("u_4", page3.get(0).getUserId());
            pageInfo.nextPage();
            assertTrue(infoMapper.listByUserName(userName, pageInfo).isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_BASEMAPPER_PAGE_INIT)
    public void using_base_mapper_pageInit() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(newAdapterConnection())) {
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
            }

            UserInfo1BaseMapper mapper = s.createMapper(UserInfo1BaseMapper.class);
            String loginName = "page_login";
            for (int i = 0; i < 5; i++) {
                UserInfo1a user = new UserInfo1a();
                user.setUid("p_" + i);
                user.setName("username_" + i);
                user.setLoginName(loginName);
                user.setLoginPassword("password_" + i);
                assertEquals(1, mapper.insert(user));
            }

            UserInfo1a sample = new UserInfo1a();
            sample.setLoginName(loginName);
            Page page = mapper.pageInitBySample(sample, 0, 2);

            assertEquals(5, page.getTotalCount());
            assertEquals(2, page.getPageSize());
            assertEquals(0, page.getCurrentPage());
            assertEquals(3, page.getTotalPage());
        }
    }
}
