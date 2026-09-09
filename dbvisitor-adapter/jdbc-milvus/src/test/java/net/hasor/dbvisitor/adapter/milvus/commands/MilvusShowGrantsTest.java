package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.request.DescribeRoleReq;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MilvusShowGrantsTest {
    private List<DescribeRoleResp.GrantInfo> grants;

    @Before
    public void install() {
        grants = Arrays.asList(grant("Collection", "b"), grant("Collection", "a"), grant("User", "a"), grant("User", "b"), grant("Global", "*"), grant("Collection", "A"), grant("Collection", "*"));
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getServerVersion":
                    return "v2.6.2";
                case "describeRole":
                    assertEquals("r", ((DescribeRoleReq) args[0]).getRoleName());
                    return DescribeRoleResp.builder().roleName("r").grantInfos(grants).build();
                default:
                    return null;
            }
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private static DescribeRoleResp.GrantInfo grant(String type, String name) {
        return DescribeRoleResp.GrantInfo.builder().roleName("r").dbName("db1").objectType(type).objectName(name).privilege("Search").build();
    }

    private Connection connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        props.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://mock:19530/db1", props);
    }

    private List<String> readGrants(Statement statement, String scope) throws SQLException {
        List<String> result = new ArrayList<>();
        try (ResultSet rows = statement.executeQuery("SHOW GRANTS FOR ROLE r" + scope)) {
            assertEquals(5, rows.getMetaData().getColumnCount());
            assertEquals("OBJECT_NAME", rows.getMetaData().getColumnLabel(4));
            while (rows.next()) {
                assertEquals("r", rows.getString("ROLE"));
                assertEquals("db1", rows.getString("DATABASE"));
                assertEquals("Search", rows.getString("PRIVILEGE"));
                result.add(rows.getString("OBJECT") + ":" + rows.getString("OBJECT_NAME"));
            }
        }
        return result;
    }

    @Test
    public void noScopeKeepsAllGrants() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(Arrays.asList("Collection:b", "Collection:a", "User:a", "User:b", "Global:*", "Collection:A", "Collection:*"), readGrants(statement, ""));
        }
    }

    @Test
    public void tableScopeMatchesTypeAndExactName() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(Collections.singletonList("Collection:a"), readGrants(statement, " ON TABLE a"));
        }
    }

    @Test
    public void userScopeDoesNotIncludeSameNamedCollection() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(Collections.singletonList("User:a"), readGrants(statement, " ON USER a"));
        }
    }

    @Test
    public void globalScopeDoesNotIncludeCollectionWildcard() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(Collections.singletonList("Global:*"), readGrants(statement, " ON GLOBAL"));
        }
    }

    @Test
    public void missingObjectReturnsEmptyResultWithMetadata() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertTrue(readGrants(statement, " ON TABLE missing").isEmpty());
            assertTrue(readGrants(statement, " ON USER missing").isEmpty());
        }
    }

    @Test
    public void objectNamesKeepTheirCase() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(Collections.singletonList("Collection:A"), readGrants(statement, " ON TABLE A"));
        }
    }

    @Test
    public void maxRowsIsAppliedAfterObjectFiltering() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            assertEquals(Collections.singletonList("Collection:a"), readGrants(statement, " ON TABLE a"));
        }
    }
}
