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
public abstract class AnnotationMapperScrollableTypeCase extends AnnotationMapperAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_INSENSITIVE)
    public void annotationAttributes_shouldApplyScrollInsensitiveResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithScrollInsensitive(PATTERN));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_SENSITIVE)
    public void annotationAttributes_shouldApplyScrollSensitiveResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithScrollSensitive(PATTERN));
    }
}
