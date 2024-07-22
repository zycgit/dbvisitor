package net.hasor.scene.jdbc.statementcreator;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.scene.UserNameResultSetExtractor;
import net.hasor.scene.UserNameRowCallback;
import net.hasor.scene.UserNameRowMapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/** 使用 PreparedStatementCreator 接口创建 PreparedStatement 对象以执行参数化SQL查询 */
public class PreparedStatementCreatorTestCase {
    @Test
    public void callBack_0() throws SQLException {
        // PreparedStatementCreator and ResultSetExtractor
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCreator(con -> {
                PreparedStatement ps = con.prepareStatement("select * from user_table where age > ? order by id");
                ps.setInt(1, 40);
                return ps;
            }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void callBack_1() throws SQLException {
        // PreparedStatementCreator and RowCallbackHandler
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();

            jdbcTemplate.executeCreator(con -> {
                PreparedStatement ps = con.prepareStatement("select * from user_table where age > ? order by id");
                ps.setInt(1, 40);
                return ps;
            }, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void callBack_2() throws SQLException {
        // PreparedStatementCreator and RowMapper
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCreator(con -> {
                PreparedStatement ps = con.prepareStatement("select * from user_table where age > ? order by id");
                ps.setInt(1, 40);
                return ps;
            }, new UserNameRowMapper());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }
}