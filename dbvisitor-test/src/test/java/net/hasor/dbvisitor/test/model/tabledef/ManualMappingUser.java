package net.hasor.dbvisitor.test.model.tabledef;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** autoMapping=false 的实体（只有 @Column 标注的字段才参与映射） */
@Table(value = "user_info", autoMapping = false)
public class ManualMappingUser {
    @Column(primary = true)
    private Integer id;
    @Column("name")
    private String  name;
    private Integer age;    // 未标注 @Column，不参与映射
    private String  email;  // 未标注 @Column，不参与映射
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
