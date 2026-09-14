/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** A named native metadata object. */
public record MetadataNode(MetadataType type, String name, Map<String, Object> attributes) {
    public static final String TYPE_NAME      = "typeName";
    public static final String JDBC_TYPE      = "jdbcType";
    public static final String NULLABLE       = "nullable";
    public static final String AUTO_INCREMENT = "autoIncrement";
    public static final String GENERATED      = "generated";
    public static final String ORDINAL        = "ordinal";
    public static final String DEFAULT_VALUE  = "defaultValue";
    public static final String REMARKS        = "remarks";
    public static final String OCTET_LENGTH   = "octetLength";
    public static final String SIZE           = "size";
    public static final String SCALE          = "scale";

    public MetadataNode {
        Objects.requireNonNull(type, "type");
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    public MetadataNode(MetadataType type, String name) {
        this(type, name, Map.of());
    }
}
