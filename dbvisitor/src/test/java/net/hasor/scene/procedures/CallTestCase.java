//package net.hasor.scene.procedures;
//import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
//import net.hasor.dbvisitor.jdbc.SqlParameterUtils;
//import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
//import net.hasor.scene.UserNameResultSetExtractor;
//import net.hasor.test.utils.DsUtils;
//import org.junit.Test;
//
//import java.sql.Connection;
//import java.sql.JDBCType;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//
//public class CallTestCase {
//    @Test
//    public void mysqlCallResultSet_2() throws SQLException {
//        try (Connection conn = initConnection()) {
//            Map<String, Object> objectMap = new JdbcTemplate(conn).call("{call proc_select_multiple_table(?)}",//
//                    Collections.singletonList(SqlParameterUtils.withInput("aaa", JDBCType.VARCHAR.getVendorTypeNumber())));
//            //
//            assert objectMap.size() == 3;
//            assert objectMap.get("#result-set-1") instanceof ArrayList;
//            assert objectMap.get("#result-set-2") instanceof ArrayList;
//            assert objectMap.get("#update-count-3").equals(0);
//            assert ((ArrayList<?>) objectMap.get("#result-set-1")).size() == 1;
//            assert ((ArrayList<?>) objectMap.get("#result-set-2")).size() == 1;
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_name").equals("aaa");
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_id").equals(1);
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_id").equals(1);
//        }
//    }
//
//    @Test
//    public void callBack_0() throws SQLException {
//        try (Connection c = DsUtils.mysqlConn()) {
//            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
//            jdbcTemplate.execute(""//
//                    + "create procedure proc_select_cross_table(in p_name varchar(200), out p_out varchar(200))" //
//                    + " begin " //
//                    + "   select * from proc_table_forcaller where c_name = p_name ;" //
//                    + "   select * from proc_table_forcaller where c_name = p_name ;" //
//                    + "   set p_out = p_name;"//
//                    + " end;");
//
//            List<String> result = jdbcTemplate.executeCallback("{call proc_select_gt_users(?)}", (CallableStatementCallback<List<String>>) cs -> {
//                cs.setInt(1, 40);
//                cs.execute();
//                try (ResultSet rs = cs.getResultSet()) {
//                    return new UserNameResultSetExtractor().extractData(rs);
//                }
//            });
//
//            Map<String, Object> objectMap = jdbcTemplate.call("{call proc_select_cross_table(?,?)}",//
//                    Arrays.asList(//
//                            SqlParameterUtils.withInput("aaa", JDBCType.VARCHAR.getVendorTypeNumber()),//
//                            SqlParameterUtils.withOutputName("bbb", JDBCType.VARCHAR.getVendorTypeNumber())));
//
//            assert objectMap.size() == 4;
//            assert objectMap.get("bbb").equals("aaa");
//            assert objectMap.get("#result-set-1") instanceof ArrayList;
//            assert objectMap.get("#result-set-2") instanceof ArrayList;
//            assert objectMap.get("#update-count-3").equals(0);
//            assert ((ArrayList<?>) objectMap.get("#result-set-1")).size() == 1;
//            assert ((ArrayList<?>) objectMap.get("#result-set-2")).size() == 1;
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_name").equals("aaa");
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_id").equals(1);
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_name").equals("aaa");
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-2")).get(0)).get("c_id").equals(1);
//
//            assert result.size() == 2;
//            assert result.get(0).equals("jon wes");
//            assert result.get(1).equals("mary");
//        }
//    }
//
//    @Test
//    public void mysqlCallResultSet_1() throws SQLException {
//        try (Connection conn = initConnection()) {
//            Map<String, Object> objectMap = new JdbcTemplate(conn).call("{call proc_select_table(?)}",//
//                    Collections.singletonList(SqlParameterUtils.withInput("aaa", JDBCType.VARCHAR.getVendorTypeNumber())));
//            //
//            assert objectMap.size() == 2;
//            assert objectMap.get("#result-set-1") instanceof ArrayList;
//            assert objectMap.get("#update-count-2").equals(0);
//            assert ((ArrayList<?>) objectMap.get("#result-set-1")).size() == 1;
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_name").equals("aaa");
//            assert ((Map) ((ArrayList<?>) objectMap.get("#result-set-1")).get(0)).get("c_id").equals(1);
//        }
//    }
//}
