package net.hasor.dbvisitor.dialect.dto;

import net.hasor.dbvisitor.dialect.SqlDialect;

import java.util.Collections;
import java.util.Set;

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
}
