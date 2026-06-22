package net.hasor.dbvisitor.test.nxn.env;

public final class Elastic6Profile extends AbstractDataSourceProfile {
    public static final Elastic6Profile INSTANCE = new Elastic6Profile();

    private Elastic6Profile() {
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.ELASTIC6;
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
        return "date";
    }
}
