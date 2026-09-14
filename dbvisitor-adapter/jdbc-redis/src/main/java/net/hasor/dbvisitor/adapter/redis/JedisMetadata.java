/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis;

import java.util.List;
import java.util.Set;
import net.hasor.dbvisitor.driver.MetadataNode;
import net.hasor.dbvisitor.driver.MetadataPath;
import net.hasor.dbvisitor.driver.MetadataSupport;
import net.hasor.dbvisitor.driver.MetadataType;

/** Redis exposes only the selected database, never keys as tables. */
final class JedisMetadata implements MetadataSupport {
    private final JedisConn connection;

    JedisMetadata(JedisConn connection) {
        this.connection = connection;
    }

    @Override
    public Set<MetadataType> supportedTypes() {
        return Set.of(MetadataType.CATALOG);
    }

    @Override
    public List<MetadataNode> query(MetadataPath path) {
        if (!path.levels().isEmpty()) {
            return List.of();
        }
        return switch (path.type()) {
            case CATALOG -> List.of(new MetadataNode(MetadataType.CATALOG, connection.getCatalog()));
            default -> List.of();
        };
    }
}
