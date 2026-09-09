/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.milvus.grpc.ObjectType;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
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

    // Users and roles

    public static Future<?> execCreateUser(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String userName = readName(c.userName);
        String password = getIdentifier(c.password.getText());

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && userExists(cmd, userName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateUserReq param = CreateUserReq.builder().userName(userName).password(password).build();
        cmd.createUser(param);

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execCreateRole(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = readName(c.roleName);

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && roleExists(cmd, roleName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateRoleReq param = CreateRoleReq.builder().roleName(roleName).build();
        cmd.createRole(param);

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

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!roleExists(cmd, roleName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("Role '" + roleName + "' doesn't exist");
            }
        }

        DropRoleReq param = DropRoleReq.builder().roleName(roleName).build();
        cmd.dropRole(param);

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

    // Role membership and privileges

    public static Future<?> execGrantCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, GrantCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
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
            map.put(COL_ROLE_STRING.name, s.getRoleName());
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
