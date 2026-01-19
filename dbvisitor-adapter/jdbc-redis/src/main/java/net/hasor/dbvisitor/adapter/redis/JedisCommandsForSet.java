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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.ConvertUtils;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

class JedisCommandsForSet extends JedisCommands {
    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SaddCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());
        List<RedisParser.IdentifierContext> contextList = cmd.identifier();
        String[] member = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            member[i] = argAsString(argIndex, request, contextList.get(i));
        }
        long result = jedisCmd.getSetCommands().sadd(key, member);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ScardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());

        long result = jedisCmd.getSetCommands().scard(key);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SdiffCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<RedisParser.SetKeyNameContext> contextList = cmd.setKeyName();
        String[] keys = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            keys[i] = argAsString(argIndex, request, contextList.get(i).identifier());
        }

        Set<String> result = jedisCmd.getSetCommands().sdiff(keys);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SdiffstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dst = argAsString(argIndex, request, cmd.identifier());

        List<RedisParser.SetKeyNameContext> nameContexts = cmd.setKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }

        long result = jedisCmd.getSetCommands().sdiffstore(dst, keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SinterCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        List<RedisParser.SetKeyNameContext> nameContexts = cmd.setKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }

        Set<String> result = jedisCmd.getSetCommands().sinter(keys);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SintercardCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        long numKeys = ConvertUtils.toLong(argOrValue(argIndex, request, cmd.integer()), true);

        List<RedisParser.SetKeyNameContext> nameContexts = cmd.setKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }
        numKeysCheck(request, "SINTERCARD", keys.length, numKeys);

        Integer limit = null;
        if (cmd.limitClause() != null) {
            limit = ConvertUtils.toInteger(argOrValue(argIndex, request, cmd.limitClause().integer()), true);
        }

        long result;
        if (limit != null) {
            result = jedisCmd.getSetCommands().sintercard(limit, keys);
        } else {
            result = jedisCmd.getSetCommands().sintercard(keys);
        }

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SinterstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = argAsString(argIndex, request, cmd.identifier());

        List<RedisParser.SetKeyNameContext> nameContexts = cmd.setKeyName();
        String[] keys = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, nameContexts.get(i).identifier());
        }

        long result = jedisCmd.getSetCommands().sinterstore(dstKey, keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SismemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());
        String member = argAsString(argIndex, request, cmd.identifier());

        boolean result = jedisCmd.getSetCommands().sismember(key, member);

        receive.responseResult(request, singleResult(request, COL_RESULT_LONG, result ? 1 : 0));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SmismemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());

        List<RedisParser.IdentifierContext> nameContexts = cmd.identifier();
        String[] member = new String[nameContexts.size()];
        for (int i = 0; i < nameContexts.size(); i++) {
            member[i] = argAsString(argIndex, request, nameContexts.get(i));
        }

        List<Boolean> result = jedisCmd.getSetCommands().smismember(key, member);

        receive.responseResult(request, listResult(request, COL_RESULT_BOOLEAN, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SmembersCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());

        Set<String> result = jedisCmd.getSetCommands().smembers(key);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SmoveCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String srcKey = argAsString(argIndex, request, cmd.src.identifier());
        String dstKey = argAsString(argIndex, request, cmd.dst.identifier());
        String member = argAsString(argIndex, request, cmd.member.identifier());

        long result = jedisCmd.getSetCommands().smove(srcKey, dstKey, member);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SpopCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());
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

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SrandmemberCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());
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

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SremCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());

        List<RedisParser.IdentifierContext> contextList = cmd.identifier();
        String[] member = new String[contextList.size()];
        for (int i = 0; i < contextList.size(); i++) {
            member[i] = argAsString(argIndex, request, contextList.get(i));
        }

        long result = jedisCmd.getSetCommands().srem(key, member);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SscanCommanContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String key = argAsString(argIndex, request, cmd.setKeyName().identifier());
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

        ScanResult<String> result = jedisCmd.getSetCommands().sscan(key, cursor, scanParams);

        if (!sync.isDone()) {
            AdapterResultCursor receiveCur = listFixedColAndResult(request, COL_CURSOR_STRING, result.getCursor(), COL_ELEMENT_STRING, result.getResult());
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

        List<RedisParser.SetKeyNameContext> keysContexts = cmd.setKeyName();
        String[] keys = new String[keysContexts.size()];
        for (int i = 0; i < keysContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, keysContexts.get(i).identifier());
        }

        Set<String> result = jedisCmd.getSetCommands().sunion(keys);

        receive.responseResult(request, listResult(request, COL_ELEMENT_STRING, result));
        return completed(sync);
    }

    public static Future<?> execCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SunionstoreCommandContext cmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dstKey = argAsString(argIndex, request, cmd.identifier());

        List<RedisParser.SetKeyNameContext> keysContexts = cmd.setKeyName();
        String[] keys = new String[keysContexts.size()];
        for (int i = 0; i < keysContexts.size(); i++) {
            keys[i] = argAsString(argIndex, request, keysContexts.get(i).identifier());
        }

        long result = jedisCmd.getSetCommands().sunionstore(dstKey, keys);

        receive.responseUpdateCount(request, result);
        return completed(sync);
    }
}
