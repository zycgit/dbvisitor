package net.hasor.dbvisitor.test.nxn.env;

public final class MongoProfile extends AbstractDataSourceProfile {
    public static final MongoProfile INSTANCE = new MongoProfile();

    private MongoProfile() {
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.MONGO;
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
        return "DATE";
    }
}
