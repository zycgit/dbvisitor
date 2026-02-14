package net.hasor.dbvisitor.test.model;

import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 复合主键实体 —— 用户角色关联表
 * 主键由 userId + roleId 两列组成。
 */
@Table("user_role")
public class UserRole {
    @Column(value = "user_id", primary = true)
    private Integer userId;

    @Column(value = "role_id", primary = true)
    private Integer roleId;

    @Column("role_name")
    private String roleName;

    @Column("create_time")
    private Date createTime;

    public UserRole() {
    }

    public UserRole(Integer userId, Integer roleId, String roleName) {
        this.userId = userId;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
