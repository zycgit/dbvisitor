package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

public final class PostgreSqlProfile extends AbstractDataSourceProfile {
    public static final PostgreSqlProfile INSTANCE = new PostgreSqlProfile();

    private PostgreSqlProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { FeatureId.PROCEDURE_RESULT_SET };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.PG;
    }

    @Override
    public String leftQualifier() {
        return "\"";
    }

    @Override
    public String rightQualifier() {
        return "\"";
    }

    @Override
    public String castToBigInt(String expression) {
        return "CAST(" + expression + " AS BIGINT)";
    }

    @Override
    public String datetimeColumnType() {
        return "TIMESTAMP";
    }
}
