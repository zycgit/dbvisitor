package net.hasor.dbvisitor.test.suite.mapping.annotation;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.model.annotation.Md5User;
import net.hasor.dbvisitor.test.model.annotation.TemplateUser;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 验证 SQL 模板支持数据库函数 (如 MD5) 以及 @Column 的各种模版能力.
 */
public class SqlTemplateFunctionTest extends AbstractOneApiTest {
    private String sqlTemplatePath() {
        return "/sql/" + OneApiDataSourceManager.getDbDialect() + "/sql_template.sql";
    }

    @Test
    public void testMd5Template() throws SQLException, IOException {
        jdbcTemplate.loadSplitSQL(";", sqlTemplatePath());

        Md5User user = new Md5User();
        user.setId("1");
        user.setName("user1");
        user.setPassword("123456");

        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(Md5User.class).applyEntity(user).executeSumResult();

        // Verify stored value is hashed
        Map<String, Object> map = jdbcTemplate.queryForMap("SELECT password FROM test_md5_user WHERE id = '1'");
        String storedPwd = (String) map.get("password");

        // MD5('123456') = 'e10adc3949ba59abbe56e057f20f883e'
        // NOTE: This assumes the underlying DB (PostgreSQL) supports MD5 function.
        assertEquals("e10adc3949ba59abbe56e057f20f883e", storedPwd);
    }

    @Test
    public void testTemplateFeatures() throws SQLException, IOException {
        jdbcTemplate.loadSplitSQL(";", sqlTemplatePath());
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 1. 测试 insertTemplate
        TemplateUser user = new TemplateUser();
        user.setId(1001);
        user.setName("lower_case");
        user.setLoginIp("1.1.1.1");    // insertTemplate = "concat('PRE_', ?)"
        user.setCreateAt(new java.util.Date());
        user.setData("init");
        lambda.insert(TemplateUser.class).applyEntity(user).executeSumResult();

        // 2. 测试 select
        TemplateUser stored = lambda.query(TemplateUser.class).eq(TemplateUser::getId, 1001).queryForObject();
        assertNotNull(stored);

        // name 应该是大写 (insertTemplate="UPPER(?)")
        assertEquals("LOWER_CASE", stored.getName());

        // loginIp 应该是模板值
        assertEquals("PRE_1.1.1.1", stored.getLoginIp());

        // createAt
        assertNotNull(stored.getCreateAt());

        // 3. 测试 setValueTemplate (concat(?, '_updated'))
        lambda.update(TemplateUser.class).eq(TemplateUser::getId, 1001).updateTo(TemplateUser::getData, "test_val").doUpdate();

        TemplateUser updated = lambda.query(TemplateUser.class).eq(TemplateUser::getId, 1001).queryForObject();
        assertEquals("test_val_updated", updated.getData());
    }
}
