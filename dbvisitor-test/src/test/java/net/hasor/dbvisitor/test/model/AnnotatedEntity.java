package net.hasor.dbvisitor.test.model;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** 有 @Table 注解的类，所有属性使用注解默认值 */
@Table("annotated_table")
public class AnnotatedEntity {
    @Column(primary = true)
    private Integer id;
    private String  name;

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
