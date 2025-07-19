package net.hasor.dbvisitor.driver;

import java.util.Objects;

public class JdbcColumn {
    public final String catalog;
    public final String schema;
    public final String table;
    public final String name;
    public final String type;

    public JdbcColumn(String name, String type, String table, String catalog, String schema) {
        if (name == null) {
            throw new IllegalArgumentException("[name] must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("[type] must not be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("[table] must not be null");
        }
        if (catalog == null) {
            throw new IllegalArgumentException("[catalog] must not be null");
        }
        if (schema == null) {
            throw new IllegalArgumentException("[schema] must not be null");
        }
        this.name = name;
        this.type = type;
        this.table = table;
        this.catalog = catalog;
        this.schema = schema;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        if (!this.table.isEmpty()) {
            b.append(this.table).append('.');
        }
        b.append(this.name).append("<type=[").append(this.type).append(']');
        if (!this.catalog.isEmpty()) {
            b.append(" catalog=[").append(this.catalog).append(']');
        }
        if (!this.schema.isEmpty()) {
            b.append(" schema=[").append(this.schema).append(']');
        }
        return b.append('>').toString();
    }

    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        JdbcColumn other = (JdbcColumn) obj;
        return (this.name.equals(other.name) &&      //
                this.type.equals(other.type) &&      //
                this.table.equals(other.table) &&    //
                this.catalog.equals(other.catalog) &&//
                this.schema.equals(other.schema));
    }

    public int hashCode() {
        return Objects.hash(this.name, this.type, this.table, this.catalog, this.schema);
    }
}