package net.hasor.dbvisitor.test.nxn.env;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import org.jetbrains.annotations.NotNull;

public final class H2Profile extends AbstractDataSourceProfile {
    public static final H2Profile INSTANCE = new H2Profile();

    private H2Profile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] {//
                FeatureId.KNN,                  //
                FeatureId.PROCEDURE,            //
                FeatureId.XML_MAPPER_CALLABLE,  //
                FeatureId.FUNCTION_CALL_CALLBACK, //
                FeatureId.VECTOR,               //
                FeatureId.GENERATED_KEY_RESULT_SET, //
                FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE,   //
                FeatureId.POSTGRES_ON_CONFLICT, //
                FeatureId.TIME_EXTREME_DATE,    //
                FeatureId.CASE_SENSITIVE_IDENTIFIERS, //
                FeatureId.MULTIPLE_RESULT_SETS };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.H2;
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
