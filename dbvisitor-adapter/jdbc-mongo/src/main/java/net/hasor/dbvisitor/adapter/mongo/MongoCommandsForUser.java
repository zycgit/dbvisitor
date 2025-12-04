package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForUser {
    public static Future<?> execCreateUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateUserOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execDropUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, DropUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execUpdateUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, UpdateUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGrantRolesToUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, GrantRolesToUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execRevokeRolesFromUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, RevokeRolesFromUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execChangeUserPassword(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, ChangeUserPasswordOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowUsers(Future<Object> sync, MongoCmd mongoCmd, CommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowRoles(Future<Object> sync, MongoCmd mongoCmd, CommandContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
