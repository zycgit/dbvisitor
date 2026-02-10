package net.hasor.dbvisitor.test.oneapi.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.ResultMap;

@ResultMap(space = "mySpace", id = "myResultMap", autoMapping = true, caseInsensitive = true, mapUnderscoreToCamelCase = true)
public class UserResultMap {
    private Integer id;
    private String  name;
    private Integer age;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
