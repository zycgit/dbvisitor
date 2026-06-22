package net.hasor.dbvisitor.test.nxn.env;

public final class Elastic7Profile extends AbstractDataSourceProfile {
    public static final Elastic7Profile INSTANCE = new Elastic7Profile();

    private Elastic7Profile() {
    }

    @Override
    public DataSourceId id() {
        return DataSourceId.ELASTIC7;
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
