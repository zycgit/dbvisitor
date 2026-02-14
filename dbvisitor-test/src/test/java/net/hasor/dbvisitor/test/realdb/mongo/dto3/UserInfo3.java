package net.hasor.dbvisitor.test.realdb.mongo.dto3;
import net.hasor.dbvisitor.mapping.Column;

public class UserInfo3 {
    @Column("uid")
    private String uid;
    @Column("name")
    private String name;
    @Column("loginName")
    private String loginName;
    @Column("loginPassword")
    private String loginPassword;

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPassword() {
        return this.loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }
}
