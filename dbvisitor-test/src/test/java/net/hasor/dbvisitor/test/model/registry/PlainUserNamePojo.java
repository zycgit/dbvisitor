package net.hasor.dbvisitor.test.model.registry;

/** 无 @Table 注解的普通类（id/userName） */
public class PlainUserNamePojo {
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
