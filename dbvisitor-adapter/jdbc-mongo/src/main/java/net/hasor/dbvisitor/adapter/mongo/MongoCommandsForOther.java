package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForOther extends MongoCommands {
    public static Future<?> execRunCommand(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, RunCommandOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowProfile(Future<Object> sync, MongoCmd mongoCmd, CommandContext c, AdapterRequest request, //
            AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execServerStatus(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, ServerStatusOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execVersion(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, VersionOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execStats(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, StatsOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
