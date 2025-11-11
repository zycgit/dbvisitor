package net.hasor.dbvisitor.adapter.redis;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.args.SortedSetOption;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZParams;
import redis.clients.jedis.params.ZRangeParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;
import redis.clients.jedis.util.KeyValue;

class JedisCommandsForStoreSet extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "keys", keys.length, numKeys);

        SortedSetOption option = getSortedSetOption(cmd.minMaxClause());
        Integer count = null;
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }

        KeyValue<String, List<Tuple>> result;
        if (count == null) {
            result = jedisCmd.getSortedSetCommands().zmpop(option, keys);
        } else {
            result = jedisCmd.getSortedSetCommands().zmpop(option, count, keys);
        }

        AdapterResultCursor resultCursor = listResult(request, COL_KEY_STRING, result);
        receive.responseResult(request, resultCursor);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BzmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toDouble(argOrValue(argIndex, request, cmd.timeout), true);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.number), true);
        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "BZMPOP", keys.length, numKeys);

        SortedSetOption option = getSortedSetOption(cmd.minMaxClause());
        Integer count = null;
        if (cmd.countClause() != null) {
            count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
        }

        KeyValue<String, List<Tuple>> result;
        if (count == null) {
            result = jedisCmd.getSortedSetCommands().bzmpop(timeout, option, keys);
        } else {
            result = jedisCmd.getSortedSetCommands().bzmpop(timeout, option, count, keys);
        }

        AdapterResultCursor resultCursor = listResult(request, COL_KEY_STRING, result);
        receive.responseResult(request, resultCursor);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZpopmaxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
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

        receive.responseResult(request, listResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BzpopmaxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }

        KeyValue<String, Tuple> result = jedisCmd.getSortedSetCommands().bzpopmax(timeout, keys);

        AdapterResultCursor resultCursor = listResult(request, COL_KEY_STRING, new KeyValue<>(result.getKey(), Collections.singletonList(result.getValue())));
        receive.responseResult(request, resultCursor);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZpopminCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
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

        receive.responseResult(request, listResult(request, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BzpopminCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }

        KeyValue<String, Tuple> result = jedisCmd.getSortedSetCommands().bzpopmin(timeout, keys);

        AdapterResultCursor resultCursor = listResult(request, COL_KEY_STRING, new KeyValue<>(result.getKey(), Collections.singletonList(result.getValue())));
        receive.responseResult(request, resultCursor);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZaddCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
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
            member = argAsString(argIndex, request, scoreMemberClauseContext.identifier());
            scoreMembers.put(member, score);
        }

        if (cmd.INCR() != null) {
            if (scoreMembers.size() > 1) {
                throw new SQLException("Only one score-element pair can be specified in this mode.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
            zAddParams = zAddParams == null ? new ZAddParams() : zAddParams;
            double result = jedisCmd.getSortedSetCommands().zaddIncr(key, score, member, zAddParams);
            receive.responseResult(request, singleResult(request, COL_RESULT_DOUBLE, result));
        } else {
            long result;
            if (zAddParams != null) {
                result = jedisCmd.getSortedSetCommands().zadd(key, scoreMembers, zAddParams);
            } else {
                result = jedisCmd.getSortedSetCommands().zadd(key, scoreMembers);
            }
            receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZcardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());

        long result = jedisCmd.getSortedSetCommands().zcard(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZcountCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min);
        String max = argAsString(argIndex, request, cmd.max);

        long result = jedisCmd.getSortedSetCommands().zcount(key, min, max);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZdiffCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZDIFF", keys.length, numKeys);

        if (cmd.WITHSCORES() != null) {
            List<Tuple> result = jedisCmd.getSortedSetCommands().zdiffWithScores(keys);
            receive.responseResult(request, listResult(request, result));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zdiff(keys);
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZdiffstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = argAsString(argIndex, request, cmd.identifier());
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZDIFFSTORE", keys.length, numKeys);

        long result = jedisCmd.getSortedSetCommands().zdiffstore(dstKey, keys);
        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZincrbyCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        long increment = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        String member = argAsString(argIndex, request, cmd.identifier());

        double value = jedisCmd.getSortedSetCommands().zincrby(key, increment, member);

        receive.responseResult(request, singleResult(request, COL_SCORE_DOUBLE, value));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZinterCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZINTER", keys.length, numKeys);

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
            List<Tuple> result = jedisCmd.getSortedSetCommands().zinterWithScores(zParams, keys);
            receive.responseResult(request, listResult(request, result));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zinter(zParams, keys);
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZintercardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZINTERCARD", keys.length, numKeys);

        long result;
        if (cmd.limitClause() != null) {
            long limit = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.limitClause().integer()), true);
            result = jedisCmd.getSortedSetCommands().zintercard(limit, keys);
        } else {
            result = jedisCmd.getSortedSetCommands().zintercard(keys);
        }

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZinterstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = argAsString(argIndex, request, cmd.identifier());
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZINTERSTORE", keys.length, numKeys);

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

        long result = jedisCmd.getSortedSetCommands().zinterstore(dstKey, zParams, keys);
        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZlexcountCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min.identifier());
        String max = argAsString(argIndex, request, cmd.max.identifier());

        long result = jedisCmd.getSortedSetCommands().zlexcount(key, min, max);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String member = argAsString(argIndex, request, cmd.identifier());

        Double result = jedisCmd.getSortedSetCommands().zscore(key, member);

        receive.responseResult(request, singleResult(request, COL_SCORE_DOUBLE, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZmscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        List<RedisParser.IdentifierContext> nameContexts = cmd.identifier();
        String[] member = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            member[i] = argAsString(argIndex, request, nameContexts.get(i));
        }

        List<Double> result = jedisCmd.getSortedSetCommands().zmscore(key, member);

        receive.responseResult(request, listResult(request, COL_SCORE_DOUBLE, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrandmemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());

        if (cmd.decimal() != null) {
            long count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
            if (cmd.WITHSCORES() != null) {
                List<Tuple> result = jedisCmd.getSortedSetCommands().zrandmemberWithScores(key, count);
                receive.responseResult(request, listResult(request, result));
            } else {
                List<String> result = jedisCmd.getSortedSetCommands().zrandmember(key, count);
                receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
            }
        } else {
            List<String> result = Collections.singletonList(jedisCmd.getSortedSetCommands().zrandmember(key));
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
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
            receive.responseResult(request, listResult(request, value));
        } else {
            List<String> value = jedisCmd.getSortedSetCommands().zrange(key, params);
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, value));
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

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, value));
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
            receive.responseResult(request, listResult(request, value));
        } else {
            List<String> value;
            if (cmd.limitOffsetClause() != null) {
                int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
                int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
                value = jedisCmd.getSortedSetCommands().zrangeByScore(key, min, max, offset, count);
            } else {
                value = jedisCmd.getSortedSetCommands().zrangeByScore(key, min, max);
            }
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, value));
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
        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrankCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String member = argAsString(argIndex, request, cmd.identifier());

        if (cmd.WITHSCORE() != null) {
            KeyValue<Long, Double> result = jedisCmd.getSortedSetCommands().zrankWithScore(key, member);

            AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                    COL_SCORE_DOUBLE, COL_RANK_LONG));
            receive.responseResult(request, receiveCur);

            receiveCur.pushData(CollectionUtils.asMap(       //
                    COL_SCORE_DOUBLE.name, result.getValue(),//
                    COL_RANK_LONG.name, result.getKey()      //
            ));

            receiveCur.pushFinish();
            return completed(sync);
        } else {
            Long result = jedisCmd.getSortedSetCommands().zrank(key, member);
            receive.responseResult(request, singleResult(request, COL_RANK_LONG, result));
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
            receive.responseResult(request, singleResult(request, COL_RANK_LONG, result));
            return completed(sync);
        }
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());

        List<RedisParser.IdentifierContext> nameContexts = cmd.identifier();
        String[] member = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            member[i] = argAsString(argIndex, request, nameContexts.get(i));
        }

        long result = jedisCmd.getSortedSetCommands().zrem(key, member);
        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremrangebylexCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String minStr = argAsString(argIndex, request, cmd.min.identifier());
        String maxStr = argAsString(argIndex, request, cmd.max.identifier());

        long result = jedisCmd.getSortedSetCommands().zremrangeByLex(key, minStr, maxStr);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremrangebyrankCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        long start = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long stop = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        long result = jedisCmd.getSortedSetCommands().zremrangeByRank(key, start, stop);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZremrangebyscoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String min = argAsString(argIndex, request, cmd.min);
        String max = argAsString(argIndex, request, cmd.max);

        long result = jedisCmd.getSortedSetCommands().zremrangeByScore(key, min, max);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZrevrangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        long start = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long stop = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        if (cmd.WITHSCORES() != null) {
            List<Tuple> result = jedisCmd.getSortedSetCommands().zrevrangeWithScores(key, start, stop);
            receive.responseResult(request, listResult(request, result));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zrevrange(key, start, stop);
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
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

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
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
            receive.responseResult(request, listResult(request, result));
        } else {
            List<String> result;
            if (cmd.limitOffsetClause() != null) {
                int offset = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().offset), true);
                int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitOffsetClause().count), true);
                result = jedisCmd.getSortedSetCommands().zrevrangeByScore(key, max, min, offset, count);
            } else {
                result = jedisCmd.getSortedSetCommands().zrevrangeByScore(key, max, min);
            }

            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZscanCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.sortedSetKeyName().identifier());
        String cursor = argAsString(argIndex, request, cmd.decimal());
        String pattern = null;
        Integer count = null;
        long maxRows = request.getMaxRows();
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

        ScanResult<Tuple> result = jedisCmd.getSortedSetCommands().zscan(key, cursor, scanParams);
        if (sync.isDone()) {
            SQLException err = new SQLException("command interrupted.");
            receive.responseFailed(request, err);
            throw err;
        }

        AdapterResultCursor receiveCur = new AdapterResultCursor(request, Arrays.asList(//
                COL_CURSOR_STRING,   //
                COL_SCORE_DOUBLE,   //
                COL_ELEMENT_STRING));
        receive.responseResult(request, receiveCur);

        int affectRows = 0;
        for (Tuple tuple : result.getResult()) {
            receiveCur.pushData(CollectionUtils.asMap(          //
                    COL_CURSOR_STRING.name, result.getCursor(), //
                    COL_SCORE_DOUBLE.name, tuple.getScore(),    //
                    COL_ELEMENT_STRING.name, tuple.getElement() //
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

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZunionCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZUNION", keys.length, numKeys);

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
            List<Tuple> result = jedisCmd.getSortedSetCommands().zunionWithScores(zParams, keys);
            receive.responseResult(request, listResult(request, result));
        } else {
            List<String> result = jedisCmd.getSortedSetCommands().zunion(zParams, keys);
            receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ZunionstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = argAsString(argIndex, request, cmd.identifier());
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);
        List<RedisParser.SortedSetKeyNameContext> nameContexts = cmd.sortedSetKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "ZUNIONSTORE", keys.length, numKeys);

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

        long result = jedisCmd.getSortedSetCommands().zunionstore(dstKey, zParams, keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }
}
