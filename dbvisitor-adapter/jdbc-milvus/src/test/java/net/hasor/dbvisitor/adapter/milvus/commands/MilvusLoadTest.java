package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import com.google.gson.Gson;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusLoadTest {
    private final List<Object> requests = new ArrayList<>();
    private final List<String> methods  = new ArrayList<>();
    private       boolean      failLoad;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            methods.add(method.getName());
            if (method.getName().equals("loadCollection") || method.getName().equals("loadPartitions")) {
                requests.add(args[0]);
                if (failLoad) {
                    throw new IllegalStateException("load rejected by server");
                }
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties);
    }

    @Test
    public void collectionOptionsShouldPreserveListsAndUseNativeRefreshWait() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("""
                    /*+ timeout=4321 */ LOAD TABLE books WITH (
                        num_replicas=2, refresh=true, load_fields=['id','v'],
                        skip_load_dynamic_field=true, resource_groups=['rg1','rg2'])
                    """));
        }
        assertEquals(1, requests.size());
        LoadCollectionReq request = (LoadCollectionReq) requests.get(0);
        assertEquals("db1", request.getDatabaseName());
        assertEquals("books", request.getCollectionName());
        assertEquals(Integer.valueOf(2), request.getNumReplicas());
        assertEquals(Arrays.asList("id", "v"), request.getLoadFields());
        assertEquals(Arrays.asList("rg1", "rg2"), request.getResourceGroups());
        assertEquals(Boolean.TRUE, request.getSkipLoadDynamicField());
        assertEquals(Boolean.TRUE, request.getRefresh());
        assertEquals(Boolean.TRUE, request.getSync());
        assertEquals(Long.valueOf(4321), request.getTimeout());
        assertFalse(methods.contains("getLoadStateV2"));
    }

    @Test
    public void partitionOptionsShouldBindHintsFirstAndReplaceValuesOnReuse() throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("""
                /*+ sync=?, timeout=? */ LOAD TABLE books PARTITION p WITH (
                    num_replicas=?, load_fields=?, resource_groups=?, refresh=?, skip_load_dynamic_field=?)
                """)) {
            statement.setBoolean(1, false);
            statement.setLong(2, 2500);
            statement.setInt(3, 3);
            statement.setObject(4, new String[] { "id", "v" });
            statement.setObject(5, Arrays.asList("rg1", "rg2"));
            statement.setBoolean(6, true);
            statement.setBoolean(7, true);
            assertEquals(0, statement.executeUpdate());
            statement.setBoolean(1, true);
            statement.setInt(3, 1);
            statement.setObject(4, Collections.emptyList());
            statement.setString(5, "[]");
            statement.setBoolean(6, false);
            statement.setBoolean(7, false);
            assertEquals(0, statement.executeUpdate());
        }
        LoadPartitionsReq first = (LoadPartitionsReq) requests.get(0);
        assertEquals("db1", first.getDatabaseName());
        assertEquals("books", first.getCollectionName());
        assertEquals(Collections.singletonList("p"), first.getPartitionNames());
        assertEquals(Integer.valueOf(3), first.getNumReplicas());
        assertEquals(Arrays.asList("id", "v"), first.getLoadFields());
        assertEquals(Arrays.asList("rg1", "rg2"), first.getResourceGroups());
        assertEquals(Boolean.TRUE, first.getRefresh());
        assertEquals(Boolean.TRUE, first.getSkipLoadDynamicField());
        assertEquals(Boolean.FALSE, first.getSync());
        assertEquals(Long.valueOf(2500), first.getTimeout());
        LoadPartitionsReq second = (LoadPartitionsReq) requests.get(1);
        assertEquals(Integer.valueOf(1), second.getNumReplicas());
        assertTrue(second.getLoadFields().isEmpty());
        assertTrue(second.getResourceGroups().isEmpty());
        assertEquals(Boolean.FALSE, second.getRefresh());
        assertEquals(Boolean.FALSE, second.getSkipLoadDynamicField());
        assertEquals(Boolean.TRUE, second.getSync());
    }

    @Test
    public void omittedOptionsShouldKeepSdkDefaultsAndLegacySyncHint() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("LOAD TABLE books");
            statement.executeUpdate("/*+ sync=false */ LOAD TABLE books PARTITION p");
        }
        LoadCollectionReq collection = (LoadCollectionReq) requests.get(0);
        assertEquals(Integer.valueOf(1), collection.getNumReplicas());
        assertEquals(Boolean.FALSE, collection.getRefresh());
        assertEquals(Boolean.FALSE, collection.getSkipLoadDynamicField());
        assertTrue(collection.getLoadFields().isEmpty());
        assertTrue(collection.getResourceGroups().isEmpty());
        assertEquals(Boolean.TRUE, collection.getSync());
        assertEquals(Long.valueOf(60000), collection.getTimeout());
        LoadPartitionsReq partition = (LoadPartitionsReq) requests.get(1);
        assertEquals(Integer.valueOf(1), partition.getNumReplicas());
        assertEquals(Boolean.FALSE, partition.getSync());
        assertEquals(Boolean.FALSE, partition.getRefresh());
        assertEquals(Boolean.FALSE, partition.getSkipLoadDynamicField());
        assertTrue(partition.getLoadFields().isEmpty());
        assertTrue(partition.getResourceGroups().isEmpty());
        assertEquals(Long.valueOf(60000), partition.getTimeout());
    }

    @Test
    public void jsonListBindingsShouldRemainSdkValuesForEveryStringBindingPath() throws SQLException {
        String name = "v'); DROP TABLE books; -- \\\"";
        String json = new Gson().toJson(Collections.singletonList(name));
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("/*+ sync=false */ LOAD TABLE books WITH (load_fields=?, resource_groups=?)")) {
            statement.setString(1, json);
            statement.setString(2, json);
            statement.executeUpdate();
            statement.setObject(1, json);
            statement.setObject(2, json);
            statement.executeUpdate();
            statement.setObject(1, json, Types.VARCHAR);
            statement.setObject(2, json, Types.VARCHAR);
            statement.executeUpdate();
        }
        assertEquals(3, requests.size());
        assertFalse(methods.contains("dropCollection"));
        for (Object value : requests) {
            LoadCollectionReq request = (LoadCollectionReq) value;
            assertEquals(Collections.singletonList(name), request.getLoadFields());
            assertEquals(Collections.singletonList(name), request.getResourceGroups());
        }
    }

    @Test
    public void invalidOptionsShouldFailBeforeAnyLoadRequest() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String options : Arrays.asList("num_replicas=0", "num_replicas=1.5", "num_replicas=2147483648", "num_replicas='2'", "refresh=1", "refresh=NULL", "skip_load_dynamic_field='true'", "load_fields=[1]", "load_fields=['']", "resource_groups=[NULL]", "resource_groups='{}'", "load_fields='[false]'", "load_fields=NULL", "unknown=true", "refresh=true, REFRESH=false")) {
                assertThrows(options, SQLException.class, () -> statement.executeUpdate("LOAD TABLE books WITH (" + options + ")"));
                assertThrows(options, SQLException.class, () -> statement.executeUpdate("LOAD TABLE books PARTITION p WITH (" + options + ")"));
            }
            assertThrows(SQLException.class, () -> statement.executeUpdate("/*+ timeout=0 */ LOAD TABLE books"));
        }
        assertTrue(requests.isEmpty());
    }

    @Test
    public void loadAndReleaseShouldAcceptNamesSupportedByTheCommonIdentifierRule() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("/*+ sync=false */ LOAD TABLE vector PARTITION index");
            statement.executeUpdate("/*+ sync=false */ RELEASE TABLE vector PARTITION index");
        }
        LoadPartitionsReq request = (LoadPartitionsReq) requests.get(0);
        assertEquals("vector", request.getCollectionName());
        assertEquals(Collections.singletonList("index"), request.getPartitionNames());
        assertTrue(methods.contains("releasePartitions"));
    }

    @Test
    public void nativeLoadErrorsShouldNotTriggerReleaseOrFallback() throws SQLException {
        failLoad = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.executeUpdate("LOAD TABLE books WITH (refresh=true)"));
            assertTrue(error.getMessage().contains("load rejected by server"));
        }
        assertEquals(1, requests.size());
        assertFalse(methods.contains("releaseCollection"));
        assertFalse(methods.contains("getLoadStateV2"));
    }
}
