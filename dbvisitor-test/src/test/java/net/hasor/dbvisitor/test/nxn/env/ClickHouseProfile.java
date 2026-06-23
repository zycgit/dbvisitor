package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

public final class ClickHouseProfile extends AbstractDataSourceProfile {
    public static final ClickHouseProfile INSTANCE = new ClickHouseProfile();

    private ClickHouseProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { //
                FeatureId.ARRAY,                 //
                FeatureId.JSON,                  //
                FeatureId.BINARY,                //
                FeatureId.SEQUENCE,              //
                FeatureId.GENERATED_KEY_COLUMN,  //
                FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE, //
                FeatureId.KNN,                   //
                FeatureId.PROCEDURE,             //
                FeatureId.XML_MAPPER_CALLABLE,   //
                FeatureId.FUNCTION_CALL_CALLBACK, //
                FeatureId.FUNCTION_RECORD_RESULT, //
                FeatureId.FUNCTION_TABLE_RESULT, //
                FeatureId.POSTGRES_ON_CONFLICT,  //
                FeatureId.DUPLICATE_KEY_STRATEGY,//
                FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED,//
                FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED, //
                FeatureId.EXACT_MUTATION_AFFECTED_ROWS,//
                FeatureId.LENGTH_LIMIT_ENFORCED, //
                FeatureId.NON_NULL_PRIMARY_KEY_REJECTED,//
                FeatureId.EMPTY_WHERE_MUTATION,  //
                FeatureId.TRANSACTION,           //
                FeatureId.JOIN_NON_EQUI_CONDITION, //
                FeatureId.LEFT_JOIN_NULL_VALUES, //
                FeatureId.SQL_NOT_IN_NULL_SEMANTICS, //
                FeatureId.BIT_CAST_NULL_VALUE, //
                FeatureId.TIME_EXTREME_DATE,     //
                FeatureId.SQL_MD5_FUNCTION,      //
                FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL, //
                FeatureId.GENERATED_KEYS_NUMERIC, //
                FeatureId.TRANSACTION_RELEASE_SAVEPOINT, //
                FeatureId.TRANSACTION_REPEATABLE_READ, //
                FeatureId.CASE_SENSITIVE_IDENTIFIERS, //
                FeatureId.MULTIPLE_RESULT_SETS };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.CLICKHOUSE;
    }

    @Override
    public String leftQualifier() {
        return "`";
    }

    @Override
    public String rightQualifier() {
        return "`";
    }

    @Override
    public String castToBigInt(String expression) {
        return "CAST(" + expression + " AS Int64)";
    }

    @Override
    public String datetimeColumnType() {
        return "DateTime64(3)";
    }

    @Override
    public String currentTimestampExpression() {
        return "current_timestamp()";
    }
}
