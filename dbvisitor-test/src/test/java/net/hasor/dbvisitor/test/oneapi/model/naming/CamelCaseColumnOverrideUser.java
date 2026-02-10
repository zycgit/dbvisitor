package net.hasor.dbvisitor.test.oneapi.model.naming;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity with mapUnderscoreToCamelCase=true, but some properties use @Column to override.
 * Property 'userName' has @Column("name"), overriding the camelCase conversion to 'user_name'.
 */
@Table(value = "user_info", mapUnderscoreToCamelCase = true)
public class CamelCaseColumnOverrideUser {
    @Column(primary = true)
    private Integer id;
    @Column("name")
    private String  userName; // @Column override: maps to "name", not auto-converted "user_name"
    private Integer age;
    private String  email;
    private Date    createTime; // auto-converted to "create_time"

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
