/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperScrollableResultContractTest extends AnnotationMapperResultHandlerSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS)
    public void annotationResultHandler_shouldKeepExtractorWhenOptionsArePresent() throws SQLException {
        List<UserInfo> defaultResult = this.mapper.selectDefault(PATTERN);
        List<UserInfo> extractorResult = this.mapper.selectWithExtractorAndOptions(PATTERN);

        assertEquals(defaultResult.size(), extractorResult.size());
        assertEquals(defaultResult.get(0).getId(), extractorResult.get(0).getId());
    }
}
