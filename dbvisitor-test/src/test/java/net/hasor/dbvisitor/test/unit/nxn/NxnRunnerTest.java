/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.nxn;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.NxnContext;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.H2Profile;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.nxn.report.NxnTestResult;
import net.hasor.dbvisitor.test.nxn.runner.NxnRunner;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

public class NxnRunnerTest {
    @Rule
    public               TemporaryFolder   temporary = new TemporaryFolder();
    private static final Map<String, Long> THREADS   = new ConcurrentHashMap<>();
    private static final AtomicInteger     ACTIVE    = new AtomicInteger();
    private static final AtomicInteger     PEAK      = new AtomicInteger();
    private static       CyclicBarrier     barrier;

    @Test
    public void databasesRunConcurrentlyButMethodsRemainSerialAndIsolated() throws Exception {
        THREADS.clear();
        ACTIVE.set(0);
        PEAK.set(0);
        barrier = new CyclicBarrier(3);
        Path root = temporary.newFolder().toPath();
        Map<String, List<Class<?>>> suites = new LinkedHashMap<>();
        for (String env : List.of("h2", "pg", "redis")) {
            suites.put(env, List.of(ParallelFixture.class));
        }
        assertTrue(new NxnRunner(root, suites, 16, List.of(), null).run());
        assertEquals(3, PEAK.get());
        assertEquals(3, THREADS.values().stream().distinct().count());
        assertFalse(NxnContext.active());
        for (String env : suites.keySet()) {
            JsonObject report = json(root.resolve(env + "/run.json"));
            assertEquals(2, report.get("tests").getAsInt());
            assertEquals(2, report.get("total").getAsInt());
            assertEquals(2, report.get("passed").getAsInt());
            assertTrue(report.get("complete").getAsBoolean());
            String log = Files.readString(root.resolve(env + "/execution.log"));
            assertTrue(log.contains("output-" + env));
            for (String other : suites.keySet()) {
                if (!other.equals(env)) {
                    assertFalse(log.contains("output-" + other));
                }
            }
            try (var results = Files.list(root.resolve(env + "/cases"))) {
                JsonObject result = json(results.findFirst().orElseThrow());
                assertEquals(env, result.get("env").getAsString());
                assertEquals(report.get("runId"), result.get("runId"));
            }
        }
    }

    @Test
    public void workerLimitOneQueuesDatasourcesAndRestoresContext() throws Exception {
        THREADS.clear();
        ACTIVE.set(0);
        PEAK.set(0);
        barrier = null;
        Path root = temporary.newFolder().toPath();
        assertTrue(new NxnRunner(root, Map.of("h2", List.of(ParallelFixture.class), "pg", List.of(ParallelFixture.class)), 1, List.of(), null).run());
        assertEquals(1, PEAK.get());
        assertEquals(1, THREADS.values().stream().distinct().count());
        assertFalse(NxnContext.active());
    }

    @Test
    public void failuresAndEmptySelectionsDoNotStopOtherDatabases() throws Exception {
        Path root = temporary.newFolder().toPath();
        assertFalse(new NxnRunner(root, Map.of("h2", List.of(FailureFixture.class), "pg", List.of(FailureFixture.class), "redis", List.of()), 16, List.of(), null).run());
        assertTrue(json(root.resolve("h2/run.json")).get("complete").getAsBoolean());
        assertEquals(1, json(root.resolve("pg/run.json")).get("failed").getAsInt());
        assertFalse(json(root.resolve("redis/run.json")).get("complete").getAsBoolean());
        assertFalse(json(root.resolve("summary.json")).get("complete").getAsBoolean());
    }

