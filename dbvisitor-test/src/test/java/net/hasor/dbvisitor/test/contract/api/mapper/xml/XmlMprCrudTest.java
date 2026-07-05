package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class XmlMprCrudTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlCrudMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i, "XmlCrud" + i, 20 + i, "crud" + i + "@test.com" });
        }
    }

    protected int baseId() {
        return 57000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_LOAD)
    public void xmlMapperLoadMappedStmt() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectAll", null);

        assertEquals(5, list.size());
        assertEquals("XmlCrud1", list.get(0).getName());
        assertEquals("XmlCrud5", list.get(4).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_INSERT)
    public void xmlMapperInsertAndSelectByResultMap() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", baseId() + 10);
        params.put("name", "XmlCrudInsert");
        params.put("age", 30);
        params.put("email", "insert@test.com");

        Object result = this.session.executeStatement("xmltest.CrudMapper.insertUser", params);
        assertEquals(1, ((Number) result).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 10));
        assertEquals(1, list.size());
        assertEquals("XmlCrudInsert", list.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_SELECT)
    public void xmlMapperSelectEntityByResultMap() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 1));
        List<UserInfo> missing = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 999));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("XmlCrud1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals("crud1@test.com", user.getEmail());
        assertNotNull(user.getCreateTime());
        assertTrue(missing.isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_MAP)
    public void xmlMapperResultTypeMapColMap() throws Exception {
        List<Map<String, Object>> list = this.session.queryStatement("xmltest.CrudMapper.selectAllAsMap", null);

        assertEquals(5, list.size());
        Map<String, Object> row = list.get(0);
        assertNotNull(value(row, "id"));
        assertNotNull(value(row, "name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_SCALAR)
    public void xmlMapperResultTypeScalarCountAndNames() throws Exception {
        List<Integer> count = this.session.queryStatement("xmltest.CrudMapper.countAll", null);
        List<String> names = this.session.queryStatement("xmltest.CrudMapper.selectNames", null);

        assertEquals(1, count.size());
        assertEquals(5, count.get(0).intValue());
        assertEquals(5, names.size());
        assertEquals("XmlCrud1", names.get(0));
        assertEquals("XmlCrud5", names.get(4));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_UPDATE)
    public void xmlMapperUpdateSelectedRow() throws Exception {
        Map<String, Object> params = mapOf("id", baseId() + 2);
        params.put("email", "updated@test.com");

        Object result = this.session.executeStatement("xmltest.CrudMapper.updateEmail", params);
        assertEquals(1, ((Number) result).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 2));
        assertEquals("updated@test.com", list.get(0).getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_DELETE)
    public void xmlMapperDeleteRemoveSelectedRow() throws Exception {
        Object result = this.session.executeStatement("xmltest.CrudMapper.deleteById", mapOf("id", baseId() + 3));
        assertEquals(1, ((Number) result).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 3));
        assertTrue(list.isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_EXECUTE)
    public void xmlMapperExecuteDmlStmtAndReturnAffectedRows() throws Exception {
        Object result = this.session.executeStatement("xmltest.CrudMapper.deleteByName", mapOf("name", "XmlCrud1"));

        List<Integer> count = this.session.queryStatement("xmltest.CrudMapper.countAll", null);
        List<UserInfo> deleted = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 1));

        assertEquals(1, ((Number) result).intValue());
        assertEquals(4, count.get(0).intValue());
        assertTrue(deleted.isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_QUERY_PAGE)
    public void xmlMapperQueryStmtPageObjectBoundaries() throws Exception {
        List<UserInfo> first = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 3));
        List<UserInfo> second = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(1, 2));
        List<UserInfo> lastPartial = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(2, 2));
        List<UserInfo> beyond = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(5, 2));
        List<UserInfo> largerThanTotal = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 100));
        List<UserInfo> firstSingle = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 1));
        List<UserInfo> lastSingle = this.session.queryStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(4, 1));

        assertEquals(3, first.size());
        assertEquals("XmlCrud1", first.get(0).getName());
        assertEquals("XmlCrud3", first.get(2).getName());
        assertEquals(2, second.size());
        assertEquals("XmlCrud3", second.get(0).getName());
        assertEquals("XmlCrud4", second.get(1).getName());
        assertEquals(1, lastPartial.size());
        assertEquals("XmlCrud5", lastPartial.get(0).getName());
        assertTrue(beyond.isEmpty());
        assertEquals(5, largerThanTotal.size());
        assertEquals("XmlCrud1", firstSingle.get(0).getName());
        assertEquals("XmlCrud5", lastSingle.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_PAGE_STATEMENT)
    public void xmlMapperPageStmtPageResult() throws Exception {
        Page page = new PageObject(1, 2);

        PageResult<UserInfo> result = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(2, result.getData().size());
        assertEquals("XmlCrud3", result.getData().get(0).getName());
        assertEquals("XmlCrud4", result.getData().get(1).getName());
        assertEquals(5, result.getTotalCount());
        assertEquals(3, result.getTotalPage());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_PAGE_STATEMENT_BOUNDARY)
    public void xmlMapperPageStmtTotalsForEdgePagesAndOffset() throws Exception {
        PageResult<UserInfo> first = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 2));
        PageResult<UserInfo> lastPartial = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(2, 2));
        PageResult<UserInfo> beyond = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(10, 2));
        PageResult<UserInfo> exactOnePage = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, new PageObject(0, 5));

        PageObject oneBased = new PageObject();
        oneBased.setPageNumberOffset(1);
        oneBased.setPageSize(2);
        oneBased.setCurrentPage(1);
        PageResult<UserInfo> offsetFirst = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, oneBased);
        oneBased.setCurrentPage(2);
        PageResult<UserInfo> offsetSecond = this.session.pageStatement("xmltest.CrudMapper.selectForPage", null, oneBased);

        assertEquals(2, first.getData().size());
        assertEquals("XmlCrud1", first.getData().get(0).getName());
        assertEquals(5, first.getTotalCount());
        assertEquals(3, first.getTotalPage());
        assertEquals(1, lastPartial.getData().size());
        assertEquals("XmlCrud5", lastPartial.getData().get(0).getName());
        assertTrue(beyond.getData().isEmpty());
        assertEquals(5, beyond.getTotalCount());
        assertEquals(5, exactOnePage.getData().size());
        assertEquals(1, exactOnePage.getTotalPage());
        assertEquals("XmlCrud1", offsetFirst.getData().get(0).getName());
        assertEquals("XmlCrud3", offsetSecond.getData().get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_NAMESPACE_MULTIPLE)
    public void xmlMapperNamespaceStmtsAcrossMultiLoadedMappers() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlCrudMapper.xml");
        config.loadMapper("/mapper/XmlResultMapMapper.xml");
        Session multiMapperSession = config.newSession(dataSource);

        List<UserInfo> fromCrud = multiMapperSession.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 1));
        List<UserInfo> fromResultMap = multiMapperSession.queryStatement("xmltest.ResultMapMapper.selectByIdExtended", mapOf("id", baseId() + 1));

        assertEquals(1, fromCrud.size());
        assertEquals(1, fromResultMap.size());
        assertEquals(fromCrud.get(0).getName(), fromResultMap.get(0).getName());
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
