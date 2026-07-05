package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class LambdaDuplicateStrategyContractTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 840000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_INTO)
    public void duplicateStrategyInto_shouldInsertNewRowsAndRejectDuplicatePrimaryKeys() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);

        int id = baseId() + 1;
        int inserted = lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Into)//
                .applyEntity(user(id, "IntoNormal", 25))//
                .executeSumResult();
        assertEquals(1, inserted);
        assertUser(id, "IntoNormal", 25);

        try {
            lambdaTemplate.insert(UserInfo.class)//
                    .onDuplicateStrategy(DuplicateKeyStrategy.Into)//
                    .applyEntity(user(id, "IntoDuplicate", 30))//
                    .executeSumResult();
            fail("Into strategy should reject duplicate primary keys.");
        } catch (SQLException e) {
            assertDuplicateMessage(e);
        }
        assertUser(id, "IntoNormal", 25);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_DEFAULT)
    public void duplicateStrategyDefault_shouldBehaveLikeInto() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);

        int id = baseId() + 11;
        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(id, "DefaultOrig", 20))//
                .executeSumResult();

        try {
            lambdaTemplate.insert(UserInfo.class)//
                    .applyEntity(user(id, "DefaultDup", 30))//
                    .executeSumResult();
            fail("Default insert strategy should reject duplicate primary keys.");
        } catch (SQLException e) {
            assertDuplicateMessage(e);
        }

        assertUser(id, "DefaultOrig", 20);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_BASIC)
    public void duplicateStrategyIgnore_shouldInsertNewRowsAndKeepOriginalOnDuplicate() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_KEY_STRATEGY);
        int newId = baseId() + 21;
        int duplicateId = baseId() + 22;

        int inserted = lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(user(newId, "IgnoreNew", 25))//
                .executeSumResult();
        assertTrue(inserted == 0 || inserted == 1);
        assertUser(newId, "IgnoreNew", 25);

        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(duplicateId, "IgnoreOriginal", 26))//
                .executeSumResult();
        int duplicateRows = lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(user(duplicateId, "IgnoreSkipped", 99))//
                .executeSumResult();
        assertTrue(duplicateRows == 0 || duplicateRows == 1);
        assertUser(duplicateId, "IgnoreOriginal", 26);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_IGNORE_MIXED)
    public void duplicateStrategyIgnore_shouldHandleRepeatedAndMixedDuplicateRows() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_KEY_STRATEGY);
        int repeatedId = baseId() + 31;
        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(repeatedId, "IgnoreFirst", 10))//
                .executeSumResult();
        for (int i = 0; i < 3; i++) {
            lambdaTemplate.insert(UserInfo.class)//
                    .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                    .applyEntity(user(repeatedId, "IgnoreAttempt" + i, 20 + i))//
                    .executeSumResult();
        }
        assertUser(repeatedId, "IgnoreFirst", 10);

        int id1 = baseId() + 41;
        int id2 = baseId() + 42;
        int id3 = baseId() + 43;
        int id4 = baseId() + 44;
        lambdaTemplate.insert(UserInfo.class).applyEntity(user(id1, "IgnoreExisting1", 11)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).applyEntity(user(id3, "IgnoreExisting3", 33)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore).applyEntity(user(id1, "IgnoreDup1", 12)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore).applyEntity(user(id2, "IgnoreNew2", 22)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore).applyEntity(user(id3, "IgnoreDup3", 34)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore).applyEntity(user(id4, "IgnoreNew4", 44)).executeSumResult();

        assertUser(id1, "IgnoreExisting1", 11);
        assertUser(id2, "IgnoreNew2", 22);
        assertUser(id3, "IgnoreExisting3", 33);
        assertUser(id4, "IgnoreNew4", 44);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_BASIC)
    public void duplicateStrategyUpdate_shouldInsertNewRowsAndUpdateDuplicateRows() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_KEY_STRATEGY);
        int newId = baseId() + 51;
        int duplicateId = baseId() + 52;

        int inserted = lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(user(newId, "UpdateNew", 25))//
                .executeSumResult();
        assertTrue(inserted >= 1);
        assertUser(newId, "UpdateNew", 25);

        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(duplicateId, "UpdateOld", 26))//
                .executeSumResult();
        UserInfo updated = user(duplicateId, "UpdateNewName", 35);
        updated.setEmail("updated-basic@strategy.test");
        lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(updated)//
                .executeSumResult();
        UserInfo loaded = loadUser(duplicateId);
        assertEquals("UpdateNewName", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
        assertEquals("updated-basic@strategy.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_UPDATE_MIXED)
    public void duplicateStrategyUpdate_shouldHandleRepeatedMixedAndNullFieldUpdates() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_KEY_STRATEGY);
        int repeatedId = baseId() + 61;
        lambdaTemplate.insert(UserInfo.class).applyEntity(user(repeatedId, "UpdateV1", 10)).executeSumResult();
        for (int i = 2; i <= 4; i++) {
            lambdaTemplate.insert(UserInfo.class)//
                    .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                    .applyEntity(user(repeatedId, "UpdateV" + i, 10 * i))//
                    .executeSumResult();
        }
        assertUser(repeatedId, "UpdateV4", 40);

        int id1 = baseId() + 71;
        int id2 = baseId() + 72;
        int id3 = baseId() + 73;
        int id4 = baseId() + 74;
        lambdaTemplate.insert(UserInfo.class).applyEntity(user(id1, "UpdateExisting1", 11)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).applyEntity(user(id3, "UpdateExisting3", 33)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update).applyEntity(user(id1, "UpdateChanged1", 12)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update).applyEntity(user(id2, "UpdateNew2", 22)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update).applyEntity(user(id3, "UpdateChanged3", 34)).executeSumResult();
        lambdaTemplate.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update).applyEntity(user(id4, "UpdateNew4", 44)).executeSumResult();
        assertUser(id1, "UpdateChanged1", 12);
        assertUser(id2, "UpdateNew2", 22);
        assertUser(id3, "UpdateChanged3", 34);
        assertUser(id4, "UpdateNew4", 44);

        int partialId = baseId() + 81;
        lambdaTemplate.insert(UserInfo.class).applyEntity(user(partialId, "WithEmail", 25)).executeSumResult();
        UserInfo partial = new UserInfo();
        partial.setId(partialId);
        partial.setName("NoEmail");
        partial.setAge(30);
        partial.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(partial)//
                .executeSumResult();
        UserInfo loaded = loadUser(partialId);
        assertEquals("NoEmail", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
        assertEquals("withemail@strategy.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_DUPLICATE_STRATEGY_TRANSITION)
    public void duplicateStrategy_shouldKeepIgnoredRowUntilUpdateStrategyOverridesIt() throws SQLException {
        requiresNxnFeature(FeatureId.DUPLICATE_KEY_STRATEGY);
        int id = baseId() + 91;
        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user(id, "StrategyOriginal", 10))//
                .executeSumResult();

        lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(user(id, "StrategyIgnored", 20))//
                .executeSumResult();
        assertUser(id, "StrategyOriginal", 10);

        lambdaTemplate.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(user(id, "StrategyUpdated", 30))//
                .executeSumResult();
        assertUser(id, "StrategyUpdated", 30);
    }

    private UserInfo user(int id, String name, Integer age) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(name.toLowerCase() + "@strategy.test");
        user.setCreateTime(new Date());
        return user;
    }

    private UserInfo loadUser(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject();
    }

    private void assertUser(int id, String name, Integer age) throws SQLException {
        UserInfo loaded = loadUser(id);
        assertEquals(name, loaded.getName());
        assertEquals(age, loaded.getAge());
    }

    private void assertDuplicateMessage(SQLException e) {
        assertTrue("Duplicate rejection should mention a key/constraint failure: " + e.getMessage(), isDuplicateKeyMessage(e));
    }
}
