package net.hasor.dbvisitor.test.oneapi.suite.mapping.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.dialect.provider.PostgreSqlDialect;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射 keyType 测试。
 * 覆盖：auto、uuid32、uuid36、Sequence::xxx、无 keyType、keyType 在 mapping 标签上。
 * 对应 annotation 侧由 keygen/ 目录测试套件覆盖。
 */
public class XmlKeygenTest extends AbstractOneApiTest {

    private static final String XML_RESOURCE = "oneapi/mapping/keygen_types.xml";

    /** keyType="auto" → keySeqHolder 不为 null */
    @Test
    public void testKeyType_Auto_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.keygen", "AutoKeyEntity");
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNotNull("keyType=auto should produce a non-null keySeqHolder", idCol.getKeySeqHolder());
        assertTrue("keySeqHolder should be Auto type", idCol.getKeySeqHolder().toString().startsWith("Auto@"));
    }

    /** keyType="uuid32" 在 <mapping> 标签上 → keySeqHolder 不为 null */
    @Test
    public void testKeyType_UUID32_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.keygen", "UUID32KeyEntity");
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNull("id without keyType should have null keySeqHolder", idCol.getKeySeqHolder());

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNotNull("keyType=uuid32 should produce a non-null keySeqHolder", nameCol.getKeySeqHolder());
        assertTrue("keySeqHolder should be UUID32 type", nameCol.getKeySeqHolder().toString().startsWith("UUID32@"));
    }

    /** keyType="uuid36" 在 <mapping> 标签上 → keySeqHolder 不为 null */
    @Test
    public void testKeyType_UUID36_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.keygen", "UUID36KeyEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNotNull("keyType=uuid36 should produce a non-null keySeqHolder", nameCol.getKeySeqHolder());
        assertTrue("keySeqHolder should be UUID36 type", nameCol.getKeySeqHolder().toString().startsWith("UUID36@"));
    }

    /** 无 keyType 属性 → keySeqHolder 为 null */
    @Test
    public void testKeyType_None_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.keygen", "NoneKeyEntity");
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNull("No keyType should have null keySeqHolder", idCol.getKeySeqHolder());

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNull("No keyType should have null keySeqHolder", nameCol.getKeySeqHolder());
    }

    /** keyType="Sequence::test_seq" → keySeqHolder 不为 null（需要 SeqSqlDialect） */
    @Test
    public void testKeyType_Sequence_Metadata() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/oneapi/mapping/keygen_sequence.xml"), "UTF-8");

        Options opts = Options.of().dialect(new PostgreSqlDialect());
        MappingRegistry registry = new MappingRegistry(null, null, opts);
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("keygen-seq", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("keygen.seq", "SeqEntity");
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNotNull("keyType=Sequence::test_seq should produce a non-null keySeqHolder", idCol.getKeySeqHolder());
        assertTrue("keySeqHolder should be Sequence type", idCol.getKeySeqHolder().toString().startsWith("Sequence@"));
    }

    /** 同一 entity 中多个字段使用不同 keyType */
    @Test
    public void testKeyType_MultipleFieldsDifferentTypes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.keygen", "MappingKeyEntity");
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNotNull("id with keyType=auto should have keySeqHolder", idCol.getKeySeqHolder());
        assertTrue("id keySeqHolder should be Auto type", idCol.getKeySeqHolder().toString().startsWith("Auto@"));

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNotNull("name with keyType=uuid36 should have keySeqHolder", nameCol.getKeySeqHolder());
        assertTrue("name keySeqHolder should be UUID36 type", nameCol.getKeySeqHolder().toString().startsWith("UUID36@"));

        ColumnMapping emailCol = mapping.getPropertyByName("email");
        assertNotNull(emailCol);
        assertNotNull("email with keyType=uuid32 should have keySeqHolder", emailCol.getKeySeqHolder());
        assertTrue("email keySeqHolder should be UUID32 type", emailCol.getKeySeqHolder().toString().startsWith("UUID32@"));
    }
}
