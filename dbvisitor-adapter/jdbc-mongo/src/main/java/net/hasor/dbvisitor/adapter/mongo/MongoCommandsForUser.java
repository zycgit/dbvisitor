package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MongoCommandsForUser extends MongoCommands {
    public static Future<?> execCreateUser(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CreateUserOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execDropUser(Future<Object> sync, MongoCmd mongoCmd, MongoParser.DropUserOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execUpdateUser(Future<Object> sync, MongoCmd mongoCmd, MongoParser.UpdateUserOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execGrantRolesToUser(Future<Object> sync, MongoCmd mongoCmd, MongoParser.GrantRolesToUserOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execRevokeRolesFromUser(Future<Object> sync, MongoCmd mongoCmd, MongoParser.RevokeRolesFromUserOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execChangeUserPassword(Future<Object> sync, MongoCmd mongoCmd, MongoParser.ChangeUserPasswordOpContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowUsers(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }

    public static Future<?> execShowRoles(Future<Object> sync, MongoCmd mongoCmd, MongoParser.CommandContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        throw new SQLException("not implemented yet");
    }
}
