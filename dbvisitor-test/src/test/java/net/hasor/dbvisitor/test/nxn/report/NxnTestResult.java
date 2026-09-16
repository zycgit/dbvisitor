/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.gson.Gson;
import net.hasor.dbvisitor.test.nxn.config.NxnContext;
import org.junit.runner.Description;

/** One observed JUnit execution; declarations alone never produce a passing result. */
public final class NxnTestResult {
    public String runId;
    public String env;
    public String fingerprint;
    public String testClass;
    public String method;
    public String capability;
    public String outcome;
    public String reason;

    public static void write(String env, Description description, String capability, String outcome, String reason) throws Exception {
        Path resultDir = NxnContext.results();
        if (resultDir == null) {
            return;
        }
        NxnTestResult result = new NxnTestResult();
        result.runId = NxnContext.executionId();
        result.env = env;
        result.fingerprint = Fingerprint.VALUE;
        result.testClass = description.getClassName();
        result.method = description.getMethodName();
        result.capability = capability;
        result.outcome = outcome;
        result.reason = reason;
        Path directory = resultDir.resolve("cases");
        Files.createDirectories(directory);
        String key = result.testClass + "#" + result.method;
        Path output = directory.resolve(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)) + ".json");
        // Repeated execution of the same method in one run is ambiguous, not another capability.
        Files.writeString(output, new Gson().toJson(result), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE_NEW);
    }

    public static String fingerprint() throws Exception {
        Path classes = Path.of(NxnTestResult.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (Stream<Path> files = Files.walk(classes)) {
            for (Path path : files.filter(Files::isRegularFile).sorted().toList()) {
                digest.update(classes.relativize(path).toString().getBytes(StandardCharsets.UTF_8));
                digest.update(Files.readAllBytes(path));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static final class Fingerprint {
        private static final String VALUE = load();

        private static String load() {
            try {
                return fingerprint();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot identify the executed NxN test classes", e);
            }
        }
    }
}
