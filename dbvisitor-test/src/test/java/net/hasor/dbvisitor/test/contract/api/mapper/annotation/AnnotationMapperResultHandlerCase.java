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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.handler.RecordingRowCallbackHandler;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperResultHandlerCase extends AnnotationMapperResultHandlerSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_DEFAULT)
    public void annotationResultHandler_shouldUseDefaultMappingWithoutCustomHandler() throws SQLException {
        List<UserInfo> users = this.mapper.selectDefault(PATTERN);

        assertEquals(10, users.size());
        assertMappedRows(users, false);
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
        assertMappedRows(users, false);
        assertMappedRows(this.mapper.selectWithExtractorAndFetchSize(PATTERN), false);
        assertTrue(this.mapper.selectWithExtractorAndFetchSize("NoAnnoHandlerMatch%").isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER)
    public void annotationResultHandler_shouldUseRowMapperForListResults() throws SQLException {
        List<UserInfo> users = this.mapper.selectWithRowMapper(PATTERN);

        assertEquals(10, users.size());
        for (int i = 0; i < users.size(); i++) {
            assertTrue(users.get(i).getName().contains("[Row" + i + "]"));
        }
        assertMappedRows(users, true);
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_SINGLE)
    public void annotationResultHandler_shouldUseRowMapperForSingleResult() throws SQLException {
        UserInfo user = this.mapper.selectSingleWithRowMapper(id(1));

        assertNotNull(user);
        assertTrue(user.getName().contains("[Row0]"));
        assertTrue(user.getName().contains("AnnoHandler1"));
        assertEquals(Integer.valueOf(id(1)), user.getId());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals(timestamp(), user.getCreateTime().getTime());
        assertEquals("AnnoHandler1", this.mapper.selectById(id(1)).getName());
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
        assertMappedRows(mappedResult, true);
        assertTrue(this.mapper.selectWithRowMapperAndOptions("NoAnnoHandlerMatch%").isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_CALLBACK)
    public void annotationResultHandler_shouldExecuteRowCallbackQueries() throws SQLException {
        try {
            RecordingRowCallbackHandler.clear();
            this.mapper.selectWithRowCallback(PATTERN);
            assertCallbackIds();
            RecordingRowCallbackHandler.clear();
            this.mapper.selectWithRowCallbackAndTimeout(PATTERN);
            assertCallbackIds();
            RecordingRowCallbackHandler.clear();
            this.mapper.selectWithRowCallback("NoAnnoHandlerMatch%");
            assertTrue(RecordingRowCallbackHandler.ids().isEmpty());
        } finally {
            RecordingRowCallbackHandler.clear();
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EMPTY)
    public void annotationResultHandler_shouldHandleEmptyAndNullResults() throws SQLException {
        assertEquals(0, this.mapper.selectWithExtractor("NoAnnoHandlerMatch%").size());
        assertEquals(0, this.mapper.selectWithRowMapper("NoAnnoHandlerMatch%").size());
        assertNull(this.mapper.selectSingleWithRowMapper(99999));
    }

    private Set<Integer> expectedIds() {
        return IntStream.rangeClosed(1, 10).map(this::id).boxed().collect(Collectors.toSet());
    }

    private void assertCallbackIds() {
        List<Integer> ids = RecordingRowCallbackHandler.ids();
        assertEquals(10, ids.size());
        assertEquals(expectedIds(), Set.copyOf(ids));
    }

    private void assertMappedRows(List<UserInfo> rows, boolean mapped) {
        assertEquals(10, rows.size());
        assertEquals(expectedIds(), rows.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (int i = 0; i < rows.size(); i++) {
            UserInfo row = rows.get(i);
            int index = row.getId() - id(1) + 1;
            assertEquals((mapped ? "[Row" + i + "]" : "") + "AnnoHandler" + index, row.getName());
            assertEquals(Integer.valueOf(20 + index), row.getAge());
            assertEquals("anno-handler" + index + "@nxn.test", row.getEmail());
            assertEquals(timestamp(), row.getCreateTime().getTime());
        }
    }
}
