package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.HintCommandContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoDistributeCall {
    public static Future<?> execMongoCmd(Future<Object> sync, MongoCmd mongoCmd, HintCommandContext h, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        if (h.command().USE() != null) {
            return MongoCommandsForDB.execUseCmd(sync, mongoCmd, h, request, receive, startArgIdx);
        }
        if (h.command().SHOW() != null) {
            return execShowOp(sync, mongoCmd, request, receive, startArgIdx, h);
        }
        if (h.command().dbOp() != null) {
            return execDbOp(sync, mongoCmd, request, receive, startArgIdx, h, conn);
        }
        if (h.command().mongoOp() != null) {
            return execMongoOp(sync, mongoCmd, request, receive, startArgIdx, h, conn);
        }
        throw new SQLException("unknown command.");
    }

    private static Future<?> execShowOp(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, HintCommandContext h) throws SQLException {
        String target = h.command().showTarget().getText();
        if (StringUtils.equalsIgnoreCase(target, "dbs") || StringUtils.equalsIgnoreCase(target, "databases")) {
            return MongoCommandsForDB.execShowDbs(sync, mongoCmd, h, request, receive, startArgIdx);
        }
        if (StringUtils.equalsIgnoreCase(target, "collections") || StringUtils.equalsIgnoreCase(target, "tables")) {
            return MongoCommandsForCollection.execShowCollections(sync, mongoCmd, h, request, receive, startArgIdx);
        }
        if (StringUtils.equalsIgnoreCase(target, "users")) {
            return MongoCommandsForUser.execShowUsers(sync, mongoCmd, h, request, receive, startArgIdx);
        }
        if (StringUtils.equalsIgnoreCase(target, "roles")) {
            return MongoCommandsForUser.execShowRoles(sync, mongoCmd, h, request, receive, startArgIdx);
        }
        if (StringUtils.equalsIgnoreCase(target, "profile")) {
            return MongoCommandsForOther.execShowProfile(sync, mongoCmd, h, request, receive, startArgIdx);
        }
        throw new SQLException("unknown show " + target + " command.");
    }

    private static Future<?> execDbOp(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, HintCommandContext h, MongoConn conn) throws SQLException {
        MongoParser.DatabaseNameContext dbName = h.command().databaseName();
        MongoParser.DbOpContext dbOp = h.command().dbOp();
        if (dbOp.createCollectionOp() != null) {
            return MongoCommandsForCollection.execCreateCollection(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.createCollectionOp());
        }
        if (dbOp.getCollectionNamesOp() != null) {
            return MongoCommandsForCollection.execGetCollectionNames(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.getCollectionNamesOp());
        }
        if (dbOp.getCollectionInfosOp() != null) {
            return MongoCommandsForCollection.execGetCollectionInfos(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.getCollectionInfosOp());
        }
        if (dbOp.createViewOp() != null) {
            return MongoCommandsForCollection.execCreateView(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.createViewOp());
        }
        if (dbOp.runCommandOp() != null) {
            return MongoCommandsForOther.execRunCommand(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.runCommandOp());
        }
        if (dbOp.dropDatabaseOp() != null) {
            return MongoCommandsForDB.execDropDatabase(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.dropDatabaseOp());
        }
        if (dbOp.createUserOp() != null) {
            return MongoCommandsForUser.execCreateUser(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.createUserOp());
        }
        if (dbOp.dropUserOp() != null) {
            return MongoCommandsForUser.execDropUser(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.dropUserOp());
        }
        if (dbOp.updateUserOp() != null) {
            return MongoCommandsForUser.execUpdateUser(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.updateUserOp());
        }
        if (dbOp.grantRolesToUserOp() != null) {
            return MongoCommandsForUser.execGrantRolesToUser(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.grantRolesToUserOp());
        }
        if (dbOp.revokeRolesFromUserOp() != null) {
            return MongoCommandsForUser.execRevokeRolesFromUser(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.revokeRolesFromUserOp());
        }
        if (dbOp.changeUserPasswordOp() != null) {
            return MongoCommandsForUser.execChangeUserPassword(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.changeUserPasswordOp());
        }
        if (dbOp.serverStatusOp() != null) {
            return MongoCommandsForOther.execServerStatus(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.serverStatusOp());
        }
        if (dbOp.versionOp() != null) {
            return MongoCommandsForOther.execVersion(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.versionOp());
        }
        if (dbOp.statsOp() != null) {
            return MongoCommandsForOther.execStats(sync, mongoCmd, request, receive, startArgIdx, h, dbName, dbOp.statsOp());
        }
        throw new SQLException("unknown command.");
    }

    private static Future<?> execMongoOp(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx, HintCommandContext h, MongoConn conn) throws SQLException {
        MongoParser.CommandContext cc = h.command();
        MongoParser.DatabaseNameContext dbName = cc.databaseName();
        MongoParser.CollectionContext collName = cc.collection();
        MongoParser.MongoOpContext c = cc.mongoOp();
        if (c.createIndexOp() != null) {
            return MongoCommandsForIndex.execCreateIndex(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.createIndexOp());
        }
        if (c.dropIndexOp() != null) {
            return MongoCommandsForIndex.execDropIndex(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.dropIndexOp());
        }
        if (c.getIndexesOp() != null) {
            return MongoCommandsForIndex.execGetIndexes(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.getIndexesOp());
        }
        if (c.insertOp() != null) {
            return MongoCommandsForCollection.execInsert(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.insertOp());
        }
        if (c.removeOp() != null) {
            return MongoCommandsForCollection.execRemove(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.removeOp());
        }
        if (c.updateOp() != null) {
            return MongoCommandsForCollection.execUpdate(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.updateOp());
        }
        if (c.bulkWriteOp() != null) {
            return MongoCommandsForCollection.execBulkWrite(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.bulkWriteOp());
        }
        if (c.findOp() != null) {
            return MongoCommandsForCollection.execFind(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.findOp(), conn);
        }
        if (c.countOp() != null) {
            return MongoCommandsForCollection.execCount(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.countOp());
        }
        if (c.distinctOp() != null) {
            return MongoCommandsForCollection.execDistinct(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.distinctOp());
        }
        if (c.aggregateOp() != null) {
            return MongoCommandsForCollection.execAggregate(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.aggregateOp());
        }
        if (c.dropOp() != null) {
            return MongoCommandsForCollection.execDrop(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.dropOp());
        }
        if (c.findOneOp() != null) {
            return MongoCommandsForCollection.execFindOne(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.findOneOp(), conn);
        }
        if (c.insertOneOp() != null) {
            return MongoCommandsForCollection.execInsertOne(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.insertOneOp());
        }
        if (c.insertManyOp() != null) {
            return MongoCommandsForCollection.execInsertMany(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.insertManyOp());
        }
        if (c.deleteOneOp() != null) {
            return MongoCommandsForCollection.execDeleteOne(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.deleteOneOp());
        }
        if (c.deleteManyOp() != null) {
            return MongoCommandsForCollection.execDeleteMany(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.deleteManyOp());
        }
        if (c.updateOneOp() != null) {
            return MongoCommandsForCollection.execUpdateOne(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.updateOneOp());
        }
        if (c.updateManyOp() != null) {
            return MongoCommandsForCollection.execUpdateMany(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.updateManyOp());
        }
        if (c.replaceOneOp() != null) {
            return MongoCommandsForCollection.execReplaceOne(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.replaceOneOp());
        }
        if (c.renameCollectionOp() != null) {
            return MongoCommandsForCollection.execRenameCollection(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.renameCollectionOp());
        }
        if (c.statsOp() != null) {
            return MongoCommandsForCollection.execStats(sync, mongoCmd, request, receive, startArgIdx, h, dbName, collName, c.statsOp());
        }
        throw new SQLException("unknown command.");
    }
}
