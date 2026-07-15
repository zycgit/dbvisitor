package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class LambdaIteratorContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 700000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_LIMIT_BATCH)
    public void lambdaIteratorForLimit_shouldIterateLimitedRowsByBatch() throws SQLException {
        seedUsers(baseId() + 1, "NXN-Iter-Limit-", 100, 20);

        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-Limit-%")//
                .orderBy("id")//
                .iteratorForLimit(50, 10);

        List<Integer> ids = collectIds(iterator);

        assertEquals(50, ids.size());
        assertEquals(Integer.valueOf(baseId() + 1), ids.get(0));
        assertEquals(Integer.valueOf(baseId() + 50), ids.get(49));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_ALL)
    public void lambdaIteratorForLimit_shouldIterateAllRowsWhenLimitIsNegative() throws SQLException {
        seedUsers(baseId() + 201, "NXN-Iter-All-", 30, 25);

        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-All-%")//
                .orderBy("id")//
                .iteratorForLimit(-1, 10);

        assertEquals(30, count(iterator));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_BATCH)
    public void lambdaIteratorByBatch_shouldIterateAllRowsUsingBatchSize() throws SQLException {
        seedUsers(baseId() + 301, "NXN-Iter-Batch-", 20, 30);

        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-Batch-%")//
                .orderBy("id")//
                .iteratorByBatch(5);

        assertEquals(20, count(iterator));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_CONDITION)
    public void lambdaIterator_shouldRespectQueryConditions() throws SQLException {
        for (int i = 1; i <= 50; i++) {
            insertUser(baseId() + 400 + i, "NXN-Iter-Cond-" + i, 20 + (i % 5));
        }

        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-Cond-%")//
                .gt(UserInfo::getAge, 22)//
                .orderBy("id")//
                .iteratorForLimit(-1, 10);

        int count = 0;
        while (iterator.hasNext()) {
            UserInfo user = iterator.next();
            assertTrue(user.getAge() > 22);
            count++;
        }

        assertEquals(20, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_TRANSFORM)
    public void lambdaIterator_shouldTransformRows() throws SQLException {
        seedUsers(baseId() + 501, "NXN-Iter-Transform-", 20, 35);

        Iterator<String> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-Transform-%")//
                .orderBy("id")//
                .iteratorForLimit(-1, 10, UserInfo::getName);

        int count = 0;
        while (iterator.hasNext()) {
            String name = iterator.next();
            assertNotNull(name);
            assertTrue(name.startsWith("NXN-Iter-Transform-"));
            count++;
        }

        assertEquals(20, count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_EMPTY)
    public void lambdaIterator_shouldHandleEmptyResults() throws SQLException {
        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-None-%")//
                .iteratorForLimit(-1, 10);

        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("Empty iterator next() should fail.");
        } catch (Exception e) {
            String message = e.getMessage();
            assertTrue(e.getClass().getName().contains("NoSuchElement") || (message != null && message.toLowerCase().contains("element")));
        }
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_EARLY_BREAK)
    public void lambdaIterator_shouldAllowConsumersToStopBeforeExhaustion() throws SQLException {
        seedUsers(baseId() + 701, "NXN-Iter-Break-", 50, 25);

        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-Break-%")//
                .orderBy("id")//
                .iteratorForLimit(-1, 10);

        int count = 0;
        while (iterator.hasNext() && count < 10) {
            iterator.next();
            count++;
        }

        assertEquals(10, count);
        assertTrue(iterator.hasNext());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_ITERATOR_LARGE_BATCH)
    public void lambdaIterator_shouldTraverseLargeBatchResultSets() throws SQLException {
        seedUsers(baseId() + 801, "NXN-Iter-Large-", 500, 25);

        Iterator<UserInfo> iterator = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Iter-Large-%")//
                .orderBy("id")//
                .iteratorByBatch(100);

        assertEquals(500, count(iterator));
    }

    private void seedUsers(int startId, String prefix, int count, int age) throws SQLException {
        for (int i = 0; i < count; i++) {
            insertUser(startId + i, prefix + (i + 1), age);
        }
    }

    private void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, name.toLowerCase() + "@nxn.test", new Date() });
    }

    private List<Integer> collectIds(Iterator<UserInfo> iterator) {
        List<Integer> ids = new ArrayList<>();
        while (iterator.hasNext()) {
            ids.add(iterator.next().getId());
        }
        return ids;
    }

    private int count(Iterator<UserInfo> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }
}
