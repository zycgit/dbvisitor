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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assertions;

public class NxnRecorderTest {
    @TempDir
    public Path temporary;

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

    @Test
    public void setupFailureIsRecordedAsFailure() throws Exception {
        // A failing setup must still produce its observed contract result.
        var results = execute("h2", SetupFailureFixture.class, 1, 1);
        assertEquals("failed", results.get("check").outcome);
        assertTrue(results.get("check").reason.contains("setup failure"));
    }

    @Test
    public void cleanupFailureIsRecordedAsFailure() throws Exception {
        // Recording follows user cleanup, so its failure cannot become a passing capability.
        var results = execute("h2", CleanupFailureFixture.class, 1, 1);
        assertEquals("failed", results.get("check").outcome);
        assertTrue(results.get("check").reason.contains("cleanup failure"));
    }

    @Test
    public void parameterizedClassInvocationsProduceDistinctObservations() throws Exception {
        // Both constructor invocations must be recorded without overwriting one method's result.
        var results = execute("h2", ParameterRecordingFixture.class, 0, 2);
        assertEquals(2, results.keySet().stream().filter(name -> name.startsWith("check[")).count());
        assertTrue(results.values().stream().allMatch(result -> "passed".equals(result.outcome)));
    }

    private Map<String, NxnTestResult> execute(String env) throws Exception {
        return execute(env, RecordingFixture.class, "h2".equals(env) ? 1 : 0, 6);
    }

    private Map<String, NxnTestResult> execute(String env, Class<?> fixture, int failures, int count) throws Exception {
        Path directory = Files.createTempDirectory(temporary, "run-");
        Map<String, String> properties = Map.of("nxn.env", env, "nxn.resultDir", directory.toString(), "nxn.runId", "unit-run");
        Map<String, String> previous = new HashMap<>();
        try {
            for (Map.Entry<String, String> property : properties.entrySet()) {
                previous.put(property.getKey(), System.getProperty(property.getKey()));
                System.setProperty(property.getKey(), property.getValue());
            }
            SummaryGeneratingListener execution = new SummaryGeneratingListener();
            var request = LauncherDiscoveryRequestBuilder.request().selectors(selectClass(fixture)).build();
            LauncherFactory.create().execute(request, execution);
            assertEquals(failures, execution.getSummary().getTestsFailedCount());
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
        assertEquals(count, results.size());
        return results;
    }

    public static class RecordingFixture extends AbstractNxnContractTest {
        @Override
        @BeforeEach
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

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void expectedException() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                throw new IllegalArgumentException("Expected command validation failure");
            });
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
            Assumptions.assumeTrue(false, "Unclassified environment issue");
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void fails() {
            fail("Intentional fixture failure");
        }
    }
    public static class SetupFailureFixture extends AbstractNxnContractTest {
        @Override
        protected DataSourceProfile profile() {
            return H2Profile.INSTANCE;
        }

        @Override
        @BeforeEach
        public void setup() {
            throw new IllegalStateException("setup failure");
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void check() {
            fail("Setup must prevent this method from running");
        }
    }

    public static class CleanupFailureFixture extends AbstractNxnContractTest {
        @Override
        protected DataSourceProfile profile() {
            return H2Profile.INSTANCE;
        }

        @Override
        @BeforeEach
        public void setup() {
            // This fixture exercises lifecycle recording without a database.
        }

        @AfterEach
        public void cleanup() {
            throw new IllegalStateException("cleanup failure");
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void check() {
            assertTrue(true);
        }
    }

    @ParameterizedClass(quoteTextArguments = false, autoCloseArguments = false)
    @MethodSource("parameters")
    public static class ParameterRecordingFixture extends AbstractNxnContractTest {
        public static java.util.List<Object[]> parameters() {
            return java.util.List.of(new Object[] { 1 }, new Object[] { 2 });
        }

        public ParameterRecordingFixture(int ignored) {
        }

        @Override
        protected DataSourceProfile profile() {
            return H2Profile.INSTANCE;
        }

        @Override
        @BeforeEach
        public void setup() {
            // This fixture exercises parameter identities without a database.
        }

        @Test
        @Capability(CapabilityId.JDBC_CRUD_INSERT)
        public void check() {
            assertTrue(true);
        }
    }

}
