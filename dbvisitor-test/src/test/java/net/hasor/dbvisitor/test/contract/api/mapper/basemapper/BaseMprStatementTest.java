package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMprStatementTest extends AbstractNxnContractTest {
    private static final String NS = "StatementTestMapper";

    private BaseMapper<UserInfo> mapper;

    @Before
    public void createBaseMapperWithStmts() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/mapper/StatementTestMapper.xml");
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createBaseMapper(UserInfo.class);
    }

    protected int baseId() {
        return 919000;
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_EXECUTE_DML)
    public void baseMapperStmtDmlStmts() {
        int id = baseId() + 1;

        assertEquals(1, execute("insertUserWithId", userParams(id, "BaseStmtDml", 25, "base-stmt@nxn.test")));
        assertEquals("BaseStmtDml", queryOne("queryUserById", mapOf("id", id)).getName());

        assertEquals(1, execute("updateUserEmail", mapOf("id", id, "email", "base-stmt-new@nxn.test")));
        assertEquals("base-stmt-new@nxn.test", queryOne("queryUserById", mapOf("id", id)).getEmail());

        assertMutationRows(0, execute("updateUserEmail", mapOf("id", baseId() + 999, "email", "missing@nxn.test")));
        assertEquals(1, execute("deleteUserById", mapOf("id", id)));
        assertTrue(query("queryUserById", mapOf("id", id)).isEmpty());
        assertMutationRows(0, execute("deleteUserById", mapOf("id", id)));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_RESULT)
    public void baseMapperStmtResultMapAndResultTypeStmts() {
        insert(baseId() + 11, "BaseStmtQuery1", 31);
        insert(baseId() + 12, "BaseStmtQuery2", 32);
        insert(baseId() + 13, "BaseStmtQuery3", 33);

        UserInfo byId = queryOne("queryUserById", mapOf("id", baseId() + 11));
        assertEquals(Integer.valueOf(baseId() + 11), byId.getId());
        assertEquals("BaseStmtQuery1", byId.getName());
        assertNotNull(byId.getCreateTime());

        List<UserInfo> byName = query("queryUsersByName", mapOf("name", "BaseStmtQuery%"));
        assertEquals(3, byName.size());
        assertEquals(Integer.valueOf(baseId() + 11), byName.get(0).getId());

        List<UserInfo> all = this.mapper.queryStatement(NS + ".queryAllUsers", null);
        assertTrue(all.size() >= 3);

        assertTrue(query("queryUserById", mapOf("id", baseId() + 999)).isEmpty());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_PAGE)
    public void baseMapperStmtPageObjectToQueryStmt() {
        for (int i = 1; i <= 10; i++) {
            insert(baseId() + 100 + i, "BaseStmtPage" + i, 20 + i);
        }

        List<UserInfo> first = this.mapper.queryStatement(NS + ".queryUsersByName", mapOf("name", "BaseStmtPage%"), page(3, 0));
        List<UserInfo> second = this.mapper.queryStatement(NS + ".queryUsersByName", mapOf("name", "BaseStmtPage%"), page(4, 1));
        List<UserInfo> beyond = this.mapper.queryStatement(NS + ".queryUsersByName", mapOf("name", "BaseStmtPage%"), page(3, 5));

        assertEquals(3, first.size());
        assertEquals(Integer.valueOf(baseId() + 101), first.get(0).getId());
        assertEquals(4, second.size());
        assertEquals(Integer.valueOf(baseId() + 105), second.get(0).getId());
        assertTrue(beyond.isEmpty());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_BATCH_DELETE)
    public void baseMapperStmtBatchDeleteByStmt() {
        for (int i = 1; i <= 5; i++) {
            insert(baseId() + 200 + i, "BaseStmtDelete" + i, 40 + i);
        }

        int deleted = execute("deleteUsersByName", mapOf("name", "BaseStmtDelete%"));

        assertMutationRows(5, deleted);
        assertTrue(query("queryUsersByName", mapOf("name", "BaseStmtDelete%")).isEmpty());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_INVALID_ID)
    public void baseMapperStmtUnknownStmtIds() {
        expectStatementFailure(new StatementCall() {
            @Override
            public void run() {
                mapper.executeStatement(NS + ".missingExecute", new HashMap<String, Object>());
            }
        });
        expectStatementFailure(new StatementCall() {
            @Override
            public void run() {
                mapper.queryStatement(NS + ".missingQuery", new HashMap<String, Object>());
            }
        });
    }

    private void insert(int id, String name, int age) {
        assertEquals(1, execute("insertUserWithId", userParams(id, name, age, name.toLowerCase() + "@nxn.test")));
    }

    private int execute(String statementId, Map<String, Object> params) {
        Object result = this.mapper.executeStatement(NS + "." + statementId, params);
        return ((Number) result).intValue();
    }

    private UserInfo queryOne(String statementId, Object params) {
        List<UserInfo> list = query(statementId, params);
        assertEquals(1, list.size());
        return list.get(0);
    }

    private List<UserInfo> query(String statementId, Object params) {
        return this.mapper.queryStatement(NS + "." + statementId, params);
    }

    private Map<String, Object> userParams(int id, String name, int age, String email) {
        return mapOf("id", id, "name", name, "age", age, "email", email);
    }

    private PageObject page(int pageSize, int currentPage) {
        PageObject page = new PageObject();
        page.setPageSize(pageSize);
        page.setCurrentPage(currentPage);
        return page;
    }

    private Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private void expectStatementFailure(StatementCall call) {
        try {
            call.run();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
            return;
        }
        throw new AssertionError("Expected statement call to fail.");
    }

    private interface StatementCall {
        void run();
    }
}
