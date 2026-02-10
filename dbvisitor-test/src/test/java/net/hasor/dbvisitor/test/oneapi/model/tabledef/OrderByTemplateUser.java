package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** orderByColTemplate 属性的实体 */
@Table("user_info")
public class OrderByTemplateUser {
    @Column(primary = true)
    private Integer id;
    @Column(value = "name", orderByColTemplate = "LOWER(name)")
    private String  name;
    @Column(value = "age", orderByColTemplate = "age * -1")
    private Integer age;
    private String  email;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }

    public void setAge(Integer age) { this.age = age; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}
