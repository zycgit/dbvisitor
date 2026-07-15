package net.hasor.dbvisitor.test.contract.feature.keygen;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.provider.Db2Dialect;
import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.dialect.provider.OracleDialect;
import net.hasor.dbvisitor.dialect.provider.PostgreSqlDialect;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoLongUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyAutoUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderAfterUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderBothUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderConnectionUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderContextUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderFailingUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderSqlExceptionUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyHolderUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyNoneUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyNoneStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceEmptyNameUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceNoAnnotationUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeySequenceUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid32User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuid36User;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyUuidStringUser;
import net.hasor.dbvisitor.test.contract.material.model.keygen.KeyWrongTypeUuidUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class KeyGenerationContractTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.KEYGEN_NONE_MANUAL)
    public void keygenNone_shouldUseManuallyAssignedPrimaryKey() throws SQLException {
        ensureStrictNoneTable();

        KeyNoneStrictUser user = new KeyNoneStrictUser();
        user.setId(88001);
        user.setName("None Key User");
        user.setAge(25);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyNoneStrictUser.class).applyEntity(user).executeSumResult();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_strict_none WHERE id = ?", new Object[] { 88001 }, Integer.class);

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(88001), user.getId());
        assertEquals(Integer.valueOf(1), count);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_NONE_MISSING)
    public void keygenNone_shouldRejectMissingPrimaryKeyOnStrictTable() throws SQLException {
        requiresNxnFeature(FeatureId.NON_NULL_PRIMARY_KEY_REJECTED);

        ensureStrictNoneTable();

        KeyNoneStrictUser user = new KeyNoneStrictUser();
        user.setName("None Missing User");
        user.setAge(26);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyNoneStrictUser.class).applyEntity(user).executeSumResult();
            fail("Expected missing primary key to be rejected");
        } catch (Exception e) {
            assertTrue(e.getMessage(), isNotNullMessage(e));
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_SINGLE)
    public void keygenAuto_shouldPopulateDatabaseGeneratedKeyOnSingleInsert() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoTable();

        KeyAutoUser user = new KeyAutoUser();
        user.setName("Auto Key User");
        user.setAge(30);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyAutoUser.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_BATCH)
    public void keygenAuto_shouldPopulateGeneratedKeysOnBatchInsert() throws SQLException {
        requiresNxnFeature(FeatureId.KEYGEN_AUTO_BATCH_EXPLICIT_NULL);
        ensureAutoTable();

        KeyAutoUser first = autoUser("Auto Batch 1", 31);
        KeyAutoUser second = autoUser("Auto Batch 2", 32);
        KeyAutoUser third = autoUser("Auto Batch 3", 33);

        Insert<KeyAutoUser> insert = lambdaTemplate.insert(KeyAutoUser.class);
        int rows = insert.applyEntity(first).applyEntity(second).applyEntity(third).executeSumResult();

        assertEquals(3, rows);
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotNull(third.getId());
        assertFalse(first.getId().equals(second.getId()));
        assertFalse(second.getId().equals(third.getId()));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_MANUAL)
    public void keygenAuto_shouldKeepManuallyAssignedPrimaryKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoTable();

        KeyAutoUser user = autoUser("Manual Auto User", 67);
        user.setId(54321);

        assertEquals(1, lambdaTemplate.insert(KeyAutoUser.class).applyEntity(user).executeSumResult());
        Integer dbId = jdbcTemplate.queryForObject("SELECT id FROM user_keygen_auto WHERE id = ?", new Object[] { 54321 }, Integer.class);

        assertEquals(Integer.valueOf(54321), user.getId());
        assertEquals(Integer.valueOf(54321), dbId);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_AUTO_LONG)
    public void keygenAuto_shouldPopulateLongPrimaryKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAutoLongTable();

        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setName("Long ID User");
        user.setAge(65);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyAutoLongUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0L);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID32)
    public void keygenUuid32_shouldPopulateHexStringKeyField() throws SQLException {
        KeyUuid32User user = new KeyUuid32User();
        user.setId(770032);
        user.setAge(35);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyUuid32User.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertNotNull(user.getName());
        assertEquals(32, user.getName().length());
        assertFalse(user.getName().contains("-"));
        assertTrue(user.getName().matches("[0-9a-f]{32}"));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID32_UNIQUE)
    public void keygenUuid32_shouldGenerateUniqueValues() throws SQLException {
        KeyUuid32User first = uuid32User(770033, 36);
        KeyUuid32User second = uuid32User(770034, 37);

        lambdaTemplate.insert(KeyUuid32User.class).applyEntity(first).executeSumResult();
        lambdaTemplate.insert(KeyUuid32User.class).applyEntity(second).executeSumResult();

        assertNotNull(first.getName());
        assertNotNull(second.getName());
        assertFalse(first.getName().equals(second.getName()));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID36)
    public void keygenUuid36_shouldPopulateStandardUuidStringKeyField() throws SQLException {
        KeyUuid36User user = new KeyUuid36User();
        user.setId(770036);
        user.setAge(36);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyUuid36User.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertNotNull(user.getName());
        assertEquals(36, user.getName().length());
        assertTrue(user.getName().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID36_STRING_ID)
    public void keygenUuid36_shouldPopulateStringPrimaryKey() throws SQLException {
        ensureUuidStringTable();

        KeyUuidStringUser user = new KeyUuidStringUser();
        user.setName("String ID User");
        user.setAge(70);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyUuidStringUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertEquals(36, user.getId().length());
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_BEFORE)
    public void keygenHolderBefore_shouldUseCustomGeneratedKeyHandler() throws SQLException {
        KeyHolderUser user = new KeyHolderUser();
        user.setAge(40);
        user.setCreateTime(new Date());

        int rows = lambdaTemplate.insert(KeyHolderUser.class).applyEntity(user).executeSumResult();

        assertEquals(1, rows);
        assertEquals(Integer.valueOf(999999), user.getId());
        assertNotNull(user.getName());
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_AFTER)
    public void keygenHolderAfter_shouldReadDatabaseGeneratedKey() throws SQLException {
        requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        ensureAfterTable();

        KeyHolderAfterUser user = new KeyHolderAfterUser();
        user.setName("After Key User");
        user.setAge(45);
        user.setCreateTime(new Date());

        assertEquals(1, lambdaTemplate.insert(KeyHolderAfterUser.class).applyEntity(user).executeSumResult());
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_BOTH)
    public void keygenHolderBoth_shouldRunBeforeAndAfterWhenBothHooksAreEnabled() throws SQLException {
        deleteUserInfoIds(888888, 777777);

        KeyHolderBothUser user = new KeyHolderBothUser();
        user.setName("Both Key User");
        user.setAge(50);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderBothUser.class).applyEntity(user).executeSumResult();

        assertEquals(Integer.valueOf(777777), user.getId());
        Integer inserted = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { 888888 }, Integer.class);
        assertEquals(Integer.valueOf(1), inserted);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_CONTEXT)
    public void keygenHolderContext_shouldExposeMappingContext() throws SQLException {
        KeyHolderContextUser user = new KeyHolderContextUser();
        user.setName("Context Key User");
        user.setAge(55);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderContextUser.class).applyEntity(user).executeSumResult();

        assertNotNull(user.getId());
        assertTrue(user.getId() >= 700000 && user.getId() < 800000);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_CONNECTION)
    public void keygenHolderConnection_shouldUseJdbcConnectionDuringBeforeGeneration() throws SQLException {
        KeyHolderConnectionUser user = new KeyHolderConnectionUser();
        user.setName("Connection Aware User");
        user.setAge(60);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(KeyHolderConnectionUser.class).applyEntity(user).executeSumResult();

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_EXCEPTION)
    public void keygenHolderException_shouldPropagateRuntimeException() throws SQLException {
        KeyHolderFailingUser user = new KeyHolderFailingUser();
        user.setName("Failing Key User");
        user.setAge(60);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyHolderFailingUser.class).applyEntity(user).executeSumResult();
            fail("Expected custom key holder runtime exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Intentional failure") || String.valueOf(e.getCause()).contains("Intentional failure"));
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_HOLDER_SQL_EXCEPTION)
    public void keygenHolderSqlException_shouldExposeSqlExceptionCause() throws SQLException {
        KeyHolderSqlExceptionUser user = new KeyHolderSqlExceptionUser();
        user.setName("SQL Ex User");
        user.setAge(40);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyHolderSqlExceptionUser.class).applyEntity(user).executeSumResult();
            fail("Expected custom key holder SQLException");
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof SQLException && cause.getMessage().contains("Intentional SQLException")) {
                    return;
                }
                cause = cause.getCause();
            }
            fail("Expected SQLException cause, got " + e);
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_DUPLICATE_KEY)
    public void keygenNone_shouldPropagateDuplicatePrimaryKeyError() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);

        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 66666");
        KeyNoneUser first = noneUser(66666, "Duplicate User 1", 30);
        KeyNoneUser second = noneUser(66666, "Duplicate User 2", 31);

        assertEquals(1, lambdaTemplate.insert(KeyNoneUser.class).applyEntity(first).executeSumResult());
        try {
            lambdaTemplate.insert(KeyNoneUser.class).applyEntity(second).executeSumResult();
            fail("Expected duplicate primary key error");
        } catch (Exception e) {
            assertTrue(e.getMessage(), isDuplicateKeyMessage(e));
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_UUID_WRONG_TYPE)
    public void keygenUuid_shouldRejectNonStringTargetField() throws SQLException {
        requiresNxnFeature(FeatureId.KEYGEN_UUID_WRONG_TYPE_REJECTED);
        KeyWrongTypeUuidUser user = new KeyWrongTypeUuidUser();
        user.setName("Wrong Type User");
        user.setAge(35);
        user.setCreateTime(new Date());

        try {
            lambdaTemplate.insert(KeyWrongTypeUuidUser.class).applyEntity(user).executeSumResult();
            fail("Expected UUID generation on Integer field to fail");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_SEQUENCE_METADATA)
    public void keygenSequenceMetadata_shouldRegisterSequenceHolderWhenKeySeqExists() {
        requiresNxnFeature(FeatureId.SEQUENCE);
        MappingRegistry registry = new MappingRegistry(null, Options.of().dialect(sequenceDialect()));
        registry.loadEntityToSpace(KeySequenceUser.class);

        TableMapping<?> mapping = registry.findByEntity(KeySequenceUser.class);
        ColumnMapping idColumn = mapping.getPropertyByName("id");

        assertTrue(idColumn.isPrimaryKey());
        assertEquals(KeyType.Sequence, idColumn.getKeyType());
        assertNotNull(idColumn.getKeySeqHolder());

        registry.loadEntityToSpace(KeySequenceNoAnnotationUser.class);
        TableMapping<?> missing = registry.findByEntity(KeySequenceNoAnnotationUser.class);
        assertNull(missing.getPropertyByName("id").getKeySeqHolder());
    }

    @Test
    @Capability(CapabilityId.KEYGEN_SEQUENCE_EMPTY_NAME)
    public void keygenSequenceMetadata_shouldRejectEmptySequenceName() {
        requiresNxnFeature(FeatureId.SEQUENCE);
        MappingRegistry registry = new MappingRegistry(null, Options.of().dialect(sequenceDialect()));
        try {
            registry.loadEntityToSpace(KeySequenceEmptyNameUser.class);
            fail("Expected empty sequence name to be rejected");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @Capability(CapabilityId.KEYGEN_SEQUENCE)
    public void keygenSequence_shouldUseDatabaseSequenceWhenSupported() throws SQLException {
        requiresNxnFeature(FeatureId.SEQUENCE);
        resetSequence("seq_key_test_seq", 2000);

        KeySequenceUser first = sequenceUser("Seq User 1", 20);
        KeySequenceUser second = sequenceUser("Seq User 2", 21);

        assertNull(first.getId());
        LambdaTemplate seqLambda = new LambdaTemplate(dataSource, Options.of().dialect(sequenceDialect()));
        assertEquals(1, seqLambda.insert(KeySequenceUser.class).applyEntity(first).executeSumResult());
        assertEquals(1, seqLambda.insert(KeySequenceUser.class).applyEntity(second).executeSumResult());

        assertEquals(Integer.valueOf(2000), first.getId());
        assertEquals(Integer.valueOf(2001), second.getId());
    }

    private void ensureStrictNoneTable() throws SQLException {
        dropTableIfExists("user_strict_none");
        jdbcTemplate.executeUpdate("CREATE TABLE user_strict_none (id INT NOT NULL PRIMARY KEY, name VARCHAR(255), age INT, create_time " + profile().datetimeColumnType() + ")");
    }

    private void ensureAutoTable() throws SQLException {
        dropTableIfExists("user_keygen_auto");
        if (isMsSql()) {
            resetSequence("user_keygen_auto_seq", 1);
        }
        jdbcTemplate.executeUpdate("CREATE TABLE user_keygen_auto (id " + autoIntPrimaryKey("user_keygen_auto_seq") + ", name VARCHAR(255), age INT, create_time " + profile().datetimeColumnType() + ")");
    }

    private void ensureAutoLongTable() throws SQLException {
        dropTableIfExists("user_keygen_auto_long");
        if (isMsSql()) {
            resetSequence("user_keygen_auto_long_seq", 1);
        }
        jdbcTemplate.executeUpdate("CREATE TABLE user_keygen_auto_long (id " + autoLongPrimaryKey() + ", name VARCHAR(255), age INT, create_time " + profile().datetimeColumnType() + ")");
    }

    private void ensureAfterTable() throws SQLException {
        dropTableIfExists("user_keygen_after");
        if (isMsSql()) {
            resetSequence("user_keygen_after_seq", 1);
        }
        jdbcTemplate.executeUpdate("CREATE TABLE user_keygen_after (id " + autoIntPrimaryKey("user_keygen_after_seq") + ", name VARCHAR(255), age INT, create_time " + profile().datetimeColumnType() + ")");
    }

    private void ensureUuidStringTable() throws SQLException {
        dropTableIfExists("user_uuid");
        jdbcTemplate.executeUpdate("CREATE TABLE user_uuid (id VARCHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(100), age INT, create_time " + profile().datetimeColumnType() + ")");
    }

    private String autoIntPrimaryKey(String sequenceName) {
        switch (profile().id()) {
            case MYSQL:
                return "INT AUTO_INCREMENT PRIMARY KEY";
            case PG:
                return "SERIAL PRIMARY KEY";
            case H2:
                return "INT AUTO_INCREMENT PRIMARY KEY";
            case MSSQL:
                return "INT PRIMARY KEY DEFAULT NEXT VALUE FOR " + sequenceName;
            case ORACLE:
                return "NUMBER(10) GENERATED BY DEFAULT ON NULL AS IDENTITY PRIMARY KEY";
            case DB2:
                return "INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
            default:
                return "INT PRIMARY KEY";
        }
    }

    private String autoLongPrimaryKey() {
        switch (profile().id()) {
            case MYSQL:
                return "BIGINT AUTO_INCREMENT PRIMARY KEY";
            case PG:
                return "BIGSERIAL PRIMARY KEY";
            case H2:
                return "BIGINT AUTO_INCREMENT PRIMARY KEY";
            case MSSQL:
                return "BIGINT PRIMARY KEY DEFAULT NEXT VALUE FOR user_keygen_auto_long_seq";
            case ORACLE:
                return "NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY PRIMARY KEY";
            case DB2:
                return "BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
            default:
                return "BIGINT PRIMARY KEY";
        }
    }

    private void resetSequence(String seqName, int startWith) throws SQLException {
        dropSequenceIfExists(seqName);
        jdbcTemplate.executeUpdate("CREATE SEQUENCE " + seqName + " START WITH " + startWith + " INCREMENT BY 1");
    }

    private SqlDialect sequenceDialect() {
        switch (profile().id()) {
            case H2:
                return H2Dialect.DEFAULT;
            case PG:
                return PostgreSqlDialect.DEFAULT;
            case ORACLE:
                return OracleDialect.DEFAULT;
            case DB2:
                return Db2Dialect.DEFAULT;
            default:
                throw new IllegalStateException("Sequence is unsupported by " + profile().env());
        }
    }

    private KeySequenceUser sequenceUser(String name, int age) {
        KeySequenceUser user = new KeySequenceUser();
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    private KeyAutoUser autoUser(String name, int age) {
        KeyAutoUser user = new KeyAutoUser();
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    private KeyUuid32User uuid32User(int id, int age) {
        KeyUuid32User user = new KeyUuid32User();
        user.setId(id);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    private KeyNoneUser noneUser(int id, String name, int age) {
        KeyNoneUser user = new KeyNoneUser();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        return user;
    }

    private void deleteUserInfoIds(int first, int second) throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id IN (?, ?)", new Object[] { first, second });
    }
}
