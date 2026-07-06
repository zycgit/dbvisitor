package net.hasor.dbvisitor.test.nxn.env;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import org.jetbrains.annotations.NotNull;

public final class OracleProfile extends AbstractDataSourceProfile {
    public static final OracleProfile INSTANCE = new OracleProfile();

    private OracleProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { //
                FeatureId.ARRAY,                 //
                FeatureId.SEQUENCE,              //
                FeatureId.KNN,                   //
                FeatureId.PROCEDURE_RESULT_SET,  //
                FeatureId.XML_MAPPER_CALLABLE,   //
                FeatureId.VECTOR,                //
                FeatureId.GENERATED_KEY_RESULT_SET, //
                FeatureId.POSTGRES_ON_CONFLICT,  //
                FeatureId.TRANSACTION_RELEASE_SAVEPOINT, //
                FeatureId.DELIMITED_LOWERCASE_STANDARD_TABLE, //
                FeatureId.DISTINCT_EMPTY_STRING, //
                FeatureId.LARGE_IN_LIST,         //
                FeatureId.XML_FOREACH_BATCH_INSERT_VALUES, //
                FeatureId.LOWERCASE_STANDARD_RESULT_COLUMNS, //
                FeatureId.TRANSACTION_REPEATABLE_READ, //
                FeatureId.MULTIPLE_RESULT_SETS };
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
