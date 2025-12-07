package net.hasor.scene.wrapper.casesensitive;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQuery;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class CaseSensitiveTest {
    // 默认大小写不敏感，数据字段全大写
    @Test
    public void insertMapByUpperKeys() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = new LambdaTemplate(c);
            wrapper.deleteFreedom("USER_TABLE").allowEmptyWhere().doDelete();

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("AGE", 120);
            userData.put("NAME", "default user");
            userData.put("CREATE_TIME", "2022-01-01 12:12:12");

            Insert<Map<String, Object>> insert = wrapper.insertFreedom("USER_TABLE").applyMap(userData);
            assert insert.getBoundSql().getSqlString().equals("INSERT INTO USER_TABLE (AGE, NAME, CREATE_TIME) VALUES (?, ?, ?)");
            assert insert.executeSumResult() == 1;

            // 校验结果
            MapQuery query = wrapper.queryFreedom("USER_TABLE");
            Map<String, Object> resultData = query.eq("AGE", 120).queryForObject();
            assert resultData.get("name").equals("default user");
        }
    }

    // 默认大小写不敏感，数据字段全小写
    @Test
    public void insertMapByLowerKeys() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = new LambdaTemplate(c);
            wrapper.deleteFreedom("USER_TABLE").allowEmptyWhere().doDelete();

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("AGE", 120);
            userData.put("NAME", "default user");
            userData.put("CREATE_TIME", "2022-01-01 12:12:12");

            Insert<Map<String, Object>> insert = wrapper.insertFreedom("USER_TABLE").applyMap(userData);
            assert insert.getBoundSql().getSqlString().equals("INSERT INTO USER_TABLE (AGE, NAME, CREATE_TIME) VALUES (?, ?, ?)");
            assert insert.executeSumResult() == 1;

            // 校验结果
            MapQuery query = wrapper.queryFreedom("user_table");
            Map<String, Object> resultData = query.eq("age", 120).queryForObject();
            assert resultData.get("name").equals("default user");
        }
    }

    // 大小写敏感（参数/表名/列名/结果集）全部敏感
    @Test
    public void caseSensitive() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = new LambdaTemplate(c, Options.of().caseInsensitive(false));

            // H2 列名/表名 默认都是大写的
            Map<String, Object> resultData = wrapper.queryFreedom("USER_TABLE")//
                    .eq("ID", 1).queryForObject();

            // H2 列名默认都是大写的
            assert resultData.get("NAME").equals("mali");
            assert resultData.get("name") == null;
        }
    }

    // 请求语句使用限定符
    @Test
    public void qualifierTest() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            Options o1 = Options.of().dialect(SqlDialectRegister.findDialect(c)).useDelimited(true);
            LambdaTemplate wrapper1 = new LambdaTemplate(c, o1);
            String sqlString1 = wrapper1.queryFreedom("USER_TABLE").eq("ID", 1).getBoundSql().getSqlString();
            assert sqlString1.equals("SELECT * FROM \"USER_TABLE\" WHERE \"ID\" = ?");

            Options o2 = Options.of().dialect(SqlDialectRegister.findDialect(c)).useDelimited(false);
            LambdaTemplate wrapper2 = new LambdaTemplate(c, o2);
            String sqlString2 = wrapper2.queryFreedom("USER_TABLE").eq("ID", 1).getBoundSql().getSqlString();
            assert sqlString2.equals("SELECT * FROM USER_TABLE WHERE ID = ?");
        }
    }
}
