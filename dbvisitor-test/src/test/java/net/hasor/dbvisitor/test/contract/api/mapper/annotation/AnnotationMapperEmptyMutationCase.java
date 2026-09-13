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
public abstract class AnnotationMapperEmptyMutationCase extends AnnotationMapperBoundarySupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS)
    public void annotationMapperDml_shouldReturnZeroWhenNoRowsMatch() throws Exception {
        requiresNxnFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS);

        assertEquals(0, this.mapper.updateUserAge(baseId() + 1000, 50));
        assertEquals(0, this.mapper.deleteById(baseId() + 1000));
    }
}
