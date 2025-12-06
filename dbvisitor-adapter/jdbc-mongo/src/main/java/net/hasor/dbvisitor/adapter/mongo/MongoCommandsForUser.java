package net.hasor.dbvisitor.adapter.mongo;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.client.MongoDatabase;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoBsonVisitor;
import net.hasor.dbvisitor.adapter.mongo.parser.MongoParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterResultCursor;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.bson.Document;

@SuppressWarnings("unchecked")
class MongoCommandsForUser extends MongoCommands {
    public static Future<?> execShowUsers(Future<Object> sync, MongoCmd mongoCmd, CommandContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        String dbName = mongoCmd.getCatalog();
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        Document command = new Document("usersInfo", 1);
        Document result = mongoDB.runCommand(command);

        List<JdbcColumn> columns = Arrays.asList(COL_USER_STRING, COL_DB_STRING, COL_ROLES_STRING, COL_JSON_);
        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
        long maxRows = request.getMaxRows();
        int affectRows = 0;

        List<Document> users = (List<Document>) result.get("users");
        if (users != null) {
            for (Document user : users) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(COL_USER_STRING.name, user.getString("user"));
                row.put(COL_DB_STRING.name, user.getString("db"));
                row.put(COL_ROLES_STRING.name, user.get("roles").toString());
                row.put(COL_JSON_.name, user.toJson());
                cursor.pushData(row);

                affectRows++;
                if (maxRows > 0 && affectRows >= maxRows) {
                    break;
                }
            }
        }
        cursor.pushFinish();
        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execCreateUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, CreateUserOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());
        String username = (String) args.get(0);
        String password = (String) args.get(1);
        List<?> roles = (List<?>) args.get(2);
        Map<String, Object> options = args.size() > 3 ? (Map<String, Object>) args.get(3) : null;

        Document command = new Document("createUser", username);
        command.put("pwd", password);
        command.put("roles", roles);
        if (options != null) {
            if (options.containsKey("authenticationRestrictions")) {
                getOptionList(options, "authenticationRestrictions");
            }
            if (options.containsKey("mechanisms")) {
                getOptionList(options, "mechanisms");
            }
            if (options.containsKey("passwordDigestor")) {
                getOptionString(options, "passwordDigestor");
            }
            command.putAll(options);
        }

        mongoDB.runCommand(command);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execDropUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, DropUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String username = (String) args.get(0);
        Document command = new Document("dropUser", username);

        mongoDB.runCommand(command);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execUpdateUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, UpdateUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String username = (String) args.get(0);
        Map<String, Object> update = (Map<String, Object>) args.get(1);

        Document command = new Document("updateUser", username);
        if (update.containsKey("pwd")) {
            getOptionString(update, "pwd");
        }
        if (update.containsKey("roles")) {
            getOptionList(update, "roles");
        }
        if (update.containsKey("authenticationRestrictions")) {
            getOptionList(update, "authenticationRestrictions");
        }
        if (update.containsKey("mechanisms")) {
            getOptionList(update, "mechanisms");
        }
        if (update.containsKey("passwordDigestor")) {
            getOptionString(update, "passwordDigestor");
        }
        command.putAll(update);

        mongoDB.runCommand(command);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execChangeUserPassword(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, ChangeUserPasswordOpContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());
        String username = (String) args.get(0);
        String password = (String) args.get(1);

        Document command = new Document("updateUser", username);
        command.put("pwd", password);

        mongoDB.runCommand(command);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    //

    public static Future<?> execShowRoles(Future<Object> sync, MongoCmd mongoCmd, CommandContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        String dbName = mongoCmd.getCatalog();
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);
        Document command = new Document("rolesInfo", 1);
        Document result = mongoDB.runCommand(command);

        List<JdbcColumn> columns = Arrays.asList(COL_ROLE_STRING, COL_DB_STRING, COL_IS_BUILTIN_BOOLEAN, COL_INHERITED_ROLES_STRING, COL_JSON_);
        AdapterResultCursor cursor = new AdapterResultCursor(request, columns);
        long maxRows = request.getMaxRows();
        int affectRows = 0;

        List<Document> roles = (List<Document>) result.get("roles");
        if (roles != null) {
            for (Document role : roles) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put(COL_ROLE_STRING.name, role.getString("role"));
                row.put(COL_DB_STRING.name, role.getString("db"));
                row.put(COL_IS_BUILTIN_BOOLEAN.name, role.getBoolean("isBuiltin"));
                row.put(COL_INHERITED_ROLES_STRING.name, role.get("roles").toString());
                row.put(COL_JSON_.name, role.toJson());
                cursor.pushData(row);

                affectRows++;
                if (maxRows > 0 && affectRows >= maxRows) {
                    break;
                }
            }
        }
        cursor.pushFinish();
        receive.responseResult(request, cursor);
        return completed(sync);
    }

    public static Future<?> execGrantRolesToUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, GrantRolesToUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String username = (String) args.get(0);
        List<?> roles = (List<?>) args.get(1);

        Document command = new Document("grantRolesToUser", username);
        command.put("roles", roles);

        mongoDB.runCommand(command);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }

    public static Future<?> execRevokeRolesFromUser(Future<Object> sync, MongoCmd mongoCmd, DatabaseNameContext database, RevokeRolesFromUserOpContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx, MongoConn conn) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        String dbName = argAsDbName(argIndex, request, database, mongoCmd);
        MongoDatabase mongoDB = mongoCmd.getClient().getDatabase(dbName);

        MongoBsonVisitor visitor = new MongoBsonVisitor(request, argIndex);
        List<Object> args = (List<Object>) visitor.visit(c.arguments());

        String username = (String) args.get(0);
        List<?> roles = (List<?>) args.get(1);

        Document command = new Document("revokeRolesFromUser", username);
        command.put("roles", roles);

        mongoDB.runCommand(command);
        receive.responseUpdateCount(request, 0);
        return completed(sync);
    }
}
