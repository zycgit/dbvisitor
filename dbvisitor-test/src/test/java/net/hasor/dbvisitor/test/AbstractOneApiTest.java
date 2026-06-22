package net.hasor.dbvisitor.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import net.hasor.dbvisitor.jdbc.core.JdbcQueryContext;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;

public abstract class AbstractOneApiTest {
    protected static DataSource dataSource;
    @Rule
    public TestName             testName = new TestName();
    protected JdbcTemplate      jdbcTemplate;
    protected LambdaTemplate    lambdaTemplate;

    @Before
    public void setup() throws IOException, SQLException {
        checkTestSkip();

        if (dataSource == null) {
            dataSource = OneApiDataSourceManager.createDataSource();
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
        registerCommonMacros(jdbcTemplate);
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
        String skipFeatures = OneApiDataSourceManager.getProperty("test.skip.features");
        if (skipFeatures == null || skipFeatures.trim().isEmpty()) {
            return; // 未配置黑名单，默认所有特性都启用
        }

        Set<String> disabledFeatures = Arrays.stream(skipFeatures.split(",")).map(String::trim).collect(Collectors.toSet());
        if (disabledFeatures.contains(feature)) {
            System.out.println("Skipping test " + testName.getMethodName() + " because feature '" + feature + "' is disabled.");
            Assume.assumeTrue("Feature '" + feature + "' is disabled (in test.skip.features)", false);
        }
    }

    protected boolean isDataSource(String dataSourceName) {
        return OneApiDataSourceManager.getDbDialect().equals(dataSourceName);
    }

    protected void requiresDataSource(String... dataSourceNames) {
        Set<String> allowedDataSources = Arrays.stream(dataSourceNames).map(String::trim).collect(Collectors.toSet());
        String currentDataSource = OneApiDataSourceManager.getDbDialect();
        if (!allowedDataSources.contains(currentDataSource)) {
            Assume.assumeTrue("Data source '" + currentDataSource + "' is not in " + allowedDataSources, false);
        }
    }

    /**
     * Ensure schema exists (workaround for H2 memory DB connection pooling)
     * Check if user_info table exists, if not, re-initialize
     */
    protected void ensureSchemaExists() {
        try {
            // Try a simple query to check if tables exist
            jdbcTemplate.execute("SELECT COUNT(*) FROM user_info WHERE 1=0");
        } catch (Exception e) {
            // Tables don't exist, need to re-initialize
            System.out.println("[OneAPI] Schema not found in current connection, re-initializing...");
            System.out.println("[OneAPI] Error was: " + e.getClass().getName() + ": " + e.getMessage());
            try {
                String dialect = OneApiDataSourceManager.getDbDialect();
                OneApiDataSourceManager.initializeDatabase(jdbcTemplate, dialect);

                // Verify tables were created
                jdbcTemplate.execute("SELECT COUNT(*) FROM user_info WHERE 1=0");
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
            deleteAll("user_order");
            deleteAll("user_info");
            deleteAll("complex_order");
            deleteAll("product_vector");
            deleteAll("array_types_test");
            deleteAll("array_types_explicit_test");
            deleteAll("array_types_annotation_test");
            deleteAll("test_special_types");
            deleteAll("basic_types_test");
            deleteAll("basic_types_explicit_test");
            deleteAll("binary_types_explicit_test");
            deleteAll("enum_types_explicit_test");
            deleteAll("json_types_explicit_test");
            deleteAll("time_types_explicit_test");
            // Composite primary key test table
            try {
                deleteAll("user_role");
            } catch (Exception ignored) {
            }
            // Case sensitivity test tables (may not exist for all dialects)
            try {
                deleteAll("case_test_lower");
            } catch (Exception ignored) {
            }
            try {
                deleteAll("\"Case_Test_Upper\"");
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            // Ignore - tables might not exist yet
            System.out.println("[OneAPI] Data cleanup skipped: " + e.getMessage());
        }
    }

    private void deleteAll(String tableName) throws SQLException {
        if (isDataSource("clickhouse")) {
            jdbcTemplate.executeUpdate("ALTER TABLE " + tableName + " DELETE WHERE 1=1");
        } else {
            jdbcTemplate.executeUpdate("DELETE FROM " + tableName);
        }
    }

    /**
     * 创建新的 Session 实例
     */
    protected Session newSession() throws SQLException {
        Configuration configuration = new Configuration();
        registerCommonMacros(configuration);
        return configuration.newSession(dataSource);
    }

    /**
     * Optional: Override in subclasses to load test-specific data
     * Schema initialization is handled automatically by OneApiDataSourceManager
     */
    protected void initData() throws SQLException {
        // Default: no additional data
    }

    protected String currentTimestampExpression() {
        if (isDataSource("clickhouse")) {
            return "current_timestamp()";
        }
        return "CURRENT_TIMESTAMP";
    }

    protected void registerCommonMacros(Configuration configuration) {
        configuration.addMacro("currentTimestamp", currentTimestampExpression());
    }

    private void registerCommonMacros(JdbcTemplate template) {
        if (template.getQueryContext() instanceof JdbcQueryContext) {
            ((JdbcQueryContext) template.getQueryContext()).addMacro("currentTimestamp", currentTimestampExpression());
        }
    }
}
