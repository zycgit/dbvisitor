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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.config.NxnContext;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.nxn.runner.NxnRunner;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class NxnConcurrencyTest {
    @Rule
    public               TemporaryFolder            temporary = new TemporaryFolder();
    private static final AtomicInteger              ACTIVE    = new AtomicInteger();
    private static final AtomicInteger              PEAK      = new AtomicInteger();
    private static final Map<String, AtomicInteger> METHODS   = new ConcurrentHashMap<>();
    private static final List<String>               EVENTS    = new CopyOnWriteArrayList<>();
    private static       CyclicBarrier              rendezvous;
    private static       CountDownLatch             progressReady;
    private static       CountDownLatch             progressRelease;

    @Before
    public void reset() {
        ACTIVE.set(0);
        PEAK.set(0);
        METHODS.clear();
        EVENTS.clear();
        rendezvous = null;
    }

    @Test
    public void optedInClassesOverlapButUnmarkedClassIsAnExclusiveBarrier() throws Exception {
        rendezvous = new CyclicBarrier(2);
        Path root = temporary.newFolder().toPath();
        List<Class<?>> classes = List.of(First.class, Second.class, Exclusive.class, Third.class, Fourth.class);
        assertTrue(new NxnRunner(root, Map.of("h2", classes), 16, 2, List.of(), null).run());
        assertEquals(2, PEAK.get());
        assertEquals(0, ACTIVE.get());
        assertFalse(NxnContext.active());
        var report = JsonParser.parseString(Files.readString(root.resolve("h2/run.json"))).getAsJsonObject();
        assertEquals(9, report.get("tests").getAsInt());
        assertEquals(9, report.get("passed").getAsInt());
        assertFalse(report.has("currentTests"));
        try (var files = Files.list(root.resolve("h2/test-results"))) {
            assertEquals(5, files.count());
        }
    }

    @Test
    public void defaultClassLimitAllowsMoreThanTwoConcurrentClasses() throws Exception {
        rendezvous = new CyclicBarrier(4);
        Path root = temporary.newFolder().toPath();
        List<Class<?>> classes = List.of(First.class, Second.class, Third.class, Fourth.class);
        assertTrue(new NxnRunner(root, Map.of("h2", classes), 16, List.of(), null).run());
        assertEquals(4, PEAK.get());
        assertEquals(0, ACTIVE.get());
        var session = JsonParser.parseString(Files.readString(root.resolve("session.json"))).getAsJsonObject();
        assertEquals(16, session.get("jobs").getAsInt());
        assertEquals(16, session.get("classJobs").getAsInt());
    }

    @Test
    public void separatedIsolatedClassesRunTogetherBeforeExclusiveClasses() throws Exception {
        rendezvous = new CyclicBarrier(4);
        Path root = temporary.newFolder().toPath();
        List<Class<?>> classes = List.of(First.class, Exclusive.class, Second.class, Third.class, Fourth.class);
        assertTrue(new NxnRunner(root, Map.of("h2", classes), 16, List.of(), null).run());
        assertEquals(4, PEAK.get());
        assertEquals("Exclusive", EVENTS.get(EVENTS.size() - 1));
        assertEquals(9, EVENTS.size());
    }

    @Test
    public void classConcurrencyCanBeDisabledWithoutRemovingAnnotations() throws Exception {
        assertTrue(new NxnRunner(temporary.newFolder().toPath(), Map.of("h2", List.of(First.class, Exclusive.class, Second.class)), 16, 1, List.of(), null).run());
        assertEquals(1, PEAK.get());
        assertEquals(List.of("First", "First", "Exclusive", "Second", "Second"), EVENTS);
    }

    @Test
    public void oneGlobalWorkerAlsoPreservesOriginalClassOrder() throws Exception {
        assertTrue(new NxnRunner(temporary.newFolder().toPath(), Map.of("h2", List.of(First.class, Exclusive.class, Second.class)), 1, 16, List.of(), null).run());
        assertEquals(1, PEAK.get());
        assertEquals(List.of("First", "First", "Exclusive", "Second", "Second"), EVENTS);
    }

    @Test
    public void annotationDoesNotOptInSubclassesOrOtherDatasources() throws Exception {
        assertNull(NotOptedIn.class.getAnnotation(NxnConcurrent.class));
        assertTrue(new NxnRunner(temporary.newFolder().toPath(), Map.of("h2", List.of(NotOptedIn.class, First.class)), 16, 4, List.of(), null).run());
        assertEquals(1, PEAK.get());
    }

    @Test
    public void databaseAndClassConcurrencyShareTheGlobalWorkerBudget() throws Exception {
        Map<String, List<Class<?>>> suites = Map.of("h2", List.of(First.class, Second.class), "pg", List.of(Third.class, Fourth.class));
        assertTrue(new NxnRunner(temporary.newFolder().toPath(), suites, 2, 4, List.of(), null).run());
        assertEquals(2, PEAK.get());
        assertEquals(0, ACTIVE.get());
    }

    @Test
    public void nextFreeWorkerRotatesAcrossDatasourcesInsteadOfRestartingAtTheFirst() throws Exception {
        Map<String, List<Class<?>>> suites = new LinkedHashMap<>();
        suites.put("h2", List.of(First.class, Second.class));
        suites.put("pg", List.of(Third.class, Fourth.class));
        assertTrue(new NxnRunner(temporary.newFolder().toPath(), suites, 1, 16, List.of(), null).run());
        assertEquals(List.of("First", "First", "Third", "Third", "Second", "Second", "Fourth", "Fourth"), EVENTS);
        assertEquals(1, PEAK.get());
    }

    @Test
    public void failedClassCleansUpBeforeTheNextExclusiveClassRuns() throws Exception {
        Path root = temporary.newFolder().toPath();
        assertFalse(new NxnRunner(root, Map.of("h2", List.of(Failing.class, First.class, Exclusive.class)), 16, 2, List.of(), null).run());
        var report = JsonParser.parseString(Files.readString(root.resolve("h2/run.json"))).getAsJsonObject();
        assertEquals(1, report.get("failed").getAsInt());
        assertEquals(0, ACTIVE.get());
    }

    @Test
    public void liveProgressIncludesEveryConcurrentClass() throws Exception {
        Path root = temporary.newFolder().toPath();
        progressReady = new CountDownLatch(2);
        progressRelease = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        var runner = new NxnRunner(root, Map.of("h2", List.of(ProgressFirst.class, ProgressSecond.class)), 16, 2, List.of(), null);
        var result = executor.submit(runner::run);
        try {
            assertTrue(progressReady.await(5, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
            int active = 0;
            while (System.nanoTime() < deadline) {
                var report = JsonParser.parseString(Files.readString(root.resolve("h2/run.json"))).getAsJsonObject();
                if (report.has("currentTests")) {
                    active = report.getAsJsonArray("currentTests").size();
                }
                if (active == 2) {
                    break;
                }
                Thread.sleep(25);
            }
            assertEquals(2, active);
        } finally {
            progressRelease.countDown();
            try {
                assertTrue(result.get(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdownNow();
            }
        }
    }

    public static class Isolated {
        @BeforeClass
        public static void open() throws Exception {
            PEAK.accumulateAndGet(ACTIVE.incrementAndGet(), Math::max);
            if (rendezvous != null) {
                rendezvous.await(5, TimeUnit.SECONDS);
            }
        }

        @AfterClass
        public static void close() throws Exception {
            try {
                Thread.sleep(40); // Class cleanup is part of the exclusive/concurrent scheduling scope.
            } finally {
                ACTIVE.decrementAndGet();
            }
        }

        @Test
        public void first() throws Exception {
            verifySerialMethod();
        }

        @Test
        public void second() throws Exception {
            verifySerialMethod();
        }

        private void verifySerialMethod() throws Exception {
            assertTrue(NxnContext.active());
            EVENTS.add(getClass().getSimpleName());
            AtomicInteger active = METHODS.computeIfAbsent(NxnContext.environment() + getClass().getName(), ignored -> new AtomicInteger());
            assertEquals(1, active.incrementAndGet());
            try {
                Thread.sleep(60);
            } finally {
                active.decrementAndGet();
            }
        }
    }

    @NxnConcurrent
    public static class First extends Isolated {
    }

    @NxnConcurrent
    public static class Second extends Isolated {
    }

    @NxnConcurrent
    public static class Third extends Isolated {
    }

    @NxnConcurrent
    public static class Fourth extends Isolated {
    }

    public static class NotOptedIn extends First {
    }

    public static class Exclusive {
        @BeforeClass
        public static void open() {
            assertEquals(0, ACTIVE.get());
        }

        @Test
        public void runsAlone() {
            assertEquals(0, ACTIVE.get());
            EVENTS.add("Exclusive");
        }

        @AfterClass
        public static void close() {
            assertEquals(0, ACTIVE.get());
        }
    }

    @NxnConcurrent
    public static class Failing extends Isolated {
        @Test
        public void failure() {
            fail("Expected fixture failure");
        }
    }

    public static class Progress {
        @Test
        public void waits() throws Exception {
            progressReady.countDown();
            assertTrue(progressRelease.await(15, TimeUnit.SECONDS));
        }
    }

    @NxnConcurrent
    public static class ProgressFirst extends Progress {
    }

    @NxnConcurrent
    public static class ProgressSecond extends Progress {
    }
}
