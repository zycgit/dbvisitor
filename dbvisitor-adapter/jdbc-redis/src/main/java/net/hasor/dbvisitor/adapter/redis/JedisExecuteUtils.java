package net.hasor.dbvisitor.adapter.redis;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.args.ExpiryOption;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.args.SortedSetOption;
import redis.clients.jedis.params.*;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;

class JedisExecuteUtils extends JedisUtils {
    private static ExpiryOption getExpiryOption(RedisParser.ExpireOptionsContext expireOpt) throws SQLException {
        if (expireOpt.NX() != null) {
            return ExpiryOption.NX;
        } else if (expireOpt.XX() != null) {
            return ExpiryOption.XX;
        } else if (expireOpt.GT() != null) {
            return ExpiryOption.GT;
        } else if (expireOpt.LT() != null) {
            return ExpiryOption.LT;
        } else {
            throw new SQLException("expire options(" + expireOpt.getText() + ") not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
    }

    private static ListDirection getListDirection(RedisParser.LeftOrRightClauseContext lr, ListDirection defaultValue) throws SQLException {
        if (lr == null) {
            return defaultValue;
        } else if (lr.LEFT() != null) {
            return ListDirection.LEFT;
        } else if (lr.RIGHT() != null) {
            return ListDirection.RIGHT;
        } else {
            throw new SQLException("LeftOrRightClause " + lr.getText() + " not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
    }

    private static SortedSetOption getSortedSetOption(RedisParser.MinMaxClauseContext minmax) throws SQLException {
        if (minmax != null) {
            if (minmax.MAX() != null) {
                return SortedSetOption.MAX;
            } else if (minmax.MIN() != null) {
                return SortedSetOption.MIN;
            }
        }
        throw new SQLException("MinMaxClause " + (minmax == null ? "null" : minmax.getText()) + " not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
    }

    private static ZParams.Aggregate getAggregateOption(RedisParser.AggregateClauseContext aggregate) throws SQLException {
        if (aggregate != null) {
            if (aggregate.MIN() != null) {
                return ZParams.Aggregate.MIN;
            } else if (aggregate.MAX() != null) {
                return ZParams.Aggregate.MAX;
            } else if (aggregate.SUM() != null) {
                return ZParams.Aggregate.SUM;
            }
        }
        throw new SQLException("AggregateClause " + aggregate.getText() + " not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
    }

    private static Future<?> completed(Future<Object> sync) {
        sync.completed(true);
        return sync;
    }

    /* ------------------------------------------------------------------------------------------------ CommonCommands */

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.CopyCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String oldKeyName = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        String newKeyName = (String) argOrValue(argIndex, request, cmd.identifier());
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
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext.identifier());
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
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext.identifier());
        }

        long result = jedisCmd.getKeyCommands().unlink(keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DumpCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyName = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        byte[] dump = jedisCmd.getKeyCommands().dump(keyName);

        receive.responseResult(request, singleValueBytesResult(request, dump));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExistsCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyNameContext> keyNameContexts = cmd.keyName();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.KeyNameContext keyNameContext = keyNameContexts.get(i);
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext.identifier());
        }

        long result = jedisCmd.getKeyCommands().exists(keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExpireCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().expire(key, seconds, option);
        } else {
            result = jedisCmd.getKeyCommands().expire(key, seconds);
        }

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        long unixTime = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().expireAt(key, unixTime, option);
        } else {
            result = jedisCmd.getKeyCommands().expireAt(key, unixTime);
        }

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ExpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().expireTime(key);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PExpireCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        long milliSeconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().pexpire(key, milliSeconds, option);
        } else {
            result = jedisCmd.getKeyCommands().pexpire(key, milliSeconds);
        }

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PExpireAtCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        long milliSecondsTimestamp = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long result;
        RedisParser.ExpireOptionsContext expireOpt = cmd.expireOptions();
        if (expireOpt != null) {
            ExpiryOption option = getExpiryOption(expireOpt);
            result = jedisCmd.getKeyCommands().pexpireAt(key, milliSecondsTimestamp, option);
        } else {
            result = jedisCmd.getKeyCommands().pexpireAt(key, milliSecondsTimestamp);
        }

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PExpireTimeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().pexpireTime(key);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.KeysCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String pattern = (String) argOrValue(argIndex, request, cmd.keyPattern().identifier());

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

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        int database = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.databaseName().integer()), true);

        long result = jedisCmd.getDatabaseCommands().move(key, database);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ObjectCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        if (cmd.objectOptions().ENCODING() != null) {
            String result = jedisCmd.getKeyCommands().objectEncoding(key);
            receive.responseResult(request, singleValueStringResult(request, result));
        } else if (cmd.objectOptions().FREQ() != null) {
            Long result = jedisCmd.getKeyCommands().objectFreq(key);
            receive.responseResult(request, singleValueLongResult(request, result));
        } else if (cmd.objectOptions().IDLETIME() != null) {
            Long result = jedisCmd.getKeyCommands().objectIdletime(key);
            receive.responseResult(request, singleValueLongResult(request, result));
        } else if (cmd.objectOptions().REFCOUNT() != null) {
            Long result = jedisCmd.getKeyCommands().objectRefcount(key);
            receive.responseResult(request, singleValueLongResult(request, result));
        } else {
            throw new SQLException("object options(" + cmd.objectOptions().getText() + ") not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PersistCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().persist(key);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.TtlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().ttl(key);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PTtlCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        long result = jedisCmd.getKeyCommands().pttl(key);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RandomKeyCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        String result = jedisCmd.getKeyCommands().randomKey();

        receive.responseResult(request, singleValueStringResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RenameCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String oldKey = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        String newKey = (String) argOrValue(argIndex, request, cmd.identifier());

        String result = jedisCmd.getKeyCommands().rename(oldKey, newKey);

        receive.responseUpdateCount(request, StringUtils.equalsIgnoreCase("ok", result) ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RenameNxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String oldKey = (String) argOrValue(argIndex, request, cmd.keyName().identifier());
        String newKey = (String) argOrValue(argIndex, request, cmd.identifier());

        long result = jedisCmd.getKeyCommands().renamenx(oldKey, newKey);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ScanCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String cursor = (String) argOrValue(argIndex, request, cmd.decimal());
        String pattern = null;
        Integer count = null;
        String type = null;
        long maxRows = request.getMaxRows();
        if (cmd.matchClause() != null) {
            pattern = (String) argOrValue(argIndex, request, cmd.matchClause().keyPattern().identifier());
        }
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }
        if (cmd.typeClause() != null) {
            type = (String) argOrValue(argIndex, request, cmd.typeClause().identifier());
        }

        ScanParams scanParams = new ScanParams();
        if (pattern != null) {
            scanParams.match(pattern);
        }
        if (count != null) {
            scanParams.count(count);
        }

        ScanResult<String> result;
        if (type == null) {
            result = jedisCmd.getKeyCommands().scan(cursor, scanParams);
        } else {
            result = jedisCmd.getKeyCommands().scan(cursor, scanParams, type);
        }

        if (!sync.isDone()) {
            AdapterResultCursor receiveCur = resultCursorAndKeyStringList(request, result.getCursor(), result.getResult(), maxRows);
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
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext.identifier());
        }

        long result = jedisCmd.getKeyCommands().touch(keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.TypeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyName = (String) argOrValue(argIndex, request, cmd.keyName().identifier());

        String type = jedisCmd.getKeyCommands().type(keyName);

        receive.responseResult(request, singleValueStringResult(request, type));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.WaitCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int replicas = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.replicas), true);
        long timeout = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.timeout), true);

        long result = jedisCmd.getServerCommands().waitReplicas(replicas, timeout);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    /* ------------------------------------------------------------------------------------------------ StringCommands */

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.StringSetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String setKey = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        String setValue = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));
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
            receive.responseResult(request, singleValueStringResult(request, value));
        } else {
            String value = jedisCmd.getStringCommands().set(setKey, setValue, params);
            receive.responseResult(request, singleValueStringResult(request, value));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));

        String value = jedisCmd.getStringCommands().get(key);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.IncrementCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));

        long value = jedisCmd.getStringCommands().incr(key);

        receive.responseResult(request, singleValueLongResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.IncrementByCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        long increment = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long value = jedisCmd.getStringCommands().incrBy(key, increment);

        receive.responseResult(request, singleValueLongResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DecrementCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));

        long value = jedisCmd.getStringCommands().decr(key);

        receive.responseResult(request, singleValueLongResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.DecrementByCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        long decrement = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        long value = jedisCmd.getStringCommands().decrBy(key, decrement);

        receive.responseResult(request, singleValueLongResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.AppendCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        String append = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        long value = jedisCmd.getStringCommands().append(key, append);

        receive.responseResult(request, singleValueLongResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetDeleteCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));

        String value = jedisCmd.getStringCommands().getDel(key);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetExCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));

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

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetRangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        long start = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.start), true);
        long end = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        String value = jedisCmd.getStringCommands().getrange(key, start, end);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.GetSetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        String append = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        String value = jedisCmd.getStringCommands().getSet(key, append);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MGetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.StringKeyNameContext> keyNameContexts = cmd.stringKeyName();
        String[] keys = new String[keyNameContexts.size()];
        for (int i = 0; i < keyNameContexts.size(); i++) {
            RedisParser.StringKeyNameContext keyNameContext = keyNameContexts.get(i);
            keys[i] = (String) argOrValue(argIndex, request, keyNameContext.identifier());
        }

        List<String> values = jedisCmd.getStringCommands().mget(keys);

        AdapterResultCursor receiveCur = resultValueStringList(request, values, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MSetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyValueClauseContext> kvContexts = cmd.keyValueClause();
        List<String> keyValues = new ArrayList<>();
        for (RedisParser.KeyValueClauseContext keyValueClauseContext : kvContexts) {
            keyValues.add((String) argOrValue(argIndex, request, keyValueClauseContext.stringKeyName().identifier()));
            keyValues.add((String) argOrValue(argIndex, request, keyValueClauseContext.identifier()));
        }

        String status = jedisCmd.getStringCommands().mset(keyValues.toArray(new String[0]));

        receive.responseUpdateCount(request, StringUtils.equalsIgnoreCase("ok", status) ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.MSetNxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);

        List<RedisParser.KeyValueClauseContext> kvContexts = cmd.keyValueClause();
        List<String> keyValues = new ArrayList<>();
        for (RedisParser.KeyValueClauseContext keyValueClauseContext : kvContexts) {
            keyValues.add((String) argOrValue(argIndex, request, keyValueClauseContext.stringKeyName().identifier()));
            keyValues.add((String) argOrValue(argIndex, request, keyValueClauseContext.identifier()));
        }

        long status = jedisCmd.getStringCommands().msetnx(keyValues.toArray(new String[0]));

        receive.responseUpdateCount(request, status);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.PSetExCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        long milliseconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        String value = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        String status = jedisCmd.getStringCommands().psetex(key, milliseconds, value);

        receive.responseUpdateCount(request, StringUtils.equalsIgnoreCase("ok", status) ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetExCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        long seconds = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        String value = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        String status = jedisCmd.getStringCommands().setex(key, seconds, value);

        receive.responseUpdateCount(request, StringUtils.equalsIgnoreCase("ok", status) ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetNxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        String value = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        long status = jedisCmd.getStringCommands().setnx(key, value);

        receive.responseUpdateCount(request, status);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetRangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        long offset = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        String value = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        long status = jedisCmd.getStringCommands().setrange(key, offset, value);

        receive.responseUpdateCount(request, status);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.StringLengthCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));

        long status = jedisCmd.getStringCommands().strlen(key);

        receive.responseUpdateCount(request, status);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SubstringCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.stringKeyName().identifier()));
        int start = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.start), true);
        int end = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.end), true);

        String value = jedisCmd.getStringCommands().substr(key, start, end);

        receive.responseResult(request, singleValueStringResult(request, value));
        return completed(sync);
    }

    /* ------------------------------------------------------------------------------------------------ ListCommands */

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

    /* ------------------------------------------------------------------------------------------------ SetCommands */

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SaddCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
        List<String> member = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            member.add((String) argOrValue(argIndex, request, identifierContext));
        }

        long result = jedisCmd.getSetCommands().sadd(key, member.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ScardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());

        long result = jedisCmd.getSetCommands().scard(key);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SdiffCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<String> keys = new ArrayList<>();
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        Set<String> result = jedisCmd.getSetCommands().sdiff(keys.toArray(new String[0]));

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SdiffstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dst = (String) argOrValue(argIndex, request, cmd.identifier());
        List<String> keys = new ArrayList<>();
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        long result = jedisCmd.getSetCommands().sdiffstore(dst, keys.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SinterCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<String> keys = new ArrayList<>();
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        Set<String> result = jedisCmd.getSetCommands().sinter(keys.toArray(new String[0]));

        AdapterResultCursor receiveCur = resultValueStringList(request, result, -1);
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SintercardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("SINTERCARD numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        Integer limit = null;
        if (cmd.limitClause() != null) {
            limit = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitClause().integer()), true);
        }

        long result;
        if (limit != null) {
            result = jedisCmd.getSetCommands().sintercard(limit, keys.toArray(new String[0]));
        } else {
            result = jedisCmd.getSetCommands().sintercard(keys.toArray(new String[0]));
        }

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SinterstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = (String) argOrValue(argIndex, request, cmd.identifier());
        List<String> keys = new ArrayList<>();
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        long result = jedisCmd.getSetCommands().sinterstore(dstKey, keys.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SismemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
        String member = (String) argOrValue(argIndex, request, cmd.identifier());

        boolean result = jedisCmd.getSetCommands().sismember(key, member);

        receive.responseUpdateCount(request, result ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SmismemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
        List<String> member = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            member.add((String) argOrValue(argIndex, request, identifierContext));
        }

        List<Boolean> result = jedisCmd.getSetCommands().smismember(key, member.toArray(new String[0]));

        receive.responseResult(request, resultValueBooleanList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SmembersCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());

        Set<String> result = jedisCmd.getSetCommands().smembers(key);

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SmoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = (String) argOrValue(argIndex, request, cmd.src.identifier());
        String dstKey = (String) argOrValue(argIndex, request, cmd.dst.identifier());
        String member = (String) argOrValue(argIndex, request, cmd.member.identifier());

        long result = jedisCmd.getSetCommands().smove(srcKey, dstKey, member);

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
        Long count = null;
        if (cmd.integer() != null) {
            count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        }

        Collection<String> result;
        if (count != null) {
            result = jedisCmd.getSetCommands().spop(key, count);
        } else {
            result = Collections.singletonList(jedisCmd.getSetCommands().spop(key));
        }

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SrandmemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
        Integer count = null;
        if (cmd.decimal() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.decimal()), true);
        }

        Collection<String> result;
        if (count != null) {
            result = jedisCmd.getSetCommands().srandmember(key, count);
        } else {
            result = Collections.singletonList(jedisCmd.getSetCommands().srandmember(key));
        }

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SremCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
        List<String> member = new ArrayList<>();
        for (RedisParser.IdentifierContext identifierContext : cmd.identifier()) {
            member.add((String) argOrValue(argIndex, request, identifierContext));
        }

        long result = jedisCmd.getSetCommands().srem(key, member.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SscanCommanContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.setKeyName().identifier());
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

        ScanResult<String> result = jedisCmd.getSetCommands().sscan(key, cursor, scanParams);

        if (!sync.isDone()) {
            AdapterResultCursor receiveCur = resultCursorAndValueStringList(request, result.getCursor(), result.getResult(), maxRows);
            receive.responseResult(request, receiveCur);
            return completed(sync);
        } else {
            SQLException err = new SQLException("command interrupted.");
            receive.responseFailed(request, err);
            throw err;
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SunionCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<String> keys = new ArrayList<>();
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        Set<String> result = jedisCmd.getSetCommands().sunion(keys.toArray(new String[0]));

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SunionstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = (String) argOrValue(argIndex, request, cmd.identifier());
        List<String> keys = new ArrayList<>();
        for (RedisParser.SetKeyNameContext setKeyNameContext : cmd.setKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        long result = jedisCmd.getSetCommands().sunionstore(dstKey, keys.toArray(new String[0]));

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    /* ------------------------------------------------------------------------------------------------ StoreSetCommands */

    private static Future<?> resultKeyAndScoreAndElement1(Future<Object> sync, AdapterRequest request, AdapterReceive receive, KeyValue<String, List<Tuple>> result) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_KEY_STRING,     //
                COL_SCORE_DOUBLE,   //
                COL_ELEMENT_STRING));
        receive.responseResult(request, receiveCur);

        for (Tuple tuple : result.getValue()) {
            receiveCur.pushData(CollectionUtils.asMap(          //
                    COL_KEY_STRING.name, result.getKey(),       //
                    COL_SCORE_DOUBLE.name, tuple.getScore(),    //
                    COL_ELEMENT_STRING.name, tuple.getElement() //
            ));
        }
        receiveCur.pushFinish();
        return completed(sync);
    }

    private static Future<?> resultKeyAndScoreAndElement2(Future<Object> sync, AdapterRequest request, AdapterReceive receive, KeyValue<String, Tuple> result) throws SQLException {
        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_KEY_STRING,     //
                COL_SCORE_DOUBLE,   //
                COL_ELEMENT_STRING));
        receive.responseResult(request, receiveCur);

        receiveCur.pushData(CollectionUtils.asMap(                      //
                COL_KEY_STRING.name, result.getKey(),                   //
                COL_SCORE_DOUBLE.name, result.getValue().getScore(),    //
                COL_ELEMENT_STRING.name, result.getValue().getElement() //
        ));

        receiveCur.pushFinish();
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext setKeyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZMPOP numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        SortedSetOption option = getSortedSetOption(cmd.minMaxClause());
        Integer count = null;
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }

        KeyValue<String, List<Tuple>> result;
        if (count == null) {
            result = jedisCmd.getSortedSetCommands().zmpop(option, keys.toArray(new String[0]));
        } else {
            result = jedisCmd.getSortedSetCommands().zmpop(option, count, keys.toArray(new String[0]));
        }

        return resultKeyAndScoreAndElement1(sync, request, receive, result);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BzmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toDouble(argOrValue(argIndex, request, cmd.timeout), true);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.number), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext setKeyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("BZMPOP numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        SortedSetOption option = getSortedSetOption(cmd.minMaxClause());
        Integer count = null;
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }

        KeyValue<String, List<Tuple>> result;
        if (count == null) {
            result = jedisCmd.getSortedSetCommands().bzmpop(timeout, option, keys.toArray(new String[0]));
        } else {
            result = jedisCmd.getSortedSetCommands().bzmpop(timeout, option, count, keys.toArray(new String[0]));
        }

        return resultKeyAndScoreAndElement1(sync, request, receive, result);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZpopmaxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier());
        Integer count = null;
        if (cmd.integer() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
        }

        List<Tuple> result;
        if (count == null) {
            result = Collections.singletonList(jedisCmd.getSortedSetCommands().zpopmax(key));
        } else {
            result = jedisCmd.getSortedSetCommands().zpopmax(key, count);
        }

        receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BzpopmaxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        for (RedisParser.SortedSetKeyNameContext setKeyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        KeyValue<String, Tuple> result = jedisCmd.getSortedSetCommands().bzpopmax(timeout, keys.toArray(new String[0]));

        return resultKeyAndScoreAndElement2(sync, request, receive, result);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZpopminCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier());
        Integer count = null;
        if (cmd.integer() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
        }

        List<Tuple> result;
        if (count == null) {
            result = Collections.singletonList(jedisCmd.getSortedSetCommands().zpopmin(key));
        } else {
            result = jedisCmd.getSortedSetCommands().zpopmin(key, count);
        }

        receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BzpopminCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        for (RedisParser.SortedSetKeyNameContext setKeyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, setKeyNameContext.identifier()));
        }

        KeyValue<String, Tuple> result = jedisCmd.getSortedSetCommands().bzpopmin(timeout, keys.toArray(new String[0]));

        return resultKeyAndScoreAndElement2(sync, request, receive, result);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZaddCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier());
        RedisParser.KeyExistenceClauseContext keyExistenceClauseContext = cmd.keyExistenceClause();
        RedisParser.KeyUpdateClauseContext keyUpdateClauseContext = cmd.keyUpdateClause();

        ZAddParams zAddParams = null;

        if (keyExistenceClauseContext != null) {
            zAddParams = new ZAddParams();
            if (keyExistenceClauseContext.NX() != null) {
                zAddParams.nx();
            } else if (keyExistenceClauseContext.XX() != null) {
                zAddParams.xx();
            } else {
                throw new SQLException("keyExistenceClause " + keyExistenceClauseContext.getText() + " not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        if (keyUpdateClauseContext != null) {
            zAddParams = zAddParams == null ? new ZAddParams() : zAddParams;
            if (keyUpdateClauseContext.GT() != null) {
                zAddParams.gt();
            } else if (keyUpdateClauseContext.LT() != null) {
                zAddParams.lt();
            } else {
                throw new SQLException("keyUpdateClause " + keyUpdateClauseContext.getText() + " not support.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        if (cmd.CH() != null) {
            zAddParams = zAddParams == null ? new ZAddParams() : zAddParams;
            zAddParams.ch();
        }

        Map<String, Double> scoreMembers = new LinkedHashMap<>();
        double score = 0;
        String member = null;
        for (RedisParser.ScoreMemberClauseContext scoreMemberClauseContext : cmd.scoreMemberClause()) {
            score = ConvertUtils.toDouble(argOrValue(argIndex, request, scoreMemberClauseContext.decimal()), true);
            member = (String) argOrValue(argIndex, request, scoreMemberClauseContext.identifier());
            scoreMembers.put(member, score);
        }

        if (cmd.INCR() != null) {
            if (scoreMembers.size() > 1) {
                throw new SQLException("Only one score-element pair can be specified in this mode.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
            zAddParams = zAddParams == null ? new ZAddParams() : zAddParams;
            double result = jedisCmd.getSortedSetCommands().zaddIncr(key, score, member, zAddParams);
            receive.responseResult(request, singleValueDoubleResult(request, result));
        } else {
            long result;
            if (zAddParams != null) {
                result = jedisCmd.getSortedSetCommands().zadd(key, scoreMembers, zAddParams);
            } else {
                result = jedisCmd.getSortedSetCommands().zadd(key, scoreMembers);
            }
            receive.responseResult(request, singleValueLongResult(request, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZcardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier());

        long result = jedisCmd.getSortedSetCommands().zcard(key);
        receive.responseResult(request, singleValueLongResult(request, result));

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZcountCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min);
        String max = argAsString(argIndex, request, cmd.max);

        long result = jedisCmd.getSortedSetCommands().zcount(key, min, max);
        receive.responseResult(request, singleValueLongResult(request, result));

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZdiffCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZDIFF numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        if (cmd.WITHSCORES() != null) {
            List<Tuple> result = jedisCmd.getSortedSetCommands().zdiffWithScores(keys.toArray(new String[0]));
            receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zdiff(keys.toArray(new String[0]));
            receive.responseResult(request, resultValueStringList(request, result, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZdiffstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = argAsString(argIndex, request, cmd.identifier());
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZDIFFSTORE numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        long result = jedisCmd.getSortedSetCommands().zdiffstore(dstKey, keys.toArray(new String[0]));
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZincrbyCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier()));
        long increment = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        String member = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));

        double value = jedisCmd.getSortedSetCommands().zincrby(key, increment, member);

        receive.responseResult(request, singleValueDoubleResult(request, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZinterCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZINTER numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        ZParams zParams = new ZParams();
        if (cmd.weightsClause() != null) {
            List<Double> weights = new ArrayList<>();
            for (RedisParser.DecimalContext decimalContext : cmd.weightsClause().decimal()) {
                weights.add(ConvertUtils.toDouble(argOrValue(argIndex, request, decimalContext), true));
            }
            double[] weightArray = new double[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightArray[i] = weights.get(i);
            }
            zParams.weights(weightArray);
        }

        if (cmd.aggregateClause() != null) {
            zParams.aggregate(getAggregateOption(cmd.aggregateClause()));
        }

        if (cmd.WITHSCORES() != null) {
            List<Tuple> value = jedisCmd.getSortedSetCommands().zinterWithScores(zParams, keys.toArray(new String[0]));
            receive.responseResult(request, resultScoreAndElement(request, receive, value, -1));
        } else {
            List<String> value = jedisCmd.getSortedSetCommands().zinter(zParams, keys.toArray(new String[0]));
            receive.responseResult(request, resultValueStringList(request, value, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZintercardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZINTERCARD numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        long result;
        if (cmd.limitClause() != null) {
            long limit = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.limitClause().integer()), true);
            result = jedisCmd.getSortedSetCommands().zintercard(limit, keys.toArray(new String[0]));
        } else {
            result = jedisCmd.getSortedSetCommands().zintercard(keys.toArray(new String[0]));
        }

        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZinterstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = ConvertUtils.toString(argOrValue(argIndex, request, cmd.identifier()));
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZINTERSTORE numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        ZParams zParams = new ZParams();
        if (cmd.weightsClause() != null) {
            List<Double> weights = new ArrayList<>();
            for (RedisParser.DecimalContext decimalContext : cmd.weightsClause().decimal()) {
                weights.add(ConvertUtils.toDouble(argOrValue(argIndex, request, decimalContext), true));
            }
            double[] weightArray = new double[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightArray[i] = weights.get(i);
            }
            zParams.weights(weightArray);
        }

        if (cmd.aggregateClause() != null) {
            zParams.aggregate(getAggregateOption(cmd.aggregateClause()));
        }

        long result = jedisCmd.getSortedSetCommands().zinterstore(dstKey, zParams, keys.toArray(new String[0]));
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZlexcountCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min.identifier());
        String max = argAsString(argIndex, request, cmd.max.identifier());

        long result = jedisCmd.getSortedSetCommands().zlexcount(key, min, max);
        receive.responseResult(request, singleValueLongResult(request, result));

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String member = argAsString(argIndex, request, cmd.identifier());

        Double result = jedisCmd.getSortedSetCommands().zscore(key, member);
        receive.responseResult(request, singleValueDoubleResult(request, result));

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZmscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier()));
        List<String> member = new ArrayList<>();
        for (RedisParser.IdentifierContext memberContext : cmd.identifier()) {
            member.add((String) argOrValue(argIndex, request, memberContext));
        }

        List<Double> result = jedisCmd.getSortedSetCommands().zmscore(key, member.toArray(new String[0]));
        receive.responseResult(request, resultValueDoubleList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrandmemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier()));

        if (cmd.decimal() != null) {
            long count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
            if (cmd.WITHSCORES() != null) {
                List<Tuple> result = jedisCmd.getSortedSetCommands().zrandmemberWithScores(key, count);
                receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
            } else {
                List<String> result = jedisCmd.getSortedSetCommands().zrandmember(key, count);
                receive.responseResult(request, resultValueStringList(request, result, -1));
            }
        } else {
            List<String> result = Collections.singletonList(jedisCmd.getSortedSetCommands().zrandmember(key));
            receive.responseResult(request, resultValueStringList(request, result, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String start = argAsString(argIndex, request, cmd.begin.identifier());
        String stop = argAsString(argIndex, request, cmd.end.identifier());

        ZRangeParams params;
        if (cmd.rangeTypeClause() != null) {
            if (cmd.rangeTypeClause().BYSCORE() != null) {
                double min = ConvertUtils.toDouble(start, true);
                double max = ConvertUtils.toDouble(stop, true);
                params = ZRangeParams.zrangeByScoreParams(min, max);
            } else if (cmd.rangeTypeClause().BYLEX() != null) {
                params = ZRangeParams.zrangeByLexParams(start, stop);
            } else {
                throw new SQLFeatureNotSupportedException("rangeTypeBy  " + cmd.rangeTypeClause().getText() + " not supported.");
            }
        } else {
            int min = ConvertUtils.toInteger(start, true);
            int max = ConvertUtils.toInteger(stop, true);
            params = ZRangeParams.zrangeParams(min, max);
        }

        if (cmd.REV() != null) {
            params.rev();
        }

        if (cmd.limitOffsetClause() != null) {
            int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
            params.limit(offset, count);
        }

        if (cmd.WITHSCORES() != null) {
            List<Tuple> value = jedisCmd.getSortedSetCommands().zrangeWithScores(key, params);
            receive.responseResult(request, resultScoreAndElement(request, receive, value, -1));
        } else {
            List<String> value = jedisCmd.getSortedSetCommands().zrange(key, params);
            receive.responseResult(request, resultValueStringList(request, value, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrangebylexCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min.identifier());
        String max = argAsString(argIndex, request, cmd.max.identifier());

        List<String> value;
        if (cmd.limitOffsetClause() != null) {
            int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
            value = jedisCmd.getSortedSetCommands().zrangeByLex(key, min, max, offset, count);
        } else {
            value = jedisCmd.getSortedSetCommands().zrangeByLex(key, min, max);
        }

        receive.responseResult(request, resultValueStringList(request, value, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrangebyscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min);
        String max = argAsString(argIndex, request, cmd.max);

        if (cmd.WITHSCORES() != null) {
            List<Tuple> value;
            if (cmd.limitOffsetClause() != null) {
                int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
                int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
                value = jedisCmd.getSortedSetCommands().zrangeByScoreWithScores(key, min, max, offset, count);
            } else {
                value = jedisCmd.getSortedSetCommands().zrangeByScoreWithScores(key, min, max);
            }
            receive.responseResult(request, resultScoreAndElement(request, receive, value, -1));
        } else {
            List<String> value;
            if (cmd.limitOffsetClause() != null) {
                int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
                int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
                value = jedisCmd.getSortedSetCommands().zrangeByScore(key, min, max, offset, count);
            } else {
                value = jedisCmd.getSortedSetCommands().zrangeByScore(key, min, max);
            }
            receive.responseResult(request, resultValueStringList(request, value, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrangestoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dst = argAsString(argIndex, request, cmd.identifier());
        String src = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String minStr = argAsString(argIndex, request, cmd.min.identifier());
        String maxStr = argAsString(argIndex, request, cmd.max.identifier());

        ZRangeParams params;
        if (cmd.rangeTypeClause() != null) {
            if (cmd.rangeTypeClause().BYSCORE() != null) {
                double min = ConvertUtils.toDouble(minStr, true);
                double max = ConvertUtils.toDouble(maxStr, true);
                params = ZRangeParams.zrangeByScoreParams(min, max);
            } else if (cmd.rangeTypeClause().BYLEX() != null) {
                params = ZRangeParams.zrangeByLexParams(minStr, maxStr);
            } else {
                throw new SQLFeatureNotSupportedException("rangeTypeBy  " + cmd.rangeTypeClause().getText() + " not supported.");
            }
        } else {
            int min = ConvertUtils.toInteger(minStr, true);
            int max = ConvertUtils.toInteger(maxStr, true);
            params = ZRangeParams.zrangeParams(min, max);
        }

        if (cmd.REV() != null) {
            params.rev();
        }

        if (cmd.limitOffsetClause() != null) {
            int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
            params.limit(offset, count);
        }

        long result = jedisCmd.getSortedSetCommands().zrangestore(dst, src, params);
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrankCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String member = argAsString(argIndex, request, cmd.identifier());

        if (cmd.WITHSCORE() != null) {
            KeyValue<Long, Double> result = jedisCmd.getSortedSetCommands().zrankWithScore(key, member);

            AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                    COL_RANK_LONG,  //
                    COL_SCORE_DOUBLE));
            receive.responseResult(request, receiveCur);

            receiveCur.pushData(CollectionUtils.asMap(       //
                    COL_RANK_LONG.name, result.getKey(),     //
                    COL_SCORE_DOUBLE.name, result.getValue() //
            ));

            receiveCur.pushFinish();
            return completed(sync);
        } else {
            Long result = jedisCmd.getSortedSetCommands().zrank(key, member);
            receive.responseResult(request, singleRankLongResult(request, result));
            return completed(sync);
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrevrankCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String member = argAsString(argIndex, request, cmd.identifier());

        if (cmd.WITHSCORE() != null) {
            KeyValue<Long, Double> result = jedisCmd.getSortedSetCommands().zrevrankWithScore(key, member);

            AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                    COL_RANK_LONG,  //
                    COL_SCORE_DOUBLE));
            receive.responseResult(request, receiveCur);

            receiveCur.pushData(CollectionUtils.asMap(       //
                    COL_RANK_LONG.name, result.getKey(),     //
                    COL_SCORE_DOUBLE.name, result.getValue() //
            ));

            receiveCur.pushFinish();
            return completed(sync);
        } else {
            Long result = jedisCmd.getSortedSetCommands().zrevrank(key, member);
            receive.responseResult(request, singleRankLongResult(request, result));
            return completed(sync);
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = ConvertUtils.toString(argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier()));
        List<String> member = new ArrayList<>();
        for (RedisParser.IdentifierContext keyContext : cmd.identifier()) {
            member.add((String) argOrValue(argIndex, request, keyContext));
        }

        long result = jedisCmd.getSortedSetCommands().zrem(key, member.toArray(new String[0]));
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremrangebylexCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String minStr = argAsString(argIndex, request, cmd.min.identifier());
        String maxStr = argAsString(argIndex, request, cmd.max.identifier());

        long result = jedisCmd.getSortedSetCommands().zremrangeByLex(key, minStr, maxStr);
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremrangebyrankCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        long start = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long stop = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        long result = jedisCmd.getSortedSetCommands().zremrangeByRank(key, start, stop);
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremrangebyscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min);
        String max = argAsString(argIndex, request, cmd.max);

        long result = jedisCmd.getSortedSetCommands().zremrangeByScore(key, min, max);
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrevrangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        long start = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long stop = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        if (cmd.WITHSCORES() != null) {
            List<Tuple> result = jedisCmd.getSortedSetCommands().zrevrangeWithScores(key, start, stop);
            receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zrevrange(key, start, stop);
            receive.responseResult(request, resultValueStringList(request, result, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrevrangebylexCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String max = argAsString(argIndex, request, cmd.max.identifier());
        String min = argAsString(argIndex, request, cmd.min.identifier());

        List<String> result;
        if (cmd.limitOffsetClause() != null) {
            int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
            result = jedisCmd.getSortedSetCommands().zrevrangeByLex(key, max, min, offset, count);
        } else {
            result = jedisCmd.getSortedSetCommands().zrevrangeByLex(key, max, min);
        }

        receive.responseResult(request, resultValueStringList(request, result, -1));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrevrangebyscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String max = argAsString(argIndex, request, cmd.max);
        String min = argAsString(argIndex, request, cmd.min);

        if (cmd.WITHSCORES() != null) {
            List<Tuple> result;
            if (cmd.limitOffsetClause() != null) {
                int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
                int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
                result = jedisCmd.getSortedSetCommands().zrevrangeByScoreWithScores(key, max, min, offset, count);
            } else {
                result = jedisCmd.getSortedSetCommands().zrevrangeByScoreWithScores(key, max, min);
            }
            receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
        } else {
            List<String> result;
            if (cmd.limitOffsetClause() != null) {
                int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
                int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
                result = jedisCmd.getSortedSetCommands().zrevrangeByScore(key, max, min, offset, count);
            } else {
                result = jedisCmd.getSortedSetCommands().zrevrangeByScore(key, max, min);
            }

            receive.responseResult(request, resultValueStringList(request, result, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZscanCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = (String) argOrValue(argIndex, request, cmd.sortedSetKeyName().identifier());
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

        ScanResult<Tuple> result = jedisCmd.getSortedSetCommands().zscan(key, cursor, scanParams);

        if (!sync.isDone()) {
            AdapterResultCursor receiveCur = resultCursorAndScoreAndElement(request, receive, result.getCursor(), result.getResult(), maxRows);
            receive.responseResult(request, receiveCur);
            return completed(sync);
        } else {
            SQLException err = new SQLException("command interrupted.");
            receive.responseFailed(request, err);
            throw err;
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZunionCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZUNION numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        ZParams zParams = new ZParams();
        if (cmd.weightsClause() != null) {
            List<Double> weights = new ArrayList<>();
            for (RedisParser.DecimalContext decimalContext : cmd.weightsClause().decimal()) {
                weights.add(ConvertUtils.toDouble(argOrValue(argIndex, request, decimalContext), true));
            }
            double[] weightArray = new double[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightArray[i] = weights.get(i);
            }
            zParams.weights(weightArray);
        }

        if (cmd.aggregateClause() != null) {
            zParams.aggregate(getAggregateOption(cmd.aggregateClause()));
        }

        if (cmd.WITHSCORES() != null) {
            List<Tuple> result = jedisCmd.getSortedSetCommands().zunionWithScores(zParams, keys.toArray(new String[0]));
            receive.responseResult(request, resultScoreAndElement(request, receive, result, -1));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zunion(zParams, keys.toArray(new String[0]));
            receive.responseResult(request, resultValueStringList(request, result, -1));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZunionstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = (String) argOrValue(argIndex, request, cmd.identifier());
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<String> keys = new ArrayList<>();
        int cnt = 0;
        for (RedisParser.SortedSetKeyNameContext keyNameContext : cmd.sortedSetKeyName()) {
            keys.add((String) argOrValue(argIndex, request, keyNameContext.identifier()));
            cnt++;
        }
        if (cnt != numKeys) {
            throw new SQLException("ZUNION numKeys " + numKeys + " not match actual keys " + cnt + ".", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        ZParams zParams = new ZParams();
        if (cmd.weightsClause() != null) {
            List<Double> weights = new ArrayList<>();
            for (RedisParser.DecimalContext decimalContext : cmd.weightsClause().decimal()) {
                weights.add(ConvertUtils.toDouble(argOrValue(argIndex, request, decimalContext), true));
            }
            double[] weightArray = new double[weights.size()];
            for (int i = 0; i < weights.size(); i++) {
                weightArray[i] = weights.get(i);
            }
            zParams.weights(weightArray);
        }

        if (cmd.aggregateClause() != null) {
            zParams.aggregate(getAggregateOption(cmd.aggregateClause()));
        }

        long result = jedisCmd.getSortedSetCommands().zunionstore(dstKey, zParams, keys.toArray(new String[0]));
        receive.responseResult(request, singleValueLongResult(request, result));
        return completed(sync);
    }

    /* ------------------------------------------------------------------------------------------------ HashCommands */

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
