/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
