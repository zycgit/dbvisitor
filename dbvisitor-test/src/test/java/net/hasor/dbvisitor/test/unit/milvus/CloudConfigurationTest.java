/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.milvus;

import java.util.Map;
import java.util.Properties;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.test.realdb.milvus_cloud.CloudTestSupport;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import static net.hasor.dbvisitor.test.realdb.milvus_cloud.CloudTestSupport.*;
import static org.junit.Assert.*;

public class CloudConfigurationTest {
    @Test
    public void endpointShouldUseHttpsPortAndAnExplicitDatabase() {
        Map<String, String> env = Map.of(ENDPOINT, "https://cluster.example", DATABASE, "application_db");
        assertEquals("jdbc:dbvisitor:milvus://cluster.example:443/application_db", jdbcUrl(env));
        env = Map.of(ENDPOINT, "https://cluster.example:19530/", DATABASE, "application_db");
        assertEquals("jdbc:dbvisitor:milvus://cluster.example:19530/application_db", jdbcUrl(env));
        assertThrows(IllegalArgumentException.class, () -> jdbcUrl(Map.of(ENDPOINT, "https://cluster.example")));
    }

    @Test
    public void unsafeEndpointsShouldFailWithoutEchoingTheirContents() {
        for (String endpoint : new String[] { "http://cluster.example", "https://secret@cluster.example", "https://cluster.example/path",
                "https://cluster.example?token=secret", "https://cluster.example/#secret", "https://cluster.example:70000", "not a uri secret" }) {
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                    () -> jdbcUrl(Map.of(ENDPOINT, endpoint, DATABASE, "db")));
            assertFalse(failure.getMessage().contains("secret"));
            assertNull(failure.getCause());
        }
        assertThrows(IllegalArgumentException.class, () -> jdbcUrl(Map.of(ENDPOINT, "https://cluster.example", DATABASE, "db?token=secret")));
    }

    @Test
    public void credentialsShouldStayInPropertiesAndRemainUnmodified() {
        String password = "space + !?[] & suffix ";
        Properties props = connectionProperties(Map.of(USER, "test_user", PASSWORD, password));
        assertEquals(password, props.getProperty(MilvusKeys.PASSWORD));
        assertEquals("true", props.getProperty(MilvusKeys.SECURE));
        assertFalse(props.containsKey(MilvusKeys.TOKEN));
        props = connectionProperties(Map.of(TOKEN, "test-token", USER, "test_user", PASSWORD, password));
        assertEquals("test-token", props.getProperty(MilvusKeys.TOKEN));
        assertEquals(password, props.getProperty(MilvusKeys.PASSWORD));
    }

    @Test
    public void missingAuthenticationShouldFailInsteadOfSkipping() {
        assertThrows(IllegalArgumentException.class, () -> connectionProperties(Map.of()));
        assertThrows(IllegalArgumentException.class, () -> connectionProperties(Map.of(USER, "test_user")));
        assertThrows(IllegalArgumentException.class, () -> connectionProperties(Map.of(TOKEN, "test-token", PASSWORD, "secret")));
        assertNotNull(connectionProperties(Map.of(TOKEN, "test-token")));
    }

    @Test
    public void cloudTestsShouldRequireExplicitOptIn() {
        String previous = System.getProperty("milvus.cloud");
        try {
            System.clearProperty("milvus.cloud");
            assertThrows(AssumptionViolatedException.class, CloudTestSupport::requireExplicitOptIn);
            System.setProperty("milvus.cloud", "false");
            assertThrows(AssumptionViolatedException.class, CloudTestSupport::requireExplicitOptIn);
            System.setProperty("milvus.cloud", "true");
            CloudTestSupport.requireExplicitOptIn();
        } finally {
            if (previous == null) {
                System.clearProperty("milvus.cloud");
            } else {
                System.setProperty("milvus.cloud", previous);
            }
        }
    }
}
