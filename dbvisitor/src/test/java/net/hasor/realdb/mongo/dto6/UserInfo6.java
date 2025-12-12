package net.hasor.realdb.mongo.dto6;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("user_info")
public class UserInfo6 {
    @Column(name = "uid", primary = true)
    private String userId;
    @Column("name")
    private String userName;
    @Column("loginName")
    private String account;
    @Column("loginPassword")
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
