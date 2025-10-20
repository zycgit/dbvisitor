package net.hasor.dbvisitor.adapter.redis;
import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class JedisDistributeCall {
    public static Future<?> execRedisCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.commonCommand() != null) {
            return execCommonCmd(sync, jedisCmd, c.commonCommand(), request, receive, startArgIdx, conn);
        }
        if (c.stringCommand() != null) {
            return execStringCmd(sync, jedisCmd, c.stringCommand(), request, receive, startArgIdx, conn);
        }
        if (c.listCommand() != null) {
            return execListCmd(sync, jedisCmd, c.listCommand(), request, receive, startArgIdx, conn);
        }
        if (c.setCommand() != null) {
            return execSetCmd(sync, jedisCmd, c.setCommand(), request, receive, startArgIdx, conn);
        }
        if (c.sortedSetCommand() != null) {
            return execSortedSetCmd(sync, jedisCmd, c.sortedSetCommand(), request, receive, startArgIdx, conn);
        }
        if (c.hashCommand() != null) {
            return execHashCmd(sync, jedisCmd, c.hashCommand(), request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown command.");
    }

    public static Future<?> execCommonCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.CommonCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.copyCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.copyCommand(), request, receive, startArgIdx);
        }
        if (c.deleteCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.deleteCommand(), request, receive, startArgIdx);
        }
        if (c.unlinkCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.unlinkCommand(), request, receive, startArgIdx);
        }
        if (c.dumpCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.dumpCommand(), request, receive, startArgIdx);
        }
        if (c.existsCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.existsCommand(), request, receive, startArgIdx);
        }
        if (c.expireCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.expireCommand(), request, receive, startArgIdx);
        }
        if (c.expireAtCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.expireAtCommand(), request, receive, startArgIdx);
        }
        if (c.expireTimeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.expireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.pExpireCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.pExpireCommand(), request, receive, startArgIdx);
        }
        if (c.pExpireAtCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.pExpireAtCommand(), request, receive, startArgIdx);
        }
        if (c.pExpireTimeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.pExpireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.keysCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.keysCommand(), request, receive, startArgIdx);
        }
        if (c.moveCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.moveCommand(), request, receive, startArgIdx);
        }
        if (c.objectCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.objectCommand(), request, receive, startArgIdx);
        }
        if (c.persistCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.persistCommand(), request, receive, startArgIdx);
        }
        if (c.ttlCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.ttlCommand(), request, receive, startArgIdx);
        }
        if (c.pTtlCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.pTtlCommand(), request, receive, startArgIdx);
        }
        if (c.randomKeyCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.randomKeyCommand(), request, receive, startArgIdx);
        }
        if (c.renameCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.renameCommand(), request, receive, startArgIdx);
        }
        if (c.renameNxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.renameNxCommand(), request, receive, startArgIdx);
        }
        if (c.scanCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.scanCommand(), request, receive, startArgIdx);
        }
        if (c.touchCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.touchCommand(), request, receive, startArgIdx);
        }
        if (c.typeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.typeCommand(), request, receive, startArgIdx);
        }
        if (c.waitCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.waitCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown Common Command.");
    }

    public static Future<?> execStringCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.StringCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.stringSetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.stringSetCommand(), request, receive, startArgIdx);
        }
        if (c.getCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.getCommand(), request, receive, startArgIdx);
        }
        if (c.incrementCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.incrementCommand(), request, receive, startArgIdx);
        }
        if (c.incrementByCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.incrementByCommand(), request, receive, startArgIdx);
        }
        if (c.decrementCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.decrementCommand(), request, receive, startArgIdx);
        }
        if (c.decrementByCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.decrementByCommand(), request, receive, startArgIdx);
        }
        if (c.appendCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.appendCommand(), request, receive, startArgIdx);
        }
        if (c.getDeleteCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.getDeleteCommand(), request, receive, startArgIdx);
        }
        if (c.getExCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.getExCommand(), request, receive, startArgIdx);
        }
        if (c.getRangeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.getRangeCommand(), request, receive, startArgIdx);
        }
        if (c.getSetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.getSetCommand(), request, receive, startArgIdx);
        }
        if (c.mGetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.mGetCommand(), request, receive, startArgIdx);
        }
        if (c.mSetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.mSetCommand(), request, receive, startArgIdx);
        }
        if (c.mSetNxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.mSetNxCommand(), request, receive, startArgIdx);
        }
        if (c.pSetExCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.pSetExCommand(), request, receive, startArgIdx);
        }
        if (c.setExCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.setExCommand(), request, receive, startArgIdx);
        }
        if (c.setNxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.setNxCommand(), request, receive, startArgIdx);
        }
        if (c.setRangeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.setRangeCommand(), request, receive, startArgIdx);
        }
        if (c.stringLengthCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.stringLengthCommand(), request, receive, startArgIdx);
        }
        if (c.substringCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.substringCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown String Command.");
    }

    public static Future<?> execListCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ListCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.lmoveCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lmoveCommand(), request, receive, startArgIdx);
        }
        if (c.blmoveCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.blmoveCommand(), request, receive, startArgIdx);
        }
        if (c.lmpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lmpopCommand(), request, receive, startArgIdx, conn);
        }
        if (c.blmpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.blmpopCommand(), request, receive, startArgIdx, conn);
        }
        if (c.lpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lpopCommand(), request, receive, startArgIdx);
        }
        if (c.blpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.blpopCommand(), request, receive, startArgIdx);
        }
        if (c.rpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.rpopCommand(), request, receive, startArgIdx);
        }
        if (c.brpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.brpopCommand(), request, receive, startArgIdx);
        }
        if (c.rpopLpushCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.rpopLpushCommand(), request, receive, startArgIdx);
        }
        if (c.brpopLpushCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.brpopLpushCommand(), request, receive, startArgIdx);
        }
        if (c.lindexCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lindexCommand(), request, receive, startArgIdx);
        }
        if (c.linsertCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.linsertCommand(), request, receive, startArgIdx);
        }
        if (c.llenCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.llenCommand(), request, receive, startArgIdx);
        }
        if (c.lposCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lposCommand(), request, receive, startArgIdx);
        }
        if (c.lpushCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lpushCommand(), request, receive, startArgIdx);
        }
        if (c.lpushxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lpushxCommand(), request, receive, startArgIdx);
        }
        if (c.rpushCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.rpushCommand(), request, receive, startArgIdx);
        }
        if (c.rpushxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.rpushxCommand(), request, receive, startArgIdx);
        }
        if (c.lrangeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lrangeCommand(), request, receive, startArgIdx);
        }
        if (c.lremCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lremCommand(), request, receive, startArgIdx);
        }
        if (c.lsetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.lsetCommand(), request, receive, startArgIdx);
        }
        if (c.ltrimCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.ltrimCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown List Command.");
    }

    public static Future<?> execSetCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.saddCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.saddCommand(), request, receive, startArgIdx);
        }
        if (c.scardCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.scardCommand(), request, receive, startArgIdx);
        }
        if (c.sdiffCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sdiffCommand(), request, receive, startArgIdx);
        }
        if (c.sdiffstoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sdiffstoreCommand(), request, receive, startArgIdx);
        }
        if (c.sinterCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sinterCommand(), request, receive, startArgIdx);
        }
        if (c.sintercardCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sintercardCommand(), request, receive, startArgIdx);
        }
        if (c.sinterstoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sinterstoreCommand(), request, receive, startArgIdx);
        }
        if (c.sismemberCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sismemberCommand(), request, receive, startArgIdx);
        }
        if (c.smismemberCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.smismemberCommand(), request, receive, startArgIdx);
        }
        if (c.smembersCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.smembersCommand(), request, receive, startArgIdx);
        }
        if (c.smoveCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.smoveCommand(), request, receive, startArgIdx);
        }
        if (c.spopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.spopCommand(), request, receive, startArgIdx);
        }
        if (c.srandmemberCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.srandmemberCommand(), request, receive, startArgIdx);
        }
        if (c.sremCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sremCommand(), request, receive, startArgIdx);
        }
        if (c.sscanComman() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sscanComman(), request, receive, startArgIdx);
        }
        if (c.sunionCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sunionCommand(), request, receive, startArgIdx);
        }
        if (c.sunionstoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.sunionstoreCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown Set Command.");
    }

    public static Future<?> execSortedSetCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SortedSetCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.zmpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zmpopCommand(), request, receive, startArgIdx, conn);
        }
        if (c.bzmpopCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.bzmpopCommand(), request, receive, startArgIdx);
        }
        if (c.zpopmaxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zpopmaxCommand(), request, receive, startArgIdx);
        }
        if (c.bzpopmaxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.bzpopmaxCommand(), request, receive, startArgIdx);
        }
        if (c.zpopminCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zpopminCommand(), request, receive, startArgIdx);
        }
        if (c.bzpopminCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.bzpopminCommand(), request, receive, startArgIdx);
        }
        if (c.zaddCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zaddCommand(), request, receive, startArgIdx);
        }
        if (c.zcardCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zcardCommand(), request, receive, startArgIdx);
        }
        if (c.zcountCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zcountCommand(), request, receive, startArgIdx);
        }
        if (c.zdiffCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zdiffCommand(), request, receive, startArgIdx);
        }
        if (c.zdiffstoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zdiffstoreCommand(), request, receive, startArgIdx);
        }
        if (c.zincrbyCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zincrbyCommand(), request, receive, startArgIdx);
        }
        if (c.zinterCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zinterCommand(), request, receive, startArgIdx);
        }
        if (c.zintercardCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zintercardCommand(), request, receive, startArgIdx);
        }
        if (c.zinterstoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zinterstoreCommand(), request, receive, startArgIdx);
        }
        if (c.zlexcountCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zlexcountCommand(), request, receive, startArgIdx);
        }
        if (c.zscoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zmscoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zmscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrandmemberCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrandmemberCommand(), request, receive, startArgIdx);
        }
        if (c.zrangeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrangeCommand(), request, receive, startArgIdx);
        }
        if (c.zrangebylexCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrangebylexCommand(), request, receive, startArgIdx);
        }
        if (c.zrangebyscoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrangebyscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrangestoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrangestoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrankCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrankCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrankCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrevrankCommand(), request, receive, startArgIdx);
        }
        if (c.zremCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zremCommand(), request, receive, startArgIdx);
        }
        if (c.zremrangebylexCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zremrangebylexCommand(), request, receive, startArgIdx);
        }
        if (c.zremrangebyrankCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zremrangebyrankCommand(), request, receive, startArgIdx);
        }
        if (c.zremrangebyscoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zremrangebyscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrangeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrevrangeCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrangebylexCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrevrangebylexCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrangebyscoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zrevrangebyscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zscanCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zscanCommand(), request, receive, startArgIdx);
        }
        if (c.zunionCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zunionCommand(), request, receive, startArgIdx);
        }
        if (c.zunionstoreCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.zunionstoreCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown SortedSet Command.");
    }

    public static Future<?> execHashCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HashCommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, Connection conn) throws SQLException {
        if (c.hdelCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hdelCommand(), request, receive, startArgIdx);
        }
        if (c.hexistsCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hexistsCommand(), request, receive, startArgIdx);
        }
        if (c.hexpireCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hexpireCommand(), request, receive, startArgIdx);
        }
        if (c.hpexpireCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hpexpireCommand(), request, receive, startArgIdx);
        }
        if (c.hexpireAtCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hexpireAtCommand(), request, receive, startArgIdx);
        }
        if (c.hpexpireAtCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hpexpireAtCommand(), request, receive, startArgIdx);
        }
        if (c.hexpireTimeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hexpireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.hpexpireTimeCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hpexpireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.hgetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hgetCommand(), request, receive, startArgIdx);
        }
        if (c.hgetAllCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hgetAllCommand(), request, receive, startArgIdx);
        }
        if (c.hincrByCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hincrByCommand(), request, receive, startArgIdx);
        }
        if (c.hkeysCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hkeysCommand(), request, receive, startArgIdx);
        }
        if (c.hlenCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hlenCommand(), request, receive, startArgIdx);
        }
        if (c.hmgetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hmgetCommand(), request, receive, startArgIdx);
        }
        if (c.hsetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hsetCommand(), request, receive, startArgIdx);
        }
        if (c.hmsetCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hmsetCommand(), request, receive, startArgIdx);
        }
        if (c.hsetnxCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hsetnxCommand(), request, receive, startArgIdx);
        }
        if (c.hpersistCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hpersistCommand(), request, receive, startArgIdx);
        }
        if (c.httlCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.httlCommand(), request, receive, startArgIdx);
        }
        if (c.hpttlCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hpttlCommand(), request, receive, startArgIdx);
        }
        if (c.hrandfieldCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hrandfieldCommand(), request, receive, startArgIdx);
        }
        if (c.hscanCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hscanCommand(), request, receive, startArgIdx);
        }
        if (c.hstrlenCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hstrlenCommand(), request, receive, startArgIdx);
        }
        if (c.hvalsCommand() != null) {
            return JedisExecuteUtils.execCmd(sync, jedisCmd, c.hvalsCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown Hash Command.");
    }
}
