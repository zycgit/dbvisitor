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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Execute the real shell launcher with a disposable compiler/runtime. */
public class NxnLauncherTest {
    @Rule
    public  TemporaryFolder temporary = new TemporaryFolder();
    private Path            repository;
    private Path            launcher;

    @Before
    public void prepare() throws Exception {
        repository = temporary.newFolder("checkout with spaces").toPath();
        launcher = repository.resolve("dbvisitor-test/runnxn.sh");
        Files.createDirectories(launcher.getParent());
        Files.copy(Path.of("runnxn.sh"), launcher);
        assertTrue(launcher.toFile().setExecutable(true));
        Files.copy(Path.of("../build.sh"), repository.resolve("build.sh"));
        Path gradle = repository.resolve("gradlew");
        Files.writeString(gradle, """
                #!/usr/bin/env bash
                printf '%s\\n' "$@" > arguments.txt
                printf '%s\\n' "$*" >> compiler-calls.txt
                if [[ -f exit-code ]]; then
                    exit "$(<exit-code)"
                fi
                """);
        assertTrue(gradle.toFile().setExecutable(true));
        Path runtime = repository.resolve("fake-java");
        Files.writeString(runtime, """
                #!/usr/bin/env bash
                printf '%s\\n' "$@" > ../runtime-arguments.txt
                printf '%s\\n' "$$" > ../runtime-pid.txt
                if [[ -f ../runtime-exit-code ]]; then
                    exit "$(<../runtime-exit-code)"
                fi
                """);
        assertTrue(runtime.toFile().setExecutable(true));
        Path launchDirectory = repository.resolve("dbvisitor-test/build/nxn-launcher");
        Files.createDirectories(launchDirectory);
        Files.writeString(launchDirectory.resolve("java.txt"), runtime + "\n");
        Files.writeString(launchDirectory.resolve("arguments.txt"), "");
    }

    @Test
    public void helpDoesNotRunTests() throws Exception {
        assertEquals(0, run().exitCode());
        assertFalse(Files.exists(repository.resolve("arguments.txt")));
        assertTrue(run("--help").output().contains("16 worker threads"));
        assertEquals(0, run("all", "--help").exitCode());
        assertFalse(Files.exists(repository.resolve("runtime-pid.txt")));
    }

    @Test
    public void dryRunNeverStartsTestsEvenWhenRuntimeFilesExist() throws Exception {
        for (String option : List.of("--dry-run", "-m", "--version")) {
            assertEquals(0, run("all", option).exitCode());
            assertFalse(Files.exists(repository.resolve("runtime-pid.txt")));
        }
    }

    @Test
    public void defaultsToSixteenThreadsAndExecReplacesShell() throws Exception {
        Execution result = run("all");
        assertEquals(0, result.exitCode());
        assertEquals(List.of(":dbvisitor-test:prepareNxnRunner"), arguments("arguments.txt"));
        assertEquals(List.of("@build/nxn-launcher/arguments.txt", "all", "--jobs", "16"), arguments("runtime-arguments.txt"));
        assertEquals(result.pid(), Long.parseLong(Files.readString(repository.resolve("runtime-pid.txt")).trim()));
    }

    @Test
    public void buildTestNxnUsesRunnerDefaultsWithoutExtraOptions() throws Exception {
        Execution result = run(repository.resolve("build.sh"), "test", "nxn");
        assertEquals(0, result.exitCode());
        assertEquals(List.of("clean --parallel --max-workers 8", "build --parallel --max-workers 8", ":dbvisitor-test:prepareNxnRunner"), arguments("compiler-calls.txt"));
        assertEquals(List.of("@build/nxn-launcher/arguments.txt", "all", "--jobs", "16"), arguments("runtime-arguments.txt"));
        assertEquals(result.pid(), Long.parseLong(Files.readString(repository.resolve("runtime-pid.txt")).trim()));
    }

    @Test
    public void concurrencyBelongsToJavaNotGradle() throws Exception {
        assertEquals(0, run("elastic7", "--jobs", "1", "--max-workers=4").exitCode());
        assertEquals(List.of(":dbvisitor-test:prepareNxnRunner", "--max-workers=4"), arguments("arguments.txt"));
        assertEquals(List.of("@build/nxn-launcher/arguments.txt", "es7", "--jobs", "1"), arguments("runtime-arguments.txt"));
        assertEquals(0, run("all", "--jobs=4").exitCode());
        assertTrue(arguments("runtime-arguments.txt").contains("4"));
        assertEquals(0, run("milvus", "--class-jobs", "1").exitCode());
        assertEquals(List.of("@build/nxn-launcher/arguments.txt", "milvus", "--jobs", "16", "--class-jobs", "1"), arguments("runtime-arguments.txt"));
        assertEquals(0, run("clickhouse", "--class-jobs=2").exitCode());
        assertTrue(arguments("runtime-arguments.txt").containsAll(List.of("--class-jobs", "2")));
    }

