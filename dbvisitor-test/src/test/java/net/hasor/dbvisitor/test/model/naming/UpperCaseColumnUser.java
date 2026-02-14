package net.hasor.dbvisitor.test.model.naming;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity with caseInsensitive=true (default) and uppercase @Column names.
 * @Column uses UPPERCASE names ("ID", "NAME", etc.).
 * Since caseInsensitive=true, the result-set column mapping uses
 * LinkedCaseInsensitiveMap, so "id" (from PG) matches "ID" (from @Column).
 */
@Table(value = "user_info", caseInsensitive = true)
public class UpperCaseColumnUser {
    @Column(primary = true, value = "ID")
    private Integer id;
    @Column("NAME")
    private String  name;
    @Column("AGE")
    private Integer age;
    @Column("EMAIL")
    private String  email;
    @Column("CREATE_TIME")
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
