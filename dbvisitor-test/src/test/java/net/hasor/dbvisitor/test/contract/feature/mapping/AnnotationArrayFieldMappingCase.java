/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.SpecialArrayTypeEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationArrayFieldMappingCase extends AnnotationSpecialTypeSupport {
    // 能力归属：类型处理器 / 数组类型 / 实体数组映射。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_ARRAY, column = "types/array-handlers/arrays")
    public void annotationSpecialType_shouldRoundTripNativeIntegerArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 4;
        Integer[] expected = new Integer[] { 10, 20 };

        SpecialArrayTypeEntity entity = new SpecialArrayTypeEntity();
        entity.setId(id);
        entity.setIntArray(expected);
        insert(entity);

        SpecialArrayTypeEntity loaded = queryArray(id);
        assertEquals(id, loaded.getId());
        assertNotNull(loaded.getIntArray());
        assertArrayEquals(expected, loaded.getIntArray());
    }
}
