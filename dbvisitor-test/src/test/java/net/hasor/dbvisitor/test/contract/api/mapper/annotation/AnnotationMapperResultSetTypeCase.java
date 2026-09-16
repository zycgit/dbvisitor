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
public abstract class AnnotationMapperResultSetTypeCase extends AnnotationMapperAttributeSupport {
    // 能力归属：Mapper API / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_RESULT_SET_TYPE, column = "mapper/execution-options/options")
    public void annotationAttributes_shouldApplyDefaultAndForwardOnlyResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithDefaultResultSetType(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithForwardOnly(PATTERN));
    }
}
