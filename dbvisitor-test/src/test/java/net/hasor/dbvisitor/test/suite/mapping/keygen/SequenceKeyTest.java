package net.hasor.dbvisitor.test.suite.mapping.keygen;

import java.util.Date;
import net.hasor.dbvisitor.dialect.features.SeqSqlDialect;
import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.dialect.provider.PostgreSqlDialect;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.keygen.KeySequenceEmptyNameUser;
import net.hasor.dbvisitor.test.model.keygen.KeySequenceNoAnnotationUser;
import net.hasor.dbvisitor.test.model.keygen.KeySequenceUser;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * KeyType.Sequence + @KeySeq 注解测试。
 * 验证序列主键的元数据注册、方言限制和异常情况，以及实际插入功能。
 */
public class SequenceKeyTest extends AbstractOneApiTest {
    private LambdaTemplate lambda;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.lambda = new LambdaTemplate(jdbcTemplate.getDataSource(), Options.of().dialect(new TestPostgreSqlDialect()));

        // Ensure table exists for tests
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_strict_none");
        jdbcTemplate.executeUpdate("CREATE TABLE user_strict_none (id INT NOT NULL PRIMARY KEY, name VARCHAR(100))");
    }

    /**
     * KeyType.Sequence + @KeySeq 使用支持序列的 H2Dialect 时，
     * 元数据应正确注册 keyType=Sequence 且 keySeqHolder 不为 null。
     */
    @Test
    public void testSequenceKeyType_MetadataWithH2Dialect() {
        Options options = Options.of().dialect(H2Dialect.DEFAULT);
        MappingRegistry registry = new MappingRegistry(null, options);
        registry.loadEntityToSpace(KeySequenceUser.class);

        TableMapping<?> mapping = registry.findByEntity(KeySequenceUser.class);
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id column should exist", idCol);
        assertTrue("id should be primary key", idCol.isPrimaryKey());
        assertEquals("keyType should be Sequence", KeyType.Sequence, idCol.getKeyType());
        assertNotNull("keySeqHolder should not be null", idCol.getKeySeqHolder());
    }

    /**
     * 缺少 @KeySeq 注解时
     */
    @Test
    public void testSequenceKeyType_MissingKeySeqAnnotation() {
        Options options = Options.of().dialect(H2Dialect.DEFAULT);
        MappingRegistry registry = new MappingRegistry(null, options);

        registry.loadEntityToSpace(KeySequenceNoAnnotationUser.class);

        TableMapping<?> mapping = registry.findByEntity(KeySequenceNoAnnotationUser.class);
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNull("keySeqHolder should be null if annotation is missing", idCol.getKeySeqHolder());
    }

    /**
     * @KeySeq 的 value 为空字符串时应报错
     */
    @Test
    public void testSequenceKeyType_EmptySequenceName() {
        Options options = Options.of().dialect(H2Dialect.DEFAULT);
        MappingRegistry registry = new MappingRegistry(null, options);

        try {
            registry.loadEntityToSpace(KeySequenceEmptyNameUser.class);
            fail("Should throw exception for empty sequence name");
        } catch (Exception e) {
            // 只要抛出异常即可，不做严格的 message 匹配
            assertNotNull(e.getMessage());
        }
    }

    // ============ 功能测试 (Functional Tests) ============

    /**
     * 测试实际的序列插入
     */
    @Test
    public void testSequenceKeyType_Insert() throws Exception {
        // 1. 创建序列
        String seqName = "seq_key_test_seq";
        try {
            jdbcTemplate.executeUpdate("DROP SEQUENCE IF EXISTS " + seqName);
        } catch (Exception ignored) {
        }
        jdbcTemplate.executeUpdate("CREATE SEQUENCE " + seqName + " START WITH 2000 INCREMENT BY 1");

        try {

            // 2. 插入数据
            KeySequenceUser user = new KeySequenceUser();
            user.setName("Seq User");
            user.setAge(20);
            user.setCreateTime(new Date());

            assertNull("ID should be null before insert", user.getId());

            int rows = lambda.insert(KeySequenceUser.class).applyEntity(user).executeSumResult();

            assertEquals("Insert should succeed", 1, rows);
            assertNotNull("ID should be generated from sequence", user.getId());
            assertEquals("First sequence value should be 2000", Integer.valueOf(2000), user.getId());

            // 3. 再次插入验证递增
            KeySequenceUser user2 = new KeySequenceUser();
            user2.setName("Seq User 2");
            user2.setAge(21);
            user2.setCreateTime(new Date());

            lambda.insert(KeySequenceUser.class).applyEntity(user2).executeSumResult();

            assertEquals("Next sequence value should be 2001", Integer.valueOf(2001), user2.getId());
        } finally {
            try {
                jdbcTemplate.executeUpdate("DROP SEQUENCE IF EXISTS seq_key_test_seq");
            } catch (Exception ignored) {
            }
        }
    }

    public static class TestPostgreSqlDialect extends PostgreSqlDialect implements SeqSqlDialect {
        @Override
        public String selectSeq(boolean useQualifier, String catalog, String schema, String seqName) {
            return "SELECT nextval('" + seqName + "')";
        }
    }
}
