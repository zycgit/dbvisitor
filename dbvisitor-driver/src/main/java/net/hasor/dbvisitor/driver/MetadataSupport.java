/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/** Connection-scoped native metadata discovery, independent of JDBC result-set layouts. */
public interface MetadataSupport {
    /** Object kinds exposed by this provider, independent of existing objects. */
    default Set<MetadataType> supportedTypes() {
        return Set.of();
    }

    /**
     * List objects of the requested kind under exact parent levels.
     * Names are literal, not JDBC patterns. Return an empty list for unavailable metadata.
     * Backend errors must propagate; implementations must not return null.
     */
    List<MetadataNode> query(MetadataPath path) throws SQLException;
}
