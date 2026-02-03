package net.hasor.realdb.milvus.dto6;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("tb_mapper_user_milvus")
public class UserInfoMilvus6 {
    @Column(value = "uid", primary = true)
    private String userId;

    @Column("name")
    private String userName;

    @Column("loginName")
    private String account;

    @Column("loginPassword")
    private String password;

    @Column("v")
    private List<Float> vector;

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

    public List<Float> getVector() {
        return vector;
    }

    public void setVector(List<Float> vector) {
        this.vector = vector;
    }
}
