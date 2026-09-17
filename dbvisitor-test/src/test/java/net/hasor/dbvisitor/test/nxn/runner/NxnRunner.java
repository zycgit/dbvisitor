/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.runner;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.hasor.dbvisitor.test.nxn.config.NxnContext;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.junit.NxnConcurrent;
import net.hasor.dbvisitor.test.nxn.report.NxnDocumentation;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

/** One JVM and a global worker budget; only explicitly isolated classes may overlap within a datasource. */
public final class NxnRunner {
    private static final int                         DEFAULT_JOBS              = 16;
    private static final int                         DEFAULT_CLASS_JOBS        = 16;
    private static final int                         PROGRESS_INTERVAL_SECONDS = 5;
    private final        Path                        root;
    private final        Map<String, List<Class<?>>> suites;
    private final        int                         jobs;
    private final        int                         classJobs;
    private final        List<Pattern>               filters;
    private final        Path                        documentation;
    private final        String                      runId                     = UUID.randomUUID().toString();
    private final        Map<String, NxnReport>      reports                   = new LinkedHashMap<>();
    private final        AtomicBoolean               cancelled                 = new AtomicBoolean();
    private final        PrintStream                 console                   = System.out;
    private              long                        startedAt;
    private              long                        startedNanos;

    public NxnRunner(Path root, Map<String, List<Class<?>>> suites, int jobs, List<String> filters, Path documentation) {
        this(root, suites, jobs, DEFAULT_CLASS_JOBS, filters, documentation);
    }

    public NxnRunner(Path root, Map<String, List<Class<?>>> suites, int jobs, int classJobs, List<String> filters, Path documentation) {
        if (jobs < 1 || classJobs < 1 || suites.isEmpty() || suites.keySet().stream().anyMatch(env -> !env.matches("[a-z][a-z0-9]*"))) {
            throw new IllegalArgumentException("Expected datasource suites and a positive worker count");
        }
        if (documentation != null && !filters.isEmpty()) {
            throw new IllegalArgumentException("Filtered test runs cannot update the compatibility matrix");
        }
        this.root = root;
        this.suites = new LinkedHashMap<>();
        suites.forEach((env, classes) -> this.suites.put(env, List.copyOf(classes)));
        this.jobs = jobs;
        this.classJobs = classJobs;
        this.filters = filters.stream().map(NxnRunner::glob).toList();
        this.documentation = documentation;
    }

