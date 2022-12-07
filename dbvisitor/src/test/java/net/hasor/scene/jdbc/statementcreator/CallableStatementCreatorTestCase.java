package net.hasor.scene.jdbc.statementcreator;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.scene.UserNameResultSetExtractor;
import net.hasor.scene.UserNameRowCallback;
import net.hasor.scene.UserNameRowMapper;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** 使用 CallableStatementCreator 接口创建 CallableStatement 对象以执行存储过程调用 */
public class CallableStatementCreatorTestCase {

    @Test
    public void callBack_0() throws SQLException {
        // CallableStatementCreator and ResultSetExtractor
        try (Connection c = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCall(con -> {
                CallableStatement cs = con.prepareCall("{call proc_select_gt_users(?)}");
                cs.setInt(1, 40);
                return cs;
            }, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void callBack_1() throws SQLException {
        // CallableStatementCreator and ResultSetExtractor
        try (Connection c = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            UserNameRowCallback callback = new UserNameRowCallback();

            jdbcTemplate.executeCall(con -> {
                CallableStatement cs = con.prepareCall("{call proc_select_gt_users(?)}");
                cs.setInt(1, 40);
                return cs;
            }, callback);

            assert callback.size() == 2;
            assert callback.getName(0).equals("jon wes");
            assert callback.getName(1).equals("mary");
        }
    }

    @Test
    public void callBack_2() throws SQLException {
        // CallableStatementCreator and RowMapper
        try (Connection c = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCall(con -> {
                CallableStatement cs = con.prepareCall("{call proc_select_gt_users(?)}");
                cs.setInt(1, 40);
                return cs;
            }, new UserNameRowMapper());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void callBack_3() throws SQLException {
        // CallableStatementCreator and CallableStatementCallback
        try (Connection c = DsUtils.mysqlConn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.executeCall(con -> {
                CallableStatement cs = con.prepareCall("{call proc_select_gt_users(?)}");
                cs.setInt(1, 40);
                return cs;
            }, (CallableStatementCallback<List<String>>) ps -> {
                ps.setInt(1, 40);
                try (ResultSet rs = ps.executeQuery()) {
                    return new UserNameResultSetExtractor().extractData(rs);
                }
            });

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }
}