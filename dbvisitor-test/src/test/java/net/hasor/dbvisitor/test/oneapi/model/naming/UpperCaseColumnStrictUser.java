package net.hasor.dbvisitor.test.oneapi.model.naming;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Entity with caseInsensitive=false and uppercase @Column names.
 * Since caseInsensitive=false, the result-set column mapping uses a
 * regular LinkedHashMap. PG returns lowercase columns ("id", "name", etc.)
 * which won't match the UPPERCASE keys ("ID", "NAME", etc.) → fields are null.
 * INSERT still works because PG lowercases unquoted identifiers automatically.
 */
@Table(value = "user_info", caseInsensitive = false)
public class UpperCaseColumnStrictUser {
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
