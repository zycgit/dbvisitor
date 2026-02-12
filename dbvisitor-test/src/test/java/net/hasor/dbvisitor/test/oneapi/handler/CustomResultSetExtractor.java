package net.hasor.dbvisitor.test.oneapi.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;

/**
 * 自定义 ResultSetExtractor - 处理整个 ResultSet
 */
public class CustomResultSetExtractor implements ResultSetExtractor<List<UserInfo>> {

    @Override
    public List<UserInfo> extractData(ResultSet rs) throws SQLException {
        List<UserInfo> result = new ArrayList<>();

        while (rs.next()) {
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

            result.add(user);
        }

        return result;
    }
}
