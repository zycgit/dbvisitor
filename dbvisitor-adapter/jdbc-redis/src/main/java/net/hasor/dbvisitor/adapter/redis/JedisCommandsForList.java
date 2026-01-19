/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.redis;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.*;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.ListPosition;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.util.KeyValue;

class JedisCommandsForList extends JedisCommands {
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

    private static ListPosition getListPosition(RedisParser.BeforeOrAfterClauseContext cmd) throws SQLException {
        ListPosition where;
        if (cmd.BEFORE() != null) {
            where = ListPosition.BEFORE;
        } else if (cmd.AFTER() != null) {
            where = ListPosition.AFTER;
        } else {
            throw new SQLException("must be BEFORE or AFTER", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        return where;
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LmoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = argAsString(argIndex, request, cmd.src.identifier());
        String dstKey = argAsString(argIndex, request, cmd.dst.identifier());
        ListDirection from = getListDirection(cmd.from, ListDirection.LEFT);
        ListDirection to = getListDirection(cmd.to, ListDirection.LEFT);

        String item = jedisCmd.getListCommands().lmove(srcKey, dstKey, from, to);

        receive.responseResult(request, singleResult(request, COL_ELEMENT_STRING, item));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BlmoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = argAsString(argIndex, request, cmd.src.identifier());
        String dstKey = argAsString(argIndex, request, cmd.dst.identifier());
        ListDirection from = getListDirection(cmd.from, ListDirection.LEFT);
        ListDirection to = getListDirection(cmd.to, ListDirection.LEFT);
        double timeout = ConvertUtils.toDouble(argOrValue(argIndex, request, cmd.integer()), true);

        String item = jedisCmd.getListCommands().blmove(srcKey, dstKey, from, to, timeout);

        receive.responseResult(request, singleResult(request, COL_ELEMENT_STRING, item));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        String[] keyValues = new String[kvContexts.size()];
        for (int i = 0; i < kvContexts.size(); i++) {
            RedisParser.ListKeyNameContext keyClauseContext = kvContexts.get(i);
            keyValues[i] = argAsString(argIndex, request, keyClauseContext.identifier());
        }
        numKeysCheck(request, "LMPOP", keyValues.length, numKeys);

        ListDirection lr = getListDirection(cmd.leftOrRightClause(), ListDirection.LEFT);

        KeyValue<String, List<String>> values;
        if (cmd.countClause() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
            values = jedisCmd.getListCommands().lmpop(lr, count, keyValues);
        } else {
            values = jedisCmd.getListCommands().lmpop(lr, keyValues);
        }

        AdapterResultCursor receiveCur = listFixedColAndResult(request, COL_KEY_STRING, values.getKey(), COL_ELEMENT_STRING, values.getValue());
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BlmpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        double timeout = ConvertUtils.toDouble(argOrValue(argIndex, request, cmd.timeout), true);
        int numKeys = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.numkeys), true);

        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        String[] keyValues = new String[kvContexts.size()];
        for (int i = 0; i < kvContexts.size(); i++) {
            RedisParser.ListKeyNameContext keyClauseContext = kvContexts.get(i);
            keyValues[i] = argAsString(argIndex, request, keyClauseContext.identifier());
        }
        numKeysCheck(request, "BLMPOP", keyValues.length, numKeys);

        ListDirection lr = getListDirection(cmd.leftOrRightClause(), ListDirection.LEFT);

        KeyValue<String, List<String>> values;
        if (cmd.countClause() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.countClause().integer()), true);
            values = jedisCmd.getListCommands().blmpop(timeout, lr, count, keyValues);
        } else {
            values = jedisCmd.getListCommands().blmpop(timeout, lr, keyValues);
        }

        AdapterResultCursor receiveCur = listFixedColAndResult(request, COL_KEY_STRING, values.getKey(), COL_ELEMENT_STRING, values.getValue());
        receive.responseResult(request, receiveCur);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyStr = argAsString(argIndex, request, cmd.listKeyName().identifier());

        List<String> result;
        if (cmd.integer() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
            result = jedisCmd.getListCommands().lpop(keyStr, count);
        } else {
            result = Collections.singletonList(jedisCmd.getListCommands().lpop(keyStr));
        }

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String keyStr = argAsString(argIndex, request, cmd.listKeyName().identifier());

        List<String> result;
        if (cmd.integer() != null) {
            int count = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);
            result = jedisCmd.getListCommands().rpop(keyStr, count);
        } else {
            result = Collections.singletonList(jedisCmd.getListCommands().rpop(keyStr));
        }

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BlpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        String[] keys = new String[kvContexts.size()];
        for (int i = 0; i < kvContexts.size(); i++) {
            RedisParser.ListKeyNameContext keyClauseContext = kvContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyClauseContext.identifier());
        }
        int timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<String> result = jedisCmd.getListCommands().blpop(timeout, keys);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BrpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<RedisParser.ListKeyNameContext> kvContexts = cmd.listKeyName();
        String[] keys = new String[kvContexts.size()];
        for (int i = 0; i < kvContexts.size(); i++) {
            RedisParser.ListKeyNameContext keyClauseContext = kvContexts.get(i);
            keys[i] = argAsString(argIndex, request, keyClauseContext.identifier());
        }
        int timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        List<String> result = jedisCmd.getListCommands().brpop(timeout, keys);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpopLpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = argAsString(argIndex, request, cmd.src.identifier());
        String dstKey = argAsString(argIndex, request, cmd.dst.identifier());

        String result = jedisCmd.getListCommands().rpoplpush(srcKey, dstKey);

        receive.responseResult(request, singleResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.BrpopLpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = argAsString(argIndex, request, cmd.src.identifier());
        String dstKey = argAsString(argIndex, request, cmd.dst.identifier());
        int timeout = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.integer()), true);

        String result = jedisCmd.getListCommands().brpoplpush(srcKey, dstKey, timeout);

        receive.responseResult(request, singleResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LindexCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        long index = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);

        String result = jedisCmd.getListCommands().lindex(key, index);

        receive.responseResult(request, singleResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LinsertCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        String pivot = argAsString(argIndex, request, cmd.pivot);
        String ele = argAsString(argIndex, request, cmd.ele);
        ListPosition where = getListPosition(cmd.beforeOrAfterClause());

        long result = jedisCmd.getListCommands().linsert(key, where, pivot, ele);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LlenCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());

        long result = jedisCmd.getListCommands().llen(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LposCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        String element = argAsString(argIndex, request, cmd.identifier());
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
            receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        } else if (count == null) {
            long result = jedisCmd.getListCommands().lpos(key, element, params);
            receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        } else {
            List<Long> result = jedisCmd.getListCommands().lpos(key, element, params, count);
            receive.responseResult(request, listResult(request, COL_RESULT_LONG, result));
        }

        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());

        List<RedisParser.IdentifierContext> contextList = cmd.identifier();
        String[] elements = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            RedisParser.IdentifierContext idContext = contextList.get(i);
            elements[i] = argAsString(argIndex, request, idContext);
        }

        long result = jedisCmd.getListCommands().lpush(key, elements);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LpushxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());

        List<RedisParser.IdentifierContext> contextList = cmd.identifier();
        String[] elements = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            RedisParser.IdentifierContext idContext = contextList.get(i);
            elements[i] = argAsString(argIndex, request, idContext);
        }

        long result = jedisCmd.getListCommands().lpushx(key, elements);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpushCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());

        List<RedisParser.IdentifierContext> contextList = cmd.identifier();
        String[] elements = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            RedisParser.IdentifierContext idContext = contextList.get(i);
            elements[i] = argAsString(argIndex, request, idContext);
        }

        long result = jedisCmd.getListCommands().rpush(key, elements);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.RpushxCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());

        List<RedisParser.IdentifierContext> contextList = cmd.identifier();
        String[] elements = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            RedisParser.IdentifierContext idContext = contextList.get(i);
            elements[i] = argAsString(argIndex, request, idContext);
        }

        long result = jedisCmd.getListCommands().rpushx(key, elements);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LrangeCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        long begin = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long end = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        List<String> result = jedisCmd.getListCommands().lrange(key, begin, end);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LremCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        long count = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        String value = argAsString(argIndex, request, cmd.identifier());

        long result = jedisCmd.getListCommands().lrem(key, count, value);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LsetCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        long index = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.decimal()), true);
        String value = argAsString(argIndex, request, cmd.identifier());

        String result = jedisCmd.getListCommands().lset(key, index, value);

        receive.responseUpdateCount(request, StringUtils.equalsIgnoreCase("ok", result) ? 1 : 0);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.LtrimCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.listKeyName().identifier());
        long begin = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.begin), true);
        long end = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.end), true);

        String result = jedisCmd.getListCommands().ltrim(key, begin, end);

        receive.responseUpdateCount(request, StringUtils.equalsIgnoreCase("ok", result) ? 1 : 0);
        return completed(sync);
    }
}