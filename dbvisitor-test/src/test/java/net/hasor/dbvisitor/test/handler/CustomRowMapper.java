package net.hasor.dbvisitor.test.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.model.UserInfo;

/**
 * 自定义 RowMapper - 将每一行映射为对象
 */
public class CustomRowMapper implements RowMapper<UserInfo> {

    @Override
    public UserInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(rs.getInt("id"));
        user.setName("[Row" + rowNum + "]" + rs.getString("name")); // 添加行号前缀
        user.setAge(rs.getInt("age"));
        user.setEmail(rs.getString("email"));

        try {
            user.setCreateTime(rs.getTimestamp("create_time"));
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }

        return user;
    }
}
