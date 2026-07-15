package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class LambdaEdgeTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 690000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_QUERY_NO_RESULT)
    public void lambdaQueryForObjectNullWhenNoRowMatches() throws SQLException {
        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 1)//
                .queryForObject();

        assertNull(result);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_QUERY_MULTI_RESULT)
    public void lambdaQueryForObjectFirstRowWhenMultiRowsMatch() throws SQLException {
        insertUser(baseId() + 11, "NXN-Lambda-Edge-Multi-1", 25);
        insertUser(baseId() + 12, "NXN-Lambda-Edge-Multi-2", 26);

        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12))//
                .orderBy("id")//
                .queryForObject();

        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 11), result.getId());
        assertEquals("NXN-Lambda-Edge-Multi-1", result.getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_UPDATE_NO_MATCH)
    public void lambdaUpdateZeroWhenNoRowMatches() throws SQLException {
        requiresNxnFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS);

        int updated = lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 21)//
                .updateTo(UserInfo::getAge, 100)//
                .doUpdate();

        assertEquals(0, updated);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_DELETE_NO_MATCH)
    public void lambdaDeleteZeroWhenNoRowMatches() throws SQLException {
        requiresNxnFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .doDelete();

        assertEquals(0, deleted);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_REJECT)
    public void lambdaDeleteEmptyWhereByDef() throws SQLException {
        insertUser(baseId() + 41, "NXN-Lambda-Edge-Delete-Reject", 25);

        try {
            lambdaTemplate.delete(UserInfo.class).doDelete();
            fail("Empty-where delete should require allowEmptyWhere().");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("allowEmptyWhere"));
        }

        assertNotNull(lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 41).queryForObject());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_DELETE_ALLOW)
    public void lambdaDeleteEmptyWhereWhenExplicitlyEnabled() throws SQLException {
        requiresNxnFeature(FeatureId.EMPTY_WHERE_MUTATION);

        insertUser(baseId() + 51, "NXN-Lambda-Edge-Delete-Allow-1", 25);
        insertUser(baseId() + 52, "NXN-Lambda-Edge-Delete-Allow-2", 26);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .allowEmptyWhere()//
                .doDelete();

        assertTrue(deleted >= 2);
        assertEquals(0, lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 51, baseId() + 52))//
                .queryForCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_REJECT)
    public void lambdaUpdateEmptyWhereByDef() throws SQLException {
        insertUser(baseId() + 61, "NXN-Lambda-Edge-Update-Reject", 25);

        try {
            lambdaTemplate.update(UserInfo.class)//
                    .updateTo(UserInfo::getAge, 99)//
                    .doUpdate();
            fail("Empty-where update should require allowEmptyWhere().");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("allowEmptyWhere"));
        }

        assertEquals(Integer.valueOf(25), lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 61).queryForObject().getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_EMPTY_WHERE_UPDATE_ALLOW)
    public void lambdaUpdateEmptyWhereWhenExplicitlyEnabled() throws SQLException {
        requiresNxnFeature(FeatureId.EMPTY_WHERE_MUTATION);

        insertUser(baseId() + 71, "NXN-Lambda-Edge-Update-Allow-1", 20);
        insertUser(baseId() + 72, "NXN-Lambda-Edge-Update-Allow-2", 30);

        int updated = lambdaTemplate.update(UserInfo.class)//
                .allowEmptyWhere()//
                .updateTo(UserInfo::getAge, 99)//
                .doUpdate();

        assertTrue(updated >= 2);
        assertEquals(Integer.valueOf(99), lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 71).queryForObject().getAge());
        assertEquals(Integer.valueOf(99), lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, baseId() + 72).queryForObject().getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_EDGE_EMPTY_IN_REJECT)
    public void lambdaQueryEmptyInList() throws SQLException {
        insertUser(baseId() + 81, "NXN-Lambda-Edge-Empty-In", 25);

        try {
            lambdaTemplate.query(UserInfo.class)//
                    .in(UserInfo::getId, Collections.emptyList())//
                    .queryForList();
            fail("Empty IN list should be rejected.");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("empty"));
        }
    }

    private void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, name.toLowerCase() + "@nxn.test", new Date() });
    }
}