    @Test
    public void filtersParameterizedMethodsAndPreservesIgnoredTests() throws Exception {
        Path root = temporary.newFolder().toPath();
        List<Class<?>> classes = List.of(ParameterFixture.class, IgnoredFixture.class);
        assertTrue(new NxnRunner(root, Map.of("h2", classes), 16, List.of("*ParameterFixture.keep*"), null).run());
        assertEquals(2, json(root.resolve("h2/run.json")).get("tests").getAsInt());
        assertEquals(2, json(root.resolve("h2/run.json")).get("total").getAsInt());
        assertFalse(new NxnRunner(root, Map.of("h2", classes), 16, List.of("*missing*"), null).run());
        assertEquals(0, json(root.resolve("h2/run.json")).get("tests").getAsInt());
        assertEquals(0, json(root.resolve("h2/run.json")).get("total").getAsInt());
        assertTrue(new NxnRunner(root, Map.of("h2", classes), 16, List.of(), null).run());
        assertEquals(5, json(root.resolve("h2/run.json")).get("tests").getAsInt());
        assertEquals(5, json(root.resolve("h2/run.json")).get("total").getAsInt());
        assertEquals(4, json(root.resolve("h2/run.json")).get("passed").getAsInt());
        assertEquals(1, json(root.resolve("h2/run.json")).get("skipped").getAsInt());
        try (var files = Files.list(root.resolve("h2/test-results"))) {
            for (Path path : files.toList()) {
                DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(path.toFile());
            }
        }
        assertTrue(Files.readString(root.resolve("h2/reports/index.html")).contains("ParameterFixture"));
    }

    @Test
    public void liveProgressIncludesMethodResultsBeforeTheClassFinishes() throws Exception {
        Path root = temporary.newFolder().toPath();
        LiveProgressFixture.ready = new CountDownLatch(1);
        LiveProgressFixture.release = new CountDownLatch(1);
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        var executor = Executors.newSingleThreadExecutor();
        try (PrintStream console = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(console);
            NxnRunner runner = new NxnRunner(root, Map.of("h2", List.of(LiveProgressFixture.class)), 16, List.of(), null);
            var result = executor.submit(runner::run);
            try {
                assertTrue("Fixture did not reach the blocking method", LiveProgressFixture.ready.await(5, TimeUnit.SECONDS));
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
                String output;
                do {
                    Thread.sleep(25);
                    output = captured.toString(StandardCharsets.UTF_8);
                } while (output.split("NxN progress", -1).length < 3 && System.nanoTime() < deadline);
                assertTrue(output, output.split("NxN progress", -1).length >= 3);
                assertTrue(output, output.contains("4/5 | PASS=1 FAIL=1 SKIP=2"));
                assertTrue(output, output.contains("current=NxnRunnerTest$LiveProgressFixture#e_block"));
                assertEquals(output, 1, output.split("PID=", -1).length - 1);
                JsonObject live = json(root.resolve("h2/run.json"));
                assertFalse(live.get("complete").getAsBoolean());
                assertEquals("running", live.get("status").getAsString());
                assertEquals(4, live.get("tests").getAsInt());
                assertEquals(5, live.get("total").getAsInt());
                assertEquals(1, live.get("passed").getAsInt());
                assertEquals(1, live.get("failed").getAsInt());
                assertEquals(2, live.get("skipped").getAsInt());
                assertTrue(live.get("averageMillis").getAsDouble() >= 10);
                assertTrue(live.get("currentTestMillis").getAsLong() >= 5000);
                JsonObject summary = json(root.resolve("summary.json"));
                assertEquals(4, summary.getAsJsonArray("datasources").get(0).getAsJsonObject().get("tests").getAsInt());
                assertFalse(result.isDone());
            } finally {
                LiveProgressFixture.release.countDown();
                assertFalse(result.get(5, TimeUnit.SECONDS));
            }
            JsonObject finished = json(root.resolve("h2/run.json"));
            assertEquals(5, finished.get("tests").getAsInt());
            assertEquals(2, finished.get("passed").getAsInt());
            assertFalse(finished.has("currentTest"));
            String output = captured.toString(StandardCharsets.UTF_8);
            assertTrue(output, output.contains("NxN summary"));
            assertTrue(output, output.contains("5/5 | PASS=2 FAIL=1 SKIP=2"));
            assertFalse(output, output.contains("{env="));
        } finally {
            executor.shutdownNow();
            System.setOut(original);
        }
    }

    @Test
    public void classSetupFailureIsCountedWithoutInventingMethodResults() throws Exception {
        Path root = temporary.newFolder().toPath();
        assertFalse(new NxnRunner(root, Map.of("h2", List.of(ClassSetupFailureFixture.class)), 16, List.of(), null).run());
        JsonObject report = json(root.resolve("h2/run.json"));
        assertEquals(2, report.get("total").getAsInt());
        assertEquals(1, report.get("tests").getAsInt());
        assertEquals(1, report.get("failed").getAsInt());
        assertEquals(0, report.get("passed").getAsInt());
    }

