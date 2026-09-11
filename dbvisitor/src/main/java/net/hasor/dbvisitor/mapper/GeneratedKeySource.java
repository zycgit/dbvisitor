/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;
import net.hasor.cobble.StringUtils;

/**
 * Generated key value source.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2026-06-27
 */
public enum GeneratedKeySource {
    /**
     * Read generated keys from {@link java.sql.Statement#getGeneratedKeys()}.
     */
    GeneratedKeys("generatedKeys"),

    /**
     * Read generated keys from the current {@link java.sql.ResultSet}.
     */
    ResultSet("resultSet"),;

    private final String typeName;

    GeneratedKeySource(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public static GeneratedKeySource valueOfCode(String code, GeneratedKeySource defaultType) {
        for (GeneratedKeySource source : GeneratedKeySource.values()) {
            if (StringUtils.equalsIgnoreCase(source.name(), code) || StringUtils.equalsIgnoreCase(source.typeName, code)) {
                return source;
            }
        }
        return defaultType;
    }
}
