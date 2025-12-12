package net.hasor.realdb.mongo;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.realdb.mongo.dto1.UserInfo1;
import net.hasor.realdb.mongo.dto1.UserInfo1Mapper;
import net.hasor.realdb.mongo.dto2.UserInfo2;
import net.hasor.realdb.mongo.dto2.UserInfo2Mapper;
import net.hasor.realdb.mongo.dto3.UserInfo3;
import net.hasor.realdb.mongo.dto3.UserInfo3Mapper;
import net.hasor.realdb.mongo.dto3.UserInfo4Mapper;
import net.hasor.realdb.mongo.dto5.UserInfo5;
import net.hasor.realdb.mongo.dto5.UserInfo5Mapper;
import net.hasor.realdb.mongo.dto6.UserInfo6;
import net.hasor.realdb.mongo.dto6.UserInfo6Mapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class MongoMapperTest {
    @Test
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mongoConn())) {
            UserInfo1Mapper infoMapper = s.createMapper(UserInfo1Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            System.out.println("Start clean");
            System.out.flush();
            try {
                jdbc.execute("test.user_info.drop()");
            } catch (Throwable e) {
                System.out.println("Clean failed: " + e.getMessage());
            }

            UserInfo1 user = new UserInfo1();
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
            UserInfo1 info = infoMapper.loadUser("1111");
            assert info != null;
            assert user != info;
            assert info.getUid().equals("1111");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // verify with raw jdbc
            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find({uid: '1111'})");
            assert list.size() == 1;
            String json = (String) list.get(0).get("_JSON");
            assert json.contains("\"uid\": \"1111\"");

            // delete
            int delStatus = infoMapper.deleteUser("1111");
            assert delStatus >= 0;

            list = jdbc.queryForList("test.user_info.find({uid: '1111'})");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mongoConn())) {
            UserInfo2Mapper infoMapper = s.createMapper(UserInfo2Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("test.user_info.drop()");
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

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find({uid: '2222'})");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mongoConn())) {
            UserInfo3Mapper infoMapper = s.createMapper(UserInfo3Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("test.user_info.drop()");
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
            UserInfo1 info2 = infoMapper.loadUser2("3333");
            assert info2 != null;
            assert info2.getUid().equals("3333");
            assert info2.getName().equals("username");
            assert info2.getLoginName().equals("login_123");
            assert info2.getLoginPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("3333");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find({uid: '3333'})");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_file_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mongoConn())) {
            UserInfo4Mapper infoMapper = s.createMapper(UserInfo4Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("test.user_info.drop()");
            } catch (Throwable e) {
            }

            UserInfo1 user = new UserInfo1();
            user.setUid("4444");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assert saveStatus >= 0;

            // load1
            UserInfo1 info1 = infoMapper.loadUser("4444");
            assert info1 != null;
            assert info1.getUid().equals("4444");
            assert info1.getName().equals("username");
            assert info1.getLoginName().equals("login_123");
            assert info1.getLoginPassword().equals("password");

            // delete
            int delStatus = infoMapper.deleteUser("4444");
            assert delStatus >= 0;

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find({uid: '4444'})");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_result_map() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mongoConn())) {
            UserInfo5Mapper infoMapper = s.createMapper(UserInfo5Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("test.user_info.drop()");
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

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find({uid: '5555'})");
            assert list.isEmpty();
        }
    }

    @Test
    public void using_mapper_annotation_map() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.mongoConn())) {
            UserInfo6Mapper infoMapper = s.createMapper(UserInfo6Mapper.class);
            JdbcTemplate jdbc = new JdbcTemplate(s.getConnection());

            // clean
            try {
                jdbc.execute("test.user_info.drop()");
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

            List<Map<String, Object>> list = jdbc.queryForList("test.user_info.find({uid: '6666'})");
            assert list.isEmpty();
        }
    }
}
