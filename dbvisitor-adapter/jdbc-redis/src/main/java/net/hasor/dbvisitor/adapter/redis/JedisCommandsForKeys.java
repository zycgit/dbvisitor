package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

class JedisCommandsForKeys extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.CopyCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String oldKeyName = argAsString(argIndex, request, cmd.keyName().identifier());
        String newKeyName = argAsString(argIndex, request, cmd.identifier());
        boolean replace = cmd.REPLACE() != null;

        boolean result;
        if (cmd.dbClause() != null) {
            int destDataBase = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.dbClause().databaseName().integer()), true);
            result = jedisCmd.getDatabaseCommands().copy(oldKeyName, newKeyName, destDataBase, replace);
        } else {
            result = jedisCmd.getKeyCommands().copy(oldKeyName, newKeyName, replace);
        }

        receive.responseUpdateCount(request, result ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DeleteCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyNameContext> keyNameContexts = cmd.keyName();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.KeyNameContext keyNameContext = keyNameContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyNameContext.identifier());
        }

        long result = jedisCmd.getKeyCommands().del(keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.UnlinkCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyNameContext> keyNameContexts = cmd.keyName();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.KeyNameContext keyNameContext = keyNameContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyNameContext.identifier());
        }

        long result = jedisCmd.getKeyCommands().unlink(keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DumpCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyName = argAsString(argIndex, request, cmd.keyName().identifier());

        byte[] dump = jedisCmd.getKeyCommands().dump(keyName);

        receive.responseResult(request, singleResult(request, COL_VALUE_BYTES, dump));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExistsCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyNameContext> keyNameContexts = cmd.keyName();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.KeyNameContext keyNameContext = keyNameContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyNameContext.identifier());
        }

        long found = jedisCmd.getKeyCommands().exists(keys);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, found));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExpireCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().expire(key, seconds, option);
        } else {
            result = jedisCmd.getKeyCommands().expire(key, seconds);
        }

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());
        long unixTime = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().expireAt(key, unixTime, option);
        } else {
            result = jedisCmd.getKeyCommands().expireAt(key, unixTime);
        }

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().expireTime(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PExpireCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());
        long milliSeconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().pexpire(key, milliSeconds, option);
        } else {
            result = jedisCmd.getKeyCommands().pexpire(key, milliSeconds);
        }

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PExpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());
        long milliSecondsTimestamp = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().pexpireAt(key, milliSecondsTimestamp, option);
        } else {
            result = jedisCmd.getKeyCommands().pexpireAt(key, milliSecondsTimestamp);
        }

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PExpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().pexpireTime(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.KeysCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String pattern = argAsString(argIndex, request, cmd.keyPattern().identifier());

        int affectRows = 0;
        long maxRows = request.getMaxRows();
        int fetchSize = request.getFetchSize();
        ScanParams scanParams = new ScanParams();
        scanParams.match(pattern);
        if (fetchSize > 0) {
            scanParams.count(fetchSize);
        }
        String cursor = null;

        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Collections.singletonList(COL_KEY_STRING));
        receive.responseResult(request, receiveCur);

        while (!sync.isDone()) {
            ScanResult<String> result = jedisCmd.getKeyCommands().scan(cursor, scanParams);
            cursor = result.getCursor();
            List<String> list = result.getResult();
            boolean breakWhile = result.isCompleteIteration();

            for (String key : list) {
                receiveCur.pushData(CollectionUtils.asMap(COL_KEY_STRING.name, key));
                affectRows++;

                if (maxRows > 0 && affectRows >= maxRows) {
                    breakWhile = true;
                    break;
                }

                if (sync.isDone()) {
                    breakWhile = true;
                    break;
                }
            }

            if (breakWhile) {
                break;
            }
        }

        if (!sync.isDone()) {
            receiveCur.pushFinish();
            return completed(sync);
        } else {
            SQLException err = new SQLException("command interrupted.");
            receiveCur.pushFinish();
            receive.responseFailed(request, err);
            throw err;
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ObjectCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());

        if (cmd.objectOptions().ENCODING() != null) {
            String result = jedisCmd.getKeyCommands().objectEncoding(key);
            receive.responseResult(request, singleResult(request, COL_RESULT_STRING, result));
        } else if (cmd.objectOptions().FREQ() != null) {
            Long result = jedisCmd.getKeyCommands().objectFreq(key);
            receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        } else if (cmd.objectOptions().IDLETIME() != null) {
            Long result = jedisCmd.getKeyCommands().objectIdletime(key);
            receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        } else if (cmd.objectOptions().REFCOUNT() != null) {
            Long result = jedisCmd.getKeyCommands().objectRefcount(key);
            receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        } else {
            throw new SQLException("object options(" + cmd.objectOptions().getText() + ") not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PersistCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().persist(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.TtlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().ttl(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PTtlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().pttl(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RandomKeyCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        String result = jedisCmd.getKeyCommands().randomKey();

        receive.responseResult(request, singleResult(request, COL_KEY_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RenameCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String oldKey = argAsString(argIndex, request, cmd.keyName().identifier());
        String newKey = argAsString(argIndex, request, cmd.identifier());

        String result = jedisCmd.getKeyCommands().rename(oldKey, newKey);

        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RenameNxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String oldKey = argAsString(argIndex, request, cmd.keyName().identifier());
        String newKey = argAsString(argIndex, request, cmd.identifier());

        long result = jedisCmd.getKeyCommands().renamenx(oldKey, newKey);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ScanCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String cursor = argAsString(argIndex, request, cmd.decimal());
        String pattern = null;
        Integer count = null;
        String type = null;
        if (cmd.matchClause() != null) {
            pattern = argAsString(argIndex, request, cmd.matchClause().keyPattern().identifier());
        }
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }
        if (cmd.typeClause() != null) {
            type = argAsString(argIndex, request, cmd.typeClause().identifier());
        }

        ScanParams scanParams = new ScanParams();
        if (pattern != null) {
            scanParams.match(pattern);
        }
        if (count != null) {
            scanParams.count(count);
            request.setMaxRows(count);
        } else {
            if (request.getMaxRows() > 0) {
                scanParams.count(Math.toIntExact(request.getMaxRows()));
            }
        }

        ScanResult<String> result;
        if (type == null) {
            result = jedisCmd.getKeyCommands().scan(cursor, scanParams);
        } else {
            result = jedisCmd.getKeyCommands().scan(cursor, scanParams, type);
        }

        if (!sync.isDone()) {
            AdapterResultCursor receiveCur = listFixedColAndResult(request, COL_CURSOR_STRING, result.getCursor(), COL_KEY_STRING, result.getResult());
            receive.responseResult(request, receiveCur);
            return completed(sync);
        } else {
            SQLException err = new SQLException("command interrupted.");
            receive.responseFailed(request, err);
            throw err;
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.TouchCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyNameContext> keyNameContexts = cmd.keyName();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.KeyNameContext keyNameContext = keyNameContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyNameContext.identifier());
        }

        long result = jedisCmd.getKeyCommands().touch(keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.TypeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyName = argAsString(argIndex, request, cmd.keyName().identifier());

        String type = jedisCmd.getKeyCommands().type(keyName);

        receive.responseResult(request, singleResult(request, COL_RESULT_STRING, type));
        return completed(sync);
    }
}
