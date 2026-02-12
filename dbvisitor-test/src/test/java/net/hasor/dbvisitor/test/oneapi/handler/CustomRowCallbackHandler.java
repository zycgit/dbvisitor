package net.hasor.dbvisitor.test.oneapi.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;

/**
 * 自定义 RowCallbackHandler - 逐行处理结果
 */
public class CustomRowCallbackHandler implements RowCallbackHandler {

    private List<UserInfo> userList = new ArrayList<>();

    @Override
    public void processRow(ResultSet rs, int rowNum) throws SQLException {
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

        userList.add(user);
    }

    public List<UserInfo> getUserList() {
        return userList;
    }
}
