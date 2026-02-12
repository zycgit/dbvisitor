package net.hasor.dbvisitor.test.oneapi.realdb.elastic7.dto6;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("user_info")
public class UserInfo6 {
    @Column(name = "uid")
    private String userId;
    @Column(name = "name")
    private String userName;
    @Column(name = "loginName")
    private String account;
    @Column(name = "loginPassword")
    private String password;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
