package net.hasor.dbvisitor.test.oneapi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.config.OneApiDataSourceManager;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class AbstractOneApiTest {
    protected static DataSource     dataSource;
    @Rule
    public TestName testName = new TestName();
    protected        JdbcTemplate   jdbcTemplate;
    protected        LambdaTemplate lambdaTemplate;

    @Before
    public void setup() throws IOException, SQLException {
        checkTestSkip();

        if (dataSource == null) {
            dataSource = OneApiDataSourceManager.createDataSource();
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
        lambdaTemplate = new LambdaTemplate(jdbcTemplate);

        // Ensure schema exists (workaround for H2 memory DB connection pooling issues)
        ensureSchemaExists();

        // Clean test data before each test
        cleanTestData();

        // Database initialization (schema + baseline data) is handled by OneApiDataSourceManager
        // Tests can override initData() to load additional test-specific data
        initData();
    }

    private void checkTestSkip() {
        String skipList = OneApiDataSourceManager.getProperty("test.skip.cases");
        if (skipList != null && !skipList.isEmpty()) {
            Set<String> skippedTests = Arrays.stream(skipList.split(",")).map(String::trim).collect(Collectors.toSet());
            String currentTest = testName.getMethodName();
            if (skippedTests.contains(currentTest)) {
                System.out.println("Skipping test " + currentTest + " as configured in test.skip.cases");
                Assume.assumeTrue("Skipping test " + currentTest + " as configured", false);
            }
        }
    }

    protected void requiresFeature(String feature) {
        String features = OneApiDataSourceManager.getProperty("test.features");
        if (features == null || features.trim().isEmpty()) {
            // 未配置 test.features 时，跳过所有需要特殊 feature 的测试
            System.out.println("Skipping test " + testName.getMethodName() + " because 'test.features' is not configured.");
            Assume.assumeTrue("Feature '" + feature + "' not supported (test.features not configured)", false);
            return;
        }

        Set<String> supportedFeatures = Arrays.stream(features.split(",")).map(String::trim).collect(Collectors.toSet());
        if (!supportedFeatures.contains(feature)) {
            System.out.println("Skipping test " + testName.getMethodName() + " because feature '" + feature + "' is not supported.");
            Assume.assumeTrue("Feature '" + feature + "' not supported", false);
        }
    }

    /**
     * Ensure schema exists (workaround for H2 memory DB connection pooling)
     * Check if user_info table exists, if not, re-initialize
     */
    protected void ensureSchemaExists() {
        try {
            // Try a simple query to check if tables exist
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE 1=0", Integer.class);
        } catch (Exception e) {
            // Tables don't exist, need to re-initialize
            System.out.println("[OneAPI] Schema not found in current connection, re-initializing...");
            System.out.println("[OneAPI] Error was: " + e.getClass().getName() + ": " + e.getMessage());
            try {
                String dialect = OneApiDataSourceManager.getDbDialect();
                String initScript = "/oneapi/sql/" + dialect + "/init.sql";
                System.out.println("[OneAPI] Loading script: " + initScript);
                jdbcTemplate.loadSplitSQL(";", initScript);

                // Verify tables were created
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE 1=0", Integer.class);
                System.out.println("[OneAPI] Schema re-initialized successfully, user_info table exists");
            } catch (Exception ex) {
                System.err.println("[OneAPI] Failed to re-initialize schema: " + ex.getMessage());
                ex.printStackTrace();
                throw new RuntimeException("Schema initialization failed", ex);
            }
        }
    }

    /**
     * Clean test data before each test
     */
    protected void cleanTestData() {
        try {
            // Delete in reverse order of foreign key dependencies
            jdbcTemplate.executeUpdate("DELETE FROM user_order");
            jdbcTemplate.executeUpdate("DELETE FROM user_info");
            jdbcTemplate.executeUpdate("DELETE FROM complex_order");
            jdbcTemplate.executeUpdate("DELETE FROM product_vector");
            jdbcTemplate.executeUpdate("DELETE FROM array_types_test");
            jdbcTemplate.executeUpdate("DELETE FROM array_types_explicit_test");
            jdbcTemplate.executeUpdate("DELETE FROM basic_types_test");
            jdbcTemplate.executeUpdate("DELETE FROM basic_types_explicit_test");
            // Composite primary key test table
            try {
                jdbcTemplate.executeUpdate("DELETE FROM user_role");
            } catch (Exception ignored) {
            }
            // Case sensitivity test tables (may not exist for all dialects)
            try {
                jdbcTemplate.executeUpdate("DELETE FROM case_test_lower");
            } catch (Exception ignored) {
            }
            try {
                jdbcTemplate.executeUpdate("DELETE FROM \"Case_Test_Upper\"");
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            // Ignore - tables might not exist yet
            System.out.println("[OneAPI] Data cleanup skipped: " + e.getMessage());
        }
    }

    /**
     * 创建新的 Session 实例
     */
    protected Session newSession() throws SQLException {
        Configuration configuration = new Configuration();
        return configuration.newSession(dataSource);
    }

    /**
     * Optional: Override in subclasses to load test-specific data
     * Schema initialization is handled automatically by OneApiDataSourceManager
     */
    protected void initData() throws SQLException {
        // Default: no additional data
    }
}