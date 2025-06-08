package net.hasor.dbvisitor.driver;

public enum JdbcArgMode {
    In(true, false),
    Out(false, true),
    InOut(true, true),
    ;

    private final boolean supportIn;
    private final boolean supportOut;

    JdbcArgMode(boolean supportIn, boolean supportOut) {
        this.supportIn = supportIn;
        this.supportOut = supportOut;
    }

    public boolean isIn() {
        return this.supportIn;
    }

    public boolean isOut() {
        return this.supportOut;
    }

}
