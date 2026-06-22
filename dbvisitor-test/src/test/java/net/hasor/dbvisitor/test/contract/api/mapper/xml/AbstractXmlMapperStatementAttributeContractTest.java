package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractXmlMapperStatementAttributeContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlStatementAttrMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i, "StmtAttr" + i, 20 + i, "attr" + i + "@nxn.test" });
        }
    }

    protected int baseId() {
        return 955000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_TYPE)
    public void statementAttributes_shouldSupportPreparedAndStatementTypes() throws Exception {
        List<UserInfo> defaultPrepared = this.session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", mapOf("id", baseId() + 1));
        List<UserInfo> explicitPrepared = this.session.queryStatement("xmltest.StatementAttrMapper.selectExplicitPrepared", mapOf("id", baseId() + 2));
        List<UserInfo> statement = this.session.queryStatement("xmltest.StatementAttrMapper.selectStatement", null);

        assertEquals(1, defaultPrepared.size());
        assertEquals("StmtAttr1", defaultPrepared.get(0).getName());
        assertEquals(1, explicitPrepared.size());
        assertEquals("StmtAttr2", explicitPrepared.get(0).getName());
        assertEquals(5, statement.size());
        assertAscendingById(statement);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_TIMEOUT)
    public void statementAttributes_shouldApplyTimeoutAttributeWithoutChangingResults() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.StatementAttrMapper.selectWithTimeout", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_FETCH_SIZE)
    public void statementAttributes_shouldApplyFetchSizeWithoutChangingResults() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.StatementAttrMapper.selectWithFetchSize", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE)
    public void statementAttributes_shouldSupportResultSetTypeVariants() throws Exception {
        List<UserInfo> forwardOnly = this.session.queryStatement("xmltest.StatementAttrMapper.selectForwardOnly", null);
        List<UserInfo> scrollInsensitive = this.session.queryStatement("xmltest.StatementAttrMapper.selectScrollInsensitive", null);

        assertEquals(5, forwardOnly.size());
        assertEquals(5, scrollInsensitive.size());
        assertAscendingById(forwardOnly);
        assertAscendingById(scrollInsensitive);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_COMBINED)
    public void statementAttributes_shouldSupportCombinedAttributesAndDml() throws Exception {
        List<UserInfo> combined = this.session.queryStatement("xmltest.StatementAttrMapper.selectCombined", null);
        assertEquals(5, combined.size());
        for (UserInfo user : combined) {
            assertNotNull(user.getId());
            assertNotNull(user.getName());
        }

        Map<String, Object> params = mapOf("id", baseId() + 10);
        params.put("name", "StmtAttrInsert");
        params.put("age", 30);
        params.put("email", "stmt@nxn.test");

        Object result = this.session.executeStatement("xmltest.StatementAttrMapper.insertWithStatement", params);
        assertEquals(1, ((Number) result).intValue());
        List<UserInfo> inserted = this.session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", mapOf("id", baseId() + 10));
        assertEquals(1, inserted.size());
        assertEquals("StmtAttrInsert", inserted.get(0).getName());
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private void assertAscendingById(List<UserInfo> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }
}
