package net.hasor.dbvisitor.test.oneapi.model.annotation;

import net.hasor.dbvisitor.mapping.Column;

/** 多层继承 */
public class NamedBaseEntity extends BaseEntity {
    @Column("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
