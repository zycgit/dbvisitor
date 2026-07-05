package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

public final class MsSqlProfile extends AbstractDataSourceProfile {
    public static final MsSqlProfile INSTANCE = new MsSqlProfile();

    private MsSqlProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { //
                FeatureId.ARRAY,                 //
                FeatureId.SEQUENCE,              //
                                FeatureId.KNN,                   //
                FeatureId.PROCEDURE_CURSOR_RESULT, //
                FeatureId.XML_MAPPER_CALLABLE,   //
                FeatureId.VECTOR,                //
                FeatureId.POSTGRES_ON_CONFLICT,  //
                FeatureId.TRANSACTION_RELEASE_SAVEPOINT, //
                FeatureId.CASE_SENSITIVE_IDENTIFIERS };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MSSQL;
    }

    @Override
    public String leftQualifier() {
        return "[";
    }

    @Override
    public String rightQualifier() {
        return "]";
    }

    @Override
    public String castToBigInt(String expression) {
        return "CAST(" + expression + " AS BIGINT)";
    }

    @Override
    public String datetimeColumnType() {
        return "DATETIME2(3)";
    }
}
