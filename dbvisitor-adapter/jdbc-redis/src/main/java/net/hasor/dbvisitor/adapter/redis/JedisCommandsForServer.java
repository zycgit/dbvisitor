package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.ConvertUtils;
import redis.clients.jedis.util.KeyValue;

class JedisCommandsForServer extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());
        int database = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.databaseName().integer()), true);

        long result = jedisCmd.getDatabaseCommands().move(key, database);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.WaitCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int replicas = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.replicas), true);
        long timeout = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.timeout), true);

        long result = jedisCmd.getServerCommands().waitReplicas(replicas, timeout);

        receive.responseResult(request, singleResult(request, COL_REPLICAS_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.WaitaofCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long local = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.numlocal), true);
        long replicas = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.replicas), true);
        long timeout = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.timeout), true);

        KeyValue<Long, Long> result = jedisCmd.getServerCommands().waitAOF(local, replicas, timeout);

        receive.responseResult(request, twoResult(request, COL_LOCAL_LONG, result.getKey(), COL_REPLICAS_LONG, result.getValue()));
        return completed(sync);
    }
}
