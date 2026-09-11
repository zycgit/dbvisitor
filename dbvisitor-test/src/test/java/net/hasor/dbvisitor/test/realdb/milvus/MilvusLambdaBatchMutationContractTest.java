/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaBatchMutationContractTest;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Shared mutation contracts without replacing unsupported predicates or ordering. */
public class MilvusLambdaBatchMutationContractTest extends LambdaBatchMutationContractTest {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_MULTI_UPDATE)
    public void nativePredicatesShouldUpdateMultipleFieldsAndPreserveOtherRows() throws SQLException {
        insertNativeRows(7);
        List<Integer> ids = List.of(1, 3, 5);
        int updated = this.lambdaTemplate.update(UserInfo.class)
                .in(UserInfo::getId, ids)
                .updateTo(UserInfo::getAge, 99)
                .updateTo(UserInfo::getEmail, null)
                .doUpdate();
        assertEquals(3, updated);
        List<UserInfo> changed = this.lambdaTemplate.query(UserInfo.class).in(UserInfo::getId, ids).queryForList();
        assertEquals(Set.of(1, 3, 5), changed.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (UserInfo row : changed) {
            assertEquals(Integer.valueOf(99), row.getAge());
            assertNull(row.getEmail());
            assertEquals("native" + row.getId(), row.getName());
        }
        List<UserInfo> unchanged = this.lambdaTemplate.query(UserInfo.class).notIn(UserInfo::getId, ids).queryForList();
        assertEquals(Set.of(2, 4, 6, 7), unchanged.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (UserInfo row : unchanged) {
            assertEquals(Integer.valueOf(20 + row.getId()), row.getAge());
            assertEquals("native@test.com", row.getEmail());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_LAMBDA_CHUNK_DELETE)
    public void nativePagedLookupShouldDeleteEveryMatchingIdOnce() throws SQLException {
        insertNativeRows(9);
        Set<Integer> deletedIds = new HashSet<>();
        int deletedCount = 0;
        while (true) {
            List<UserInfo> page = this.lambdaTemplate.query(UserInfo.class)
                    .ge(UserInfo::getId, 2).le(UserInfo::getId, 8).initPage(2, 0).queryForList();
            if (page.isEmpty()) {
                break;
            }
            assertTrue(page.size() <= 2);
            List<Integer> ids = new ArrayList<>();
            for (UserInfo row : page) {
                assertTrue("each selected id must be new", deletedIds.add(row.getId()));
                ids.add(row.getId());
            }
            deletedCount += this.lambdaTemplate.delete(UserInfo.class).in(UserInfo::getId, ids).doDelete();
            assertTrue("bounded progress", deletedCount <= 7);
        }
        assertEquals(7, deletedCount);
        assertEquals(Set.of(2, 3, 4, 5, 6, 7, 8), deletedIds);
        List<UserInfo> remaining = this.lambdaTemplate.query(UserInfo.class).queryForList();
        assertEquals(Set.of(1, 9), remaining.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        assertEquals(2, this.lambdaTemplate.delete(UserInfo.class).notIn(UserInfo::getId, deletedIds).doDelete());
        assertEquals(0, this.lambdaTemplate.query(UserInfo.class).queryForCount());
    }

    private void insertNativeRows(int count) throws SQLException {
        List<UserInfo> rows = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            UserInfo row = new UserInfo();
            row.setId(i);
            row.setName("native" + i);
            row.setAge(20 + i);
            row.setEmail("native@test.com");
            rows.add(row);
        }
        assertEquals(count, this.lambdaTemplate.insert(UserInfo.class).applyEntity(rows).executeSumResult());
    }
}
