package net.hasor.dbvisitor.test.oneapi.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** autoMapping=false，只有 @Column 注解的字段参与映射 */
@Table(value = "user_info", autoMapping = false)
public class ExplicitMappingUser {
    @Column(primary = true)
    private Integer id;
    @Column("name")
    private String  name;
    private Integer age;     // 无 @Column → 不参与映射
    private String  email;    // 无 @Column → 不参与映射
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
