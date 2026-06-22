package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

public final class OracleProfile extends AbstractDataSourceProfile {
    public static final OracleProfile INSTANCE = new OracleProfile();

    private OracleProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { //
                FeatureId.ARRAY,                 //
                FeatureId.JSON,                  //
                FeatureId.SEQUENCE,              //
                FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE, //
                FeatureId.KNN,                   //
                FeatureId.XML_MAPPER_CALLABLE,   //
                FeatureId.FUNCTION_RECORD_RESULT, //
                FeatureId.FUNCTION_TABLE_RESULT, //
                FeatureId.VECTOR,                //
                FeatureId.POSTGRES_ON_CONFLICT,  //
                FeatureId.SQL_MD5_FUNCTION,      //
                FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL, //
                FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE, //
                FeatureId.DISTINCT_EMPTY_STRING, //
                FeatureId.LARGE_IN_LIST,         //
                FeatureId.XML_FOREACH_BATCH_INSERT_VALUES, //
                FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS, //
                FeatureId.TRANSACTION_RELEASE_SAVEPOINT, //
                FeatureId.TRANSACTION_REPEATABLE_READ };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.ORACLE;
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
        return "CAST(" + expression + " AS NUMBER(19))";
    }

    @Override
    public String datetimeColumnType() {
        return "TIMESTAMP";
    }
}
