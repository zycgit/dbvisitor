package net.hasor.dbvisitor.test.oneapi.model;

/** 无 @Table 注解的类，使用 Options 全局默认值 */
public class PlainEntity {
    private Integer id;
    private String  userName;

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
}
