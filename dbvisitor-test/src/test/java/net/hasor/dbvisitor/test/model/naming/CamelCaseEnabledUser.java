package net.hasor.dbvisitor.test.model.naming;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity with mapUnderscoreToCamelCase enabled.
 * Property 'createTime' auto-maps to column 'create_time' without explicit @Column.
 */
@Table(value = "user_info", mapUnderscoreToCamelCase = true)
public class CamelCaseEnabledUser {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private Integer age;
    private String  email;
    private Date    createTime; // auto-mapped to create_time

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
