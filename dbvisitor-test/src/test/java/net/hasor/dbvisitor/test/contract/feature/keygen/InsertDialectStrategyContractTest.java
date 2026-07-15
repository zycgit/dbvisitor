package net.hasor.dbvisitor.test.contract.feature.keygen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.dialect.features.InsertSqlDialect;
import net.hasor.dbvisitor.jdbc.JdbcHelper;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.GeneratedKeyStrategy;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class InsertDialectStrategyContractTest extends AbstractNxnContractTest {
    private final List<String> primaryKey = Collections.singletonList("id");
    private final List<String> columns = Arrays.asList("id", "name", "age");
    private final List<String> keyOnlyColumns = Collections.singletonList("id");
    private final List<String> returnColumns = Collections.singletonList("id");

    @Test
    @Capability(CapabilityId.LAMBDA_INSERT_STRATEGY_GENERATED_KEY_NO_RETURN)
    public void generatedKeyStrategy_withoutReturnColumns_shouldMatchDialect() {
        GeneratedKeyStrategy strategy = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, Collections.emptyList(), DuplicateKeyStrategy.Into);
        assertEquals(expectedNoReturnColumnsStrategy(), strategy);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_INSERT_STRATEGY_GENERATED_KEY_INTO)
    public void generatedKeyStrategy_withReturnColumnsAndInto_shouldMatchDialect() {
        GeneratedKeyStrategy strategy = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, this.returnColumns, DuplicateKeyStrategy.Into);
        assertEquals(expectedReturnColumnsIntoStrategy(), strategy);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_INSERT_STRATEGY_GENERATED_KEY_IGNORE)
    public void generatedKeyStrategy_withReturnColumnsAndIgnore_shouldMatchDialect() {
        GeneratedKeyStrategy strategy = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, this.returnColumns, DuplicateKeyStrategy.Ignore);
        assertEquals(expectedReturnColumnsIgnoreStrategy(), strategy);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_INSERT_STRATEGY_GENERATED_KEY_UPDATE)
    public void generatedKeyStrategy_withReturnColumnsAndUpdate_shouldMatchDialect() {
        GeneratedKeyStrategy strategy = insertDialect().generatedKeyStrategy(this.primaryKey, this.columns, this.returnColumns, DuplicateKeyStrategy.Update);
        assertEquals(expectedReturnColumnsUpdateStrategy(), strategy);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_INSERT_STRATEGY_DUPLICATE_SUPPORT)
    public void supportDuplicateStrategy_shouldMatchDialect() {
        InsertSqlDialect dialect = insertDialect();
        assertTrue(dialect.supportDuplicateStrategy(this.primaryKey, this.columns, Collections.emptyList(), DuplicateKeyStrategy.Into));
        assertEquals(expectedIgnoreSupport(), dialect.supportDuplicateStrategy(this.primaryKey, this.columns, Collections.emptyList(), DuplicateKeyStrategy.Ignore));
        assertEquals(expectedUpdateSupport(), dialect.supportDuplicateStrategy(this.primaryKey, this.columns, Collections.emptyList(), DuplicateKeyStrategy.Update));
        assertEquals(expectedIgnoreWithoutPrimaryKeySupport(), dialect.supportDuplicateStrategy(Collections.emptyList(), this.columns, Collections.emptyList(), DuplicateKeyStrategy.Ignore));
        assertEquals(expectedUpdateWithoutPrimaryKeySupport(), dialect.supportDuplicateStrategy(Collections.emptyList(), this.columns, Collections.emptyList(), DuplicateKeyStrategy.Update));
        assertEquals(expectedUpdateWithKeyOnlyColumnsSupport(), dialect.supportDuplicateStrategy(this.primaryKey, this.keyOnlyColumns, Collections.emptyList(), DuplicateKeyStrategy.Update));
    }

    protected InsertSqlDialect insertDialect() {
        SqlDialect dialect = SqlDialectRegister.findOrCreate(dialectName());
        assertTrue("Dialect '" + dialectName() + "' must implement InsertSqlDialect.", dialect instanceof InsertSqlDialect);
        return (InsertSqlDialect) dialect;
    }

    protected GeneratedKeyStrategy expectedNoReturnColumnsStrategy() {
        return GeneratedKeyStrategy.JdbcBatch;
    }

    protected GeneratedKeyStrategy expectedReturnColumnsIntoStrategy() {
        return GeneratedKeyStrategy.OneByOne;
    }

    protected GeneratedKeyStrategy expectedReturnColumnsIgnoreStrategy() {
        return GeneratedKeyStrategy.OneByOne;
    }

    protected GeneratedKeyStrategy expectedReturnColumnsUpdateStrategy() {
        return GeneratedKeyStrategy.OneByOne;
    }

    protected boolean expectedIgnoreSupport() {
        return true;
    }

    protected boolean expectedUpdateSupport() {
        return true;
    }

    protected boolean expectedUpdateWithKeyOnlyColumnsSupport() {
        return true;
    }

    protected boolean expectedIgnoreWithoutPrimaryKeySupport() {
        return false;
    }

    protected boolean expectedUpdateWithoutPrimaryKeySupport() {
        return false;
    }

    private String dialectName() {
        DataSourceId id = profile().id();
        return switch (id) {
            case H2 -> JdbcHelper.H2;
            case MYSQL -> JdbcHelper.MYSQL;
            case PG -> JdbcHelper.POSTGRESQL;
            case MSSQL -> JdbcHelper.SQL_SERVER;
            case ORACLE -> JdbcHelper.ORACLE;
            case DB2 -> JdbcHelper.DB2;
            case CLICKHOUSE -> JdbcHelper.CLICKHOUSE;
            default -> throw new IllegalStateException("Data source '" + id + "' is not an InsertSqlDialect target.");
        };
    }
}
