package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

class JedisCommandsForSet extends JedisCommands {
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
}
