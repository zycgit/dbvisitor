package net.hasor.dbvisitor.adapter.redis;

import java.util.Map;
import org.powermock.api.mockito.PowerMockito;
import redis.clients.jedis.Jedis;

public class RedisCustomJedis implements CustomJedis {
    @Override
    public Object createJedisCmd(String jdbcUrl, Map<String, String> props) {
        return PowerMockito.mock(Jedis.class);
    }
}
