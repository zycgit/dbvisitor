/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.runner;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.nxn.config.NxnContext;

/** Routes test-thread output without changing System.out separately in each worker. */
final class NxnOutput implements AutoCloseable {
    private final PrintStream              stdout  = System.out;
    private final PrintStream              stderr  = System.err;
    private final Map<String, PrintStream> streams = new LinkedHashMap<>();
    private final PrintStream              fallback;

    NxnOutput(Path root, Iterable<String> environments) throws IOException {
        fallback = new PrintStream(Files.newOutputStream(root.resolve("runner.log")), true, StandardCharsets.UTF_8);
        try {
            for (String env : environments) {
                streams.put(env, new PrintStream(Files.newOutputStream(root.resolve(env).resolve("execution.log")), true, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            close();
            throw e;
        }
        PrintStream routed = new PrintStream(new OutputStream() {
            @Override
            public void write(int value) {
                destination().write(value);
            }

            @Override
            public void write(byte[] bytes, int offset, int length) {
                destination().write(bytes, offset, length);
            }
        }, true, StandardCharsets.UTF_8);
        System.setOut(routed);
        System.setErr(routed);
    }

    private PrintStream destination() {
        return NxnContext.active() ? streams.getOrDefault(NxnContext.environment(), fallback) : fallback;
    }

    @Override
    public void close() {
        System.setOut(stdout);
        System.setErr(stderr);
        streams.values().forEach(PrintStream::close);
        fallback.close();
    }
}
