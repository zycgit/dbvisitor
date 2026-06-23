package net.hasor.dbvisitor.test.nxn.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.junit.Assume;
import org.junit.Before;

import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;

public abstract class AbstractNxnContractTest extends AbstractOneApiTest {
    protected abstract DataSourceProfile profile();

    @Override
    @Before
    public void setup() throws IOException, SQLException {
        String currentEnv = currentEnv();
        Assume.assumeTrue("NXN contract for env '" + profile().env() + "' skipped because current env is '" + currentEnv + "'", profile().env().equals(currentEnv));
        super.setup();
    }

    protected void requiresNxnFeature(String featureId) {
        Assume.assumeTrue("Feature '" + featureId + "' is unsupported by " + profile().env(), profile().supportsFeature(featureId));
    }

    protected void assertMutationRows(int expected, int actual) {
        if (profile().supportsFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS)) {
            assertEquals(expected, actual);
        } else {
            assertTrue(actual >= 0);
        }
    }

    protected void dropTableIfExists(String tableName) throws SQLException {
        if (profile().id().name().equals("ORACLE")) {
            executeDropIgnoringMissing("DROP TABLE " + tableName + " PURGE");
            return;
        }
        if (isDb2()) {
            executeDropIgnoringMissing("DROP TABLE " + tableName);
            return;
        }
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS " + tableName);
    }

    protected void dropSequenceIfExists(String sequenceName) throws SQLException {
        if (profile().id().name().equals("ORACLE")) {
            executeDropIgnoringMissing("DROP SEQUENCE " + sequenceName);
            return;
        }
        if (isDb2()) {
            executeDropIgnoringMissing("DROP SEQUENCE " + sequenceName);
            return;
        }
        jdbcTemplate.executeUpdate("DROP SEQUENCE IF EXISTS " + sequenceName);
    }

    protected String addColumnSql(String tableName, String columnDefinition) {
        if (profile().id().name().equals("ORACLE")) {
            return "ALTER TABLE " + tableName + " ADD (" + columnDefinition + ")";
        }
        if (profile().id().name().equals("MSSQL")) {
            return "ALTER TABLE " + tableName + " ADD " + columnDefinition;
        }
        return "ALTER TABLE " + tableName + " ADD COLUMN " + columnDefinition;
    }

    protected void deleteAllRows(String tableName) throws SQLException {
        if (profile().id().name().equals("CLICKHOUSE")) {
            jdbcTemplate.executeUpdate("ALTER TABLE " + tableName + " DELETE WHERE 1=1");
        } else {
            jdbcTemplate.executeUpdate("DELETE FROM " + tableName);
        }
    }

    protected String primaryKeyColumn(String columnName, String columnType) {
        return columnName + " " + columnType + (isDb2() ? " NOT NULL PRIMARY KEY" : " PRIMARY KEY");
    }

    protected String createSimpleTempTableSql(String tableName) {
        if (profile().id().name().equals("CLICKHOUSE")) {
            return "CREATE TABLE " + tableName + " (id Int32, name Nullable(String)) ENGINE = MergeTree ORDER BY id";
        }
        return "CREATE TABLE " + tableName + " (id INT, name VARCHAR(50))";
    }

    protected boolean isOracle() {
        return profile().id().name().equals("ORACLE");
    }

    protected boolean isMsSql() {
        return profile().id().name().equals("MSSQL");
    }

    protected boolean isDb2() {
        return profile().id().name().equals("DB2");
    }

    protected boolean isDuplicateKeyMessage(Throwable throwable) {
        String message = lowerMessage(throwable);
        return message.contains("duplicate") || message.contains("unique") || message.contains("constraint") || message.contains("primary") //
                || message.contains("sqlstate=23505") || message.contains("sqlcode=-803");
    }

    protected boolean isNotNullMessage(Throwable throwable) {
        String message = lowerMessage(throwable);
        return message.contains("null") || message.contains("constraint") || message.contains("default") //
                || message.contains("sqlstate=23502") || message.contains("sqlcode=-407");
    }

    protected boolean isLengthLimitMessage(Throwable throwable) {
        String message = lowerMessage(throwable);
        return message.contains("length") || message.contains("too long") || message.contains("data too long") //
                || message.contains("value too long") || message.contains("right truncation") || message.contains("value too large") //
                || message.contains("truncated") || message.contains("sqlstate=22001") || message.contains("sqlcode=-302");
    }

    protected String lowerMessage(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        appendMessages(builder, throwable, Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>()));
        return builder.toString().toLowerCase();
    }

    private void appendMessages(StringBuilder builder, Throwable throwable, Set<Throwable> visited) {
        if (throwable == null || !visited.add(throwable)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(throwable.getClass().getName()).append(": ").append(String.valueOf(throwable.getMessage()));
        for (Throwable suppressed : throwable.getSuppressed()) {
            appendMessages(builder, suppressed, visited);
        }
        if (throwable instanceof SQLException) {
            appendMessages(builder, ((SQLException) throwable).getNextException(), visited);
        }
        appendMessages(builder, throwable.getCause(), visited);
    }

    protected Configuration newConfiguration() {
        Configuration configuration = new Configuration();
        applyNxnMacros(configuration);
        return configuration;
    }

    protected Configuration newConfiguration(Options options) {
        Configuration configuration = new Configuration(options);
        applyNxnMacros(configuration);
        return configuration;
    }

    protected String currentTimestampExpression() {
        return profile().currentTimestampExpression();
    }

    private void applyNxnMacros(Configuration configuration) {
        configuration.addMacro("currentTimestamp", profile().currentTimestampExpression());
    }

    private void executeDropIgnoringMissing(String sql) throws SQLException {
        try {
            jdbcTemplate.executeUpdate(sql);
        } catch (SQLException e) {
            String message = String.valueOf(e.getMessage()).toLowerCase();
            if (!message.contains("does not exist") && !message.contains("not exist") && !message.contains("sqlstate=42704") //
                    && !message.contains("sqlcode=-204") && !message.contains("ora-00942") && !message.contains("ora-02289")) {
                throw e;
            }
        }
    }

    private String currentEnv() {
        return OneApiDataSourceManager.getDbDialect();
    }
}
