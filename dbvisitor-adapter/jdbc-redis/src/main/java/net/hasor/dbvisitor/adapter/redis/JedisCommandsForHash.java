package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

class JedisCommandsForHash extends JedisCommands {
    private static Future<?> resultFiledValueList1(Future<Object> sync, AdapterRequest request, AdapterReceive receive, List<Map.Entry<String, String>> result) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_KEY_STRING,   //
                COL_VALUE_STRING));
        receive.responseResult(request, receiveCur);

        for (Map.Entry<String, String> item : result) {
            receiveCur.pushData(CollectionUtils.asMap(    //
                    COL_KEY_STRING.name, item.getKey(),   //
                    COL_VALUE_STRING.name, item.getValue()//
            ));
        }
        receiveCur.pushFinish();
        return completed(sync);
    }

    private static Future<?> resultFiledValueList2(Future<Object> sync, AdapterRequest request, AdapterReceive receive, String cursor, List<Map.Entry<String, String>> result) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_CURSOR_STRING,//
                COL_KEY_STRING,   //
                COL_VALUE_STRING));
        receive.responseResult(request, receiveCur);

        for (Map.Entry<String, String> item : result) {
            receiveCur.pushData(CollectionUtils.asMap(    //
                    COL_CURSOR_STRING.name, cursor,       //
                    COL_KEY_STRING.name, item.getKey(),   //
                    COL_VALUE_STRING.name, item.getValue()//
            ));
        }
        receiveCur.pushFinish();
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HdelCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.IdentifierContext> keyNameContexts = cmd.identifier();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.IdentifierContext keyNameContext = keyNameContexts.get(i);
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext);
        }

        long result = jedisCmd.getHashCommands().hdel(key, keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HexistsCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String field = argAsString(argIndex, request, cmd.identifier());

        boolean result = jedisCmd.getHashCommands().hexists(key, field);

        receive.responseUpdateCount(request, result ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HexpireCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        ExpiryOption option = null;
        if (cmd.expireOptions() != null) {
            option = getExpiryOption(cmd.expireOptions());
        }

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HEXPIRE numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hexpire(key, seconds, option, fields.toArray(new String[0]));
        } else {
            result = jedisCmd.getHashCommands().hexpire(key, seconds, fields.toArray(new String[0]));
        }

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HexpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        ExpiryOption option = null;
        if (cmd.expireOptions() != null) {
            option = getExpiryOption(cmd.expireOptions());
        }

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HEXPIREAT numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hexpireAt(key, seconds, option, fields.toArray(new String[0]));
        } else {
            result = jedisCmd.getHashCommands().hexpireAt(key, seconds, fields.toArray(new String[0]));
        }

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HexpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HEXPIRETIME numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result = jedisCmd.getHashCommands().hexpireTime(key, fields.toArray(new String[0]));

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpexpireCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        ExpiryOption option = null;
        if (cmd.expireOptions() != null) {
            option = getExpiryOption(cmd.expireOptions());
        }

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HPEXPIRE numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hpexpire(key, seconds, option, fields.toArray(new String[0]));
        } else {
            result = jedisCmd.getHashCommands().hpexpire(key, seconds, fields.toArray(new String[0]));
        }

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpexpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        ExpiryOption option = null;
        if (cmd.expireOptions() != null) {
            option = getExpiryOption(cmd.expireOptions());
        }

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HPEXPIREAT numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hpexpireAt(key, seconds, option, fields.toArray(new String[0]));
        } else {
            result = jedisCmd.getHashCommands().hpexpireAt(key, seconds, fields.toArray(new String[0]));
        }

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpexpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HPEXPIRETIME numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result = jedisCmd.getHashCommands().hpexpireTime(key, fields.toArray(new String[0]));

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HgetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String field = argAsString(argIndex, request, cmd.identifier());

        String result = jedisCmd.getHashCommands().hget(key, field);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HgetAllCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        Map<String, String> result = jedisCmd.getHashCommands().hgetAll(key);

        receive.responseResult(request, resultFiledValueList(request, receive, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HincrByCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.hashKeyName().identifier()));
        String field = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));
        long increment = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long value = jedisCmd.getHashCommands().hincrBy(key, field, increment);

        receive.responseResult(request, singleValueLongResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HkeysCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.hashKeyName().identifier()));

        Set<String> value = jedisCmd.getHashCommands().hkeys(key);

        receive.responseResult(request, resultKeysStringList(request, value, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HlenCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.hashKeyName().identifier()));

        long result = jedisCmd.getHashCommands().hlen(key);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HmgetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.IdentifierContext> keyNameContexts = cmd.identifier();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.IdentifierContext keyNameContext = keyNameContexts.get(i);
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext);
        }

        List<String> result = jedisCmd.getHashCommands().hmget(key, keys);

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HsetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.FiledValueClauseContext> kvContexts = cmd.filedValueClause();
        Map<String, String> data = new LinkedHashMap<>();
        for (RedisParser.FiledValueClauseContext keyNameContext : kvContexts) {
            String vKey = (String) argOrValue(argIndex, request, keyNameContext.field);
            String vValue = (String) argOrValue(argIndex, request, keyNameContext.value);
            data.put(vKey, vValue);
        }

        long result = jedisCmd.getHashCommands().hset(key, data);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HmsetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.FiledValueClauseContext> kvContexts = cmd.filedValueClause();
        Map<String, String> data = new LinkedHashMap<>();
        for (RedisParser.FiledValueClauseContext keyNameContext : kvContexts) {
            String vKey = (String) argOrValue(argIndex, request, keyNameContext.field);
            String vValue = (String) argOrValue(argIndex, request, keyNameContext.value);
            data.put(vKey, vValue);
        }

        String result = jedisCmd.getHashCommands().hmset(key, data);

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HsetnxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String vKey = (String) argOrValue(argIndex, request, cmd.filedValueClause().field);
        String vValue = (String) argOrValue(argIndex, request, cmd.filedValueClause().value);

        long result = jedisCmd.getHashCommands().hsetnx(key, vKey, vValue);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpersistCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HPERSIST numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result = jedisCmd.getHashCommands().hpersist(key, fields.toArray(new String[0]));

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HttlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HTTL numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result = jedisCmd.getHashCommands().httl(key, fields.toArray(new String[0]));

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpttlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, fieldsClause.integer()), true);
        List<String> fields = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.IdentifierContext fieldContext : fieldsClause.identifier()) {
            fields.add((String) argOrValue(argIndex, request, fieldContext));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("HPTTL numFields " + numKeys + " not match actual fields " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        List<Long> result = jedisCmd.getHashCommands().hpttl(key, fields.toArray(new String[0]));

        receive.responseResult(request, resultValueLongList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HrandfieldCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        Long count = null;
        if (cmd.decimal() != null) {
            count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        }

        if (count != null) {
            if (cmd.WITHVALUES() != null) {
                List<Map.Entry<String, String>> result = jedisCmd.getHashCommands().hrandfieldWithValues(key, count);
                return resultFiledValueList1(sync, request, receive, result);
            } else {
                List<String> result = jedisCmd.getHashCommands().hrandfield(key, count);
                receive.responseResult(request, resultValueStringList(request, result, -1));
                return completed(sync);
            }
        } else {
            List<String> result = Collections.singletonList(jedisCmd.getHashCommands().hrandfield(key));
            receive.responseResult(request, resultValueStringList(request, result, -1));
            return completed(sync);
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HscanCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.hashKeyName().identifier());
        String cursor = (String) argOrValue(argIndex, request, cmd.decimal());
        String pattern = null;
        Integer count = null;
        long maxRows = request.getMaxRows();
        if (cmd.matchClause() != null) {
            pattern = (String) argOrValue(argIndex, request, cmd.matchClause().keyPattern().identifier());
        }
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }

        ScanParams scanParams = new ScanParams();
        if (pattern != null) {
            scanParams.match(pattern);
        }
        if (count != null) {
            scanParams.count(count);
        }

        if (cmd.NOVALUES() != null) {
            ScanResult<String> result = jedisCmd.getHashCommands().hscanNoValues(key, cursor, scanParams);
            AdapterResultCursor receiveCur = resultCursorAndValueStringList(request, result.getCursor(), result.getResult(), maxRows);
            receive.responseResult(request, receiveCur);
            return completed(sync);
        } else {
            ScanResult<Map.Entry<String, String>> result = jedisCmd.getHashCommands().hscan(key, cursor, scanParams);
            return resultFiledValueList2(sync, request, receive, result.getCursor(), result.getResult());
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HstrlenCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String field = argAsString(argIndex, request, cmd.identifier());

        long result = jedisCmd.getHashCommands().hstrlen(key, field);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HvalsCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<String> result = jedisCmd.getHashCommands().hvals(key);

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }
}
