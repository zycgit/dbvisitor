package net.hasor.dbvisitor.test.nxn.env;

import org.jetbrains.annotations.NotNull;

import net.hasor.dbvisitor.test.nxn.capability.FeatureId;

public final class MySqlProfile extends AbstractDataSourceProfile {
    public static final MySqlProfile INSTANCE = new MySqlProfile();

    private MySqlProfile() {
        super(features());
    }

    @NotNull
    private static String[] features() {
        return new String[] { FeatureId.ARRAY, FeatureId.SEQUENCE, FeatureId.KNN, FeatureId.PROCEDURE_CURSOR_RESULT, FeatureId.FUNCTION_RECORD_RESULT, FeatureId.FUNCTION_TABLE_RESULT, FeatureId.VECTOR, FeatureId.GENERATED_KEY_RESULT_SET, FeatureId.XML_SELECT_KEY_USER_INFO_SEQUENCE, FeatureId.POSTGRES_ON_CONFLICT };
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MYSQL;
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
        return "CAST(" + expression + " AS SIGNED)";
    }

    @Override
    public String datetimeColumnType() {
        return "DATETIME(3)";
    }
}
