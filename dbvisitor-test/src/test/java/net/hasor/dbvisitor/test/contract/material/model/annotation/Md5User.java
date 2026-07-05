package net.hasor.dbvisitor.test.contract.material.model.annotation;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

@Table(value = "test_md5_user")
public class Md5User extends AbstractMd5User {
    @Override
    @Column(primary = true)
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    @Column
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    @Column(insertTemplate = "MD5(?)")
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }
}
