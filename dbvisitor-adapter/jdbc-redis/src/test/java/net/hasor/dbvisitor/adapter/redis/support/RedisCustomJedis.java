package net.hasor.dbvisitor.adapter.redis.support;

import java.util.Properties;
import net.hasor.dbvisitor.adapter.redis.CustomJedis;
import org.powermock.api.mockito.PowerMockito;
import redis.clients.jedis.Jedis;

public class RedisCustomJedis implements CustomJedis {
    @Override
    public Object createJedisCmd(String jdbcUrl, Properties props) {
        return PowerMockito.mock(Jedis.class);
    }
}
