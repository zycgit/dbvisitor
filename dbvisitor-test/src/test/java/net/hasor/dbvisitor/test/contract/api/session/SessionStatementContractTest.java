package net.hasor.dbvisitor.test.contract.api.session;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class SessionStatementContractTest extends AbstractNxnContractTest {
    private static final String NS = "session.UserSessionMapper";

    private Session session;

    @Before
    public void createStatementSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/session/UserSessionMapper.xml");
        this.session = configuration.newSession(dataSource);
    }

    protected int baseId() {
        return 814000;
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_EXECUTE_DML)
    public void sessionStatement_shouldExecuteInsertUpdateDeleteAndReturnAffectedRows() throws Exception {
        int id = baseId() + 1;
        assertEquals(1, execute("insertUser", userParams(id, "StmtDml", 25, "stmt-dml@nxn.test")));

        List<UserInfo> inserted = queryUsers("queryUserById", mapOf("id", id));
        assertEquals(1, inserted.size());
        assertEquals("StmtDml", inserted.get(0).getName());

        assertEquals(1, execute("updateUserEmail", mapOf("id", id, "email", "stmt-dml-new@nxn.test")));
        assertEquals("stmt-dml-new@nxn.test", queryUsers("queryUserById", mapOf("id", id)).get(0).getEmail());

        assertMutationRows(0, execute("updateUserEmail", mapOf("id", baseId() + 999, "email", "none@nxn.test")));
        assertEquals(1, execute("deleteUserById", mapOf("id", id)));
        assertTrue(queryUsers("queryUserById", mapOf("id", id)).isEmpty());
        assertMutationRows(0, execute("deleteUserById", mapOf("id", id)));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_QUERY_RESULT)
    public void sessionStatement_shouldQueryResultMapListScalarAndEmptyList() throws Exception {
        insertUser(baseId() + 10, "StmtQueryOne", 40, "q1@nxn.test");
        insertUser(baseId() + 11, "StmtQueryTwo", 40, "q2@nxn.test");
        insertUser(baseId() + 12, "StmtQueryThree", 41, "q3@nxn.test");

        List<UserInfo> byId = queryUsers("queryUserById", mapOf("id", baseId() + 10));
        assertEquals(1, byId.size());
        assertEquals(Integer.valueOf(baseId() + 10), byId.get(0).getId());
        assertNotNull(byId.get(0).getCreateTime());

        assertEquals(2, queryUsers("queryUsersByAge", mapOf("age", 40)).size());
        assertEquals(3, queryUsers("queryAllUsers", null).size());

        List<Integer> counts = this.session.queryStatement(NS + ".countUsers", null);
        assertEquals(1, counts.size());
        assertEquals(Integer.valueOf(3), counts.get(0));

        assertTrue(queryUsers("queryUserById", mapOf("id", baseId() + 999)).isEmpty());
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_DYNAMIC_PARAMETER)
    public void sessionStatement_shouldBindDynamicMapAndBeanParameters() throws Exception {
        insertUser(baseId() + 20, "StmtDyn", 28, "dyn@nxn.test");
        insertUser(baseId() + 21, "StmtOther", 29, "other@nxn.test");

        List<UserInfo> dynamic = queryUsers("queryUsersByCondition", mapOf("name", "StmtDyn", "age", 28));
        assertEquals(1, dynamic.size());
        assertEquals(Integer.valueOf(baseId() + 20), dynamic.get(0).getId());

        UserInfo bean = new UserInfo();
        bean.setName("StmtDyn");
        bean.setAge(28);
        List<UserInfo> byBean = this.session.queryStatement(NS + ".queryUserByBean", bean);
        assertEquals(1, byBean.size());
        assertEquals(Integer.valueOf(baseId() + 20), byBean.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_JOIN)
    public void sessionStatement_shouldQueryJoinResultIntoDto() throws Exception {
        int id = baseId() + 30;
        insertUser(id, "StmtJoin", 30, "join@nxn.test");
        execute("insertOrder", orderParams(id, id, "ORD-STMT-" + id, "199.50"));

        List<UserOrderDTO> list = this.session.queryStatement(NS + ".queryOrderWithUser", mapOf("orderId", id));
        assertEquals(1, list.size());

        UserOrderDTO dto = list.get(0);
        assertEquals(Integer.valueOf(id), dto.getOrderId());
        assertEquals("ORD-STMT-" + id, dto.getOrderNo());
        assertEquals(0, new BigDecimal("199.50").compareTo(dto.getAmount()));
        assertEquals("StmtJoin", dto.getUserName());
        assertEquals("join@nxn.test", dto.getUserEmail());
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_QUERY_PAGE)
    public void sessionStatement_shouldApplyPageObjectToQueryStatement() throws Exception {
        for (int i = 1; i <= 10; i++) {
            insertUser(baseId() + 100 + i, "StmtPage" + i, 20 + i, "page" + i + "@nxn.test");
        }

        PageObject first = page(3, 0);
        List<UserInfo> firstPage = this.session.queryStatement(NS + ".queryAllUsers", null, first);
        assertEquals(3, firstPage.size());
        assertEquals(Integer.valueOf(baseId() + 101), firstPage.get(0).getId());

        PageObject second = page(4, 1);
        List<UserInfo> secondPage = this.session.queryStatement(NS + ".queryAllUsers", null, second);
        assertEquals(4, secondPage.size());
        assertEquals(Integer.valueOf(baseId() + 105), secondPage.get(0).getId());

        PageObject beyond = page(3, 5);
        List<UserInfo> beyondPage = this.session.queryStatement(NS + ".queryAllUsers", null, beyond);
        assertTrue(beyondPage.isEmpty());
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_PAGE_RESULT)
    public void sessionStatement_shouldReturnPageResultWithTotalCount() throws Exception {
        for (int i = 1; i <= 6; i++) {
            insertUser(baseId() + 200 + i, "StmtPageResult" + i, 35, "pageres" + i + "@nxn.test");
        }
        insertUser(baseId() + 210, "StmtOtherAge", 99, "other-age@nxn.test");

        PageResult<UserInfo> first = this.session.pageStatement(NS + ".queryAllUsers", null, page(3, 0));
        assertEquals(3, first.getData().size());
        assertEquals(7, first.getTotalCount());

        PageResult<UserInfo> filtered = this.session.pageStatement(NS + ".queryUsersByAge", mapOf("age", 35), page(2, 0));
        assertEquals(2, filtered.getData().size());
        assertEquals(6, filtered.getTotalCount());

        PageResult<UserInfo> empty = this.session.pageStatement(NS + ".queryUsersByAge", mapOf("age", 9999), page(10, 0));
        assertTrue(empty.getData().isEmpty());
        assertEquals(0, empty.getTotalCount());
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_INVALID_ID)
    public void sessionStatement_shouldRejectUnknownStatementIdsForExecuteAndQuery() throws Exception {
        expectStatementFailure(new StatementCall() {
            @Override
            public void run() throws Exception {
                session.executeStatement(NS + ".nonExistent", new HashMap<String, Object>());
            }
        });
        expectStatementFailure(new StatementCall() {
            @Override
            public void run() throws Exception {
                session.queryStatement(NS + ".nonExistent", new HashMap<String, Object>());
            }
        });
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_CROSS_TABLE)
    public void sessionStatement_shouldCoordinateCrossTableOperations() throws Exception {
        int userId = baseId() + 300;
        insertUser(userId, "StmtCross", 30, "cross@nxn.test");
        execute("insertOrder", orderParams(userId + 1, userId, "ORD-" + (userId + 1), "50.00"));
        execute("insertOrder", orderParams(userId + 2, userId, "ORD-" + (userId + 2), "100.00"));

        List<UserOrder> orders = this.session.queryStatement(NS + ".queryOrdersByUserId", mapOf("userId", userId));
        assertEquals(2, orders.size());

        assertEquals(1, execute("deleteOrderById", mapOf("id", userId + 1)));
        orders = this.session.queryStatement(NS + ".queryOrdersByUserId", mapOf("userId", userId));
        assertEquals(1, orders.size());
        assertEquals("ORD-" + (userId + 2), orders.get(0).getOrderNo());
    }

    private int execute(String statementId, Map<String, Object> params) throws Exception {
        Object result = this.session.executeStatement(NS + "." + statementId, params);
        return ((Number) result).intValue();
    }

    private void insertUser(int id, String name, int age, String email) throws Exception {
        execute("insertUser", userParams(id, name, age, email));
    }

    private List<UserInfo> queryUsers(String statementId, Object params) throws Exception {
        return this.session.queryStatement(NS + "." + statementId, params);
    }

    private Map<String, Object> userParams(int id, String name, int age, String email) {
        return mapOf("id", id, "name", name, "age", age, "email", email);
    }

    private Map<String, Object> orderParams(int id, int userId, String orderNo, String amount) {
        return mapOf("id", id, "userId", userId, "orderNo", orderNo, "amount", new BigDecimal(amount));
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

    private void expectStatementFailure(StatementCall call) throws Exception {
        try {
            call.run();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
            return;
        }
        throw new AssertionError("Expected statement call to fail.");
    }

    private interface StatementCall {
        void run() throws Exception;
    }
}
