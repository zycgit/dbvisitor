/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.PrivilegeGroup;
import io.milvus.v2.service.rbac.request.AddPrivilegesToGroupReq;
import io.milvus.v2.service.rbac.request.CreatePrivilegeGroupReq;
import io.milvus.v2.service.rbac.request.DropPrivilegeGroupReq;
import io.milvus.v2.service.rbac.request.RemovePrivilegesFromGroupReq;
import io.milvus.v2.service.rbac.response.ListPrivilegeGroupsResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusPrivilegeGroupTest {
    private final List<Object> requests = new ArrayList<>();
    private final List<String> methods  = new ArrayList<>();
    private       boolean      emptyGroups;
    private       boolean      failChanges;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            methods.add(method.getName());
            if (args.length > 0) {
                requests.add(args[0]);
            }
            if (method.getName().equals("listPrivilegeGroups")) {
                return ListPrivilegeGroupsResp.builder().privilegeGroups(emptyGroups ? Collections.emptyList() : Arrays.asList(PrivilegeGroup.builder().groupName("readers").privileges(Arrays.asList("Search", "Query")).build(), PrivilegeGroup.builder().groupName("empty").privileges(Collections.emptyList()).build())).build();
            }
            if (failChanges) {
                throw new IllegalStateException("privilege group change rejected");
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", props);
    }

    private <T> T request(Class<T> type) {
        return requests.stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    public void groupLifecycleUsesNativeRequestsWithoutImplicitRoleAssignments() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("CREATE PRIVILEGE GROUP readers"));
            assertEquals(0, statement.executeUpdate("ALTER PRIVILEGE GROUP readers ADD (Search, Query)"));
            assertEquals(0, statement.executeUpdate("ALTER PRIVILEGE GROUP readers DROP (Query)"));
            assertEquals(0, statement.executeUpdate("DROP PRIVILEGE GROUP readers"));
        }
        assertEquals("readers", request(CreatePrivilegeGroupReq.class).getGroupName());
        assertEquals("readers", request(AddPrivilegesToGroupReq.class).getGroupName());
        assertEquals(Arrays.asList("Search", "Query"), request(AddPrivilegesToGroupReq.class).getPrivileges());
        assertEquals("readers", request(RemovePrivilegesFromGroupReq.class).getGroupName());
        assertEquals(Collections.singletonList("Query"), request(RemovePrivilegesFromGroupReq.class).getPrivileges());
        assertEquals("readers", request(DropPrivilegeGroupReq.class).getGroupName());
        assertFalse(methods.contains("grantPrivilegeV2"));
        assertFalse(methods.contains("createRole"));
    }

    @Test
    public void groupListingPreservesEmptyMembersAndJdbcMaxRows() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SHOW PRIVILEGE GROUPS")) {
                assertEquals(2, result.getMetaData().getColumnCount());
                assertEquals(Types.VARCHAR, result.getMetaData().getColumnType(2));
                assertTrue(result.next());
                assertEquals("readers", result.getString("PRIVILEGE_GROUP"));
                assertEquals("[\"Search\",\"Query\"]", result.getString("PRIVILEGES"));
                assertTrue(result.next());
                assertEquals("empty", result.getString("PRIVILEGE_GROUP"));
                assertEquals("[]", result.getString("PRIVILEGES"));
                assertFalse(result.next());
            }
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("SHOW PRIVILEGE GROUPS")) {
                assertTrue(result.next());
                assertFalse(result.next());
            }
        }
    }

    @Test
    public void emptyGroupListingRetainsColumnMetadata() throws Exception {
        emptyGroups = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW PRIVILEGE GROUPS")) {
            assertEquals("PRIVILEGE_GROUP", result.getMetaData().getColumnLabel(1));
            assertEquals("PRIVILEGES", result.getMetaData().getColumnLabel(2));
            assertFalse(result.next());
        }
    }

    @Test
    public void emptyMemberListsFailBeforeSdkMutation() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER PRIVILEGE GROUP readers ADD ()"));
            assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER PRIVILEGE GROUP readers DROP ()"));
        }
        assertFalse(methods.contains("addPrivilegesToGroup"));
        assertFalse(methods.contains("removePrivilegesFromGroup"));
    }

    @Test
    public void serverRejectionDoesNotTriggerRecreationOrPartialMemberCalls() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            failChanges = true;
            assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER PRIVILEGE GROUP readers ADD (Search, InvalidPrivilege)"));
            failChanges = false;
        }
        assertEquals(1, Collections.frequency(methods, "addPrivilegesToGroup"));
        assertEquals(Arrays.asList("Search", "InvalidPrivilege"), request(AddPrivilegesToGroupReq.class).getPrivileges());
        assertFalse(methods.contains("createPrivilegeGroup"));
        assertFalse(methods.contains("dropPrivilegeGroup"));
    }
}
