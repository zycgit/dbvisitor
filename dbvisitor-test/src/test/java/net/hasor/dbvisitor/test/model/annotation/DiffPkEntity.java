package net.hasor.dbvisitor.test.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("diff_pk_entity")
public class DiffPkEntity {
    @Column(primary = true)
    private String code;
    @Column
    private int    id; // Same name as UserInfo's PK, but here it is NOT PK

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
