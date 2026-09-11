package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.RenameCollectionReq;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusRenameTest {
    private final List<RenameCollectionReq> requests = new ArrayList<>();
    private final List<String>              methods  = new ArrayList<>();
    private       boolean                   failRename;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            methods.add(method.getName());
            if (method.getName().equals("renameCollection")) {
                requests.add((RenameCollectionReq) args[0]);
                if (failRename) {
                    throw new IllegalStateException("target database unavailable");
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
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/source_db", properties);
    }

    @Test
    public void explicitDestinationShouldNotChangeConnectionCatalogOrFollowingCommand() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("ALTER TABLE books RENAME TO archived IN DATABASE archive"));
            assertEquals("source_db", connection.getCatalog());
            assertEquals(0, statement.executeUpdate("ALTER TABLE vector RENAME TO index"));
        }
        assertEquals(2, requests.size());
        RenameCollectionReq move = requests.get(0);
        assertEquals("source_db", move.getDatabaseName());
        assertEquals("books", move.getCollectionName());
        assertEquals("archived", move.getNewCollectionName());
        assertEquals("archive", move.getTargetDbName());
        RenameCollectionReq local = requests.get(1);
        assertEquals("source_db", local.getDatabaseName());
        assertEquals("vector", local.getCollectionName());
        assertEquals("index", local.getNewCollectionName());
        assertNull(local.getTargetDbName());
        assertFalse(methods.contains("useDatabase"));
    }

    @Test
    public void databaseAndCollectionNamesShouldRequireIdentifiersNotBoundValues() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : Arrays.asList("ALTER TABLE ? RENAME TO other", "ALTER TABLE books RENAME TO ?", "ALTER TABLE books RENAME TO other IN DATABASE ?", "ALTER TABLE books RENAME TO other IN DATABASE")) {
                assertThrows(sql, SQLException.class, () -> statement.executeUpdate(sql));
            }
        }
        assertTrue(requests.isEmpty());
    }

    @Test
    public void nativeFailureShouldNotCopyDropOrCreateAnything() throws SQLException {
        failRename = true;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.executeUpdate("ALTER TABLE books RENAME TO other IN DATABASE absent"));
            assertTrue(error.getMessage().contains("target database unavailable"));
            assertEquals("source_db", connection.getCatalog());
            assertEquals(Collections.singletonList("renameCollection"), methods);
        }
        assertEquals(1, requests.size());
    }
}
