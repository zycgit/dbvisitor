package net.hasor.dbvisitor.test.oneapi.model.annotation;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table("test_template_user")
public class TemplateUser {
    @Column(primary = true)
    private Integer id;

    @Column(insertTemplate = "UPPER(?)")
    private String name;

    @Column(name = "login_ip", insertTemplate = "concat('PRE_', ?)")
    private String loginIp;

    @Column(name = "create_at")
    private Date createAt;

    @Column(setValueTemplate = "concat(?, '_updated')")
    private String data;

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

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
