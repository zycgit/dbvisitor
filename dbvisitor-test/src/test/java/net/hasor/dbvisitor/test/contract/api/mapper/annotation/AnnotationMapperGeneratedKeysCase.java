/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationMapperGeneratedKeysCase extends AnnotationMapperAttributeSupport {
    // 能力归属：Mapper API / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_GENERATED_KEYS, column = "mapper/key-strategies/strategies")
    public void annotationAttributes_shouldPopulateGeneratedKeysAndSupportExplicitIds() throws Exception {
        if (numericGeneratedKeys()) {
            requiresNxnFeature(FeatureId.GENERATED_KEYS_NUMERIC);
        }
        Object generated = keyRecord(null, "AttrGeneratedKey", 31, "generated@nxn.test");
        assertNull(keyValue(generated));

        assertEquals(1, writeKeyRecord(KeyWrite.GENERATED, generated));

        assertGeneratedKey(keyValue(generated));
        assertEquals("AttrGeneratedKey", readKeyName(keyValue(generated)));

        Object explicitId = explicitKey();
        Object explicit = keyRecord(explicitId, "AttrManualKey", 32, "manual@nxn.test");
        assertEquals(1, writeKeyRecord(KeyWrite.EXPLICIT, explicit));
        assertEquals(explicitId, keyValue(explicit));
        assertEquals("AttrManualKey", readKeyName(explicitId));
    }

    // 能力归属：Mapper API / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_KEY_COLUMN, column = "mapper/key-strategies/strategies")
    public void annotationAttributes_shouldPopulateGeneratedKeysWithKeyColumnWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_COLUMN);
        Object user = keyRecord(null, "AttrKeyColumn", 33, "key-column@nxn.test");

        assertEquals(1, writeKeyRecord(KeyWrite.COLUMN, user));

        assertGeneratedKey(keyValue(user));
        assertEquals("AttrKeyColumn", readKeyName(keyValue(user)));
    }

    // 能力归属：Mapper API / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_KEY_SOURCE, column = "mapper/key-strategies/strategies")
    public void annotationAttributes_shouldPopulateGeneratedKeysFromCurrentResultSetWhenSupported() throws Exception {
        requiresNxnFeature(FeatureId.GENERATED_KEY_RESULT_SET);
        Object user = keyRecord(null, "AttrResultSetKeySource", 34, "result-set-key-source@nxn.test");

        assertEquals(1, writeKeyRecord(KeyWrite.RESULT_SET, user));

        assertGeneratedKey(keyValue(user));
        assertEquals("AttrResultSetKeySource", readKeyName(keyValue(user)));

        Object second = keyRecord(null, "AttrResultSetKeySourceSecond", 35, "result-set-key-source-second@nxn.test");
        assertNull(keyValue(second));
        assertEquals(1, writeKeyRecord(KeyWrite.RESULT_SET, second));
        assertGeneratedKey(keyValue(second));
        assertNotEquals(keyValue(user), keyValue(second));
        assertEquals("AttrResultSetKeySource", readKeyName(keyValue(user)));
        assertEquals("AttrResultSetKeySourceSecond", readKeyName(keyValue(second)));
    }
}
