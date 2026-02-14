package net.hasor.dbvisitor.test.model.naming;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;

/**
 * Plain entity WITHOUT @Table annotation.
 * When used with LambdaTemplate(dataSource, Options), the naming options
 * from Options serve as global defaults:
 * - mapUnderscoreToCamelCase=true → table name: plain_user, column createTime → create_time
 * - mapUnderscoreToCamelCase=false → table name: PlainUser (PG lowercases to "plainuser")
 */
public class PlainUser {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private Integer age;
    private String  email;
    private Date    createTime;

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
