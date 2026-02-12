package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.IndexDescribe;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.mapping.TableDescribe;

@Table("idx_empty_col_table")
@TableDescribe(comment = "test")
@IndexDescribe(name = "idx_ok", columns = { "id" })
@IndexDescribe(name = "idx_test", columns = { "name", "" })
public class EmptyIndexColEntity {
    @Column(value = "id", primary = true)
    private Integer id;

    @Column("name")
    private String name;

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
}
