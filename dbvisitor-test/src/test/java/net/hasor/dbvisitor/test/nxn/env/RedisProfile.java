package net.hasor.dbvisitor.test.nxn.env;

public final class RedisProfile extends AbstractDataSourceProfile {
    public static final RedisProfile INSTANCE = new RedisProfile();

    private RedisProfile() {
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.REDIS;
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
        return "STRING";
    }
}
