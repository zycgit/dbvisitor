/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdapterConnManager {
    private static final Map<String, AdapterConnection> connectionMap = new ConcurrentHashMap<>();

    static void newConnection(AdapterConnection connection) {
        connectionMap.put(connection.getObjectId(), connection);
    }

    static void removeConnection(AdapterConnection connection) {
        connectionMap.remove(connection.getObjectId());
    }

    public static AdapterConnection getConnection(String objectId) {
        return connectionMap.get(objectId);
    }
}
