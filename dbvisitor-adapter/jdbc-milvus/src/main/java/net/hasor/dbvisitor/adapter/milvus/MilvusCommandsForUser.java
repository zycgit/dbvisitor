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
package net.hasor.dbvisitor.adapter.milvus;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.milvus.grpc.ListCredUsersResponse;
import io.milvus.grpc.SelectGrantResponse;
import io.milvus.grpc.SelectRoleResponse;
import io.milvus.param.R;
import io.milvus.param.credential.CreateCredentialParam;
import io.milvus.param.credential.DeleteCredentialParam;
import io.milvus.param.credential.ListCredUsersParam;
import io.milvus.param.role.*;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;

/***
 * User/Role commands for Milvus
 */
class MilvusCommandsForUser extends MilvusCommands {
    private static final JdbcColumn COL_OBJECT_STRING      = new JdbcColumn("OBJECT", AdapterType.String, "", "", "");
    private static final JdbcColumn COL_OBJECT_NAME_STRING = new JdbcColumn("OBJECT_NAME", AdapterType.String, "", "", "");
    private static final JdbcColumn COL_PRIVILEGE_STRING   = new JdbcColumn("PRIVILEGE", AdapterType.String, "", "", "");

    private static boolean userExists(MilvusCmd milvusCmd, String username) throws SQLException {
        R<ListCredUsersResponse> response = milvusCmd.getClient().listCredUsers(ListCredUsersParam.newBuilder().build());
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(response.getMessage());
        }
        return response.getData().getUsernamesList().contains(username);
    }

    private static boolean roleExists(MilvusCmd milvusCmd, String roleName) throws SQLException {
        R<SelectRoleResponse> response = milvusCmd.getClient().selectRole(SelectRoleParam.newBuilder().withRoleName(roleName).build());
        if (response.getStatus() != R.Status.Success.getCode()) {
            return false;
        }
        return response.getData().getResultsList().stream().anyMatch(roleResult -> {
            try {
                return roleResult.getRole().getName().equals(roleName);
            } catch (Exception e) {
                return false;
            }
        });
    }

    //

    public static Future<?> execCreateUser(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String userName = argAsName(argIndex, request, c.userName);
        String password = getIdentifier(c.password.getText());

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && userExists(cmd, userName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateCredentialParam param = CreateCredentialParam.newBuilder().withUsername(userName).withPassword(password).build();
        R<?> result = cmd.getClient().createCredential(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execCreateRole(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c, //
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = argAsName(argIndex, request, c.roleName);

        boolean ifNotExists = c.IF() != null && c.NOT() != null && c.EXISTS() != null;
        if (ifNotExists && roleExists(cmd, roleName)) {
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        CreateRoleParam param = CreateRoleParam.newBuilder().withRoleName(roleName).build();
        R<?> result = cmd.getClient().createRole(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropUser(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String userName = argAsName(argIndex, request, c.userName);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!userExists(cmd, userName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("User '" + userName + "' doesn't exist");
            }
        }

        DeleteCredentialParam param = DeleteCredentialParam.newBuilder().withUsername(userName).build();
        R<?> result = cmd.getClient().deleteCredential(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDropRole(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = argAsName(argIndex, request, c.roleName);

        boolean ifExists = c.IF() != null && c.EXISTS() != null;
        if (!roleExists(cmd, roleName)) {
            if (ifExists) {
                receive.responseUpdateCount(request, 0);
                return completed(future);
            } else {
                throw new SQLException("Role '" + roleName + "' doesn't exist");
            }
        }

        DropRoleParam param = DropRoleParam.newBuilder().withRoleName(roleName).build();
        R<?> result = cmd.getClient().dropRole(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShowUsers(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        ListCredUsersParam param = ListCredUsersParam.newBuilder().build();
        R<ListCredUsersResponse> result = cmd.getClient().listCredUsers(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        List<String> usernames = result.getData().getUsernamesList();
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

        R<SelectRoleResponse> result = cmd.getClient().selectRole(SelectRoleParam.newBuilder().build());
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        List<?> roles = result.getData().getResultsList();
        List<Map<String, Object>> listResult = roles.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            try {
                Object roleEntity = s.getClass().getMethod("getRole").invoke(s);
                String name = (String) roleEntity.getClass().getMethod("getName").invoke(roleEntity);
                map.put(COL_ROLE_STRING.name, name);
            } catch (Exception e) {
                map.put(COL_ROLE_STRING.name, s.toString());
            }
            return map;
        }).collect(Collectors.toList());

        receive.responseResult(request, listResult(request, Collections.singletonList(COL_ROLE_STRING), listResult));
        return completed(future);
    }

    //

    public static Future<?> execGrantCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, GrantCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        if (c instanceof GrantRoleToUserContext) {
            GrantRoleToUserContext ctx = (GrantRoleToUserContext) c;
            String roleName = argAsName(argIndex, request, ctx.roleName);
            String userName = argAsName(argIndex, request, ctx.userName);

            AddUserToRoleParam param = AddUserToRoleParam.newBuilder().withRoleName(roleName).withUserName(userName).build();
            R<?> result = cmd.getClient().addUserToRole(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
            receive.responseUpdateCount(request, 0);
            return completed(future);
        } else if (c instanceof GrantPrivilegeToRoleContext) {
            GrantPrivilegeToRoleContext ctx = (GrantPrivilegeToRoleContext) c;
            String roleName = argAsName(argIndex, request, ctx.roleName);
            String privilege = argAsName(argIndex, request, ctx.privilege);
            String objectType = argAsName(argIndex, request, ctx.objectType);
            String objectName = "*";
            if (ctx.star == null) {
                objectName = argAsName(argIndex, request, ctx.objectName);
            }

            GrantRolePrivilegeParam param = GrantRolePrivilegeParam.newBuilder() //
                    .withRoleName(roleName) //
                    .withPrivilege(privilege) //
                    .withObject(objectType) //
                    .withObjectName(objectName) //
                    .build();
            R<?> result = cmd.getClient().grantRolePrivilege(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        throw new SQLException("Unknown GRANT command");
    }

    public static Future<?> execRevokeCmd(Future<Object> future, MilvusCmd cmd, HintCommandContext h, RevokeCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());

        if (c instanceof RevokeRoleFromUserContext) {
            RevokeRoleFromUserContext ctx = (RevokeRoleFromUserContext) c;
            String roleName = argAsName(argIndex, request, ctx.roleName);
            String userName = argAsName(argIndex, request, ctx.userName);

            RemoveUserFromRoleParam param = RemoveUserFromRoleParam.newBuilder().withRoleName(roleName).withUserName(userName).build();
            R<?> result = cmd.getClient().removeUserFromRole(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
            receive.responseUpdateCount(request, 0);
            return completed(future);
        } else if (c instanceof RevokePrivilegeFromRoleContext) {
            RevokePrivilegeFromRoleContext ctx = (RevokePrivilegeFromRoleContext) c;
            String roleName = argAsName(argIndex, request, ctx.roleName);
            String privilege = argAsName(argIndex, request, ctx.privilege);
            String objectType = argAsName(argIndex, request, ctx.objectType);
            String objectName = "*";
            if (ctx.star == null) {
                objectName = argAsName(argIndex, request, ctx.objectName);
            }

            RevokeRolePrivilegeParam param = RevokeRolePrivilegeParam.newBuilder() //
                    .withRoleName(roleName) //
                    .withPrivilege(privilege) //
                    .withObject(objectType) //
                    .withObjectName(objectName) //
                    .build();
            R<?> result = cmd.getClient().revokeRolePrivilege(param);
            if (result.getStatus() != R.Status.Success.getCode()) {
                throw new SQLException(result.getMessage());
            }
            receive.responseUpdateCount(request, 0);
            return completed(future);
        }

        throw new SQLException("Unknown REVOKE command");
    }

    public static Future<?> execShowGrants(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c,//
            AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        AtomicInteger argIndex = new AtomicInteger(startArgIdx);
        readHints(argIndex, request, h.hint());
        String roleName = argAsName(argIndex, request, c.roleName);

        SelectGrantForRoleParam param = SelectGrantForRoleParam.newBuilder().withRoleName(roleName).build();
        R<SelectGrantResponse> result = cmd.getClient().selectGrantForRole(param);
        if (result.getStatus() != R.Status.Success.getCode()) {
            throw new SQLException(result.getMessage());
        }

        List<io.milvus.grpc.GrantEntity> grants = result.getData().getEntitiesList();
        List<Map<String, Object>> listResult = grants.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(COL_DATABASE_STRING.name, s.getDbName());
            map.put(COL_ROLE_STRING.name, s.getRole().getName());
            map.put(COL_OBJECT_STRING.name, s.getObject().getName());
            map.put(COL_OBJECT_NAME_STRING.name, s.getObjectName());
            map.put(COL_PRIVILEGE_STRING.name, s.getGrantor().getPrivilege().getName());
            return map;
        }).collect(Collectors.toList());

        List<JdbcColumn> cols = Arrays.asList(COL_DATABASE_STRING, COL_ROLE_STRING, COL_OBJECT_STRING, COL_OBJECT_NAME_STRING, COL_PRIVILEGE_STRING);
        receive.responseResult(request, listResult(request, cols, listResult));
        return completed(future);
    }
}
