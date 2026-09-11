/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.Gson;
import io.milvus.grpc.ObjectType;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import org.antlr.v4.runtime.Token;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;

/***
 * User/Role commands for Milvus
 */

public final class MilvusCommandsForUser extends MilvusCommands {
    private MilvusCommandsForUser() {
    }

    private static final JdbcColumn COL_OBJECT_STRING      = new JdbcColumn("OBJECT", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_OBJECT_NAME_STRING = new JdbcColumn("OBJECT_NAME", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_PRIVILEGE_STRING   = new JdbcColumn("PRIVILEGE", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_ROLES_STRING       = new JdbcColumn("ROLES", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_DESCRIPTION_STRING = new JdbcColumn("DESCRIPTION", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn COL_GRANTS_STRING      = new JdbcColumn("GRANTS", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final Gson       JSON                   = new Gson();

    // Users and roles

    public static Future<?> execCreateUser(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String userName = readName(c.userName);
        String password = readPassword(c.password, argIndex, request);
        String description = readDescription(readProperties(argIndex, request, c.propertiesList()));
        CreateUserReq.CreateUserReqBuilder builder = CreateUserReq.builder().userName(userName).password(password);
        if (description != null) {
            builder.description(description);
        }

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && userExists(cmd, userName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        cmd.createUser(builder.build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execUpdatePassword(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        // Bind in SQL order: the new password precedes REPLACE's old password.
        String password = readPassword(c.password, args, request);
        String oldPassword = readPassword(c.oldPassword, args, request);
        UpdatePasswordReq.UpdatePasswordReqBuilder builder = UpdatePasswordReq.builder().userName(readName(c.userName)).newPassword(password).password(oldPassword);
        for (Map.Entry<String, Object> option : readProperties(args, request, c.propertiesList()).entrySet()) {
            String key = option.getKey();
            Object value = option.getValue();
            if (MilvusCommandKeys.RESET_CONNECTION.equals(key)) {
                if (!(value instanceof Boolean)) {
                    throw new SQLException("WITH " + key + " requires a boolean.");
                }
                builder.resetConnection((Boolean) value);
            } else if (MilvusCommandKeys.DESCRIPTION.equals(key)) {
                if (!(value instanceof String)) {
                    throw new SQLException("WITH " + key + " requires a string.");
                }
                builder.description((String) value);
            } else {
                throw new SQLException("Unknown password-update option: " + key);
            }
        }
        cmd.updatePassword(builder.build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlterDescription(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger args = new AtomicInteger(startArgIdx);
        readHints(args, request, h.hint());
        String description = readDescription(readProperties(args, request, c.propertiesList()));
        if (description == null) {
            throw new SQLException("ALTER USER/ROLE WITH requires description.");
        }
        if (c.userName != null) {
            cmd.updateUser(UpdateUserReq.builder().userName(readName(c.userName)).description(description).build());
        } else {
            cmd.alterRole(AlterRoleReq.builder().roleName(readName(c.roleName)).description(description).build());
        }
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowUser(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        DescribeUserResp result = cmd.describeUser(DescribeUserReq.builder().userName(readName(c.userName)).build());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(COL_USER_STRING.name, result.getUserName());
        row.put(COL_ROLES_STRING.name, JSON.toJson(result.getRoles()));
        row.put(COL_DESCRIPTION_STRING.name, result.getDescription());
        receive.responseResult(request, listResult(request, Arrays.asList(COL_USER_STRING, COL_ROLES_STRING, COL_DESCRIPTION_STRING), Collections.singletonList(row)));
        return completed(future);
    }

    private static String readPassword(Token token, AtomicInteger args, AdapterRequest request) throws SQLException {
        Object value = token.getType() == MilvusParser.ARG ? getArg(args, request) : getIdentifier(token.getText());
        if (!(value instanceof String)) {
            throw new SQLException("Password requires a non-null string.");
        }
        return (String) value;
    }

    private static String readDescription(Map<String, Object> options) throws SQLException {
        if (options.isEmpty()) {
            return null;
        }
        Object description = options.get(MilvusCommandKeys.DESCRIPTION);
        if (options.size() != 1 || !(description instanceof String)) {
            throw new SQLException("USER/ROLE WITH accepts only description, a non-null string.");
        }
        return (String) description;
    }

    public static Future<?> execCreateRole(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = readName(c.roleName);
        String description = readDescription(readProperties(argIndex, request, c.propertiesList()));
        CreateRoleReq.CreateRoleReqBuilder builder = CreateRoleReq.builder().roleName(roleName);
        if (description != null) {
            builder.description(description);
        }

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && roleExists(cmd, roleName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        cmd.createRole(builder.build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropUser(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String userName = readName(c.userName);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!userExists(cmd, userName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("User '" + userName + "' doesn't exist");
            }
        }

        DropUserReq param = DropUserReq.builder().userName(userName).build();
        cmd.dropUser(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropRole(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = readName(c.roleName);
        Map<String, Object> options = readProperties(argIndex, request, c.propertiesList());
        DropRoleReq.DropRoleReqBuilder builder = DropRoleReq.builder().roleName(roleName);
        if (!options.isEmpty()) {
            Object forceDrop = options.get(MilvusCommandKeys.FORCE_DROP);
            if (options.size() != 1 || !(forceDrop instanceof Boolean)) {
                throw new SQLException("DROP ROLE WITH accepts only force_drop, a non-null boolean.");
            }
            builder.forceDrop((Boolean) forceDrop);
        }

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!roleExists(cmd, roleName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("Role '" + roleName + "' doesn't exist");
            }
        }

        cmd.dropRole(builder.build());

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowUsers(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        List<String> usernames = cmd.listUsers();
        List<Map<String, Object>> listResult = usernames.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(COL_USER_STRING.name, s);
            return map;
        }).collect(Collectors.toList());

        receive.responseResult(request, listResult(request, Collections.singletonList(COL_USER_STRING), listResult));
        return completed(future);
    }

    public static Future<?> execShowRoles(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        List<Map<String, Object>> listResult = cmd.listRoles().stream().map(name -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(COL_ROLE_STRING.name, name);
            return map;
        }).collect(Collectors.toList());

        receive.responseResult(request, listResult(request, Collections.singletonList(COL_ROLE_STRING), listResult));
        return completed(future);
    }

    public static Future<?> execShowRole(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        String database = cmd.getCatalog();
        if (c.database != null) {
            database = c.database.STAR() == null ? readName(c.database.identifier()) : "*";
        }
        DescribeRoleResp result = cmd.describeRole(DescribeRoleReq.builder().roleName(readName(c.roleName)).dbName(database).build());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(COL_ROLE_STRING.name, result.getRoleName());
        row.put(COL_DESCRIPTION_STRING.name, result.getDescription());
        row.put(COL_GRANTS_STRING.name, JSON.toJson(result.getGrantInfos()));
        receive.responseResult(request, listResult(request, Arrays.asList(COL_ROLE_STRING, COL_DESCRIPTION_STRING, COL_GRANTS_STRING), Collections.singletonList(row)));
        return completed(future);
    }

    // Role membership and privileges

    public static Future<?> execGrantCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, GrantCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        if (c instanceof GrantScopedPrivilegeContext scoped) {
            return MilvusCommandsForScopedPrivileges.execGrant(future, cmd, h, scoped, request, receive, startArgIdx);
        }
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        if (c instanceof GrantRoleToUserContext ctx) {
            String roleName = readName(ctx.roleName);
            String userName = readName(ctx.userName);

            GrantRoleReq param = GrantRoleReq.builder().roleName(roleName).userName(userName).build();
            cmd.grantRole(param);
            receive.responseUpdateCount(request, 0);
            return completed(future);
        } else if (c instanceof GrantPrivilegeToRoleContext ctx) {
            String roleName = readName(ctx.roleName);
            String privilege = readName(ctx.privilege);
            String objectType = readName(ctx.objectType);
            String objectName = "*";
            if (ctx.star == null) {
                objectName = readName(ctx.objectName);
            }

            GrantPrivilegeReq param = GrantPrivilegeReq.builder() //
                    .roleName(roleName) //
                    .privilege(privilege) //
                    .objectType(objectType) //
                    .objectName(objectName) //
                    .build();
            cmd.grantPrivilege(param);
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        throw new SQLException("Unknown GRANT command");
    }

    public static Future<?> execRevokeCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, RevokeCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        if (c instanceof RevokeScopedPrivilegeContext scoped) {
            return MilvusCommandsForScopedPrivileges.execRevoke(future, cmd, h, scoped, request, receive, startArgIdx);
        }
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        if (c instanceof RevokeRoleFromUserContext ctx) {
            String roleName = readName(ctx.roleName);
            String userName = readName(ctx.userName);

            RevokeRoleReq param = RevokeRoleReq.builder().roleName(roleName).userName(userName).build();
            cmd.revokeRole(param);
            receive.responseUpdateCount(request, 0);
            return completed(future);
        } else if (c instanceof RevokePrivilegeFromRoleContext ctx) {
            String roleName = readName(ctx.roleName);
            String privilege = readName(ctx.privilege);
            String objectType = readName(ctx.objectType);
            String objectName = "*";
            if (ctx.star == null) {
                objectName = readName(ctx.objectName);
            }

            RevokePrivilegeReq param = RevokePrivilegeReq.builder() //
                    .roleName(roleName) //
                    .privilege(privilege) //
                    .objectType(objectType) //
                    .objectName(objectName) //
                    .build();
            cmd.revokePrivilege(param);
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        throw new SQLException("Unknown REVOKE command");
    }

    public static Future<?> execShowGrants(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = readName(c.roleName);

        DescribeRoleReq param = DescribeRoleReq.builder().roleName(roleName).build();
        DescribeRoleResp result = cmd.describeRole(param);

        // DescribeRole returns all grants; ON narrows the rows, not the effective privileges of an object.
        Stream<DescribeRoleResp.GrantInfo> grants = result.getGrantInfos().stream();
        if (c.TABLE() != null) {
            grants = grants.filter(grant -> ObjectType.Collection.name().equals(grant.getObjectType()));
        } else if (c.USER() != null) {
            grants = grants.filter(grant -> ObjectType.User.name().equals(grant.getObjectType()));
        } else if (c.GLOBAL() != null) {
            grants = grants.filter(grant -> ObjectType.Global.name().equals(grant.getObjectType()));
        }
        String objectName = readName(c.objectName);
        if (objectName != null) {
            grants = grants.filter(grant -> objectName.equals(grant.getObjectName()));
        }

        List<Map<String, Object>> listResult = grants.map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(COL_DATABASE_STRING.name, s.getDbName());
            // SDK 2.6.22 populates the role on the response, not on individual grants.
            map.put(COL_ROLE_STRING.name, s.getRoleName() == null ? result.getRoleName() : s.getRoleName());
            map.put(COL_OBJECT_STRING.name, s.getObjectType());
            map.put(COL_OBJECT_NAME_STRING.name, s.getObjectName());
            map.put(COL_PRIVILEGE_STRING.name, s.getPrivilege());
            return map;
        }).collect(Collectors.toList());

        List<JdbcColumn> cols = Arrays.asList(COL_DATABASE_STRING, COL_ROLE_STRING, COL_OBJECT_STRING, COL_OBJECT_NAME_STRING, COL_PRIVILEGE_STRING);
        receive.responseResult(request, listResult(request, cols, listResult));
        return completed(future);
    }

    // Existence checks for IF [NOT] EXISTS

    private static boolean userExists(MilvusCmd milvusCmd, String username) throws SQLException {
        return milvusCmd.listUsers().contains(username);
    }

    private static boolean roleExists(MilvusCmd milvusCmd, String roleName) throws SQLException {
        return milvusCmd.listRoles().contains(roleName);
    }
}
