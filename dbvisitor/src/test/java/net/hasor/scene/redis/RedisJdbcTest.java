package net.hasor.scene.redis;
import java.sql.Connection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.scene.redis.dto1.UserInfo1;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisJdbcTest {
    @Test
    public void using_jdbc_1() throws Exception {
        Configuration config = new Configuration();
        config.options().mapUnderscoreToCamelCase(true);

        try (Connection c = DsUtils.redisConn()) {
            JdbcTemplate jdbc = new JdbcTemplate(c);

            UserInfo1 user = new UserInfo1();
            user.setUid("j1111");
            user.setName("username");
            user.setLoginName("login_123");
            user.setLoginPassword("password");

            // insert
            assert jdbc.executeUpdate("set #{'user_' + arg0.uid} #{arg0}", user) == 1;

            // load
            UserInfo1 info = jdbc.queryForObject("get #{'user_' + arg0}", "j1111", UserInfo1.class);
            assert user != info;
            assert info.getUid().equals("j1111");
            assert info.getName().equals("username");
            assert info.getLoginName().equals("login_123");
            assert info.getLoginPassword().equals("password");

            // delete

            assert c.unwrap(Jedis.class).get("user_j1111") != null;
            assert jdbc.executeUpdate("del user_j1111") == 1;
            assert c.unwrap(Jedis.class).get("user_j1111") == null;
        }
    }
}
