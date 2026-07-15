package net.hasor.dbvisitor.test.contract.api.session;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.DeclarativeOrderMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class SessionMapperContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 812000;
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_SIMPLE)
    public void sessionCreateMapper_shouldCreateSimpleMapperProxyForCrudListAndScalarResults() throws Exception {
        SessionUserMapper mapper = createSession().createMapper(SessionUserMapper.class);
        int firstId = baseId() + 1;
        int secondId = baseId() + 2;

        assertNotNull(mapper);
        assertEquals(1, mapper.insertUser(user(firstId, "SimpleOne", 25, "simple1@nxn.test")));
        assertEquals(1, mapper.insertUser(user(secondId, "SimpleTwo", 26, "simple2@nxn.test")));

        assertEquals("SimpleOne", mapper.selectById(firstId).getName());
        assertEquals(2, mapper.selectAll().size());
        assertEquals(2, mapper.countAll());

        UserInfo update = user(firstId, "SimpleUpdated", 27, "simple-updated@nxn.test");
        assertEquals(1, mapper.updateUser(update));
        assertEquals("SimpleUpdated", mapper.selectById(firstId).getName());

        assertEquals(1, mapper.deleteById(secondId));
        assertNull(mapper.selectById(secondId));
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REF)
    public void sessionCreateMapper_shouldCreateRefMapperProxyForXmlCrudListAndScalarResults() throws Exception {
        SessionRefUserMapper mapper = createSession().createMapper(SessionRefUserMapper.class);
        int firstId = baseId() + 10;
        int secondId = baseId() + 11;

        assertNotNull(mapper);
        assertEquals(1, mapper.insertUser(user(firstId, "RefOne", 30, "ref1@nxn.test")));
        assertEquals(1, mapper.insertUser(user(secondId, "RefTwo", 31, "ref2@nxn.test")));

        assertEquals("RefOne", mapper.queryUserById(firstId).getName());
        assertEquals(2, mapper.queryAllUsers().size());
        assertEquals(2, mapper.countUsers());

        assertEquals(1, mapper.updateUserEmail(firstId, "ref-new@nxn.test"));
        assertEquals("ref-new@nxn.test", mapper.queryUserById(firstId).getEmail());

        assertEquals(1, mapper.deleteUserById(secondId));
        assertEquals(1, mapper.countUsers());
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REF_JOIN)
    public void sessionCreateMapper_shouldLetRefMapperJoinWithBaseMapperDataInSameSession() throws Exception {
        Session session = createSession();
        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);
        BaseMapper<UserOrder> orderMapper = session.createBaseMapper(UserOrder.class);
        int id = baseId() + 20;

        assertEquals(1, mapper.insertUser(user(id, "RefJoinUser", 32, "refjoin@nxn.test")));
        assertEquals(1, orderMapper.insert(order(id, id, "ORD-REF-" + id, "299.99")));

        UserOrderDTO dto = mapper.queryOrderWithUser(id);
        assertNotNull(dto);
        assertEquals(Integer.valueOf(id), dto.getOrderId());
        assertEquals("ORD-REF-" + id, dto.getOrderNo());
        assertEquals("RefJoinUser", dto.getUserName());
        assertEquals("refjoin@nxn.test", dto.getUserEmail());
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_DECLARATIVE)
    public void sessionCreateMapper_shouldReuseDeclarativeOrderMapperInSameSession() throws Exception {
        Session session = createSession();
        BaseMapper<UserInfo> userMapper = session.createBaseMapper(UserInfo.class);
        DeclarativeOrderMapper orderMapper = session.createMapper(DeclarativeOrderMapper.class);
        int userId = baseId() + 30;

        assertEquals(1, userMapper.insert(user(userId, "OrderOwner", 35, "owner@nxn.test")));
        assertEquals(1, orderMapper.insertOrder(order(null, userId, "ORD-DECL-" + userId, "150.00")));

        Integer orderCount = session.jdbc().queryForObject("SELECT COUNT(*) FROM user_order WHERE user_id = ?", new Object[] { userId }, Integer.class);
        assertEquals(Integer.valueOf(1), orderCount);
        assertEquals(1, orderMapper.countAll());
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_MIXED)
    public void sessionCreateMapper_shouldAllowMapperTypesAndBaseMapperToShareOneSession() throws Exception {
        Session session = createSession();
        BaseMapper<UserInfo> baseMapper = session.createBaseMapper(UserInfo.class);
        SessionUserMapper userMapper = session.createMapper(SessionUserMapper.class);
        DeclarativeOrderMapper orderMapper = session.createMapper(DeclarativeOrderMapper.class);
        int userId = baseId() + 40;

        assertEquals(1, baseMapper.insert(user(userId, "MixedUser", 28, "mixed@nxn.test")));
        assertEquals("MixedUser", userMapper.selectById(userId).getName());

        assertEquals(1, userMapper.updateUser(user(userId, "MixedUpdated", 29, "mixed@nxn.test")));
        assertEquals("MixedUpdated", baseMapper.selectById(userId).getName());

        assertEquals(1, orderMapper.insertOrder(order(null, userId, "ORD-MIXED-" + userId, "75.00")));
        assertEquals(1, userMapper.countAll());
        assertEquals(1, orderMapper.countAll());
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_INVALID)
    public void sessionCreateMapper_shouldRejectInterfacesWithoutMapperAnnotation() throws Exception {
        try {
            createSession().createMapper(Runnable.class);
        } catch (Exception e) {
            assertNotNull(e.getMessage());
            return;
        }
        throw new AssertionError("Expected plain interface mapper creation to fail.");
    }

    private Session createSession() throws Exception {
        Configuration configuration = newConfiguration();
        return configuration.newSession(dataSource);
    }

    private UserInfo user(Integer id, String name, Integer age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        return user;
    }

    private UserOrder order(Integer id, Integer userId, String orderNo, String amount) {
        UserOrder order = new UserOrder();
        order.setId(id);
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(new BigDecimal(amount));
        order.setCreateTime(new Date());
        return order;
    }
}
