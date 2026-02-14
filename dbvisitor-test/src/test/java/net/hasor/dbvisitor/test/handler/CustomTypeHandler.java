package net.hasor.dbvisitor.test.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.types.TypeHandler;

/**
 * 自定义 TypeHandler - 处理特定类型的数据
 */
public class CustomTypeHandler implements TypeHandler<UserInfo> {

    @Override
    public void setParameter(PreparedStatement ps, int i, UserInfo parameter, Integer jdbcType) throws SQLException {
        // 这里只是示例，实际使用中可能不会这样设置
        if (parameter != null) {
            ps.setInt(i, parameter.getId());
        } else {
            ps.setNull(i, java.sql.Types.INTEGER);
        }
    }

    @Override
    public UserInfo getResult(ResultSet rs, int columnIndex) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(rs.getInt(columnIndex));
        user.setName(rs.getString("name"));
        user.setAge(rs.getInt("age"));
        user.setEmail(rs.getString("email"));

        try {
            user.setCreateTime(rs.getTimestamp("create_time"));
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }

        return user;
    }

    @Override
    public UserInfo getResult(ResultSet rs, String columnName) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setAge(rs.getInt("age"));
        user.setEmail(rs.getString("email"));

        try {
            user.setCreateTime(rs.getTimestamp("create_time"));
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }

        return user;
    }

    @Override
    public UserInfo getResult(CallableStatement cs, int columnIndex) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(cs.getInt(columnIndex));

        return user;
    }
}