    public static void main(String[] args) {
        int exitCode = 1;
        try {
            String target = args.length == 0 ? "all" : args[0];
            int jobs = DEFAULT_JOBS;
            int classJobs = DEFAULT_CLASS_JOBS;
            Path output = Path.of("build/nxn");
            List<String> filters = new ArrayList<>();
            Path documentation = null;
            for (int index = 1; index < args.length; index++) {
                switch (args[index]) {
                    case "--jobs" -> jobs = Integer.parseInt(args[++index]);
                    case "--class-jobs" -> classJobs = Integer.parseInt(args[++index]);
                    case "--output" -> output = Path.of(args[++index]);
                    case "--tests" -> filters.add(args[++index]);
                    case "--update-docs" -> documentation = Path.of("../dbvisitor-doc/src/data/capabilities");
                    default -> throw new IllegalArgumentException("Unknown runner option: " + args[index]);
                }
            }
            Map<String, List<Class<?>>> suites = new LinkedHashMap<>();
            for (DataSourceId id : DataSourceId.values()) {
                if (target.equals("all") || target.equals(id.env())) {
                    suites.put(id.env(), discover(id));
                }
            }
            exitCode = new NxnRunner(output, suites, jobs, classJobs, filters, documentation).run() ? 0 : 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // SDK housekeeping threads must not keep the test process alive after the run finishes.
        System.exit(exitCode);
    }

    public boolean run() throws Exception {
        Files.createDirectories(root);
        try (FileChannel channel = FileChannel.open(root.resolve(".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.tryLock()) {
            if (lock == null) {
                throw new IllegalStateException("Another NxN runner owns " + root);
            }
            return execute();
        }
    }

    private boolean execute() throws Exception {
        startedNanos = System.nanoTime();
        startedAt = System.currentTimeMillis();
        for (String env : suites.keySet()) {
            Path directory = root.resolve(env);
            clearDirectory(directory);
            reports.put(env, new NxnReport(directory, env, runId, !filters.isEmpty(), documentation != null));
        }
        NxnReport.writeJson(root.resolve("session.json"), Map.of("runId", runId, "startedAt", startedAt, "environments", suites.keySet(), "jobs", jobs, "classJobs", classJobs, "pid", ProcessHandle.current().pid()));
        summary();
        console.println("NxN started | PID=" + ProcessHandle.current().pid() + " | workers=" + jobs + " | isolated classes/database=" + classJobs + " | databases=" + String.join(", ", suites.keySet()));
        console.println("Progress every " + PROGRESS_INTERVAL_SECONDS + "s: completed/selected tests | PASS / FAIL / SKIP | elapsed / avg | current test");
        console.println("Reports and detailed logs: " + root.toAbsolutePath());
        ExecutorService workers = Executors.newFixedThreadPool(jobs, task -> {
            Thread thread = new Thread(task, "nxn-worker");
            thread.setDaemon(true);
            return thread;
        });
        ScheduledExecutorService progress = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "nxn-progress");
            thread.setDaemon(true);
            return thread;
        });
        Thread shutdown = new Thread(() -> {
            cancelled.set(true);
            progress.shutdownNow();
            workers.shutdownNow();
            try {
                for (NxnReport report : reports.values()) {
                    report.cancel();
                }
                printProgress(true);
            } catch (Exception e) {
                console.println("Cannot finish interrupted NxN report: " + e);
            }
            // Do not wait for JDBC/SDK calls during JVM shutdown. All test threads exit with this JVM.
        }, "nxn-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdown);
        try (NxnOutput output = new NxnOutput(root, suites.keySet())) {
            progress.scheduleWithFixedDelay(() -> {
                synchronized (NxnRunner.this) {
                    if (progress.isShutdown()) {
                        return;
                    }
                    try {
                        printProgress(false);
                    } catch (Exception e) {
                        if (!progress.isShutdown()) {
                            console.println("Cannot update NxN progress: " + e);
                        }
                    }
                }
            }, PROGRESS_INTERVAL_SECONDS, PROGRESS_INTERVAL_SECONDS, TimeUnit.SECONDS);
            runSuites(workers);
            workers.shutdown();
            if (documentation != null) {
                console.println("NxN: updating compatibility JSON for successful datasource suites");
                updateDocumentation();
            }
            progress.shutdown();
            printProgress(true);
            return summary();
        } finally {
            progress.shutdownNow();
            workers.shutdownNow();
            if (!cancelled.get()) {
                Runtime.getRuntime().removeShutdownHook(shutdown);
            }
        }
    }

    private void runSuites(ExecutorService workers) throws Exception {
        List<DatasourcePlan> plans = new ArrayList<>();
        for (var suite : suites.entrySet()) {
            DatasourcePlan plan = new DatasourcePlan(suite.getKey());
            plans.add(plan);
            try (NxnContext context = plan.context()) {
                plan.pending.addAll(prepareRunners(suite.getValue()));
                plan.report.planned(plan.pending.stream().mapToInt(Runner::testCount).sum());
                if (plan.pending.isEmpty()) {
                    plan.error = "No tests selected; check the datasource and --tests filter";
                }
            } catch (Exception failure) {
                plan.error = failure.toString();
            }
        }
        CompletionService<DatasourcePlan> completions = new ExecutorCompletionService<>(workers);
        int active = 0;
        int nextDatasource = 0;
        while (plans.stream().anyMatch(plan -> !plan.finished)) {
            boolean submitted;
            do {
                submitted = false;
                // Resume after the last submission, including when just one worker becomes free.
                int firstDatasource = nextDatasource;
                for (int i = 0; i < plans.size(); i++) {
                    int index = (firstDatasource + i) % plans.size();
                    DatasourcePlan plan = plans.get(index);
                    if (!plan.finished && plan.pending.isEmpty() && plan.active == 0) {
                        finishDatasource(plan);
                    }
                    if (active >= jobs || !plan.canStart()) {
                        continue;
                    }
                    if (!plan.started) {
                        plan.report.start();
                        plan.report.clearCurrent();
                        plan.started = true;
                    }
                    Runner runner = plan.pending.removeFirst();
                    plan.exclusive = !concurrent(runner);
                    plan.active++;
                    active++;
                    nextDatasource = (index + 1) % plans.size();
                    submitted = true;
                    completions.submit(() -> {
                        runClass(plan, runner);
                        return plan;
                    });
                }
            } while (submitted && active < jobs);
            if (active > 0) {
                DatasourcePlan completed = completions.take().get();
                completed.active--;
                completed.exclusive = false;
                active--;
            }
        }
    }

    private void runClass(DatasourcePlan plan, Runner runner) {
        Thread.currentThread().setName("nxn-" + plan.env + "-" + runner.getDescription().getDisplayName());
        try (NxnContext context = plan.context()) {
            plan.report.current(runner.getDescription().getDisplayName() + " (class setup)");
            try {
                JUnitCore junit = new JUnitCore();
                junit.addListener(plan.report.listener());
                if (!junit.run(runner).wasSuccessful()) {
                    plan.error = "One or more JUnit tests failed; see the datasource report";
                }
            } catch (Throwable failure) {
                plan.error = failure.toString();
                failure.printStackTrace();
            } finally {
                plan.report.clearCurrent();
            }
        }
    }

    private void finishDatasource(DatasourcePlan plan) throws Exception {
        try (NxnContext context = plan.context()) {
            OneApiDataSourceManager.reset();
        } catch (Exception failure) {
            plan.error = failure.toString();
        }
        plan.finished = true;
        if (!cancelled.get()) {
            plan.report.finish(plan.error);
            console.println(plan.report.progressLine());
            if (plan.error != null) {
                console.println("NxN " + plan.env + ": " + plan.error + "; details: " + plan.report.directory.resolve("execution.log"));
            }
        }
    }

    private static boolean concurrent(Runner runner) {
        Class<?> type = runner.getDescription().getTestClass();
        return type != null && type.getDeclaredAnnotation(NxnConcurrent.class) != null;
    }

    private final class DatasourcePlan {
        final    String        env;
        final    NxnReport     report;
        final    Deque<Runner> pending = new ArrayDeque<>();
        volatile String        error;
        int     active;
        boolean exclusive;
        boolean started;
        boolean finished;

        DatasourcePlan(String env) {
            this.env = env;
            this.report = reports.get(env);
        }

        NxnContext context() {
            return new NxnContext(env, report.directory, runId).enter();
        }

        boolean canStart() {
            if (finished || pending.isEmpty() || exclusive || active >= classJobs) {
                return false;
            }
            return active == 0 || concurrent(pending.peekFirst());
        }
    }

    private List<Runner> prepareRunners(List<Class<?>> classes) throws InterruptedException {
        List<Runner> runners = new ArrayList<>();
        Filter selection = filter();
        for (Class<?> testClass : classes) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Datasource planning interrupted");
            }
            Runner runner = Request.aClass(testClass).getRunner();
            if (!filters.isEmpty()) {
                if (!selection.shouldRun(runner.getDescription())) {
                    continue;
                }
                try {
                    selection.apply(runner);
                } catch (NoTestsRemainException ignored) {
                    continue;
                }
            }
            runners.add(runner);
        }
        if (jobs > 1 && classJobs > 1) {
            // Stable grouping: isolated classes may overlap even when separated by class names.
            // Exclusive classes still wait for all isolated setup, methods and cleanup to finish.
            runners.sort(Comparator.comparing(runner -> !concurrent(runner)));
        }
        return runners;
    }

    private Filter filter() {
        return new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                if (description.isTest()) {
                    String className = description.getClassName();
                    String simpleName = className.substring(className.lastIndexOf('.') + 1);
                    return filters.stream().anyMatch(pattern -> pattern.matcher(className).matches() || pattern.matcher(simpleName).matches() || pattern.matcher(className + "." + description.getMethodName()).matches() || pattern.matcher(simpleName + "." + description.getMethodName()).matches());
                }
                return description.getChildren().stream().anyMatch(this::shouldRun);
            }

            @Override
            public String describe() {
                return "NxN test filter";
            }
        };
    }

    private void updateDocumentation() throws Exception {
        NxnDocumentation updater = new NxnDocumentation(documentation);
        for (Map.Entry<String, NxnReport> entry : reports.entrySet()) {
            NxnReport report = entry.getValue();
            if (Boolean.TRUE.equals(report.snapshot().get("complete"))) {
                try {
                    updater.update(entry.getKey(), report.directory);
                    report.documentation(true, null);
                    console.println("NxN " + entry.getKey() + ": compatibility JSON updated");
                } catch (Exception e) {
                    report.documentation(false, e.toString());
                    console.println("NxN " + entry.getKey() + ": compatibility JSON update failed: " + e);
                }
            }
        }
    }

    private synchronized void printProgress(boolean finished) throws Exception {
        int running = 0;
        int queued = 0;
        int tests = 0;
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        StringBuilder lines = new StringBuilder();
        for (NxnReport report : reports.values()) {
            Map<String, Object> run = report.snapshot();
            String status = (String) run.get("status");
            running += status.equals("running") ? 1 : 0;
            queued += status.equals("not-run") ? 1 : 0;
            tests += (int) run.get("tests");
            passed += (int) run.get("passed");
            failed += (int) run.get("failed");
            skipped += (int) run.get("skipped");
            if (finished || status.equals("running") || status.equals("not-run")) {
                lines.append(report.progressLine()).append('\n');
                report.save();
            }
        }
        String heading = finished ? "NxN summary" : "NxN progress";
        summary();
        console.println(heading + " | databases finished=" + (reports.size() - running - queued) + "/" + reports.size() + " running=" + running + " queued=" + queued + " | completed=" + tests + " PASS=" + passed + " FAIL=" + failed + " SKIP=" + skipped + " | elapsed=" + NxnReport.duration((System.nanoTime() - startedNanos) / 1_000_000) + "\n" + lines);
    }

    private synchronized boolean summary() throws Exception {
        List<Map<String, Object>> snapshots = reports.values().stream().map(NxnReport::snapshot).toList();
        boolean complete = !cancelled.get() && snapshots.stream().allMatch(run -> run.get("status").equals("passed") && List.of("not-requested", "updated").contains(run.get("documentation")));
        NxnReport.writeJson(root.resolve("summary.json"), Map.of("runId", runId, "startedAt", startedAt, "finishedAt", System.currentTimeMillis(), "complete", complete, "datasources", snapshots));
        return complete;
    }

    private static List<Class<?>> discover(DataSourceId id) throws Exception {
        String packageName = switch (id) {
            case ELASTIC6 -> "elastic6";
            case ELASTIC7 -> "elastic7";
            default -> id.env();
        };
        Path classes = Path.of(NxnRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path source = classes.resolve("net/hasor/dbvisitor/test/realdb/" + packageName);
        List<Class<?>> result = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path path : paths.filter(Files::isRegularFile).filter(file -> file.toString().endsWith("Test.class") && !file.toString().contains("$")).sorted().toList()) {
                String name = classes.relativize(path).toString().replace('/', '.').replace('\\', '.');
                Class<?> type = Class.forName(name.substring(0, name.length() - 6), false, NxnRunner.class.getClassLoader());
                if (!Modifier.isAbstract(type.getModifiers()) && isTestClass(type)) {
                    result.add(type);
                }
            }
        }
        return result;
    }

    private static boolean isTestClass(Class<?> type) {
        if (type.getAnnotation(org.junit.runner.RunWith.class) != null || junit.framework.TestCase.class.isAssignableFrom(type)) {
            return true;
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (Arrays.stream(current.getDeclaredMethods()).anyMatch(method -> method.getAnnotation(org.junit.Test.class) != null)) {
                return true;
            }
        }
        return false;
    }

    private static Pattern glob(String value) {
        return Pattern.compile(Arrays.stream(value.split("\\*", -1)).map(Pattern::quote).collect(java.util.stream.Collectors.joining(".*")));
    }

    private static void clearDirectory(Path directory) throws Exception {
        if (Files.exists(directory)) {
            if (!Files.isRegularFile(directory.resolve("run.json"))) {
                try (Stream<Path> entries = Files.list(directory)) {
                    if (entries.findAny().isPresent()) {
                        throw new IllegalArgumentException("Refusing to replace a non-NxN report directory: " + directory);
                    }
                }
            }
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }
}
