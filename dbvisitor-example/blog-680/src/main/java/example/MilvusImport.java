/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;

/** Run against a disposable collection and a file already accessible to the Milvus server. */
public class MilvusImport {
    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !args[0].matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Expected: <existing_collection> <server_file_path>");
        }
        String collection = args[0];
        String jobId;
        try (Connection conn = Connections.milvus()) {
            try (PreparedStatement submit = conn.prepareStatement("/*+ sync=false */ IMPORT FROM ? INTO " + collection + " RETURNING JOB_ID")) {
                submit.setString(1, args[1]);
                try (ResultSet result = submit.executeQuery()) {
                    result.next();
                    jobId = result.getString("JOB_ID");
                    // Keep the ID even if later polling fails. Never resubmit automatically.
                    System.out.println("jobId=" + jobId);
                }
            }
            long deadline = System.nanoTime() + Duration.ofMinutes(2).toNanos();
            try (PreparedStatement progress = conn.prepareStatement("SHOW IMPORT ?")) {
                progress.setString(1, jobId);
                while (System.nanoTime() < deadline) {
                    try (ResultSet result = progress.executeQuery()) {
                        result.next();
                        String state = result.getString("STATE");
                        System.out.println("state=" + state);
                        if ("Completed".equals(state)) {
                            return;
                        }
                        if ("Failed".equals(state)) {
                            throw new IllegalStateException("jobId=" + jobId + "; reason=" + result.getString("REASON"));
                        }
                    }
                    Thread.sleep(1000);
                }
            }
            throw new IllegalStateException("Polling stopped; task may still run. Keep jobId=" + jobId);
        }
    }
}
