package net.hasor.dbvisitor.test.realdb.milvus.dto;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("tb_user_info_milvus")
public class UserInfoMilvus {
    @Column(value = "uid", primary = true)
    private String uid;
    @Column("name")
    private String name;
    @Column("loginName")
    private String loginName;
    @Column("loginPassword")
    private String loginPassword;
    @Column("v")
    private List<Float> v;

    public String getUid() { return this.uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getLoginName() { return this.loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getLoginPassword() { return this.loginPassword; }
    public void setLoginPassword(String loginPassword) { this.loginPassword = loginPassword; }
    public List<Float> getV() { return v; }
    public void setV(List<Float> v) { this.v = v; }
}
