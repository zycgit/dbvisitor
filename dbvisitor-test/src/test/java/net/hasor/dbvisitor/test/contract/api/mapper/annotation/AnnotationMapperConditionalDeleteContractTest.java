/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperConditionalDeleteContractTest extends AnnotationMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DELETE_CONDITION)
    public void annotationMapperDelete_shouldDeleteMatchingRows() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 6, "AnnoDeleteTwo", 42, "d2@test.com");
        this.mapper.insertUserWithParams(baseId() + 7, "AnnoDeleteThree", 42, "d3@test.com");

        int deletedByAge = this.mapper.deleteByAge(42);
        if (profile().supportsFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS)) {
            assertEquals(2, deletedByAge);
        }
        assertEquals(0, this.mapper.countByAge(42));
    }
}
