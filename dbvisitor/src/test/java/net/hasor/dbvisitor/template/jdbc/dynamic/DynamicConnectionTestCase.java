package net.hasor.dbvisitor.template.jdbc.dynamic;
import net.hasor.dbvisitor.template.jdbc.UserDTO;
import net.hasor.dbvisitor.template.jdbc.core.JdbcTemplate;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

/** 通过 DynamicConnection 接口实现 JdbcTemplate 的动态连接 */
public class DynamicConnectionTestCase {
    @Test
    public void callBack_0() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDynamic(new H2DynamicConnection());

        Object[] args = new Object[] { 40 };
        List<UserDTO> result = jdbcTemplate.queryForList("select * from user_table where age > ? order by id", args, UserDTO.class);

        assert result.size() == 2;
        assert result.get(0).getName().equals("jon wes");
        assert result.get(1).getName().equals("mary");
    }
}