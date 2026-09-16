/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.runner;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import com.google.gson.GsonBuilder;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/** Execution state plus standard JUnit XML and a small browsable report, one directory per datasource. */
final class NxnReport {
    final         Path                directory;
    private final Map<String, Object> state   = new LinkedHashMap<>();
    private final List<TestEntry>     entries = new ArrayList<>();
    private       long                startedNanos;
    private       long                finishedNanos;
    private       long                testNanos;
    private final Map<Long, Activity> current = new LinkedHashMap<>();

    NxnReport(Path directory, String env, String runId, boolean filtered, boolean update) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory.resolve("test-results"));
        Files.createDirectories(directory.resolve("reports"));
        state.putAll(Map.of("env", env, "runId", runId, "filtered", filtered, "complete", false, "tests", 0, "failed", 0, "skipped", 0, "status", "not-run", "documentation", update ? "pending" : "not-requested"));
        state.put("total", -1);
        state.put("passed", 0);
        save();
    }

    synchronized void start() throws IOException {
        startedNanos = System.nanoTime();
        state.put("startedAt", System.currentTimeMillis());
        state.put("status", "running");
        current("Preparing test plan");
        save();
    }

    synchronized void planned(int total) throws IOException {
        state.put("total", total);
        save();
    }

    synchronized void current(String name) {
        if (finishedNanos != 0) {
            return;
        }
        current.put(Thread.currentThread().getId(), new Activity(name, System.nanoTime()));
    }

    synchronized void clearCurrent() {
        current.remove(Thread.currentThread().getId());
    }

    synchronized void finish(String error) throws IOException {
        finishedNanos = System.nanoTime();
        current.clear();
        state.put("finishedAt", System.currentTimeMillis());
        boolean passed = error == null && (int) state.get("tests") > 0 && (int) state.get("failed") == 0;
        state.put("complete", passed);
        state.put("status", passed ? "passed" : "failed");
        if (error != null) {
            state.put("error", error);
        }
        save();
        StringBuilder html = new StringBuilder("<!doctype html><meta charset=\"utf-8\"><title>NxN tests</title><h1>" + escape(state.get("env")) + "</h1>");
        html.append("<p>").append(escape(snapshot())).append("</p><p><a href='../execution.log'>Execution log</a></p><table><tr><th>Test</th><th>Result</th><th>Details</th></tr>");
        for (TestEntry entry : entries) {
            html.append("<tr><td>").append(escape(entry.description.getDisplayName())).append("</td><td>").append(entry.outcome).append("</td><td><pre>").append(escape(entry.trace)).append("</pre></td></tr>");
        }
        Files.writeString(directory.resolve("reports/index.html"), html.append("</table>").toString());
    }

    synchronized void documentation(boolean success, String error) throws IOException {
        state.put("documentation", success ? "updated" : "failed");
        if (!success) {
            state.put("status", "failed");
            state.put("error", error);
        }
        save();
    }

    synchronized void cancel() throws IOException {
        if (!"passed".equals(state.get("status")) && !"failed".equals(state.get("status"))) {
            finishedNanos = System.nanoTime();
            state.put("status", "cancelled");
            state.put("complete", false);
            save();
        }
    }

    synchronized Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>(state);
        long now = finishedNanos == 0 ? System.nanoTime() : finishedNanos;
        result.put("durationMillis", startedNanos == 0 ? 0L : (now - startedNanos) / 1_000_000);
        int executed = (int) state.get("passed") + (int) state.get("failed");
        result.put("averageMillis", executed == 0 ? 0.0 : testNanos / 1_000_000.0 / executed);
        if (!current.isEmpty()) {
            List<Map<String, Object>> activities = current.values().stream().map(activity -> Map.<String, Object>of("test", activity.name, "durationMillis", (now - activity.started) / 1_000_000)).toList();
            result.put("currentTests", activities);
            result.put("currentTest", activities.get(0).get("test"));
            result.put("currentTestMillis", activities.get(0).get("durationMillis"));
        }
        return result;
    }

    synchronized void save() throws IOException {
        writeJson(directory.resolve("run.json"), snapshot());
    }

    synchronized String progressLine() {
        Map<String, Object> run = snapshot();
        int total = (int) run.get("total");
        String progress = run.get("tests") + "/" + (total < 0 ? "?" : total);
        int executed = (int) run.get("passed") + (int) run.get("failed");
        String average = executed == 0 ? "-" : String.format(Locale.ROOT, "%.3fs/test", (double) run.get("averageMillis") / 1000);
        String status = run.get("status").equals("not-run") ? "queued" : (String) run.get("status");
        String line = String.format(Locale.ROOT, "NxN %-10s %-9s %s | PASS=%s FAIL=%s SKIP=%s | elapsed=%s avg=%s", run.get("env"), status.toUpperCase(Locale.ROOT), progress, run.get("passed"), run.get("failed"), run.get("skipped"), duration((long) run.get("durationMillis")), average);
        if (!current.isEmpty()) {
            long now = System.nanoTime();
            line += " | current=" + current.values().stream().map(activity -> activity.name + " (" + duration((now - activity.started) / 1_000_000) + ")").collect(java.util.stream.Collectors.joining("; "));
        }
        return line;
    }

    static String duration(long millis) {
        long seconds = millis / 1000;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    private synchronized void completed(TestEntry entry) {
        if (entry.completed || finishedNanos != 0) {
            return;
        }
        entry.completed = true;
        entries.add(entry);
        state.put("tests", entries.size());
        state.put(entry.outcome, (int) state.get(entry.outcome) + 1);
        if (!entry.outcome.equals("skipped")) {
            testNanos += entry.nanos;
        }
    }

    static void writeJson(Path file, Object value) throws IOException {
        // Readers may inspect a live report while it is being refreshed.
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().create().toJson(value) + "\n");
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    RunListener listener() {
        return new RunListener() {
            private final Map<Description, TestEntry> current = new LinkedHashMap<>();

            private TestEntry entry(Description description) {
                return current.computeIfAbsent(description, TestEntry::new);
            }

            @Override
            public void testStarted(Description description) {
                entry(description);
                current(testName(description));
            }

            @Override
            public void testFailure(Failure failure) {
                TestEntry entry = entry(failure.getDescription());
                entry.outcome = "failed";
                entry.trace += failure.getTrace();
            }

            @Override
            public void testAssumptionFailure(Failure failure) {
                TestEntry entry = entry(failure.getDescription());
                if (!entry.outcome.equals("failed")) {
                    entry.outcome = "skipped";
                    entry.trace = failure.getMessage();
                }
            }

            @Override
            public void testIgnored(Description description) {
                TestEntry entry = entry(description);
                entry.outcome = "skipped";
                completed(entry);
            }

            @Override
            public void testFinished(Description description) {
                TestEntry entry = entry(description);
                entry.nanos = System.nanoTime() - entry.started;
                completed(entry);
                current(description.getClassName() + " (class cleanup)");
            }

            @Override
            public void testRunFinished(org.junit.runner.Result result) throws Exception {
                List<TestEntry> suite = new ArrayList<>(current.values());
                if (suite.isEmpty()) {
                    return;
                }
                // Class-level setup/cleanup failures may have no testStarted/testFinished pair.
                suite.forEach(NxnReport.this::completed);
                writeSuite(suite);
                save();
                for (TestEntry entry : suite) {
                    System.out.println(entry.outcome + " " + entry.description.getDisplayName());
                    if (entry.outcome.equals("failed")) {
                        System.err.println(entry.trace);
                    }
                }
            }
        };
    }

    private void writeSuite(List<TestEntry> suite) throws Exception {
        String name = suite.get(0).description.getClassName();
        try (var output = Files.newOutputStream(directory.resolve("test-results/TEST-" + name + ".xml"))) {
            XMLStreamWriter xml = XMLOutputFactory.newDefaultFactory().createXMLStreamWriter(output, "UTF-8");
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("testsuite");
            xml.writeAttribute("name", name);
            xml.writeAttribute("tests", String.valueOf(suite.size()));
            xml.writeAttribute("failures", String.valueOf(suite.stream().filter(entry -> entry.outcome.equals("failed")).count()));
            xml.writeAttribute("errors", "0");
            xml.writeAttribute("skipped", String.valueOf(suite.stream().filter(entry -> entry.outcome.equals("skipped")).count()));
            for (TestEntry entry : suite) {
                xml.writeStartElement("testcase");
                xml.writeAttribute("classname", name);
                xml.writeAttribute("name", String.valueOf(entry.description.getMethodName()));
                xml.writeAttribute("time", String.valueOf(entry.nanos / 1_000_000_000.0));
                if (!entry.outcome.equals("passed")) {
                    xml.writeStartElement(entry.outcome.equals("failed") ? "failure" : "skipped");
                    xml.writeCharacters(entry.trace == null ? "" : entry.trace.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", ""));
                    xml.writeEndElement();
                }
                xml.writeEndElement();
            }
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
        }
    }

    private static String escape(Object value) {
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String testName(Description description) {
        String className = description.getClassName();
        return className.substring(className.lastIndexOf('.') + 1) + "#" + description.getMethodName();
    }

    private static final class TestEntry {
        final Description description;
        final long        started = System.nanoTime();
        String  outcome = "passed";
        String  trace   = "";
        long    nanos;
        boolean completed;

        TestEntry(Description description) {
            this.description = description;
        }
    }

    private record Activity(String name, long started) {
    }
}
