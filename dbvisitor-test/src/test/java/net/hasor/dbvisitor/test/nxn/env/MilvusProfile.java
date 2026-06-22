package net.hasor.dbvisitor.test.nxn.env;

public final class MilvusProfile extends AbstractDataSourceProfile {
    public static final MilvusProfile INSTANCE = new MilvusProfile();

    private MilvusProfile() {
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MILVUS;
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
    public String castToBigInt(String expression) {
        return expression;
    }

    @Override
    public String datetimeColumnType() {
        return "VARCHAR(32)";
    }
}
