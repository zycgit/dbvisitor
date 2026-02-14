package net.hasor.dbvisitor.test.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** @Column(value="xxx") 指定列名 (value 等价于 name) */
@Table("user_info")
public class ColumnValueUser {
    @Column(primary = true)
    private Integer id;
    @Column(value = "name")
    private String  userName;
    private Integer age;
    private String  email;
    @Column(value = "create_time")
    private Date    createTime;

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
