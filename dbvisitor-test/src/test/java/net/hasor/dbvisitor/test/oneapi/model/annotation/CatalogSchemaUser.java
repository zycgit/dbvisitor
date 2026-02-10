package net.hasor.dbvisitor.test.oneapi.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** @Table 指定 catalog 和 schema 属性 */
@Table(catalog = "test_catalog", schema = "test_schema", value = "user_info")
public class CatalogSchemaUser {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private Integer age;

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
