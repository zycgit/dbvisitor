/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.nxn;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import com.google.gson.Gson;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import net.hasor.dbvisitor.test.nxn.env.*;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.report.NxnTestResult;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import static org.junit.Assert.*;

public class NxnRecorderTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void capturesPassesLimitsUnknownSkipsAndFailures() throws Exception {
        Map<String, NxnTestResult> results = execute("h2");
        assertEquals("passed", results.get("passes").outcome);
        assertEquals("passed", results.get("expectedException").outcome);
        assertEquals("unsupported", results.get("featureLimit").outcome);
        assertTrue(results.get("featureLimit").reason.contains("vector"));
        assertEquals("unsupported", results.get("declaredLimit").outcome);
        assertEquals("unverified", results.get("unknownSkip").outcome);
        assertEquals("failed", results.get("fails").outcome);
        assertEquals(NxnTestResult.fingerprint(), results.get("passes").fingerprint);
    }

    @Test
    public void wrongEnvironmentIsNotAnUnsupportedCapability() throws Exception {
        for (NxnTestResult result : execute("redis").values()) {
            assertEquals("unverified", result.outcome);
        }
    }

    @Test
    public void builderUsageDifferencesDoNotSkipApiContracts() {
        assertEquals(SupportStatus.SUPPORTED, OracleProfile.INSTANCE.support(CapabilityId.LAMBDA_EMPTY_STRING_VS_NULL));
        assertEquals(SupportStatus.SUPPORTED, OracleProfile.INSTANCE.support(CapabilityId.LAMBDA_PREDICATE_IN_LARGE));
        assertFalse(OracleProfile.INSTANCE.supportsFeature(FeatureId.LARGE_IN_LIST));
        assertEquals(SupportStatus.SUPPORTED, MsSqlProfile.INSTANCE.support(CapabilityId.LAMBDA_SORT_REPEATED_COLUMN));
        assertFalse(MsSqlProfile.INSTANCE.supportsFeature(FeatureId.REPEATED_ORDER_BY_COLUMN));
        assertEquals(SupportStatus.SUPPORTED, ClickHouseProfile.INSTANCE.support(CapabilityId.LAMBDA_SPECIAL_LENGTH_CONSTRAINT));
        assertEquals(SupportStatus.SUPPORTED, Elastic7Profile.INSTANCE.support(CapabilityId.LAMBDA_SPECIAL_LENGTH_CONSTRAINT));
        assertEquals(SupportStatus.SUPPORTED, MongoProfile.INSTANCE.support(CapabilityId.LAMBDA_PREDICATE_NOT_IN_NULL));
        assertEquals(SupportStatus.SUPPORTED, MilvusProfile.INSTANCE.support(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_CHUNKS));
        assertEquals(SupportStatus.SUPPORTED, MilvusProfile.INSTANCE.support(CapabilityId.LAMBDA_EDGE_QUERY_MULTI_RESULT));
        assertEquals(SupportStatus.SUPPORTED, MilvusProfile.INSTANCE.support(CapabilityId.LAMBDA_SECURITY_APPLY_SCOPED_TRUE));

        // Native-usage adjustments do not enable genuinely missing dialect operations.
        assertEquals(SupportStatus.UNSUPPORTED_BY_DBVISITOR, MongoProfile.INSTANCE.support(CapabilityId.LAMBDA_SORT_NULL_FIRST_ASC));
        assertEquals(SupportStatus.UNSUPPORTED_BY_DATABASE, MilvusProfile.INSTANCE.support(CapabilityId.LAMBDA_QUERY_ORDER));
    }

    private Map<String, NxnTestResult> execute(String env) throws Exception {
        Path directory = temporary.newFolder().toPath();
        Map<String, String> properties = Map.of("nxn.env", env, "nxn.resultDir", directory.toString(), "nxn.runId", "unit-run");
        Map<String, String> previous = new HashMap<>();
        try {
            for (Map.Entry<String, String> property : properties.entrySet()) {
                previous.put(property.getKey(), System.getProperty(property.getKey()));
                System.setProperty(property.getKey(), property.getValue());
            }
            Result execution = JUnitCore.runClasses(RecordingFixture.class);
            assertEquals("h2".equals(env) ? 1 : 0, execution.getFailureCount());
        } finally {
            for (Map.Entry<String, String> property : previous.entrySet()) {
                if (property.getValue() == null) {
                    System.clearProperty(property.getKey());
                } else {
                    System.setProperty(property.getKey(), property.getValue());
                }
            }
        }
        Map<String, NxnTestResult> results = new HashMap<>();
        try (Stream<Path> files = Files.list(directory.resolve("cases"))) {
            for (Path file : files.toList()) {
                NxnTestResult result = new Gson().fromJson(Files.readString(file), NxnTestResult.class);
                results.put(result.method, result);
                assertEquals("unit-run", result.runId);
            }
        }
        assertEquals(6, results.size());
        return results;
    }

    public static class RecordingFixture extends AbstractNxnContractTest {
        @Override
        @Before
        public void setup() {
            // These fixtures exercise JUnit reporting, not a database connection.
        }

        @Override
        protected DataSourceProfile profile() {
            return H2Profile.INSTANCE;
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void passes() {
            assertTrue(true);
        }

        @Test(expected = IllegalArgumentException.class)
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void expectedException() {
            throw new IllegalArgumentException("Expected command validation failure");
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void featureLimit() {
            requiresNxnFeature(FeatureId.VECTOR);
            fail("H2 must reject the vector feature gate");
        }

        @Test
        @Capability(CapabilityId.VECTOR_KNN_ORDER_L2)
        public void declaredLimit() {
            fail("An unsupported capability must be skipped before the body runs");
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void unknownSkip() {
            Assume.assumeTrue("Unclassified environment issue", false);
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void fails() {
            fail("Intentional fixture failure");
        }
    }
}
