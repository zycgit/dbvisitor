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
import com.google.gson.Gson;
import io.milvus.v2.service.rbac.PrivilegeGroup;
import io.milvus.v2.service.rbac.request.*;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommands;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;
import net.hasor.dbvisitor.driver.AdapterType;
import net.hasor.dbvisitor.driver.JdbcColumn;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readHints;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** Native privilege-group definitions, separate from assigning groups to roles. */
public final class MilvusCommandsForPrivilegeGroups extends MilvusCommands {
    private static final JdbcColumn GROUP      = new JdbcColumn("PRIVILEGE_GROUP", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final JdbcColumn PRIVILEGES = new JdbcColumn("PRIVILEGES", AdapterType.String, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array);
    private static final Gson       JSON       = new Gson();

    private MilvusCommandsForPrivilegeGroups() {
    }

    public static Future<?> execCreate(Future<Object> future, MilvusCmd cmd, HintCommandContext h, CreateCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        cmd.createPrivilegeGroup(CreatePrivilegeGroupReq.builder().groupName(readName(c.groupName)).build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execDrop(Future<Object> future, MilvusCmd cmd, HintCommandContext h, DropCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        cmd.dropPrivilegeGroup(DropPrivilegeGroupReq.builder().groupName(readName(c.groupName)).build());
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execAlter(Future<Object> future, MilvusCmd cmd, HintCommandContext h, AlterCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        String group = readName(c.groupName);
        List<String> privileges = c.privilegeNames().identifier().stream().map(identifier -> readName(identifier)).collect(Collectors.toList());
        if (c.ADD() != null) {
            cmd.addPrivilegesToGroup(AddPrivilegesToGroupReq.builder().groupName(group).privileges(privileges).build());
        } else {
            cmd.removePrivilegesFromGroup(RemovePrivilegesFromGroupReq.builder().groupName(group).privileges(privileges).build());
        }
        receive.responseUpdateCount(request, 0);
        return completed(future);
    }

    public static Future<?> execShow(Future<Object> future, MilvusCmd cmd, HintCommandContext h, ShowCmdContext c, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        readHints(new AtomicInteger(startArgIdx), request, h.hint());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PrivilegeGroup group : cmd.listPrivilegeGroups(ListPrivilegeGroupsReq.builder().build()).getPrivilegeGroups()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(GROUP.name, group.getGroupName());
            row.put(PRIVILEGES.name, JSON.toJson(group.getPrivileges()));
            rows.add(row);
        }
        receive.responseResult(request, listResult(request, Arrays.asList(GROUP, PRIVILEGES), rows));
        return completed(future);
    }
}