    @Test
    public void rejectsFilteredDocumentationAndUnsafeDatasourceNames() throws Exception {
        Path root = temporary.newFolder().toPath();
        assertThrows(IllegalArgumentException.class, () -> new NxnRunner(root, Map.of("h2", List.of()), 0, List.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new NxnRunner(root, Map.of("../other", List.of()), 16, List.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new NxnRunner(root, Map.of("h2", List.of()), 16, List.of("*"), root));
        assertThrows(IllegalArgumentException.class, () -> new NxnRunner(root, Map.of("h2", List.of()), 16, 0, List.of(), null));
    }

    @Test
    public void refusesToClearUnrelatedFilesInACustomOutputDirectory() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path file = Files.createDirectories(root.resolve("h2")).resolve("user-file.txt");
        Files.writeString(file, "keep this file");
        assertThrows(IllegalArgumentException.class, () -> new NxnRunner(root, Map.of("h2", List.of(ParallelFixture.class)), 1, List.of(), null).run());
        assertEquals("keep this file", Files.readString(file));
    }

    @Test
    public void fixtureConfigurationDoesNotModifyGlobalHandlersOrMacros() throws Exception {
        var originalMacro = MacroRegistry.DEFAULT.findMacro("currentTimestamp");
        boolean originalHandler = TypeHandlerRegistry.DEFAULT.hasTypeHandler(UserInfo.class);
        IsolatedConfigurationFixture fixture = new IsolatedConfigurationFixture();
        fixture.setupWithoutDatabase();
        assertSame(originalMacro, MacroRegistry.DEFAULT.findMacro("currentTimestamp"));
        Configuration one = fixture.configuration();
        Configuration two = fixture.configuration();
        one.getTypeRegistry().register(UserInfo.class, new JsonTypeHandler(UserInfo.class));
        assertTrue(one.getTypeRegistry().hasTypeHandler(UserInfo.class));
        assertFalse(two.getTypeRegistry().hasTypeHandler(UserInfo.class));
        assertEquals(originalHandler, TypeHandlerRegistry.DEFAULT.hasTypeHandler(UserInfo.class));
    }

    @Test
    public void concurrentFixturesUseIndependentMappingRegistries() throws Exception {
        var first = new ConcurrentConfigurationFixture();
        var second = new ConcurrentConfigurationFixture();
        var serial = new IsolatedConfigurationFixture();
        first.setupWithoutDatabase();
        second.setupWithoutDatabase();
        serial.setupWithoutDatabase();
        assertNotSame(first.registry(), second.registry());
        assertNotSame(MappingRegistry.DEFAULT, first.registry());
        assertSame(MappingRegistry.DEFAULT, serial.registry());
    }

    @Test
    public void terminatingJvmStopsBlockedTestAndBackgroundThreads() throws Exception {
        Path root = temporary.newFolder().toPath();
        String javaCommand = ProcessHandle.current().info().command().orElseThrow();
        // URLClassLoader is used by Gradle test workers; capture its resolved runtime for the child probe.
        ClassLoader loader = getClass().getClassLoader();
        String classpath = loader instanceof java.net.URLClassLoader urls ? java.util.Arrays.stream(urls.getURLs()).map(url -> Path.of(java.net.URI.create(url.toString())).toString()).collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator)) : System.getProperty("java.class.path");
        Process process = new ProcessBuilder(javaCommand, "-cp", classpath, CancellationProbe.class.getName(), root.toString()).redirectErrorStream(true).redirectOutput(root.resolve("process.log").toFile()).start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (!Files.exists(root.resolve("h2/ready")) && process.isAlive() && System.nanoTime() < deadline) {
                Thread.sleep(25);
            }
            assertTrue(Files.readString(root.resolve("process.log")), Files.exists(root.resolve("h2/ready")));
            assertEquals(0, process.descendants().count());
            process.destroy();
            assertTrue("JVM did not exit after TERM", process.waitFor(5, TimeUnit.SECONDS));
            assertNotEquals(0, process.exitValue());
            assertFalse(json(root.resolve("summary.json")).get("complete").getAsBoolean());
            assertEquals("cancelled", json(root.resolve("h2/run.json")).get("status").getAsString());
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class ParallelFixture {
        @Test
        public void first() throws Exception {
            String env = OneApiDataSourceManager.getDbDialect();
            THREADS.put(env, Thread.currentThread().getId());
            PEAK.accumulateAndGet(ACTIVE.incrementAndGet(), Math::max);
            try {
                if (barrier != null) {
                    barrier.await(5, TimeUnit.SECONDS);
                }
                assertNotNull(OneApiDataSourceManager.getProperty("jdbc.url"));
                System.out.println("output-" + env);
                AtomicReference<String> inherited = new AtomicReference<>();
                Thread child = new Thread(() -> inherited.set(NxnContext.environment()));
                child.start();
                child.join();
                assertEquals(env, inherited.get());
                NxnTestResult.write(env, Description.createTestDescription(getClass(), "first"), null, "passed", null);
            } finally {
                ACTIVE.decrementAndGet();
            }
        }

        @Test
        public void second() {
            assertEquals(THREADS.get(NxnContext.environment()).longValue(), Thread.currentThread().getId());
        }
    }

