package net.hasor.dbvisitor.test.realdb.milvus;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

import org.junit.Test;

import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;

/** Milvus connection tests: missing or misconfigured plaintext/TLS/mTLS services fail, never skip. */
public class MilvusTlsConnectionTest {
    private static String certificate(String name) {
        Path directory = Paths.get("docker/certs");
        if (!Files.isDirectory(directory)) {
            directory = Paths.get("dbvisitor-test/docker/certs");
        }
        return directory.resolve(name).toAbsolutePath().normalize().toString();
    }

    private static Properties properties(boolean mutual) {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.SECURE, "true");
        properties.setProperty(MilvusKeys.CA_PEM_PATH, certificate("ca.crt"));
        properties.setProperty(MilvusKeys.SERVER_NAME, "localhost");
        properties.setProperty(MilvusKeys.CONNECT_TIMEOUT, "2500");
        properties.setProperty(MilvusKeys.RPC_DEADLINE, "5000");
        if (mutual) {
            properties.setProperty(MilvusKeys.CLIENT_PEM_PATH, certificate("client.crt"));
            properties.setProperty(MilvusKeys.CLIENT_KEY_PATH, certificate("client.key"));
        }
        return properties;
    }

    private static Connection connect(boolean mutual, Properties properties) throws SQLException {
        int port = mutual ? 2955 : 2954;
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://127.0.0.1:" + port + "/default", properties);
    }

    private void roundTrip(int port, Properties properties) throws Exception {
        String table = "jdbc_tls_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://127.0.0.1:" + port + "/default", properties); Statement statement = connection.createStatement()) {
            assertTrue(connection.getMetaData().getDatabaseProductVersion().contains("2.6."));
            statement.executeUpdate("CREATE TABLE " + table + " (id INT64 PRIMARY KEY, embedding FLOAT_VECTOR(2))");
            try {
                assertEquals(1, statement.executeUpdate("INSERT INTO " + table + " (id, embedding) VALUES (1, [0.1, 0.2])"));
                try (ResultSet jobs = statement.executeQuery("SHOW IMPORTS FROM " + table)) {
                    assertEquals("JOB_ID", jobs.getMetaData().getColumnLabel(1));
                    assertFalse(jobs.next());
                }
            } finally {
                statement.executeUpdate("DROP TABLE " + table);
            }
        }
    }

    @Test
    public void oneWayTlsReadsWritesAndObservesImportJobs() throws Exception {
        roundTrip(2954, properties(false));
    }

    @Test
    public void mutualTlsReadsWritesAndObservesImportJobs() throws Exception {
        roundTrip(2955, properties(true));
    }

    @Test
    public void plaintextReadsWritesAndObservesImportJobsOnTheSamePort() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CONNECT_TIMEOUT, "2500");
        properties.setProperty(MilvusKeys.RPC_DEADLINE, "5000");
        roundTrip(2956, properties);
    }

    @Test
    public void rejectsWrongServerName() throws Exception {
        Properties properties = properties(false);
        properties.setProperty(MilvusKeys.SERVER_NAME, "wrong.invalid");
        try (Connection ignored = connect(false, properties)) {
            fail("Expected hostname verification failure");
        } catch (SQLException expected) {
            assertNotNull(expected.getCause());
        }
    }

    @Test
    public void rejectsMissingClientCertificate() throws Exception {
        Properties properties = properties(false);
        try (Connection ignored = connect(true, properties)) {
            fail("Expected mutual TLS authentication failure");
        } catch (SQLException expected) {
            assertNotNull(expected.getCause());
        }
    }

    @Test
    public void rejectsUntrustedServerAndPlaintext() throws Exception {
        for (boolean secure : new boolean[] { true, false }) {
            Properties properties = new Properties();
            properties.setProperty(MilvusKeys.SECURE, Boolean.toString(secure));
            properties.setProperty(MilvusKeys.CONNECT_TIMEOUT, "2000");
            properties.setProperty(MilvusKeys.RPC_DEADLINE, "2000");
            try (Connection ignored = connect(false, properties)) {
                fail("Expected rejected connection, secure=" + secure);
            } catch (SQLException expected) {
                assertNotNull(expected.getCause());
            }
        }
    }
}
