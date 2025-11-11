package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.ConvertUtils;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

class JedisCommandsForHash extends JedisCommands {
    private static Future<?> resultFiledValueList1(Future<Object> sync, AdapterRequest request, AdapterReceive receive, List<Map.Entry<String, String>> result) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_FIELD_STRING,   //
                COL_VALUE_STRING));
        receive.responseResult(request, receiveCur);

        for (Map.Entry<String, String> item : result) {
            receiveCur.pushData(CollectionUtils.asMap(    //
                    COL_FIELD_STRING.name, item.getKey(), //
                    COL_VALUE_STRING.name, item.getValue()//
            ));
        }
        receiveCur.pushFinish();
        return completed(sync);
    }

    private static Future<?> resultFiledValueList2(Future<Object> sync, AdapterRequest request, AdapterReceive receive, String cursor, List<Map.Entry<String, String>> result) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_CURSOR_STRING,//
                COL_FIELD_STRING, //
                COL_VALUE_STRING));
        receive.responseResult(request, receiveCur);

        for (Map.Entry<String, String> item : result) {
            receiveCur.pushData(CollectionUtils.asMap(    //
                    COL_CURSOR_STRING.name, cursor,       //
                    COL_FIELD_STRING.name, item.getKey(), //
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
            keys[i] = argAsString(argIndex, request, keyNameContext);
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

        receive.responseResult(request, singleResult(request, COL_RESULT_BOOLEAN, result));
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
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HEXPIRE", fields.length, numKeys);

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hexpire(key, seconds, option, fields);
        } else {
            result = jedisCmd.getHashCommands().hexpire(key, seconds, fields);
        }

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HexpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HEXPIREAT", fields.length, numKeys);

        ExpiryOption option = null;
        if (cmd.expireOptions() != null) {
            option = getExpiryOption(cmd.expireOptions());
        }

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hexpireAt(key, seconds, option, fields);
        } else {
            result = jedisCmd.getHashCommands().hexpireAt(key, seconds, fields);
        }

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HexpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HEXPIRETIME", fields.length, numKeys);

        List<Long> result = jedisCmd.getHashCommands().hexpireTime(key, fields);

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
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
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HPEXPIRE", fields.length, numKeys);

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hpexpire(key, seconds, option, fields);
        } else {
            result = jedisCmd.getHashCommands().hpexpire(key, seconds, fields);
        }

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
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
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HPEXPIREAT", fields.length, numKeys);

        List<Long> result;
        if (option != null) {
            result = jedisCmd.getHashCommands().hpexpireAt(key, seconds, option, fields);
        } else {
            result = jedisCmd.getHashCommands().hpexpireAt(key, seconds, fields);
        }

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpexpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HPEXPIRETIME", fields.length, numKeys);

        List<Long> result = jedisCmd.getHashCommands().hpexpireTime(key, fields);
        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HgetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String field = argAsString(argIndex, request, cmd.identifier());

        String result = jedisCmd.getHashCommands().hget(key, field);

        receive.responseResult(request, singleResult(request, COL_VALUE_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HgetAllCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        Map<String, String> result = jedisCmd.getHashCommands().hgetAll(key);

        long maxRows = request.getMaxRows();
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(COL_FIELD_STRING, COL_VALUE_STRING));

        int affectRows = 0;
        for (Map.Entry<String, String> item : result.entrySet()) {
            receiveCur.pushData(CollectionUtils.asMap(    //
                    COL_FIELD_STRING.name, item.getKey(), //
                    COL_VALUE_STRING.name, item.getValue()//
            ));
            affectRows++;

            if (maxRows > 0 && affectRows >= maxRows) {
                break;
            }
        }
        receiveCur.pushFinish();
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HincrByCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String field = argAsString(argIndex, request, cmd.identifier());
        long increment = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long value = jedisCmd.getHashCommands().hincrBy(key, field, increment);

        receive.responseResult(request, singleResult(request, COL_VALUE_LONG, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HkeysCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        Set<String> value = jedisCmd.getHashCommands().hkeys(key);

        receive.responseResult(request, listResult(request, COL_FIELD_STRING, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HlenCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        long result = jedisCmd.getHashCommands().hlen(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HmgetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.IdentifierContext> keyNameContexts = cmd.identifier();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.IdentifierContext keyNameContext = keyNameContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyNameContext);
        }

        List<String> result = jedisCmd.getHashCommands().hmget(key, keys);

        receive.responseResult(request, listResult(request, COL_VALUE_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HsetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.FiledValueClauseContext> kvContexts = cmd.filedValueClause();
        Map<String, String> data = new LinkedHashMap<>();
        for (RedisParser.FiledValueClauseContext keyNameContext : kvContexts) {
            String vKey = argAsString(argIndex, request, keyNameContext.field);
            String vValue = argAsString(argIndex, request, keyNameContext.value);
            data.put(vKey, vValue);
        }

        long result = jedisCmd.getHashCommands().hset(key, data);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HmsetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<RedisParser.FiledValueClauseContext> kvContexts = cmd.filedValueClause();
        Map<String, String> data = new LinkedHashMap<>();
        for (RedisParser.FiledValueClauseContext keyNameContext : kvContexts) {
            String vKey = argAsString(argIndex, request, keyNameContext.field);
            String vValue = argAsString(argIndex, request, keyNameContext.value);
            data.put(vKey, vValue);
        }

        jedisCmd.getHashCommands().hmset(key, data);

        receive.responseUpdateCount(request, data.size());
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HsetnxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String vKey = argAsString(argIndex, request, cmd.filedValueClause().field);
        String vValue = argAsString(argIndex, request, cmd.filedValueClause().value);

        long result = jedisCmd.getHashCommands().hsetnx(key, vKey, vValue);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpersistCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HPERSIST", fields.length, numKeys);

        List<Long> result = jedisCmd.getHashCommands().hpersist(key, fields);

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HttlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HTTL", fields.length, numKeys);

        List<Long> result = jedisCmd.getHashCommands().httl(key, fields);

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HpttlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        RedisParser.FieldsClauseContext fieldsClause = cmd.fieldsClause();
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, fieldsClause.integer()), true);

        List<RedisParser.IdentifierContext> fieldContexts = fieldsClause.identifier();
        String[] fields = new String[fieldContexts.size()];
        for (int i = 0; i < fieldContexts.size(); i++) {
            fields[i] = argAsString(argIndex, request, fieldContexts.get(i));
        }
        numKeysCheck(request, "HPTTL", fields.length, numKeys);

        List<Long> result = jedisCmd.getHashCommands().hpttl(key, fields);

        receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
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
                receive.responseResult(request, listResult(request, COL_FIELD_STRING, result));
                return completed(sync);
            }
        } else {
            List<String> result = Collections.singletonList(jedisCmd.getHashCommands().hrandfield(key));
            receive.responseResult(request, listResult(request, COL_FIELD_STRING, result));
            return completed(sync);
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HscanCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());
        String cursor = argAsString(argIndex, request, cmd.decimal());
        String pattern = null;
        Integer count = null;
        if (cmd.matchClause() != null) {
            pattern = argAsString(argIndex, request, cmd.matchClause().keyPattern().identifier());
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
            AdapterResultCursor receiveCur = listFixedColAndResult(request, COL_CURSOR_STRING, result.getCursor(), COL_FIELD_STRING, result.getResult());
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

        long value = jedisCmd.getHashCommands().hstrlen(key, field);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HvalsCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.hashKeyName().identifier());

        List<String> value = jedisCmd.getHashCommands().hvals(key);

        receive.responseResult(request, listResult(request, COL_VALUE_STRING, value));
        return completed(sync);
    }
}