    public static class FailureFixture {
        @Test
        public void check() {
            assertNotEquals("intentional fixture failure", "pg", NxnContext.environment());
        }
    }

    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class LiveProgressFixture {
        private static CountDownLatch ready;
        private static CountDownLatch release;

        @Test
        public void a_pass() throws Exception {
            Thread.sleep(25);
        }

        @Test
        public void b_fail() {
            fail("intentional fixture failure");
        }

        @Test
        public void c_assume() {
            Assume.assumeTrue("intentional fixture skip", false);
        }

        @Ignore
        @Test
        public void d_ignore() {
            fail("Must stay ignored");
        }

        @Test
        public void e_block() throws Exception {
            ready.countDown();
            assertTrue(release.await(20, TimeUnit.SECONDS));
        }
    }

    public static class ClassSetupFailureFixture {
        @BeforeClass
        public static void setup() {
            fail("intentional class setup failure");
        }

        @Test
        public void first() {
            fail("Must not run");
        }

        @Test
        public void second() {
            fail("Must not run");
        }
    }

    @RunWith(Parameterized.class)
    public static class ParameterFixture {
        @Parameterized.Parameters
        public static List<Object[]> parameters() {
            return List.of(new Object[] { 1 }, new Object[] { 2 });
        }

        public ParameterFixture(int ignored) {
        }

        @Test
        public void keep() {
        }

        @Test
        public void extra() {
        }
    }

    @Ignore
    public static class IgnoredFixture {
        @Test
        public void ignored() {
            fail("Must stay ignored");
        }
    }

    public static class CancellationProbe {
        public static void main(String[] args) throws Exception {
            new NxnRunner(Path.of(args[0]), Map.of("h2", List.of(CancellationProbe.class)), 16, List.of(), null).run();
        }

        @Test
        public void blockingTest() throws Exception {
            Thread background = new Thread(() -> {
                while (true) {
                    try {
                        new CountDownLatch(1).await();
                    } catch (InterruptedException ignored) {
                        // Simulate an SDK background thread which does not stop on interruption.
                    }
                }
            }, "simulated-sdk-background");
            background.setDaemon(false);
            background.start();
            Files.writeString(NxnContext.results().resolve("ready"), "ready");
            new CountDownLatch(1).await();
        }
    }

    private static class IsolatedConfigurationFixture extends AbstractNxnContractTest {
        @Override
        protected DataSourceProfile profile() {
            return H2Profile.INSTANCE;
        }

        void setupWithoutDatabase() throws Exception {
            dataSource = (DataSource) java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DataSource.class }, (proxy, method, arguments) -> {
                throw new AssertionError("Must not access a database");
            });
            try (NxnContext ignored = new NxnContext("h2", null, null).enter()) {
                super.setup();
            }
        }

        Configuration configuration() {
            return newConfiguration();
        }

        MappingRegistry registry() {
            assertSame(jdbcTemplate.getRegistry(), lambdaTemplate.getRegistry());
            return jdbcTemplate.getRegistry();
        }

        @Override
        protected void ensureSchemaExists() {
        }

        @Override
        protected void cleanTestData() {
        }
    }

    @NxnConcurrent
    private static class ConcurrentConfigurationFixture extends IsolatedConfigurationFixture {
    }
}
