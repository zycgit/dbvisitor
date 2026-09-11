/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus2;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3Mapper;
import org.junit.Test;
import static org.junit.Assert.*;

/** Data visibility across Mapper proxies and JdbcTemplate in the same Session. */
public class MilvusSessionMapperSharingContractTest extends MilvusSessionMapperSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SESSION_MAPPER_DECLARATIVE)
    public void declarativeMapperAndJdbcShouldObserveBaseMapperWrites() throws Exception {
        BaseMapper<UserInfoMilvus2> base = this.session.createBaseMapper(UserInfoMilvus2.class);
        UserInfoMilvus1Mapper annotation = this.session.createMapper(UserInfoMilvus1Mapper.class);
        UserInfoMilvus2 first = new UserInfoMilvus2();
        first.setUid("base");
        first.setName("Base");
        first.setLoginName("login");
        first.setLoginPassword("password");
        first.setV(List.of(1F, 0F));
        assertEquals(1, base.insert(first));
        assertEquals("Base", annotation.selectUser("base").getName());
        assertEquals(1, annotation.insertUser(user("annotation", "Annotation")));
        assertEquals("Annotation", base.selectById("annotation").getName());
        assertEquals(Integer.valueOf(2), this.session.jdbc().queryForInt("SELECT COUNT(*) FROM tb_mapper_user_milvus"));
        assertEquals(2, annotation.countAll());
        assertEquals(1, this.session.jdbc().queryForInt("SELECT COUNT(*) FROM tb_mapper_user_milvus WHERE uid = 'annotation'").intValue());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SESSION_MAPPER_MIXED)
    public void annotationXmlAndBaseMapperShouldShareTheSameSession() throws Exception {
        UserInfoMilvus1Mapper annotation = this.session.createMapper(UserInfoMilvus1Mapper.class);
        UserInfoMilvus3Mapper xml = this.session.createMapper(UserInfoMilvus3Mapper.class);
        BaseMapper<UserInfoMilvus2> base = this.session.createBaseMapper(UserInfoMilvus2.class);
        UserInfoMilvus1 user = new UserInfoMilvus1();
        user.setUid("shared");
        user.setName("bound '\" name");
        user.setLoginName("login");
        user.setLoginPassword("password");
        user.setV(List.of(1F, 0F));
        assertEquals(1, annotation.insertUser(user));
        assertEquals(user.getName(), base.selectById("shared").getName());
        List<UserInfoMilvus3> rows = xml.queryAll();
        assertEquals(1, rows.size());
        assertEquals(user.getName(), rows.get(0).getName());
        assertEquals(List.of(1F, 0F), rows.get(0).getV());
        assertEquals(1, annotation.updateName("shared", "Updated"));
        assertEquals("Updated", base.selectById("shared").getName());
        assertEquals("Updated", xml.selectUser("shared").getName());
        assertEquals(1, annotation.countAll());
        assertEquals(1, xml.countAll());
        assertEquals(1, base.deleteById("shared"));
        assertNull(annotation.selectUser("shared"));
        assertTrue(xml.queryAll().isEmpty());
        UnsupportedOperationException invalid = assertThrows(UnsupportedOperationException.class,
                () -> this.session.createMapper(Runnable.class));
        assertTrue(invalid.getMessage().contains("java.lang.Runnable"));
    }
}
