package net.hasor.dbvisitor.adapter.redis;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.redis.parser.RedisParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class JedisDistributeCall {
    public static Future<?> execRedisCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.serverCommands() != null) {
            return execServerCmd(sync, jedisCmd, c.serverCommands(), request, receive, startArgIdx, conn);
        }
        if (c.keysCommands() != null) {
            return execKeysCmd(sync, jedisCmd, c.keysCommands(), request, receive, startArgIdx, conn);
        }
        if (c.stringCommands() != null) {
            return execStringCmd(sync, jedisCmd, c.stringCommands(), request, receive, startArgIdx, conn);
        }
        if (c.listCommands() != null) {
            return execListCmd(sync, jedisCmd, c.listCommands(), request, receive, startArgIdx, conn);
        }
        if (c.setCommands() != null) {
            return execSetCmd(sync, jedisCmd, c.setCommands(), request, receive, startArgIdx, conn);
        }
        if (c.sortedSetCommands() != null) {
            return execSortedSetCmd(sync, jedisCmd, c.sortedSetCommands(), request, receive, startArgIdx, conn);
        }
        if (c.hashCommands() != null) {
            return execHashCmd(sync, jedisCmd, c.hashCommands(), request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown command.");
    }

    private static Future<?> execServerCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ServerCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.moveCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.moveCommand(), request, receive, startArgIdx);
        }
        if (c.waitCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.waitCommand(), request, receive, startArgIdx);
        }
        if (c.waitaofCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.waitaofCommand(), request, receive, startArgIdx);
        }
        if (c.pingCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.pingCommand(), request, receive, startArgIdx);
        }
        if (c.echoCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.echoCommand(), request, receive, startArgIdx);
        }
        if (c.selectCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.selectCommand(), request, receive, startArgIdx, conn);
        }
        if (c.infoCommand() != null) {
            return JedisCommandsForServer.execCmd(sync, jedisCmd, c.infoCommand(), request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown Common Command.");
    }

    private static Future<?> execKeysCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.KeysCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.copyCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.copyCommand(), request, receive, startArgIdx);
        }
        if (c.deleteCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.deleteCommand(), request, receive, startArgIdx);
        }
        if (c.unlinkCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.unlinkCommand(), request, receive, startArgIdx);
        }
        if (c.dumpCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.dumpCommand(), request, receive, startArgIdx);
        }
        if (c.existsCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.existsCommand(), request, receive, startArgIdx);
        }
        if (c.expireCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.expireCommand(), request, receive, startArgIdx);
        }
        if (c.expireAtCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.expireAtCommand(), request, receive, startArgIdx);
        }
        if (c.expireTimeCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.expireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.pExpireCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.pExpireCommand(), request, receive, startArgIdx);
        }
        if (c.pExpireAtCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.pExpireAtCommand(), request, receive, startArgIdx);
        }
        if (c.pExpireTimeCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.pExpireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.keysCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.keysCommand(), request, receive, startArgIdx);
        }
        if (c.objectCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.objectCommand(), request, receive, startArgIdx);
        }
        if (c.persistCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.persistCommand(), request, receive, startArgIdx);
        }
        if (c.ttlCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.ttlCommand(), request, receive, startArgIdx);
        }
        if (c.pTtlCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.pTtlCommand(), request, receive, startArgIdx);
        }
        if (c.randomKeyCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.randomKeyCommand(), request, receive, startArgIdx);
        }
        if (c.renameCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.renameCommand(), request, receive, startArgIdx);
        }
        if (c.renameNxCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.renameNxCommand(), request, receive, startArgIdx);
        }
        if (c.scanCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.scanCommand(), request, receive, startArgIdx);
        }
        if (c.touchCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.touchCommand(), request, receive, startArgIdx);
        }
        if (c.typeCommand() != null) {
            return JedisCommandsForKeys.execCmd(sync, jedisCmd, c.typeCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown Common Command.");
    }

    private static Future<?> execStringCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.StringCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.strSetCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.strSetCommand(), request, receive, startArgIdx);
        }
        if (c.getCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.getCommand(), request, receive, startArgIdx);
        }
        if (c.incrementCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.incrementCommand(), request, receive, startArgIdx);
        }
        if (c.incrementByCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.incrementByCommand(), request, receive, startArgIdx);
        }
        if (c.decrementCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.decrementCommand(), request, receive, startArgIdx);
        }
        if (c.decrementByCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.decrementByCommand(), request, receive, startArgIdx);
        }
        if (c.appendCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.appendCommand(), request, receive, startArgIdx);
        }
        if (c.getDeleteCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.getDeleteCommand(), request, receive, startArgIdx);
        }
        if (c.getExCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.getExCommand(), request, receive, startArgIdx);
        }
        if (c.getRangeCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.getRangeCommand(), request, receive, startArgIdx);
        }
        if (c.getSetCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.getSetCommand(), request, receive, startArgIdx);
        }
        if (c.mGetCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.mGetCommand(), request, receive, startArgIdx);
        }
        if (c.mSetCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.mSetCommand(), request, receive, startArgIdx);
        }
        if (c.mSetNxCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.mSetNxCommand(), request, receive, startArgIdx);
        }
        if (c.pSetExCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.pSetExCommand(), request, receive, startArgIdx);
        }
        if (c.setExCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.setExCommand(), request, receive, startArgIdx);
        }
        if (c.setNxCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.setNxCommand(), request, receive, startArgIdx);
        }
        if (c.setRangeCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.setRangeCommand(), request, receive, startArgIdx);
        }
        if (c.stringLengthCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.stringLengthCommand(), request, receive, startArgIdx);
        }
        if (c.substringCommand() != null) {
            return JedisCommandsForString.execCmd(sync, jedisCmd, c.substringCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown String Command.");
    }

    private static Future<?> execListCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.ListCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.lmoveCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lmoveCommand(), request, receive, startArgIdx);
        }
        if (c.blmoveCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.blmoveCommand(), request, receive, startArgIdx);
        }
        if (c.lmpopCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lmpopCommand(), request, receive, startArgIdx, conn.getOwner());
        }
        if (c.blmpopCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.blmpopCommand(), request, receive, startArgIdx, conn.getOwner());
        }
        if (c.lpopCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lpopCommand(), request, receive, startArgIdx);
        }
        if (c.blpopCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.blpopCommand(), request, receive, startArgIdx);
        }
        if (c.rpopCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.rpopCommand(), request, receive, startArgIdx);
        }
        if (c.brpopCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.brpopCommand(), request, receive, startArgIdx);
        }
        if (c.rpopLpushCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.rpopLpushCommand(), request, receive, startArgIdx);
        }
        if (c.brpopLpushCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.brpopLpushCommand(), request, receive, startArgIdx);
        }
        if (c.lindexCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lindexCommand(), request, receive, startArgIdx);
        }
        if (c.linsertCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.linsertCommand(), request, receive, startArgIdx);
        }
        if (c.llenCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.llenCommand(), request, receive, startArgIdx);
        }
        if (c.lposCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lposCommand(), request, receive, startArgIdx);
        }
        if (c.lpushCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lpushCommand(), request, receive, startArgIdx);
        }
        if (c.lpushxCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lpushxCommand(), request, receive, startArgIdx);
        }
        if (c.rpushCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.rpushCommand(), request, receive, startArgIdx);
        }
        if (c.rpushxCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.rpushxCommand(), request, receive, startArgIdx);
        }
        if (c.lrangeCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lrangeCommand(), request, receive, startArgIdx);
        }
        if (c.lremCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lremCommand(), request, receive, startArgIdx);
        }
        if (c.lsetCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.lsetCommand(), request, receive, startArgIdx);
        }
        if (c.ltrimCommand() != null) {
            return JedisCommandsForList.execCmd(sync, jedisCmd, c.ltrimCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown List Command.");
    }

    private static Future<?> execSetCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SetCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.saddCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.saddCommand(), request, receive, startArgIdx);
        }
        if (c.scardCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.scardCommand(), request, receive, startArgIdx);
        }
        if (c.sdiffCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sdiffCommand(), request, receive, startArgIdx);
        }
        if (c.sdiffstoreCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sdiffstoreCommand(), request, receive, startArgIdx);
        }
        if (c.sinterCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sinterCommand(), request, receive, startArgIdx);
        }
        if (c.sintercardCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sintercardCommand(), request, receive, startArgIdx);
        }
        if (c.sinterstoreCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sinterstoreCommand(), request, receive, startArgIdx);
        }
        if (c.sismemberCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sismemberCommand(), request, receive, startArgIdx);
        }
        if (c.smismemberCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.smismemberCommand(), request, receive, startArgIdx);
        }
        if (c.smembersCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.smembersCommand(), request, receive, startArgIdx);
        }
        if (c.smoveCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.smoveCommand(), request, receive, startArgIdx);
        }
        if (c.spopCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.spopCommand(), request, receive, startArgIdx);
        }
        if (c.srandmemberCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.srandmemberCommand(), request, receive, startArgIdx);
        }
        if (c.sremCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sremCommand(), request, receive, startArgIdx);
        }
        if (c.sscanComman() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sscanComman(), request, receive, startArgIdx);
        }
        if (c.sunionCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sunionCommand(), request, receive, startArgIdx);
        }
        if (c.sunionstoreCommand() != null) {
            return JedisCommandsForSet.execCmd(sync, jedisCmd, c.sunionstoreCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown Set Command.");
    }

    private static Future<?> execSortedSetCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.SortedSetCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.zmpopCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zmpopCommand(), request, receive, startArgIdx, conn.getOwner());
        }
        if (c.bzmpopCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.bzmpopCommand(), request, receive, startArgIdx);
        }
        if (c.zpopmaxCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zpopmaxCommand(), request, receive, startArgIdx);
        }
        if (c.bzpopmaxCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.bzpopmaxCommand(), request, receive, startArgIdx);
        }
        if (c.zpopminCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zpopminCommand(), request, receive, startArgIdx);
        }
        if (c.bzpopminCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.bzpopminCommand(), request, receive, startArgIdx);
        }
        if (c.zaddCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zaddCommand(), request, receive, startArgIdx);
        }
        if (c.zcardCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zcardCommand(), request, receive, startArgIdx);
        }
        if (c.zcountCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zcountCommand(), request, receive, startArgIdx);
        }
        if (c.zdiffCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zdiffCommand(), request, receive, startArgIdx);
        }
        if (c.zdiffstoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zdiffstoreCommand(), request, receive, startArgIdx);
        }
        if (c.zincrbyCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zincrbyCommand(), request, receive, startArgIdx);
        }
        if (c.zinterCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zinterCommand(), request, receive, startArgIdx);
        }
        if (c.zintercardCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zintercardCommand(), request, receive, startArgIdx);
        }
        if (c.zinterstoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zinterstoreCommand(), request, receive, startArgIdx);
        }
        if (c.zlexcountCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zlexcountCommand(), request, receive, startArgIdx);
        }
        if (c.zscoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zmscoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zmscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrandmemberCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrandmemberCommand(), request, receive, startArgIdx);
        }
        if (c.zrangeCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrangeCommand(), request, receive, startArgIdx);
        }
        if (c.zrangebylexCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrangebylexCommand(), request, receive, startArgIdx);
        }
        if (c.zrangebyscoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrangebyscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrangestoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrangestoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrankCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrankCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrankCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrevrankCommand(), request, receive, startArgIdx);
        }
        if (c.zremCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zremCommand(), request, receive, startArgIdx);
        }
        if (c.zremrangebylexCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zremrangebylexCommand(), request, receive, startArgIdx);
        }
        if (c.zremrangebyrankCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zremrangebyrankCommand(), request, receive, startArgIdx);
        }
        if (c.zremrangebyscoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zremrangebyscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrangeCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrevrangeCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrangebylexCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrevrangebylexCommand(), request, receive, startArgIdx);
        }
        if (c.zrevrangebyscoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zrevrangebyscoreCommand(), request, receive, startArgIdx);
        }
        if (c.zscanCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zscanCommand(), request, receive, startArgIdx);
        }
        if (c.zunionCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zunionCommand(), request, receive, startArgIdx);
        }
        if (c.zunionstoreCommand() != null) {
            return JedisCommandsForStoreSet.execCmd(sync, jedisCmd, c.zunionstoreCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown SortedSet Command.");
    }

    private static Future<?> execHashCmd(Future<Object> sync, JedisCmd jedisCmd, RedisParser.HashCommandsContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, JedisConn conn) throws SQLException {
        if (c.hdelCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hdelCommand(), request, receive, startArgIdx);
        }
        if (c.hexistsCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hexistsCommand(), request, receive, startArgIdx);
        }
        if (c.hexpireCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hexpireCommand(), request, receive, startArgIdx);
        }
        if (c.hpexpireCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hpexpireCommand(), request, receive, startArgIdx);
        }
        if (c.hexpireAtCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hexpireAtCommand(), request, receive, startArgIdx);
        }
        if (c.hpexpireAtCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hpexpireAtCommand(), request, receive, startArgIdx);
        }
        if (c.hexpireTimeCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hexpireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.hpexpireTimeCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hpexpireTimeCommand(), request, receive, startArgIdx);
        }
        if (c.hgetCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hgetCommand(), request, receive, startArgIdx);
        }
        if (c.hgetAllCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hgetAllCommand(), request, receive, startArgIdx);
        }
        if (c.hincrByCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hincrByCommand(), request, receive, startArgIdx);
        }
        if (c.hkeysCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hkeysCommand(), request, receive, startArgIdx);
        }
        if (c.hlenCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hlenCommand(), request, receive, startArgIdx);
        }
        if (c.hmgetCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hmgetCommand(), request, receive, startArgIdx);
        }
        if (c.hsetCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hsetCommand(), request, receive, startArgIdx);
        }
        if (c.hmsetCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hmsetCommand(), request, receive, startArgIdx);
        }
        if (c.hsetnxCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hsetnxCommand(), request, receive, startArgIdx);
        }
        if (c.hpersistCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hpersistCommand(), request, receive, startArgIdx);
        }
        if (c.httlCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.httlCommand(), request, receive, startArgIdx);
        }
        if (c.hpttlCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hpttlCommand(), request, receive, startArgIdx);
        }
        if (c.hrandfieldCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hrandfieldCommand(), request, receive, startArgIdx);
        }
        if (c.hscanCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hscanCommand(), request, receive, startArgIdx);
        }
        if (c.hstrlenCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hstrlenCommand(), request, receive, startArgIdx);
        }
        if (c.hvalsCommand() != null) {
            return JedisCommandsForHash.execCmd(sync, jedisCmd, c.hvalsCommand(), request, receive, startArgIdx);
        }
        throw new SQLException("unknown Hash Command.");
    }
}
