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
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class AnnotationMapperSelectKeyCase extends AnnotationMapperAttributeSupport {
    // 能力归属：Mapper API / 主键策略。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SELECT_KEY, column = "mapper/key-strategies/strategies")
    public void annotationAttributes_shouldRunSelectKeySqlWhenFixtureSequenceIsSupported() throws Exception {

        Object before = keyRecord(null, "AttrSelectKeyBefore", 34, "select-before@nxn.test");
        assertEquals(1, writeKeyRecord(KeyWrite.BEFORE, before));
        assertNotNull(keyValue(before));
        assertEquals("AttrSelectKeyBefore", readKeyName(keyValue(before)));

        Object after = keyRecord(null, "AttrSelectKeyAfter", 35, "select-after@nxn.test");
        assertEquals(1, writeKeyRecord(KeyWrite.AFTER, after));
        assertNotNull(keyValue(after));
        assertEquals("AttrSelectKeyAfter", readKeyName(keyValue(after)));

        Object full = keyRecord(null, "AttrSelectKeyFull", 36, "select-full@nxn.test");
        assertEquals(1, writeKeyRecord(KeyWrite.OPTIONS, full));
        assertNotNull(keyValue(full));
        assertEquals("AttrSelectKeyFull", readKeyName(keyValue(full)));
    }
}
