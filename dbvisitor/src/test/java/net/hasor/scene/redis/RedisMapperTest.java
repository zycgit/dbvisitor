package net.hasor.scene.redis;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.scene.redis.dto1.UserInfo1;
import net.hasor.scene.redis.dto1.UserInfo1Mapper;
import net.hasor.scene.redis.dto2.UserInfo2;
import net.hasor.scene.redis.dto2.UserInfo2Mapper;
import net.hasor.scene.redis.dto3.UserInfo3;
import net.hasor.scene.redis.dto3.UserInfo3Mapper;
import net.hasor.scene.redis.dto3.UserInfo4Mapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisMapperTest {
    @Test
    public void using_mapper_api_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.redisConn())) {
            UserInfo1Mapper infoMapper = s.createMapper(UserInfo1Mapper.class);

            UserInfo1 user = new UserInfo1();
            user.setUid("1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assert saveStatus == 1;

            // load
            UserInfo1 info = infoMapper.loadUser("1111");
            assert user != info;
            assert info.getUid().equals("1111");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // delete
            assert s.getConnection().unwrap(Jedis.class).get("user_1111") != null;
            int delStatus = infoMapper.deleteUser("1111");
            assert delStatus == 1;
            assert s.getConnection().unwrap(Jedis.class).get("user_1111") == null;
        }
    }

    @Test
    public void using_mapper_api_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.redisConn())) {
            UserInfo2Mapper infoMapper = s.createMapper(UserInfo2Mapper.class);

            UserInfo2 user = new UserInfo2();
            user.setUid("2222");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assert saveStatus == 1;

            // load
            UserInfo2 info = infoMapper.loadUser("2222");
            assert user != info;
            assert info.getUid().equals("2222");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // delete
            assert s.getConnection().unwrap(Jedis.class).get("user_2222") != null;
            int delStatus = infoMapper.deleteUser("2222");
            assert delStatus == 1;
            assert s.getConnection().unwrap(Jedis.class).get("user_2222") == null;
        }
    }

    @Test
    public void using_mapper_file_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.redisConnSeparatorChar())) {
            UserInfo3Mapper infoMapper = s.createMapper(UserInfo3Mapper.class);

            UserInfo3 user = new UserInfo3();
            user.setUid("3333");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assert saveStatus == 1;

            // load1
            UserInfo3 info1 = infoMapper.loadUser1("3333");
            assert info1.getUid().equals("3333");
            assert info1.getName().equals("username");
            assert info1.getLoginName().equals("login_123");
            assert info1.getLoginPassword().equals("password");

            // load2
            UserInfo1 info2 = infoMapper.loadUser2("3333");
            assert info2.getUid().equals("3333");
            assert info2.getName().equals("username");
            assert info2.getLoginName().equals("login_123");
            assert info2.getLoginPassword().equals("password");

            // delete
            assert s.getConnection().unwrap(Jedis.class).get("user_3333") != null;
            int delStatus = infoMapper.deleteUser("3333");
            assert delStatus == 1;
            assert s.getConnection().unwrap(Jedis.class).get("user_3333") == null;
        }
    }

    @Test
    public void using_mapper_file_2() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Session s = config.newSession(DsUtils.redisConnSeparatorChar())) {
            UserInfo4Mapper infoMapper = s.createMapper(UserInfo4Mapper.class);

            UserInfo1 user = new UserInfo1();
            user.setUid("4444");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            int saveStatus = infoMapper.saveUser(user);
            assert saveStatus == 1;

            // load1
            UserInfo1 info1 = infoMapper.loadUser("4444");
            assert info1.getUid().equals("4444");
            assert info1.getName().equals("username");
            assert info1.getLoginName().equals("login_123");
            assert info1.getLoginPassword().equals("password");

            // delete
            assert s.getConnection().unwrap(Jedis.class).get("user_4444") != null;
            int delStatus = infoMapper.deleteUser("4444");
            assert delStatus == 1;
            assert s.getConnection().unwrap(Jedis.class).get("user4444") == null;
        }
    }
}
