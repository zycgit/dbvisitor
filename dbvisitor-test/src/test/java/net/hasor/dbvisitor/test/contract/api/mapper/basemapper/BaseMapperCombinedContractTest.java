/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.sql.SQLException;
import java.util.Date;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperCombinedContractTest extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_RESULT_BATCH)
    public void baseMapperBatchResults_shouldReportAffectedRowsAndMapLargeSelections() {
        // @formatter:off
        List<UserInfo> users = Arrays.asList(
            user(baseId() + 401, "BaseResultBatch1", 31, null),
            user(baseId() + 402, "BaseResultBatch2", 32, null),
            user(baseId() + 403, "BaseResultBatch3", 33, null)
        );
        // @formatter:on
        assertEquals(3, this.mapper.insert(users));

        UserInfo update1 = user(baseId() + 401, "BaseResultBatch1", 36, null);
        UserInfo update2 = user(baseId() + 402, "BaseResultBatch2", 37, null);
        assertEquals(2, this.mapper.replace(update1) + this.mapper.replace(update2));
        assertEquals(Integer.valueOf(36), this.mapper.selectById(baseId() + 401).getAge());
        assertEquals(Integer.valueOf(37), this.mapper.selectById(baseId() + 402).getAge());

        List<Integer> ids = Arrays.asList(baseId() + 401, baseId() + 402, baseId() + 403);
        List<UserInfo> selected = this.mapper.selectByIds(ids);
        assertEquals(3, selected.size());
        assertTrue(selected.stream().allMatch(user -> user.getName().startsWith("BaseResultBatch")));

        assertMutationRows(3, this.mapper.deleteByIds(ids));
        assertEquals(0, this.mapper.selectByIds(ids).size());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_PARAMETER_BATCH)
    public void baseMapperBatchParameters_shouldRoundTripSelectedPrimaryKeys() {
        for (int i = 1; i <= 10; i++) {
            this.mapper.insert(user(baseId() + 350 + i, "BaseParamBatch" + i, 20 + i, null));
        }
        List<Integer> ids = Arrays.asList(baseId() + 352, baseId() + 354, baseId() + 356, baseId() + 358, baseId() + 360);

        List<UserInfo> loaded = this.mapper.selectByIds(ids);
        int deleted = this.mapper.deleteByIds(ids);

        assertEquals(5, loaded.size());
        assertTrue(loaded.stream().allMatch(user -> user.getName().startsWith("BaseParamBatch")));
        assertMutationRows(5, deleted);
        assertEquals(0, this.mapper.selectByIds(ids).size());
        assertNotNull(this.mapper.selectById(baseId() + 351));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_ACCESSORS)
    public void baseMapperAccessors_shouldExposeEntityTypeSessionJdbcAndLambdaApis() throws SQLException {
        int lambdaId = baseId() + 141;
        int jdbcId = baseId() + 142;

        assertEquals(UserInfo.class, this.mapper.entityType());
        assertNotNull(this.mapper.session());
        assertNotNull(this.mapper.lambda());
        assertNotNull(this.mapper.jdbc());

        int lambdaResult = this.mapper.lambda().insert(UserInfo.class).applyEntity(user(lambdaId, "BaseAccessorLambda", 141, null)).executeSumResult();
        int jdbcResult = this.mapper.jdbc().executeUpdate(//
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, ?)", //
                new Object[] { jdbcId, "BaseAccessorJdbc", 142, new Date() });

        assertEquals(1, lambdaResult);
        assertEquals(1, jdbcResult);
        assertNotNull(this.mapper.selectById(lambdaId));
        assertNotNull(this.mapper.selectById(jdbcId));
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_MIXED_OPERATIONS)
    public void baseMapperMixedOperations_shouldSupportCrudAndUpsertInOneMapper() {
        int id = baseId() + 151;

        this.mapper.insert(user(id, "BaseMixed", 151, "mixed@basemapper.com"));
        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);

        loaded.setAge(152);
        this.mapper.update(loaded);
        assertEquals(Integer.valueOf(152), this.mapper.selectById(id).getAge());

        loaded.setAge(153);
        this.mapper.upsert(loaded);
        assertEquals(Integer.valueOf(153), this.mapper.selectById(id).getAge());

        assertEquals(1, this.mapper.deleteById(id));
        assertNull(this.mapper.selectById(id));
    }
}
