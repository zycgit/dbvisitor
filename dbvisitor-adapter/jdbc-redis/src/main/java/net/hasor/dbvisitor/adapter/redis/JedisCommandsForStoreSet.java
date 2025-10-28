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
}
