package net.hasor.dbvisitor.test.oneapi.model.naming;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity with a SQL keyword table name ("order") but useDelimited=false (default).
 * Used to verify dialect auto-detection: fmtName should automatically add qualifiers
 * for table names that are SQL keywords.
 */
@Table("order")
public class KeywordTableNoDelimitedEntity {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private String  description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
