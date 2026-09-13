/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import org.elasticsearch.client.ResponseException;

/** Index cleanup may ignore absence, but must expose transport, permission and parser errors. */
final class Elastic7Cleanup {
    private Elastic7Cleanup() {
    }

    static void requireMissingIndex(Exception error) throws SQLException {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException && "E404".equals(((SQLException) cause).getSQLState())) {
                return;
            }
            if (cause instanceof ResponseException && ((ResponseException) cause).getResponse().getStatusLine().getStatusCode() == 404) {
                return;
            }
        }
        throw new SQLException("Elasticsearch index cleanup failed", error);
    }
}
