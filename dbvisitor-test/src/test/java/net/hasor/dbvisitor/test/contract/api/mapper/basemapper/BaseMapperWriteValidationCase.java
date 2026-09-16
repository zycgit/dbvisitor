/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class BaseMapperWriteValidationCase extends BaseMapperCrudSupport {
    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_INSERT_DUPLICATE_KEY, column = "mapper/base-mapper/operations")
    public void baseMapperInsert_shouldRejectDuplicatePrimaryKey() {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);
        int duplicateId = baseId() + 203;
        assertEquals(1, this.mapper.insert(user(duplicateId, "BaseDuplicate1", 20, null)));
        try {
            this.mapper.insert(user(duplicateId, "BaseDuplicate2", 21, null));
            fail("Duplicate primary key should be rejected.");
        } catch (Exception e) {
            assertTrue(isDuplicateKeyMessage(e));
        }
    }

    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_INSERT_LENGTH_ERROR, column = "mapper/base-mapper/operations")
    public void baseMapperInsert_shouldRejectValueExceedingColumnLength() {
        requiresNxnFeature(FeatureId.LENGTH_LIMIT_ENFORCED);
        char[] chars = new char[1000];
        Arrays.fill(chars, 'A');
        try {
            this.mapper.insert(user(baseId() + 362, new String(chars), 25, null));
            fail("Name longer than user_info.name should be rejected.");
        } catch (Exception e) {
            assertTrue(isLengthLimitMessage(e));
        }
    }

    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_EDGE_BATCH_FAILURE, column = "mapper/base-mapper/operations")
    public void baseMapperBatchInsert_shouldRejectBatchContainingDuplicatePrimaryKey() {
        requiresNxnFeature(FeatureId.BATCH_DUPLICATE_FAILURE_PROPAGATED);

        int duplicateId = baseId() + 371;
        this.mapper.insert(user(duplicateId, "BaseBatchExisting", 25, null));

        List<UserInfo> batch = Arrays.asList(//
                user(baseId() + 372, "BaseBatchCandidate", 26, null), //
                user(duplicateId, "BaseBatchDuplicate", 27, null));
        try {
            this.mapper.insert(batch);
            fail("Batch insert containing duplicate primary key should fail.");
        } catch (Exception e) {
            assertTrue(lowerMessage(e), isDuplicateKeyMessage(e));
        }
    }
}
