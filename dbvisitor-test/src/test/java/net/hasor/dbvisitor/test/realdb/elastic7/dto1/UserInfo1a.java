package net.hasor.dbvisitor.test.realdb.elastic7.dto1;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("user_info")
public class UserInfo1a {
    @Column(value = "uid", primary = true)
    private String uid;
    @Column("name")
    private String name;
    @Column("loginName")
    private String loginName;
    @Column("loginPassword")
    private String loginPassword;
    @Column("uid.keyword")
    private String uidKeyword;

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

    public String getUidKeyword() {
        return uidKeyword;
    }

    public void setUidKeyword(String uidKeyword) {
        this.uidKeyword = uidKeyword;
    }
}
