package net.hasor.dbvisitor.adapter.redis;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.ConvertUtils;
import redis.clients.jedis.commands.ServerCommands;
import redis.clients.jedis.util.KeyValue;

class JedisCommandsForServer extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());
        int database = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.databaseName().integer()), true);

        long result = jedisCmd.getDatabaseCommands().move(key, database);

        receive.responseUpdateCount(request, result);
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

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PingCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String ping;
        if (cmd.stringKeyName() != null) {
            ping = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        } else {
            ping = null;
        }

        ServerCommands serverCommands = jedisCmd.getServerCommands();
        String pong = ping == null ? serverCommands.ping() : serverCommands.ping(ping);

        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, pong));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.EchoCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String ping = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        String pong = jedisCmd.getServerCommands().echo(ping);

        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, pong));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SelectCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int db = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        conn.setSchema(String.valueOf(db));
        receive.responseUpdateCount(request, 1);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.InfoCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String infoValue;

        List<RedisParser.StringKeyNameContext> infoContext = cmd.stringKeyName();
        if (CollectionUtils.isNotEmpty(infoContext)) {
            StringBuilder sections = new StringBuilder();
            for (RedisParser.StringKeyNameContext infoItem : infoContext) {
                String key = argAsString(argIndex, request, infoItem.identifier());
                if (StringUtils.isNotBlank(key)) {
                    sections.append(key.toUpperCase());
                    sections.append(" ");
                }
            }

            infoValue = jedisCmd.getServerCommands().info(sections.toString().trim());
        } else {
            infoValue = jedisCmd.getServerCommands().info();
        }

        List<String> strings;
        try {
            strings = IOUtils.readLines(new StringReader(infoValue));
        } catch (Exception e) {
            return failed(sync, e);
        }

        String groupName = "INFO";
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_GROUP_STRING, COL_NAME_STRING, COL_VALUE_STRING));
        for (String line : strings) {
            if (StringUtils.isBlank(line)) {
                continue;
            }

            if (line.startsWith("#")) {
                groupName = line.replaceFirst("#\\s*", "").trim();
                continue;
            }

            int i = line.indexOf(":");
            if (i < 0) {
                continue;
            }

            receiveCur.pushData(CollectionUtils.asMap(          //
                    COL_GROUP_STRING.name, groupName,           //
                    COL_NAME_STRING.name, line.substring(0, i), //
                    COL_VALUE_STRING.name, line.substring(i + 1)//
            ));
        }
        receiveCur.pushFinish();
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }
}
