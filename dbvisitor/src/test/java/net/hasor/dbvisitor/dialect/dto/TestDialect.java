/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dialect.dto;

import java.util.Collections;
import java.util.Set;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;

public class TestDialect implements SqlDialect {
    @Override
    public Set<String> keywords() {
        return Collections.emptySet();
    }

    @Override
    public String leftQualifier() {
        return "";
    }

    @Override
    public String rightQualifier() {
        return "";
    }

    @Override
    public String aliasSeparator() {
        return " ";
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        return "";
    }

    @Override
    public String fmtName(boolean useQualifier, String name) {
        return "";
    }

    @Override
    public SqlCommandBuilder newBuilder() {
        return null;
    }
}
