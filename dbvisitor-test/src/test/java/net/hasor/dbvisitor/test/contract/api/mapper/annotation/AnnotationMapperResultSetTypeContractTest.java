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
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class AnnotationMapperResultSetTypeContractTest extends AnnotationMapperAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_TYPE)
    public void annotationAttributes_shouldApplyResultSetTypeVariantsWithoutChangingResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithDefaultResultSetType(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithForwardOnly(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithScrollInsensitive(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithScrollSensitive(PATTERN));
    }
}
