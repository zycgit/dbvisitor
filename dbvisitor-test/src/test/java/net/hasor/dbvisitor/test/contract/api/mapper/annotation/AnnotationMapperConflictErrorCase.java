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
public abstract class AnnotationMapperConflictErrorCase extends AnnotationMapperBoundarySupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DUPLICATE_KEY)
    public void annotationMapperDuplicateKey_shouldPropagateDuplicatePrimaryKeyFailure() throws Exception {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);

        int id = baseId() + 4;
        assertEquals(1, this.mapper.insertUserWithParams(id, "AnnoPkOne", 25, "pk1@nxn.test"));
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.insertUserWithParams(id, "AnnoPkTwo", 26, "pk2@nxn.test");
            }
        });
    }
}
