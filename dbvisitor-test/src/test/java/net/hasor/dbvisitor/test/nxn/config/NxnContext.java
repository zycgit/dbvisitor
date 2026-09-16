/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.config;

import java.nio.file.Path;

/** One datasource task's context; JVM properties remain a fallback for IDE/Gradle runs. */
public record NxnContext(String env, Path resultDirectory, String runId) implements AutoCloseable {
    private static final InheritableThreadLocal<NxnContext> CURRENT = new InheritableThreadLocal<>();

    public NxnContext enter() {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("A datasource context is already active");
        }
        CURRENT.set(this);
        return this;
    }

    public static String environment() {
        NxnContext context = CURRENT.get();
        return context == null ? System.getProperty("nxn.env", "pg").trim() : context.env;
    }

    public static Path results() {
        NxnContext context = CURRENT.get();
        if (context != null) {
            return context.resultDirectory;
        }
        String directory = System.getProperty("nxn.resultDir");
        return directory == null ? null : Path.of(directory);
    }

    public static String executionId() {
        NxnContext context = CURRENT.get();
        return context == null ? System.getProperty("nxn.runId") : context.runId;
    }

    public static boolean active() {
        return CURRENT.get() != null;
    }

    @Override
    public void close() {
        CURRENT.remove();
    }
}
