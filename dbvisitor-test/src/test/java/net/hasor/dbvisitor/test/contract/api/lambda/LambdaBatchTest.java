package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class LambdaBatchTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 760000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_CONDITION)
    public void lambdaBatchMutationManyRowsByCond() throws SQLException {
        insertUsers("LBUpdCond", ages(16, 35), baseId() + 10);

        int updated = lambdaTemplate.update(UserInfo.class)//
                .like(UserInfo::getName, "LBUpdCond%")//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .updateTo(UserInfo::getAge, 25)//
                .doUpdate();

        assertMutationRows(11, updated);
        assertEquals(11, countByNameAndAge("LBUpdCond%", 25));
        assertEquals(20, countByName("LBUpdCond%"));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_MULTI_FIELD)
    public void lambdaBatchMutationMultiFieldsForIdList() throws SQLException {
        insertUsers("LBUpdFields", new int[] { 20, 25, 30 }, baseId() + 40);
        List<Integer> ids = Arrays.asList(baseId() + 40, baseId() + 41, baseId() + 42);

        int updated = lambdaTemplate.update(UserInfo.class)//
                .in(UserInfo::getId, ids)//
                .updateTo(UserInfo::getAge, 99)//
                .updateTo(UserInfo::getEmail, "lambda-batch@updated.test")//
                .doUpdate();

        assertMutationRows(3, updated);
        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids)//
                .orderBy("id")//
                .queryForList();
        assertEquals(3, users.size());
        for (UserInfo user : users) {
            assertEquals(Integer.valueOf(99), user.getAge());
            assertEquals("lambda-batch@updated.test", user.getEmail());
        }
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_LOOP)
    public void lambdaBatchMutationLoopedEntityStyleUpdates() throws SQLException {
        insertUsers("LBUpdLoop", new int[] { 21, 22, 23, 24, 25 }, baseId() + 60);

        int totalUpdated = 0;
        for (int i = 0; i < 5; i++) {
            totalUpdated += lambdaTemplate.update(UserInfo.class)//
                    .eq(UserInfo::getId, baseId() + 60 + i)//
                    .updateTo(UserInfo::getAge, 31 + i)//
                    .updateTo(UserInfo::getEmail, "loop-updated@test.com")//
                    .doUpdate();
        }

        assertEquals(5, totalUpdated);
        UserInfo first = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .queryForObject();
        UserInfo last = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 64)//
                .queryForObject();
        assertEquals(Integer.valueOf(31), first.getAge());
        assertEquals(Integer.valueOf(35), last.getAge());
        assertEquals(5, countByNameAndEmail("LBUpdLoop%", "loop-updated@test.com"));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_CONDITION)
    public void lambdaBatchMutationManyRowsByCond2() throws SQLException {
        insertUsers("LBDelCond", ages(16, 25), baseId() + 80);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .like(UserInfo::getName, "LBDelCond%")//
                .lt(UserInfo::getAge, 18)//
                .doDelete();

        assertMutationRows(2, deleted);
        assertEquals(8, countByName("LBDelCond%"));
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LBDelCond%")//
                .lt(UserInfo::getAge, 18)//
                .queryForCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_IN)
    public void lambdaBatchMutationRowsByIdList() throws SQLException {
        insertUsers("LBDelIn", ages(25, 34), baseId() + 100);
        List<Integer> ids = Arrays.asList(baseId() + 100, baseId() + 102, baseId() + 104, baseId() + 106);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .in(UserInfo::getId, ids)//
                .doDelete();

        assertMutationRows(4, deleted);
        assertEquals(6, countByName("LBDelIn%"));
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids)//
                .queryForCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_MATCHING)
    public void lambdaBatchMutationAllRowsMatchingPred() throws SQLException {
        insertUsers("LBDelAll", ages(30, 49), baseId() + 120);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .like(UserInfo::getName, "LBDelAll%")//
                .doDelete();

        assertMutationRows(20, deleted);
        assertEquals(0, countByName("LBDelAll%"));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_DELETE_CHUNKS)
    public void lambdaBatchMutationRowsInChunksUsingPagedIdLookup() throws SQLException {
        insertUsers("LBDelChunk", ages(25, 74), baseId() + 160);

        int totalDeleted = 0;
        while (true) {
            List<UserInfo> batch = lambdaTemplate.query(UserInfo.class)//
                    .like(UserInfo::getName, "LBDelChunk%")//
                    .orderBy("id")//
                    .initPage(10, 0)//
                    .queryForList();
            if (batch.isEmpty()) {
                break;
            }

            List<Integer> ids = new ArrayList<>();
            for (UserInfo user : batch) {
                ids.add(user.getId());
            }
            totalDeleted += lambdaTemplate.delete(UserInfo.class)//
                    .in(UserInfo::getId, ids)//
                    .doDelete();
            assertTrue("chunk delete should make progress", totalDeleted <= 50);
        }

        assertMutationRows(50, totalDeleted);
        assertEquals(0, countByName("LBDelChunk%"));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_UPDATE_DYNAMIC)
    public void lambdaBatchMutationHonorDynamicUpdateFieldFlags() throws SQLException {
        insertByJdbc(baseId() + 230, "LBUpdDynamic", 25, "dynamic-original@test.com");

        int updated = lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 230)//
                .updateTo(true, UserInfo::getAge, 30)//
                .updateTo(false, UserInfo::getEmail, "dynamic-skipped@test.com")//
                .doUpdate();
        UserInfo row = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 230)//
                .queryForObject();

        assertEquals(1, updated);
        assertEquals(Integer.valueOf(30), row.getAge());
        assertEquals("dynamic-original@test.com", row.getEmail());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_BATCH_MUTATION_NULL_BOUNDARY)
    public void lambdaBatchMutationAndDeleteRowsUsingNullPreds() throws SQLException {
        insertByJdbc(baseId() + 240, "LBNullBoundary1", 30, "null-boundary@test.com");
        insertByJdbc(baseId() + 241, "LBNullBoundary2", null, "null-boundary@test.com");

        int updated = lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 240)//
                .updateTo(UserInfo::getEmail, null)//
                .doUpdate();
        long nullEmail = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 240)//
                .isNull(UserInfo::getEmail)//
                .queryForCount();
        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .like(UserInfo::getName, "LBNullBoundary%")//
                .isNull(UserInfo::getAge)//
                .doDelete();
        long remaining = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LBNullBoundary%")//
                .queryForCount();

        assertEquals(1, updated);
        assertEquals(1, nullEmail);
        assertEquals(1, deleted);
        assertEquals(1, remaining);
    }

    private int[] ages(int startInclusive, int endInclusive) {
        int[] values = new int[endInclusive - startInclusive + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = startInclusive + i;
        }
        return values;
    }

    private void insertUsers(String prefix, int[] ages, int startId) throws SQLException {
        for (int i = 0; i < ages.length; i++) {
            insertByJdbc(startId + i, prefix + (i + 1), ages[i], prefix.toLowerCase() + (i + 1) + "@test.com");
        }
    }

    private void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private long countByName(String namePattern) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, namePattern)//
                .queryForCount();
    }

    private long countByNameAndAge(String namePattern, Integer age) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, namePattern)//
                .eq(UserInfo::getAge, age)//
                .queryForCount();
    }

    private long countByNameAndEmail(String namePattern, String email) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, namePattern)//
                .eq(UserInfo::getEmail, email)//
                .queryForCount();
    }
}
