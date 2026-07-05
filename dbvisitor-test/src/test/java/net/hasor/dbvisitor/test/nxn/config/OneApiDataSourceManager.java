package net.hasor.dbvisitor.test.nxn.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.Assume;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

/**
 * OneAPI DataSource Manager - Follows DsUtils pattern
 * Provides database initialization with SQL script loading
 */
public class OneApiDataSourceManager {
    private static final String                  DEFAULT_ENV        = "pg";
    private static final String                  PROP_FILE_TEMPLATE = "/jdbc-%s.properties";
    private static final Map<String, Properties> adapterPropsCache  = new HashMap<>();
    private static Properties                    cachedProperties;
    private static DataSource                    cachedDataSource;
    private static boolean                       initialized        = false;

    private static synchronized Properties loadProperties() throws IOException {
        if (cachedProperties != null) {
            return cachedProperties;
        }
        String env = getDbDialect();
        String propFileName = String.format(PROP_FILE_TEMPLATE, env);

        Properties props = new Properties();
        try (InputStream in = OneApiDataSourceManager.class.getResourceAsStream(propFileName)) {
            if (in == null) {
                throw new RuntimeException("Test config not found: " + propFileName);
            }
            props.load(in);
        }
        cachedProperties = props;
        return props;
    }

    /**
     * Initialize database schema using SQL scripts from classpath
     * Pattern: /sql/{dialect}/init.sql
     */
    private static void initDatabase(JdbcTemplate jdbcTemplate, String dialect) {
        try {
            initializeDatabase(jdbcTemplate, dialect);
        } catch (Exception e) {
            System.err.println("[OneAPI] Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - allow tests to continue with manual schema setup
        }
    }

    public static void initializeDatabase(JdbcTemplate jdbcTemplate, String dialect) throws IOException, SQLException {
        String initScript = "/sql/" + dialect + "/init.sql";
        System.out.println("[OneAPI] Initializing database: " + dialect + " using " + initScript);

        // Check if init script exists
        try (InputStream in = OneApiDataSourceManager.class.getResourceAsStream(initScript)) {
            if (in != null) {
                // Use loadSplitSQL with semicolon delimiter to handle multiple statements
                if ("oracle".equals(dialect) || "db2".equals(dialect)) {
                    loadInitScriptIgnoringMissingDrops(jdbcTemplate, in, dialect);
                } else {
                    jdbcTemplate.loadSplitSQL(";", initScript);
                }
                System.out.println("[OneAPI] Database initialization completed");
            } else {
                System.out.println("[OneAPI] No init script found at: " + initScript + ", skipping initialization");
            }
        }
    }

    private static void loadInitScriptIgnoringMissingDrops(JdbcTemplate jdbcTemplate, InputStream in, String dialect) throws IOException, SQLException {
        String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        for (String rawSql : script.split(";")) {
            String sql = removeLineComments(rawSql).trim();
            if (sql.isEmpty()) {
                continue;
            }
            try {
                jdbcTemplate.execute(sql);
            } catch (SQLException e) {
                if (!isIgnorableDropFailure(sql, e, dialect)) {
                    throw e;
                }
            }
        }
    }

    private static String removeLineComments(String sql) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\\R")) {
            if (!line.trim().startsWith("--")) {
                cleaned.append(line).append('\n');
            }
        }
        return cleaned.toString();
    }

    private static boolean isIgnorableDropFailure(String sql, SQLException e, String dialect) {
        String loweredSql = sql.trim().toLowerCase();
        String message = String.valueOf(e.getMessage()).toLowerCase();
        if (!loweredSql.startsWith("drop ")) {
            return false;
        }
        if ("oracle".equals(dialect)) {
            return message.contains("ora-00942") || message.contains("ora-02289") || message.contains("does not exist");
        }
        if ("db2".equals(dialect)) {
            return message.contains("sqlcode=-204") || message.contains("sqlstate=42704") || message.contains("undefined name");
        }
        return false;
    }

    public static synchronized DataSource createDataSource() throws IOException {
        if (cachedDataSource != null) {
            return cachedDataSource;
        }

        Properties props = loadProperties();
        String dialect = getDbDialect();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("jdbc.url"));
        config.setUsername(props.getProperty("jdbc.username"));
        config.setPassword(props.getProperty("jdbc.password"));
        config.setDriverClassName(props.getProperty("jdbc.driver"));
        config.setAutoCommit(true);
        String connectionTimeout = props.getProperty("jdbc.connectionTimeoutMs");
        if (connectionTimeout != null && !connectionTimeout.trim().isEmpty()) {
            config.setConnectionTimeout(Long.parseLong(connectionTimeout.trim()));
        }
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);

        cachedDataSource = new HikariDataSource(config);

        // Initialize database on first creation
        if (!initialized) {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(cachedDataSource);
                initDatabase(jdbcTemplate, dialect);
                initialized = true;
            } catch (Exception e) {
                System.err.println("[OneAPI] Database initialization failed: " + e.getMessage());
                // Continue anyway - tests will handle missing schema
            }
        }

        return cachedDataSource;
    }

    public static String getDbDialect() {
        String env = System.getProperty("nxn.env");
        if (env == null || env.trim().isEmpty()) {
            return DEFAULT_ENV;
        }
        return env.trim();
    }

    public static void assumeCurrentDataSource(String targetEnv) {
        String currentEnv = getDbDialect();
        Assume.assumeTrue("Skipping data source '" + targetEnv + "' scenario because nxn.env=" + currentEnv, targetEnv.equals(currentEnv));
    }

    public static String getProperty(String key) {
        try {
            return loadProperties().getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    // ==================== NoSQL Adapter Connection Support ====================

    /**
     * Reset cached data source (for testing or reconfiguration)
     */
    public static synchronized void reset() {
        if (cachedDataSource != null && cachedDataSource instanceof HikariDataSource) {
            ((HikariDataSource) cachedDataSource).close();
        }
        cachedDataSource = null;
        cachedProperties = null;
        initialized = false;
    }

    /**
     * 加载指定适配器的配置文件 /jdbc-{adapter}.properties
     * @param adapter 适配器名称（redis/mongo/es6/es7/milvus）
     */
    public static synchronized Properties loadAdapterProperties(String adapter) {
        Properties cached = adapterPropsCache.get(adapter);
        if (cached != null) {
            return cached;
        }

        String propFileName = String.format(PROP_FILE_TEMPLATE, adapter);
        Properties props = new Properties();
        try (InputStream in = OneApiDataSourceManager.class.getResourceAsStream(propFileName)) {
            if (in == null) {
                throw new RuntimeException("Adapter config not found: " + propFileName);
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load adapter config: " + propFileName, e);
        }

        adapterPropsCache.put(adapter, props);
        return props;
    }

    /**
     * 获取指定适配器的数据库连接
     * @param adapter 适配器名称（redis/mongo/es6/es7/milvus）
     */
    public static Connection getConnection(String adapter) throws SQLException {
        Properties props = loadAdapterProperties(adapter);
        String url = props.getProperty("jdbc.url");
        String user = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        Properties connProps = new Properties();
        if (user != null && !user.trim().isEmpty()) {
            connProps.setProperty("username", user);
        }
        if (password != null && !password.trim().isEmpty()) {
            connProps.setProperty("password", password);
        }

        // 加载所有 conn.* 开头的额外连接属性
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("conn.")) {
                connProps.setProperty(key.substring(5), props.getProperty(key));
            }
        }

        return DriverManager.getConnection(url, connProps);
    }

    /**
     * 获取指定适配器的数据库连接，并附加额外的连接属性
     * @param adapter 适配器名称
     * @param extraProps 额外连接属性
     */
    public static Connection getConnection(String adapter, Properties extraProps) throws SQLException {
        Properties props = loadAdapterProperties(adapter);
        String url = props.getProperty("jdbc.url");
        String user = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");

        Properties connProps = new Properties();
        if (user != null && !user.trim().isEmpty()) {
            connProps.setProperty("username", user);
        }
        if (password != null && !password.trim().isEmpty()) {
            connProps.setProperty("password", password);
        }

        for (String key : props.stringPropertyNames()) {
            if (key.startsWith("conn.")) {
                connProps.setProperty(key.substring(5), props.getProperty(key));
            }
        }

        if (extraProps != null) {
            connProps.putAll(extraProps);
        }

        return DriverManager.getConnection(url, connProps);
    }

    /** 获取适配器配置属性 */
    public static String getAdapterProperty(String adapter, String key) {
        return loadAdapterProperties(adapter).getProperty(key);
    }
}
