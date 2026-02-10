package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/** useDelimited=true 的实体 */
@Table(value = "user_info", useDelimited = true)
public class DelimitedUser {
    @Column(primary = true)
    private Integer id;
    private String  name;
    private Integer age;
    @Column("create_time")
    private Date    createTime;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }

    public void setAge(Integer age) { this.age = age; }

    public Date getCreateTime() { return createTime; }

    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
