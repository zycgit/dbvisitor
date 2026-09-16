/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.nxn;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import net.hasor.dbvisitor.test.nxn.config.NxnDatabaseNames;
import org.junit.Test;
import static org.junit.Assert.*;

public class NxnDatabaseNamesTest {
    @Test
    public void skipsExistingNumbersAndNeverDropsOrAdoptsExistingDatabases() throws Exception {
        Server server = new Server();
        server.databases.addAll(List.of("default", "dbv_nxn_numbered_000009", "dbv_nxn_numbered_not_a_number"));
        assertEquals("dbv_nxn_numbered_000010", NxnDatabaseNames.create(server.connection(), "numbered"));
        server.databases.add("dbv_nxn_numbered_000020");
        assertEquals("dbv_nxn_numbered_000021", NxnDatabaseNames.create(server.connection(), "numbered"));
        assertTrue(server.databases.contains("dbv_nxn_numbered_000009"));
        assertEquals(List.of("CREATE DATABASE dbv_nxn_numbered_000010", "CREATE DATABASE dbv_nxn_numbered_000021"), server.commands);
    }

    @Test
    public void concurrentAllocationsHaveDistinctIncreasingNumbers() throws Exception {
        Server server = new Server();
        var workers = Executors.newFixedThreadPool(4);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tasks.add(() -> NxnDatabaseNames.create(server.connection(), "parallel"));
            }
            HashSet<String> names = new HashSet<>();
            for (var result : workers.invokeAll(tasks)) {
                names.add(result.get());
            }
            assertEquals(8, names.size());
            assertTrue(names.contains("dbv_nxn_parallel_000001"));
            assertTrue(names.contains("dbv_nxn_parallel_000008"));
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    public void failedCreationDoesNotClaimOwnershipOrAttemptCleanup() {
        Server server = new Server();
        server.failCreate = true;
        assertThrows(SQLException.class, () -> NxnDatabaseNames.create(server.connection(), "failure"));
        assertTrue(server.databases.isEmpty());
        assertEquals(List.of("CREATE DATABASE dbv_nxn_failure_000001"), server.commands);
        assertThrows(IllegalArgumentException.class, () -> NxnDatabaseNames.create(server.connection(), "bad;sql"));
    }

    private static final class Server {
        final List<String> databases = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        boolean failCreate;

        Connection connection() {
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                if (method.getName().equals("createStatement")) {
                    return statement();
                }
                throw new AssertionError("Unexpected Connection method " + method.getName());
            });
        }

        Statement statement() {
            return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Statement.class }, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "close":
                        return null;
                    case "executeQuery":
                        assertEquals("SHOW DATABASES", args[0]);
                        var names = List.copyOf(databases).iterator();
                        String[] current = new String[1];
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { ResultSet.class }, (rows, call, values) -> {
                            switch (call.getName()) {
                                case "close":
                                    return null;
                                case "next":
                                    if (!names.hasNext()) {
                                        return false;
                                    }
                                    current[0] = names.next();
                                    return true;
                                case "getString":
                                    return current[0];
                                default:
                                    throw new AssertionError("Unexpected ResultSet method " + call.getName());
                            }
                        });
                    case "executeUpdate":
                        String sql = (String) args[0];
                        commands.add(sql);
                        if (failCreate) {
                            throw new SQLException("Database already exists");
                        }
                        assertTrue(sql.startsWith("CREATE DATABASE "));
                        databases.add(sql.substring("CREATE DATABASE ".length()));
                        return 0;
                    default:
                        throw new AssertionError("Unexpected Statement method " + method.getName());
                }
            });
        }
    }
}
