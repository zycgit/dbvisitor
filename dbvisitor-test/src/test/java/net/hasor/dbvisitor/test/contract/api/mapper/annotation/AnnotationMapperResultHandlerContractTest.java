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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperResultHandlerContractTest extends AnnotationMapperResultHandlerSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_DEFAULT)
    public void annotationResultHandler_shouldUseDefaultMappingWithoutCustomHandler() throws SQLException {
        List<UserInfo> users = this.mapper.selectDefault(PATTERN);

        assertEquals(10, users.size());
        assertEquals("AnnoHandler1", users.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR)
    public void annotationResultHandler_shouldUseResultSetExtractor() throws SQLException {
        List<UserInfo> users = this.mapper.selectWithExtractor(PATTERN);

        assertEquals(10, users.size());
        for (UserInfo user : users) {
            assertNotNull(user.getId());
            assertTrue(user.getName().startsWith("AnnoHandler"));
            assertNotNull(user.getCreateTime());
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER)
    public void annotationResultHandler_shouldUseRowMapperForListResults() throws SQLException {
        List<UserInfo> users = this.mapper.selectWithRowMapper(PATTERN);

        assertEquals(10, users.size());
        for (int i = 0; i < users.size(); i++) {
            assertTrue(users.get(i).getName().contains("[Row" + i + "]"));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_SINGLE)
    public void annotationResultHandler_shouldUseRowMapperForSingleResult() throws SQLException {
        UserInfo user = this.mapper.selectSingleWithRowMapper(id(1));

        assertNotNull(user);
        assertTrue(user.getName().contains("[Row0]"));
        assertTrue(user.getName().contains("AnnoHandler1"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_OPTIONS)
    public void annotationResultHandler_shouldKeepRowMapperWhenOptionsArePresent() throws SQLException {
        List<UserInfo> defaultResult = this.mapper.selectDefault(PATTERN);
        List<UserInfo> mappedResult = this.mapper.selectWithRowMapperAndOptions(PATTERN);

        assertEquals(defaultResult.size(), mappedResult.size());
        for (UserInfo user : mappedResult) {
            assertTrue(user.getName().contains("[Row"));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_CALLBACK)
    public void annotationResultHandler_shouldExecuteRowCallbackQueries() throws SQLException {
        this.mapper.selectWithRowCallback(PATTERN);
        this.mapper.selectWithRowCallbackAndTimeout(PATTERN);
        this.mapper.selectWithRowCallback("NoAnnoHandlerMatch%");
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EMPTY)
    public void annotationResultHandler_shouldHandleEmptyAndNullResults() throws SQLException {
        assertEquals(0, this.mapper.selectWithExtractor("NoAnnoHandlerMatch%").size());
        assertEquals(0, this.mapper.selectWithRowMapper("NoAnnoHandlerMatch%").size());
        assertNull(this.mapper.selectSingleWithRowMapper(99999));
    }
}
