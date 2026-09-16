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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.hasor.dbvisitor.test.contract.material.handler.RecordingRowCallbackHandler;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationMapperResultHandlerCase extends AnnotationMapperResultHandlerSupport {
    // 能力归属：Mapper API / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_DEFAULT, column = "mapper/method-annotations/execution")
    public void annotationResultHandler_shouldUseDefaultMappingWithoutCustomHandler() throws SQLException {
        List<UserInfo> users = this.mapper.selectDefault(PATTERN);

        assertEquals(10, users.size());
        assertMappedRows(users, false);
    }

    // 能力归属：结果接收 / resultsetextractor / mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR, column = "results/resultsetextractor/extraction", variants = { "mapper" })
    public void annotationResultHandler_shouldUseResultSetExtractor() throws SQLException {
        List<UserInfo> users = ResultHandlerProbe.verify(1, () -> this.mapper.selectWithExtractor(PATTERN));

        assertEquals(10, users.size());
        for (UserInfo user : users) {
            assertNotNull(user.getId());
            assertTrue(user.getName().startsWith("AnnoHandler"));
            assertNotNull(user.getCreateTime());
        }
        assertMappedRows(users, false);
        assertMappedRows(ResultHandlerProbe.verify(1, () -> this.mapper.selectWithExtractorAndFetchSize(PATTERN)), false);
        assertTrue(ResultHandlerProbe.verify(1, () -> this.mapper.selectWithExtractorAndFetchSize("NoAnnoHandlerMatch%")).isEmpty());
        ResultHandlerProbe.verifyFailure(() -> this.mapper.selectWithExtractor(PATTERN));
        Map<Integer, String> expectedNames = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toMap(this::id, index -> "AnnoHandler" + index));
        assertEquals(expectedNames, ResultHandlerProbe.verify(1, () -> this.mapper.selectMapWithExtractor(PATTERN)));
        assertTrue(ResultHandlerProbe.verify(1, () -> this.mapper.selectMapWithExtractor("NoAnnoHandlerMatch%")).isEmpty());
    }

    // 能力归属：结果接收 / rowmapper / mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER, column = "results/rowmapper/mapping", variants = { "mapper" })
    public void annotationResultHandler_shouldUseRowMapperForListResults() throws SQLException {
        List<UserInfo> users = ResultHandlerProbe.verify(10, () -> this.mapper.selectWithRowMapper(PATTERN));

        assertEquals(10, users.size());
        for (int i = 0; i < users.size(); i++) {
            assertTrue(users.get(i).getName().contains("[Row" + i + "]"));
        }
        assertMappedRows(users, true);
        assertTrue(ResultHandlerProbe.verify(0, () -> this.mapper.selectWithRowMapper("NoAnnoHandlerMatch%")).isEmpty());
        ResultHandlerProbe.verifyFailure(() -> this.mapper.selectWithRowMapper(PATTERN));
    }

    // 能力归属：结果接收 / rowmapper / mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_SINGLE, column = "results/rowmapper/mapping", variants = { "mapper" })
    public void annotationResultHandler_shouldUseRowMapperForSingleResult() throws SQLException {
        UserInfo user = ResultHandlerProbe.verify(1, () -> this.mapper.selectSingleWithRowMapper(id(1)));

        assertNotNull(user);
        assertTrue(user.getName().contains("[Row0]"));
        assertTrue(user.getName().contains("AnnoHandler1"));
        assertEquals(Integer.valueOf(id(1)), user.getId());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals(timestamp(), user.getCreateTime().getTime());
        assertEquals("AnnoHandler1", this.mapper.selectById(id(1)).getName());
        assertNull(ResultHandlerProbe.verify(0, () -> this.mapper.selectSingleWithRowMapper(99999)));
    }

    // 能力归属：结果接收 / rowmapper / mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_OPTIONS, column = "results/rowmapper/mapping", variants = { "mapper" })
    public void annotationResultHandler_shouldKeepRowMapperWhenOptionsArePresent() throws SQLException {
        List<UserInfo> defaultResult = this.mapper.selectDefault(PATTERN);
        List<UserInfo> mappedResult = ResultHandlerProbe.verify(10, () -> this.mapper.selectWithRowMapperAndOptions(PATTERN));

        assertEquals(defaultResult.size(), mappedResult.size());
        for (UserInfo user : mappedResult) {
            assertTrue(user.getName().contains("[Row"));
        }
        assertMappedRows(mappedResult, true);
        assertTrue(ResultHandlerProbe.verify(0, () -> this.mapper.selectWithRowMapperAndOptions("NoAnnoHandlerMatch%")).isEmpty());
    }

    // 能力归属：结果接收 / rowcallbackhandler / mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_CALLBACK, column = "results/rowcallbackhandler/callback", variants = { "mapper" })
    public void annotationResultHandler_shouldExecuteRowCallbackQueries() throws SQLException {
        try {
            RecordingRowCallbackHandler.clear();
            ResultHandlerProbe.verify(10, () -> {
                this.mapper.selectWithRowCallback(PATTERN);
                return null;
            });
            assertCallbackIds();
            RecordingRowCallbackHandler.clear();
            ResultHandlerProbe.verify(10, () -> {
                this.mapper.selectWithRowCallbackAndTimeout(PATTERN);
                return null;
            });
            assertCallbackIds();
            RecordingRowCallbackHandler.clear();
            ResultHandlerProbe.verify(0, () -> {
                this.mapper.selectWithRowCallback("NoAnnoHandlerMatch%");
                return null;
            });
            assertTrue(RecordingRowCallbackHandler.ids().isEmpty());
            ResultHandlerProbe.verifyFailure(() -> {
                this.mapper.selectWithRowCallback(PATTERN);
                return null;
            });
        } finally {
            RecordingRowCallbackHandler.clear();
        }
    }

    // 能力归属：Mapper API / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EMPTY, column = "mapper/method-annotations/execution")
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
