package net.hasor.dbvisitor.adapter.redis;
import java.util.Properties;

public interface CustomJedis {

    /** return JedisCluster or Jedis */
    Object createJedisCmd(String jdbcUrl, Properties props);
}
