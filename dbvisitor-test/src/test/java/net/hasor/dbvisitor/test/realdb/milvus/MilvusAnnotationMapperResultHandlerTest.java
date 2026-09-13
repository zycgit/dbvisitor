/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusAnnotationMapperResultHandlerTest extends MilvusMapperResultSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_RESULT_DEFAULT)
    public void annotationHandlersShouldPreserveDefaultAndCustomMappingsWithoutOptions() throws Exception {
        NativeResultMapper mapper = this.session.createMapper(NativeResultMapper.class);
        List<UserInfo> defaults = mapper.defaults(1);
        assertEquals(3, defaults.size());
        assertEquals(Set.of(1, 2, 3), defaults.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (UserInfo row : defaults) {
            assertEquals("row" + row.getId(), row.getName());
            assertEquals(Integer.valueOf(20 + row.getId()), row.getAge());
            assertEquals("row" + row.getId() + "@test.com", row.getEmail());
            assertEquals(1700000000123L, row.getCreateTime().getTime());
        }

        List<UserInfo> rows = mapper.plainRows(1);
        assertEquals(3, rows.size());
        assertEquals(Set.of(1, 2, 3), rows.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (int i = 0; i < rows.size(); i++) {
            UserInfo row = rows.get(i);
            assertEquals("[Row" + i + "]row" + row.getId(), row.getName());
        }
        List<UserInfo> extracted = mapper.plainExtracted(1);
        assertEquals(3, extracted.size());
        assertEquals(Set.of(1, 2, 3), extracted.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (UserInfo row : extracted) {
            assertEquals("row" + row.getId(), row.getName());
            assertEquals(1700000000123L, row.getCreateTime().getTime());
        }
        assertTrue(mapper.plainRows(99).isEmpty());
        assertTrue(mapper.plainExtracted(99).isEmpty());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_RESULT_CALLBACK)
    public void annotationCallbackShouldVisitEachRowWithAndWithoutOptions() throws Exception {
        NativeResultMapper mapper = this.session.createMapper(NativeResultMapper.class);
        CALLBACK_IDS.get().clear();
        mapper.callback(1);
        assertEquals(3, CALLBACK_IDS.get().size());
        assertEquals(Set.of(1, 2, 3), Set.copyOf(CALLBACK_IDS.get()));

        CALLBACK_IDS.get().clear();
        mapper.callbackWithTimeout(2);
        assertEquals(2, CALLBACK_IDS.get().size());
        assertEquals(Set.of(2, 3), Set.copyOf(CALLBACK_IDS.get()));

        CALLBACK_IDS.get().clear();
        mapper.callback(99);
        assertTrue(CALLBACK_IDS.get().isEmpty());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_RESULT_SINGLE)
    public void sharedMapperShouldApplyCustomSingleRowMappingAndPreserveEmptyResult() throws Exception {
        UserInfo loaded = this.shared.selectSingleWithRowMapper(2);
        assertEquals(Integer.valueOf(2), loaded.getId());
        assertEquals("[Row0]row2", loaded.getName());
        assertEquals(Integer.valueOf(22), loaded.getAge());
        assertEquals(1700000000123L, loaded.getCreateTime().getTime());
        assertEquals("row2", this.shared.selectById(2).getName());
        assertNull(this.shared.selectSingleWithRowMapper(99));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_RESULT_HANDLERS)
    public void annotationHandlersShouldProcessAllPagesAndEmptyResults() throws Exception {
        NativeResultMapper mapper = this.session.createMapper(NativeResultMapper.class);
        List<UserInfo> rows = mapper.rows(1);
        assertEquals(3, rows.size());
        assertEquals(Set.of(1, 2, 3), rows.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (int i = 0; i < rows.size(); i++) {
            UserInfo row = rows.get(i);
            assertEquals("[Row" + i + "]row" + row.getId(), row.getName());
            assertEquals(1700000000123L, row.getCreateTime().getTime());
        }
        List<UserInfo> extracted = mapper.extracted(2);
        assertEquals(2, extracted.size());
        assertEquals(Set.of(2, 3), extracted.stream().map(UserInfo::getId).collect(Collectors.toSet()));
        for (UserInfo row : extracted) {
            assertEquals("row" + row.getId(), row.getName());
            assertEquals("row" + row.getId() + "@test.com", row.getEmail());
        }
        assertTrue(mapper.rows(99).isEmpty());
        assertTrue(mapper.extracted(99).isEmpty());
    }
}
