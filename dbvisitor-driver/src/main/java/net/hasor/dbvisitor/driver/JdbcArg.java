package net.hasor.dbvisitor.driver;

import net.hasor.dbvisitor.dynamic.SqlMode;

class JdbcArg {
    private SqlMode mode;
    private String  name;
    private String  type;
    private Object  value;

    public JdbcArg(String name, SqlMode mode) {
        this.mode = mode;
        this.name = name;
    }

    public SqlMode getMode() {
        return this.mode;
    }

    public void setMode(SqlMode mode) {
        this.mode = mode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
