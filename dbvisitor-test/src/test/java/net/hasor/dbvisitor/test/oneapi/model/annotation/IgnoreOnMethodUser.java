package net.hasor.dbvisitor.test.oneapi.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Ignore;
import net.hasor.dbvisitor.mapping.Table;

/** @Ignore 标注在 getter 方法上 */
@Table("user_info")
public class IgnoreOnMethodUser {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private Integer age;
    private String  email;
    @Column("create_time")
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

    @Ignore
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
