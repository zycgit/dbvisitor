package net.hasor.dbvisitor.adapter.redis;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.util.KeyValue;

class JedisCommandsForList extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LmoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = ConvertUtils.toString(argOrValue(argIndex, request, cmd.src.identifier()));
        String dstKey = ConvertUtils.toString(argOrValue(argIndex, request, cmd.dst.identifier()));
        ListDirection from = getListDirection(cmd.from, ListDirection.LEFT);
        ListDirection to = getListDirection(cmd.to, ListDirection.LEFT);

        String value = jedisCmd.getListCommands().lmove(srcKey, dstKey, from, to);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BlmoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = ConvertUtils.toString(argOrValue(argIndex, request, cmd.src.identifier()));
        String dstKey = ConvertUtils.toString(argOrValue(argIndex, request, cmd.dst.identifier()));
        ListDirection from = getListDirection(cmd.from, ListDirection.LEFT);
        ListDirection to = getListDirection(cmd.to, ListDirection.LEFT);
        double timeout = ConvertUtils.toDouble(argOrValue(argIndex, request, cmd.integer()), true);

        String value = jedisCmd.getListCommands().blmove(srcKey, dstKey, from, to, timeout);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int numkeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        List<String> keyValues = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.ListKeyNameContext keyClauseContext : kvContexts) {
            keyValues.add((String) argOrValue(argIndex, request, keyClauseContext.identifier()));
            cnt++;
        }
        if (cnt != numkeys) {
            throw new SQLException("LMPOP numkeys " + numkeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        ListDirection lr = getListDirection(cmd.leftOrRightClause(), ListDirection.LEFT);

        KeyValue<String, List<String>> values;
        if (cmd.countClause() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
            values = jedisCmd.getListCommands().lmpop(lr, count, keyValues.toArray(new String[0]));
        } else {
            values = jedisCmd.getListCommands().lmpop(lr, keyValues.toArray(new String[0]));
        }

        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_KEY_STRING, COL_VALUE_LIST));
        receive.responseResult(request, receiveCur);

        String resultKey = values.getKey();
        Array resultValue = conn.createArrayOf(AdapterType.String, values.getValue().toArray());
        receiveCur.pushData(CollectionUtils.asMap(COL_KEY_STRING.name, resultKey, COL_VALUE_LIST.name, resultValue));
        receiveCur.pushFinish();
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BlmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toDouble(argOrValue(argIndex, request, cmd.timeout), true);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.numkeys), true);

        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        List<String> keyValues = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.ListKeyNameContext keyClauseContext : kvContexts) {
            keyValues.add((String) argOrValue(argIndex, request, keyClauseContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("BLMPOP numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        ListDirection lr = getListDirection(cmd.leftOrRightClause(), ListDirection.LEFT);

        KeyValue<String, List<String>> values;
        if (cmd.countClause() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
            values = jedisCmd.getListCommands().blmpop(timeout, lr, count, keyValues.toArray(new String[0]));
        } else {
            values = jedisCmd.getListCommands().blmpop(timeout, lr, keyValues.toArray(new String[0]));
        }

        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_KEY_STRING, COL_VALUE_LIST));
        receive.responseResult(request, receiveCur);

        String resultKey = values.getKey();
        Array resultValue = conn.createArrayOf(AdapterType.String, values.getValue().toArray());
        receiveCur.pushData(CollectionUtils.asMap(COL_KEY_STRING.name, resultKey, COL_VALUE_LIST.name, resultValue));
        receiveCur.pushFinish();
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyStr = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());

        List<String> result;
        if (cmd.integer() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
            result = jedisCmd.getListCommands().lpop(keyStr, count);
        } else {
            result = Collections.singletonList(jedisCmd.getListCommands().lpop(keyStr));
        }

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyStr = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());

        List<String> result;
        if (cmd.integer() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
            result = jedisCmd.getListCommands().rpop(keyStr, count);
        } else {
            result = Collections.singletonList(jedisCmd.getListCommands().rpop(keyStr));
        }

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BlpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        List<String> keys = new ArrayList<>();
        for (RedisParser.ListKeyNameContext keyClauseContext : kvContexts) {
            keys.add((String) argOrValue(argIndex, request, keyClauseContext.identifier()));
        }
        int timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<String> result = jedisCmd.getListCommands().blpop(timeout, keys.toArray(new String[0]));

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BrpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        List<String> keys = new ArrayList<>();
        for (RedisParser.ListKeyNameContext keyClauseContext : kvContexts) {
            keys.add((String) argOrValue(argIndex, request, keyClauseContext.identifier()));
        }
        int timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<String> result = jedisCmd.getListCommands().brpop(timeout, keys.toArray(new String[0]));

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpopLpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = (String) argOrValue(argIndex, request, cmd.src.identifier());
        String dstKey = (String) argOrValue(argIndex, request, cmd.dst.identifier());

        String result = jedisCmd.getListCommands().rpoplpush(srcKey, dstKey);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BrpopLpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = (String) argOrValue(argIndex, request, cmd.src.identifier());
        String dstKey = (String) argOrValue(argIndex, request, cmd.dst.identifier());
        int timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        String result = jedisCmd.getListCommands().brpoplpush(srcKey, dstKey, timeout);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LindexCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        long index = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        String result = jedisCmd.getListCommands().lindex(key, index);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LinsertCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        String pivot = (String) argOrValue(argIndex, request, cmd.pivot);
        String ele = (String) argOrValue(argIndex, request, cmd.ele);

        ListPosition where;
        if (cmd.beforeOrAfterClause().BEFORE() != null) {
            where = ListPosition.BEFORE;
        } else if (cmd.beforeOrAfterClause().AFTER() != null) {
            where = ListPosition.AFTER;
        } else {
            throw new SQLException("linsert must be BEFORE or AFTER", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        long result = jedisCmd.getListCommands().linsert(key, where, pivot, ele);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LlenCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());

        long result = jedisCmd.getListCommands().llen(key);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LposCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        String element = (String) argOrValue(argIndex, request, cmd.identifier());
        LPosParams params = null;
        Long count = null;

        if (cmd.rankClause() != null) {
            params = new LPosParams();
            int rank = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.rankClause().decimal()), true);
            params.rank(rank);
        }

        if (cmd.countClause() != null) {
            count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }

        if (cmd.maxLenClause() != null) {
            params = params == null ? new LPosParams() : params;
            int maxLen = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.maxLenClause().integer()), true);
            params.maxlen(maxLen);
        }

        if (params == null) {
            long result = jedisCmd.getListCommands().lpos(key, element);
            receive.responseResult(request, singleValueLongResult(request, result));
        } else if (count == null) {
            long result = jedisCmd.getListCommands().lpos(key, element, params);
            receive.responseResult(request, singleValueLongResult(request, result));
        } else {
            List<Long> result = jedisCmd.getListCommands().lpos(key, element, params, count);

            AdapterResultCursor receiveCur = resultValueLongList(request, result, -1);
            receive.responseResult(request, receiveCur);
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        List<String> elements = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            elements.add((String) argOrValue(argIndex, request, identifierContext));
        }

        long result = jedisCmd.getListCommands().lpush(key, elements.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LpushxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        List<String> elements = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            elements.add((String) argOrValue(argIndex, request, identifierContext));
        }

        long result = jedisCmd.getListCommands().lpushx(key, elements.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        List<String> elements = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            elements.add((String) argOrValue(argIndex, request, identifierContext));
        }

        long result = jedisCmd.getListCommands().rpush(key, elements.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpushxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        List<String> elements = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            elements.add((String) argOrValue(argIndex, request, identifierContext));
        }

        long result = jedisCmd.getListCommands().rpushx(key, elements.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LrangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        long begin = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long end = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        List<String> result = jedisCmd.getListCommands().lrange(key, begin, end);

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LremCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        long count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        String value = (String) argOrValue(argIndex, request, cmd.identifier());

        long result = jedisCmd.getListCommands().lrem(key, count, value);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LsetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        long index = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        String value = (String) argOrValue(argIndex, request, cmd.identifier());

        String result = jedisCmd.getListCommands().lset(key, index, value);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LtrimCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.listKeyName().identifier());
        long begin = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long end = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        String result = jedisCmd.getListCommands().ltrim(key, begin, end);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }
}