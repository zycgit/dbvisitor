/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusMappingSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPING_REGISTRY_SQL)
    public void overriddenEntityTableShouldDriveBaseMapperSqlAndPreserveMappingMetadata() throws Exception {
        createCollection("""
                id INT64 PRIMARY KEY, name VARCHAR(128) WITH (enable_analyzer=true),
                age INT32 NULL, email VARCHAR(128) NULL, create_time VARCHAR(128) NULL,
                v SPARSE_FLOAT_VECTOR, FUNCTION name_vector USING BM25 (name) INTO (v)
                """);
        createIndex("v", "SPARSE_INVERTED_INDEX", "BM25");
        loadCollection();

        Configuration configuration = new Configuration();
        MappingRegistry registry = configuration.getMappingRegistry();
        TableMapping<UserInfo> mapping = registry.loadEntityAsTable(UserInfo.class, this.collection);
        assertSame(mapping, registry.findByEntity(UserInfo.class));
        assertSame(mapping, registry.findByTable(this.collection));
        assertTrue(mapping.getPropertyByName("id").isPrimaryKey());
        assertEquals("create_time", mapping.getPropertyByName("createTime").getColumn());
        assertNull(registry.findByTable("user_info"));

        try (Session session = configuration.newSession(newAdapterConnection())) {
            BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
            UserInfo entity = new UserInfo();
            entity.setId(1);
            entity.setName("mapping before");
            entity.setAge(25);
            entity.setEmail("mapping@dbvisitor.test");
            assertEquals(1, mapper.insert(entity));
            UserInfo loaded = mapper.selectById(1);
            assertNotNull(loaded);
            assertEquals(entity.getName(), loaded.getName());
            assertEquals(entity.getEmail(), loaded.getEmail());
            assertNull(loaded.getCreateTime());

            loaded.setName("mapping after");
            loaded.setAge(30);
            assertEquals(1, mapper.update(loaded));
            assertEquals("mapping after", mapper.selectById(1).getName());
            assertEquals(Integer.valueOf(30), mapper.selectById(1).getAge());
            assertEquals(Long.valueOf(1), session.jdbc().queryForLong("COUNT FROM " + this.collection + " WHERE name='mapping after'"));
            assertEquals(1, mapper.deleteById(1));
            assertNull(mapper.selectById(1));
        }
    }
}
