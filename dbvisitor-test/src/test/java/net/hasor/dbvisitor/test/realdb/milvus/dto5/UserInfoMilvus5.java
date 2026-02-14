package net.hasor.dbvisitor.test.realdb.milvus.dto5;

import java.util.List;

public class UserInfoMilvus5 {
    private String      userId;
    private String      userName;
    private String      account;
    private String      password;
    private List<Float> vector;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Float> getVector() { return vector; }
    public void setVector(List<Float> vector) { this.vector = vector; }
}
