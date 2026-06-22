package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

public final class Db2Profile extends AbstractDataSourceProfile {
    public static final Db2Profile INSTANCE = new Db2Profile();

    private Db2Profile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { //
                FeatureId.ARRAY,                 //
                FeatureId.JSON,                  //
                FeatureId.KNN,                   //
                FeatureId.PROCEDURE_CURSOR_RESULT, //
                FeatureId.XML_MAPPER_CALLABLE,   //
                FeatureId.FUNCTION_RECORD_RESULT, //
                FeatureId.FUNCTION_TABLE_RESULT, //
                FeatureId.VECTOR,                //
                FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE, //
                FeatureId.POSTGRES_ON_CONFLICT,  //
                FeatureId.DUPLICATE_KEY_STRATEGY,//
                FeatureId.BIT_CAST_NULL_VALUE,   //
                FeatureId.SQL_MD5_FUNCTION,      //
                FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE, //
                FeatureId.CASE_SENSITIVE_IDENTIFIERS };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.DB2;
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
