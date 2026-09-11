/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.SpecialArrayTypeEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class AnnotationArrayFieldMappingContractTest extends AnnotationSpecialTypeSupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_TYPE_ARRAY)
    public void annotationSpecialType_shouldRoundTripNativeIntegerArray() throws SQLException {
        requiresNxnFeature(FeatureId.ARRAY);
        int id = baseId() + 4;
        Integer[] expected = new Integer[] { 10, 20 };

        SpecialArrayTypeEntity entity = new SpecialArrayTypeEntity();
        entity.setId(id);
        entity.setIntArray(expected);
        insert(entity);

        SpecialArrayTypeEntity loaded = queryArray(id);
        assertNotNull(loaded.getIntArray());
        assertArrayEquals(expected, loaded.getIntArray());
    }
}
