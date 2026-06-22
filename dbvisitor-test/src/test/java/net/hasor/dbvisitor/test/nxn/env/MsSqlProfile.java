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
                FeatureId.JSON,                  //
                FeatureId.SEQUENCE,              //
                FeatureId.GENERATED_KEY_COLUMN,  //
                FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE, //
                FeatureId.KNN,                   //
                FeatureId.PROCEDURE,             //
                FeatureId.XML_MAPPER_CALLABLE,   //
                FeatureId.FUNCTION,              //
                FeatureId.VECTOR,                //
                FeatureId.POSTGRES_ON_CONFLICT,  //
                FeatureId.DUPLICATE_KEY_STRATEGY,//
                FeatureId.SQL_MD5_FUNCTION,      //
                FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL, //
                FeatureId.GENERATED_KEYS_NUMERIC, //
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
