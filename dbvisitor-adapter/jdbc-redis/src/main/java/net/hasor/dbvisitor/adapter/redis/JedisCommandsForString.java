package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.ConvertUtils;
import net.hasor.dbvisitor.driver.JdbcErrorCode;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;

class JedisCommandsForString extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.StrSetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String setKey = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        String setValue = argAsString(argIndex, request, cmd.identifier());
        SetParams params = new SetParams();

        if (cmd.keyExistenceClause() != null) {
            String text = cmd.keyExistenceClause().getText();
            if (StringUtils.equalsIgnoreCase(text, "NX")) {
                params.nx();
            } else if (StringUtils.equalsIgnoreCase(text, "XX")) {
                params.xx();
            } else {
                throw new SQLException("keyExistenceClause not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        RedisParser.ExpirationClauseContext expirationClause = cmd.expirationClause();
        if (expirationClause != null) {
            if (expirationClause.EX() != null) {
                params.ex(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else if (expirationClause.PX() != null) {
                params.px(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else if (expirationClause.EXAT() != null) {
                params.exAt(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else if (expirationClause.PXAT() != null) {
                params.pxAt(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else {
                throw new SQLException("expirationClause not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } else if (cmd.KEEPTTL() != null) {
            params.keepTtl();
        }

        if (cmd.GET() != null) {
            String value = jedisCmd.getStringCommands().setGet(setKey, setValue, params);
            receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        } else {
            String result = jedisCmd.getStringCommands().set(setKey, setValue, params);
            receive.responseResult(request, singleResult(request, COL_RESULT_STRING, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        String value = jedisCmd.getStringCommands().get(key);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.IncrementCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        long value = jedisCmd.getStringCommands().incr(key);

        receive.responseResult(request, singleResult(request, COL_VALUE_LONG, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.IncrementByCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        long increment = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long value = jedisCmd.getStringCommands().incrBy(key, increment);

        receive.responseResult(request, singleResult(request, COL_VALUE_LONG, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DecrementCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        long value = jedisCmd.getStringCommands().decr(key);

        receive.responseResult(request, singleResult(request, COL_VALUE_LONG, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DecrementByCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        long decrement = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long value = jedisCmd.getStringCommands().decrBy(key, decrement);

        receive.responseResult(request, singleResult(request, COL_VALUE_LONG, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.AppendCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        String append = argAsString(argIndex, request, cmd.identifier());

        long result = jedisCmd.getStringCommands().append(key, append);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetDeleteCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        String value = jedisCmd.getStringCommands().getDel(key);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetExCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        GetExParams params = new GetExParams();

        if (cmd.expirationClause() != null) {
            RedisParser.ExpirationClauseContext expirationClause = cmd.expirationClause();
            if (expirationClause.EX() != null) {
                params.ex(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else if (expirationClause.PX() != null) {
                params.px(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else if (expirationClause.EXAT() != null) {
                params.exAt(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else if (expirationClause.PXAT() != null) {
                params.pxAt(ConvertUtils.toInteger(argOrValue(argIndex, request, expirationClause.integer()), true));
            } else {
                throw new SQLException("expiration " + expirationClause.getText() + " not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        } else if (cmd.PERSIST() != null) {
            params.persist();
        }

        String value = jedisCmd.getStringCommands().getEx(key, params);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetRangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        long start = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.start), true);
        long end = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        String value = jedisCmd.getStringCommands().getrange(key, start, end);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetSetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        String append = argAsString(argIndex, request, cmd.identifier());

        String value = jedisCmd.getStringCommands().getSet(key, append);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MGetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.StringKeyNameContext> keyContexts = cmd.stringKeyName();
        String[] keys = new String[keyContexts.size()];
        for (int i = 0; i < keyContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, keyContexts.get(i).identifier());
        }

        List<String> values = jedisCmd.getStringCommands().mget(keys);

        receive.responseResult(request, listResult(request, COL_VALUE_STRING, values));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MSetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyValueClauseContext> kvContexts = cmd.keyValueClause();
        String[] keyValues = new String[kvContexts.size() * 2];
        int i = 0;
        for (RedisParser.KeyValueClauseContext keyValueClauseContext : kvContexts) {
            keyValues[i++] = argAsString(argIndex, request, keyValueClauseContext.stringKeyName().identifier());
            keyValues[i++] = argAsString(argIndex, request, keyValueClauseContext.identifier());
        }

        String status = jedisCmd.getStringCommands().mset(keyValues);
        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MSetNxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyValueClauseContext> kvContexts = cmd.keyValueClause();
        String[] keyValues = new String[kvContexts.size() * 2];
        int i = 0;
        for (RedisParser.KeyValueClauseContext keyValueClauseContext : kvContexts) {
            keyValues[i++] = argAsString(argIndex, request, keyValueClauseContext.stringKeyName().identifier());
            keyValues[i++] = argAsString(argIndex, request, keyValueClauseContext.identifier());
        }

        long status = jedisCmd.getStringCommands().msetnx(keyValues);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PSetExCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        long milliseconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        String value = argAsString(argIndex, request, cmd.identifier());

        String status = jedisCmd.getStringCommands().psetex(key, milliseconds, value);

        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetExCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        String value = argAsString(argIndex, request, cmd.identifier());

        String status = jedisCmd.getStringCommands().setex(key, seconds, value);

        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetNxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        String value = argAsString(argIndex, request, cmd.identifier());

        long status = jedisCmd.getStringCommands().setnx(key, value);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetRangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        long offset = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        String value = argAsString(argIndex, request, cmd.identifier());

        long status = jedisCmd.getStringCommands().setrange(key, offset, value);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.StringLengthCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());

        long status = jedisCmd.getStringCommands().strlen(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, status));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SubstringCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.stringKeyName().identifier());
        int start = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.start), true);
        int end = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.end), true);

        String value = jedisCmd.getStringCommands().substr(key, start, end);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }
}