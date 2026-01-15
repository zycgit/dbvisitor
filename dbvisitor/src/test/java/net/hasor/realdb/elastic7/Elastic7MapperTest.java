package net.hasor.realdb.elastic7;

import java.util.List;
import java.util.Map;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.realdb.elastic7.dto1.UserInfo1BaseMapper;
import net.hasor.realdb.elastic7.dto1.UserInfo1Mapper;
import net.hasor.realdb.elastic7.dto1.UserInfo1a;
import net.hasor.realdb.elastic7.dto1.UserInfo1b;
import net.hasor.realdb.elastic7.dto2.UserInfo2;
import net.hasor.realdb.elastic7.dto2.UserInfo2Mapper;
import net.hasor.realdb.elastic7.dto3.UserInfo3;
import net.hasor.realdb.elastic7.dto3.UserInfo3Mapper;
import net.hasor.realdb.elastic7.dto3.UserInfo4Mapper;
import net.hasor.realdb.elastic7.dto5.UserInfo5;
import net.hasor.realdb.elastic7.dto5.UserInfo5Mapper;
import net.hasor.realdb.elastic7.dto6.UserInfo6;
import net.hasor.realdb.elastic7.dto6.UserInfo6Mapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class Elastic7MapperTest {
    @Test
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
            UserInfo1Mapper infoMapper = s.createMapper(UserInfo1Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            System.out.println("Start clean");
            System.out.flush();
            try {
                jdbc.execute("DELETE /user_info");
            } catch (Throwable e) {
                System.out.println("Clean failed: " + e.getMessage());
            }

            UserInfo1b user = new UserInfo1b();
            user.setUid("1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            System.out.println("Before insert");
            int saveStatus = infoMapper.saveUser(user);
            System.out.println("After insert: " + saveStatus);
            assert saveStatus >= 0;

            // load
            UserInfo1b info = infoMapper.loadUser("1111");
            assert info != null;
            assert user != info;
            assert info.getUid().equals("1111");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // verify with raw jdbc
            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"1111\"}}}");
            assert list.size() == 1;
            // Elastic returns hits in a specific structure, but the driver might flatten it or return the source.
            // Let's assume the driver returns the source map or similar.
            // In MongoMapperTest: String json = (String) list.get(0).get("_JSON");
            // In Elastic driver, let's see what it returns.
            // If it returns the document source, we can check fields directly.

            // delete
            int delStatus = infoMapper.deleteUser("1111");
            assert delStatus >= 0;

            list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"1111\"}}}");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
            assert saveStatus >= 0;

            // load
            UserInfo2 info = infoMapper.loadUser("2222");
            assert info != null;
            assert user != info;
            assert info.getUid().equals("2222");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("2222");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"match\": {\"uid\": \"2222\"}}}");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
            assert saveStatus >= 0;

            // load1
            UserInfo3 info1 = infoMapper.loadUser1("3333");
            assert info1 != null;
            assert info1.getUid().equals("3333");
            assert info1.getName().equals("username");
            assert info1.getLoginName().equals("login_123");
            assert info1.getLoginPassword().equals("password");

            // load2
            UserInfo1a info2 = infoMapper.loadUser2("3333");
            assert info2 != null;
            assert info2.getUid().equals("3333");
            assert info2.getName().equals("username");
            assert info2.getLoginName().equals("login_123");
            assert info2.getLoginPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("3333");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"3333\"}}}");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_file_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
            assert saveStatus >= 0;

            // load1
            UserInfo1a info1 = infoMapper.loadUser("4444");
            assert info1 != null;
            assert info1.getUid().equals("4444");
            assert info1.getName().equals("username");
            assert info1.getLoginName().equals("login_123");
            assert info1.getLoginPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("4444");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"match\": {\"uid\": \"4444\"}}}");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_result_map() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
            assert saveStatus >= 0;

            // load
            UserInfo5 info = infoMapper.loadUser("5555");
            assert info != null;
            assert user != info;
            assert info.getUserId().equals("5555");
            assert info.getUserName().equals("username");
            assert info.getAccount().equals("login_123");
            assert info.getPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("5555");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"5555\"}}}");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_annotation_map() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
            assert saveStatus >= 0;

            // load
            UserInfo6 info = infoMapper.loadUser("6666");
            assert info != null;
            assert user != info;
            assert info.getUserId().equals("6666");
            assert info.getUserName().equals("username");
            assert info.getAccount().equals("login_123");
            assert info.getPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("6666");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("POST /user_info/_search {\"query\": {\"term\": {\"uid\": \"6666\"}}}");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_base_mapper_pageBySample() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
                assert mapper.insert(user) == 1;
            }

            UserInfo1a sample = new UserInfo1a();
            sample.setLoginName(loginName);

            PageObject pageInfo = new PageObject(0, 2);
            PageResult<UserInfo1a> page1 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uidKeyword", OrderType.ASC));
            assert page1.getData().size() == 2;
            assert "p_0".equals(page1.getData().get(0).getUid());
            assert "p_1".equals(page1.getData().get(1).getUid());

            pageInfo.nextPage();
            PageResult<UserInfo1a> page2 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uidKeyword", OrderType.ASC));
            assert page2.getData().size() == 2;
            assert "p_2".equals(page2.getData().get(0).getUid());
            assert "p_3".equals(page2.getData().get(1).getUid());

            pageInfo.nextPage();
            PageResult<UserInfo1a> page3 = mapper.pageBySample(sample, pageInfo, CollectionUtils.asMap("uidKeyword", OrderType.ASC));
            assert page3.getData().size() == 1;
            assert "p_4".equals(page3.getData().get(0).getUid());
        }
    }

    @Test
    public void using_mapper_result_map_pageByParam() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
                assert saveStatus >= 0;
            }

            PageObject pageInfo = new PageObject(0, 2);
            List<UserInfo5> page1 = infoMapper.listByUserName(userName, pageInfo);
            assert page1.size() == 2;

            pageInfo.nextPage();
            List<UserInfo5> page2 = infoMapper.listByUserName(userName, pageInfo);
            assert page2.size() == 2;

            pageInfo.nextPage();
            List<UserInfo5> page3 = infoMapper.listByUserName(userName, pageInfo);
            assert page3.size() == 1;
        }
    }

    @Test
    public void using_base_mapper_pageInit() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.es7Conn())) {
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
                assert mapper.insert(user) == 1;
            }

            UserInfo1a sample = new UserInfo1a();
            sample.setLoginName(loginName);
            Page page = mapper.pageInitBySample(sample, 0, 2);

            assert page.getTotalCount() == 5;
            assert page.getPageSize() == 2;
            assert page.getCurrentPage() == 0;
            assert page.getTotalPage() == 3;
        }
    }
}