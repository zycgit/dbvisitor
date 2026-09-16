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

@NxnContract
public abstract class AnnotationMapperScrollableTypeCase extends AnnotationMapperAttributeSupport {
    // 能力归属：Mapper API / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_INSENSITIVE, column = "mapper/execution-options/options")
    public void annotationAttributes_shouldApplyScrollInsensitiveResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithScrollInsensitive(PATTERN));
    }

    // 能力归属：Mapper API / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_SCROLL_SENSITIVE, column = "mapper/execution-options/options")
    public void annotationAttributes_shouldApplyScrollSensitiveResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithScrollSensitive(PATTERN));
    }
}
