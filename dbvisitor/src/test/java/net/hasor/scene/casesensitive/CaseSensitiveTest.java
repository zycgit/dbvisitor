package net.hasor.scene.casesensitive;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQueryOperation;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CaseSensitiveTest {
    // 默认大小写不敏感，数据字段全大写
    @Test
    public void insertMapByUpperKeys() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.deleteByTable("USER_TABLE").allowEmptyWhere().doDelete();

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("AGE", 120);
            userData.put("NAME", "default user");
            userData.put("CREATE_TIME", "2022-01-01 12:12:12");

            InsertOperation<Map<String, Object>> insert = lambdaTemplate.insertByTable("USER_TABLE").asMap().applyMap(userData);
            assert insert.getBoundSql().getSqlString().equals("INSERT INTO USER_TABLE (AGE, NAME, CREATE_TIME) VALUES (?, ?, ?)");
            assert insert.executeSumResult() == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("USER_TABLE").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("AGE", 120).queryForObject();
            assert resultData.get("name").equals("default user");
        }
    }

    // 默认大小写不敏感，数据字段全小写
    @Test
    public void insertMapByLowerKeys() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.deleteByTable("USER_TABLE").allowEmptyWhere().doDelete();

            Map<String, Object> userData = new LinkedHashMap<>();
            userData.put("AGE", 120);
            userData.put("NAME", "default user");
            userData.put("CREATE_TIME", "2022-01-01 12:12:12");

            InsertOperation<Map<String, Object>> insert = lambdaTemplate.insertByTable("USER_TABLE").asMap().applyMap(userData);
            assert insert.getBoundSql().getSqlString().equals("INSERT INTO USER_TABLE (AGE, NAME, CREATE_TIME) VALUES (?, ?, ?)");
            assert insert.executeSumResult() == 1;

            // 校验结果
            MapQueryOperation lambdaQuery = lambdaTemplate.queryByTable("user_table").asMap();
            Map<String, Object> resultData = lambdaQuery.eq("age", 120).queryForObject();
            assert resultData.get("name").equals("default user");
        }
    }

    // 大小写敏感（参数/表名/列名/结果集）全部敏感
    @Test
    public void caseSensitive() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            lambdaTemplate.getJdbc().setResultsCaseInsensitive(false); //设置为敏感

            // H2 列名/表名 默认都是大写的
            Map<String, Object> resultData = lambdaTemplate.queryByTable("USER_TABLE").asMap()//
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
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            //lambdaTemplate.setUseQualifier(true); //请求参数使用限定符

            String sqlString1 = lambdaTemplate.queryByTable("USER_TABLE").eq("ID", 1).getBoundSql().getSqlString();
            assert sqlString1.equals("SELECT * FROM \"USER_TABLE\" WHERE \"ID\" = ?");

            //lambdaTemplate.setUseQualifier(false); //请求参数不使用限定符

            String sqlString2 = lambdaTemplate.queryByTable("USER_TABLE").eq("ID", 1).getBoundSql().getSqlString();
            assert sqlString2.equals("SELECT * FROM USER_TABLE WHERE ID = ?");
        }
    }
}
