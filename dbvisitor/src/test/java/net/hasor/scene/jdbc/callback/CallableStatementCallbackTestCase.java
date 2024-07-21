//package net.hasor.scene.jdbc.callback;
//import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
//import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
//import net.hasor.scene.UserNameResultSetExtractor;
//import net.hasor.test.utils.DsUtils;
//import org.junit.Test;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
///** 存储过程调用使用 CallableStatementCallback 接口来处理 */
//public class CallableStatementCallbackTestCase {
//    @Test
//    public void callBack_0() throws SQLException {
//        try (Connection c = DsUtils.mysqlConn()) {
//            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
//
//            List<String> result = jdbcTemplate.executeCallback("{call proc_select_gt_users(?)}", (CallableStatementCallback<List<String>>) cs -> {
//                cs.setInt(1, 40);
//                cs.execute();
//                try (ResultSet rs = cs.getResultSet()) {
//                    return new UserNameResultSetExtractor().extractData(rs);
//                }
//            });
//
//            assert result.size() == 2;
//            assert result.get(0).equals("jon wes");
//            assert result.get(1).equals("mary");
//        }
//    }
//
//    @Test
//    public void callBack_2() throws SQLException {
//        try (Connection c = DsUtils.mysqlConn()) {
//            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
//
//            List<List<String>> result = jdbcTemplate.executeCallback("{call proc_select_gt_users_repeat(?)}", (CallableStatementCallback<List<List<String>>>) cs -> {
//                cs.setInt(1, 40);
//                cs.execute();
//
//                List<List<String>> dataSet = new ArrayList<>();
//                try (ResultSet rs = cs.getResultSet()) {
//                    dataSet.add(new UserNameResultSetExtractor().extractData(rs));
//                }
//
//                while (cs.getMoreResults()) {
//                    try (ResultSet rs = cs.getResultSet()) {
//                        dataSet.add(new UserNameResultSetExtractor().extractData(rs));
//                    }
//                }
//                return dataSet;
//            });
//
//            assert result.size() == 2;
//            assert result.get(0).size() == 2;
//            assert result.get(1).size() == 2;
//
//            assert result.get(0).get(0).equals("jon wes");
//            assert result.get(0).get(1).equals("mary");
//            assert result.get(1).get(0).equals("jon wes");
//            assert result.get(1).get(1).equals("mary");
//        }
//    }
//}