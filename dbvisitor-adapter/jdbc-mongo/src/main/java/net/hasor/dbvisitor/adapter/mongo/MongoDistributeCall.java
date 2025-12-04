package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.CommandContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoDistributeCall {
    public static Future<?> execMongoCmd(Future<Object> sync, MongoCmd mongoCmd, CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        if (c.USE() != null) {
            return MongoCommandsForOther.execCmd(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        if (c.SHOW() != null) {
            return execShowOp(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        if (c.dbOp() != null) {
            return execDbOp(sync, mongoCmd, c.databaseName(), c.dbOp(), request, receive, startArgIdx, conn);
        }
        if (c.mongoOp() != null) {
            return execMongoOp(sync, mongoCmd, c.databaseName(), c.collection(), c.mongoOp(), request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown command.");
    }

    private static Future<?> execShowOp(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        String target = c.showTarget().getText();
        if (StringUtils.equalsIgnoreCase(target, "dbs") || StringUtils.equalsIgnoreCase(target, "databases")) {
            return MongoCommandsForDB.execShowDbs(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        if (StringUtils.equalsIgnoreCase(target, "collections") || StringUtils.equalsIgnoreCase(target, "tables")) {
            return MongoCommandsForCollection.execShowCollections(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        if (StringUtils.equalsIgnoreCase(target, "users")) {
            return MongoCommandsForUser.execShowUsers(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        if (StringUtils.equalsIgnoreCase(target, "roles")) {
            return MongoCommandsForUser.execShowRoles(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        if (StringUtils.equalsIgnoreCase(target, "profile")) {
            return MongoCommandsForOther.execShowProfile(sync, mongoCmd, c, request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown show " + target + " command.");
    }

    private static Future<?> execDbOp(Future<Object> sync, MongoCmd mongoCmd, MongoParser.DatabaseNameContext database, MongoParser.DbOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        if (c.createCollectionOp() != null) {
            return MongoCommandsForCollection.execCreateCollection(sync, mongoCmd, database, c.createCollectionOp(), request, receive, startArgIdx, conn);
        }
        if (c.getCollectionNamesOp() != null) {
            return MongoCommandsForCollection.execGetCollectionNames(sync, mongoCmd, database, c.getCollectionNamesOp(), request, receive, startArgIdx, conn);
        }
        if (c.getCollectionInfosOp() != null) {
            return MongoCommandsForCollection.execGetCollectionInfos(sync, mongoCmd, database, c.getCollectionInfosOp(), request, receive, startArgIdx, conn);
        }
        if (c.createViewOp() != null) {
            return MongoCommandsForView.execCreateView(sync, mongoCmd, database, c.createViewOp(), request, receive, startArgIdx, conn);
        }
        if (c.runCommandOp() != null) {
            return MongoCommandsForOther.execRunCommand(sync, mongoCmd, database, c.runCommandOp(), request, receive, startArgIdx, conn);
        }
        if (c.dropDatabaseOp() != null) {
            return MongoCommandsForDB.execDropDatabase(sync, mongoCmd, database, c.dropDatabaseOp(), request, receive, startArgIdx, conn);
        }
        if (c.createUserOp() != null) {
            return MongoCommandsForUser.execCreateUser(sync, mongoCmd, database, c.createUserOp(), request, receive, startArgIdx, conn);
        }
        if (c.dropUserOp() != null) {
            return MongoCommandsForUser.execDropUser(sync, mongoCmd, database, c.dropUserOp(), request, receive, startArgIdx, conn);
        }
        if (c.updateUserOp() != null) {
            return MongoCommandsForUser.execUpdateUser(sync, mongoCmd, database, c.updateUserOp(), request, receive, startArgIdx, conn);
        }
        if (c.grantRolesToUserOp() != null) {
            return MongoCommandsForUser.execGrantRolesToUser(sync, mongoCmd, database, c.grantRolesToUserOp(), request, receive, startArgIdx, conn);
        }
        if (c.revokeRolesFromUserOp() != null) {
            return MongoCommandsForUser.execRevokeRolesFromUser(sync, mongoCmd, database, c.revokeRolesFromUserOp(), request, receive, startArgIdx, conn);
        }
        if (c.changeUserPasswordOp() != null) {
            return MongoCommandsForUser.execChangeUserPassword(sync, mongoCmd, database, c.changeUserPasswordOp(), request, receive, startArgIdx, conn);
        }
        if (c.serverStatusOp() != null) {
            return MongoCommandsForOther.execServerStatus(sync, mongoCmd, database, c.serverStatusOp(), request, receive, startArgIdx, conn);
        }
        if (c.versionOp() != null) {
            return MongoCommandsForOther.execVersion(sync, mongoCmd, database, c.versionOp(), request, receive, startArgIdx, conn);
        }
        if (c.statsOp() != null) {
            return MongoCommandsForOther.execStats(sync, mongoCmd, database, c.statsOp(), request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown command.");
    }

    private static Future<?> execMongoOp(Future<Object> sync, MongoCmd mongoCmd, MongoParser.DatabaseNameContext database, MongoParser.CollectionContext collection, MongoParser.MongoOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        if (c.createIndexOp() != null) {
            return MongoCommandsForIndex.execCreateIndex(sync, mongoCmd, database, collection, c.createIndexOp(), request, receive, startArgIdx, conn);
        }
        if (c.dropIndexOp() != null) {
            return MongoCommandsForIndex.execDropIndex(sync, mongoCmd, database, collection, c.dropIndexOp(), request, receive, startArgIdx, conn);
        }
        if (c.getIndexesOp() != null) {
            return MongoCommandsForIndex.execGetIndexes(sync, mongoCmd, database, collection, c.getIndexesOp(), request, receive, startArgIdx, conn);
        }
        if (c.insertOp() != null) {
            return MongoCommandsForCollection.execInsert(sync, mongoCmd, database, collection, c.insertOp(), request, receive, startArgIdx, conn);
        }
        if (c.removeOp() != null) {
            return MongoCommandsForCollection.execRemove(sync, mongoCmd, database, collection, c.removeOp(), request, receive, startArgIdx, conn);
        }
        if (c.updateOp() != null) {
            return MongoCommandsForCollection.execUpdate(sync, mongoCmd, database, collection, c.updateOp(), request, receive, startArgIdx, conn);
        }
        if (c.findOp() != null) {
            return MongoCommandsForCollection.execFind(sync, mongoCmd, database, collection, c.findOp(), request, receive, startArgIdx, conn);
        }
        if (c.countOp() != null) {
            return MongoCommandsForCollection.execCount(sync, mongoCmd, database, collection, c.countOp(), request, receive, startArgIdx, conn);
        }
        if (c.distinctOp() != null) {
            return MongoCommandsForCollection.execDistinct(sync, mongoCmd, database, collection, c.distinctOp(), request, receive, startArgIdx, conn);
        }
        if (c.aggregateOp() != null) {
            return MongoCommandsForCollection.execAggregate(sync, mongoCmd, database, collection, c.aggregateOp(), request, receive, startArgIdx, conn);
        }
        if (c.bulkWriteOp() != null) {
            return MongoCommandsForCollection.execBulkWrite(sync, mongoCmd, database, collection, c.bulkWriteOp(), request, receive, startArgIdx, conn);
        }
        throw new SQLException("unknown command.");
    }
}
