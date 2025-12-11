package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DatabaseNameContext;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.DropDatabaseOpContext;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.HintCommandContext;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForDB extends MongoCommands {
    public static Future<?> execUseCmd(Future<Object> sync, MongoCmd mongoCmd, HintCommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hint = readHints(argIndex, request, c.hint());
        String dbName = argAsDbName(argIndex, request, c.command().databaseName(), mongoCmd);

        String catalog = mongoCmd.getCatalog();
        if (!StringUtils.equals(catalog, dbName)) {
            mongoCmd.setCatalog(dbName);
        }
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execShowDbs(Future<Object> sync, MongoCmd mongoCmd, HintCommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        ArrayList<String> listResult = new ArrayList<>();
        for (String name : mongoCmd.getClient().listDatabaseNames()) {
            listResult.add(name);
        }

        receive.responseResult(request, listResult(request, COL_DATABASE_STRING, listResult));
        return completed(sync);
    }

    public static Future<?> execDropDatabase(Future<Object> sync, MongoCmd mongoCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx,//
            HintCommandContext h, DatabaseNameContext database, DropDatabaseOpContext c) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        Map<String, Object> hint = readHints(argIndex, request, h.hint());
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);

        boolean exists = false;
        for (String name : mongoCmd.getClient().listDatabaseNames()) {
            if (StringUtils.equals(name, dbName)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            throw new SQLException("database not exists.");
        } else {
            mongoCmd.getClient().getDatabase(dbName).drop();
        }

        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }
}