    @Test
    public void forwardsFiltersAndDocumentationToRunnerOnly() throws Exception {
        assertEquals(0, run("all", "--tests", "*JdbcCrudTest", "--tests=*MetadataShapeTest").exitCode());
        assertEquals(List.of("@build/nxn-launcher/arguments.txt", "all", "--jobs", "16", "--tests", "*JdbcCrudTest", "--tests", "*MetadataShapeTest"), arguments("runtime-arguments.txt"));
        assertEquals(0, run("h2", "--update-docs").exitCode());
        assertTrue(arguments("runtime-arguments.txt").contains("--update-docs"));
        assertEquals(List.of(":dbvisitor-test:prepareNxnRunner"), arguments("arguments.txt"));
        assertEquals(0, run("milvus", "--output", "build/comparison report").exitCode());
        assertTrue(arguments("runtime-arguments.txt").containsAll(List.of("--output", "build/comparison report")));
    }

    @Test
    public void rejectsBadOptionsBeforeCompiling() throws Exception {
        for (String jobs : List.of("0", "-1", "sixteen", "", "--offline")) {
            assertEquals(2, run("all", "--jobs", jobs).exitCode());
            assertEquals(2, run("all", "--class-jobs", jobs).exitCode());
            assertEquals(2, run("all", "--class-jobs=" + jobs).exitCode());
        }
        assertEquals(2, run("all", "--class-jobs").exitCode());
        assertEquals(2, run("all", "--output").exitCode());
        assertEquals(2, run("all", "--jobs").exitCode());
        assertEquals(2, run("all", "--tests").exitCode());
        assertEquals(2, run("unknown").exitCode());
        assertEquals(2, run("h2", "--tests", "*CrudTest", "--update-docs").exitCode());
        for (String option : List.of("-Pnxn.env=pg", "-Pmilvus.cloud=true", "--exclude-task=test", "-xtest")) {
            assertEquals(option, 2, run("all", option).exitCode());
        }
        assertFalse(Files.exists(repository.resolve("arguments.txt")));
    }

    @Test
    public void compilationFailureNeverStartsTests() throws Exception {
        Files.writeString(repository.resolve("exit-code"), "42");
        assertEquals(42, run("h2", "--offline", "--console=plain").exitCode());
        assertTrue(arguments("arguments.txt").containsAll(List.of("--offline", "--console=plain")));
        assertFalse(Files.exists(repository.resolve("runtime-pid.txt")));
    }

    @Test
    public void preservesTestExitCode() throws Exception {
        Files.writeString(repository.resolve("runtime-exit-code"), "7");
        assertEquals(7, run("h2").exitCode());
    }

    @Test
    public void refusesToOverwriteAnotherRun() throws Exception {
        Path lock = repository.resolve(".gradle/nxn.lock");
        Files.createDirectories(lock.getParent());
        Process holder = new ProcessBuilder("flock", lock.toString(), "bash", "-c", "printf 'ready\\n'; read -r unused").start();
        try {
            assertEquals("ready", holder.inputReader().readLine());
            Execution result = run("all");
            assertEquals(75, result.exitCode());
            assertTrue(result.output().contains("Another NxN run"));
            assertFalse(Files.exists(repository.resolve("arguments.txt")));
        } finally {
            holder.getOutputStream().close();
            if (!holder.waitFor(5, TimeUnit.SECONDS)) {
                holder.destroyForcibly();
            }
        }
        assertEquals(0, run("h2").exitCode());
    }

    private List<String> arguments(String file) throws Exception {
        return Files.readAllLines(repository.resolve(file));
    }

    private Execution run(String... args) throws Exception {
        return run(launcher, args);
    }

    private Execution run(Path script, String... args) throws Exception {
        List<String> command = new ArrayList<>(List.of("bash", script.toString()));
        command.addAll(List.of(args));
        Path output = repository.resolve("launcher-output.txt");
        Process process = new ProcessBuilder(command).directory(temporary.getRoot()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Launcher did not finish");
        }
        return new Execution(process.pid(), process.exitValue(), Files.readString(output));
    }

    private record Execution(long pid, int exitCode, String output) {
    }
}
