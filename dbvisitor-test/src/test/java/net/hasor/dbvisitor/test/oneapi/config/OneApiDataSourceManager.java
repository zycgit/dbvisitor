package net.hasor.dbvisitor.test.oneapi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * OneAPI DataSource Manager - Follows DsUtils pattern
 * Provides database initialization with SQL script loading
 */
public class OneApiDataSourceManager {
    private static final String DEFAULT_ENV = "h2";
    private static final String PROP_FILE_TEMPLATE = "/oneapi/jdbc-%s.properties";
    private static Properties cachedProperties;
    private static DataSource cachedDataSource;
    private static boolean initialized = false;

    private static synchronized Properties loadProperties() throws IOException {
        if (cachedProperties != null) {
            return cachedProperties;
        }
        String env = System.getProperty("test.env", DEFAULT_ENV);
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
     * Pattern: /oneapi/sql/{dialect}/init.sql
     */
    private static void initDatabase(JdbcTemplate jdbcTemplate, String dialect) {
        try {
            String initScript = "/oneapi/sql/" + dialect + "/init.sql";
            System.out.println("[OneAPI] Initializing database: " + dialect + " using " + initScript);
            
            // Check if init script exists
            try (InputStream in = OneApiDataSourceManager.class.getResourceAsStream(initScript)) {
                if (in != null) {
                    // Use loadSplitSQL with semicolon delimiter to handle multiple statements
                    jdbcTemplate.loadSplitSQL(";", initScript);
                    System.out.println("[OneAPI] Database initialization completed");
                } else {
                    System.out.println("[OneAPI] No init script found at: " + initScript + ", skipping initialization");
                }
            }
        } catch (Exception e) {
            System.err.println("[OneAPI] Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - allow tests to continue with manual schema setup
        }
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
        String env = System.getProperty("test.env", DEFAULT_ENV);
        return env;
    }

    public static String getProperty(String key) {
        try {
            return loadProperties().getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties", e);
        }
    }
    
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
}