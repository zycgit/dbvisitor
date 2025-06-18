package net.hasor.dbvisitor.driver;

class JdbcArg {
    private JdbcArgMode mode;
    private String      name;
    private String      type;
    private Object      value;

    public JdbcArg(String name, JdbcArgMode mode) {
        this.mode = mode;
        this.name = name;
    }

    public JdbcArgMode getMode() {
        return this.mode;
    }

    public void setMode(JdbcArgMode mode) {
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
