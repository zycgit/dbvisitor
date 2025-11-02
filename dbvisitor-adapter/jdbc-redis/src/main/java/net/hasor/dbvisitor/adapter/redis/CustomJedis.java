package net.hasor.dbvisitor.adapter.redis;
import java.util.Map;

public interface CustomJedis {
    /** return JedisCluster or Jedis */
    Object createJedisCmd(String jdbcUrl, Map<String, String> props);
}
