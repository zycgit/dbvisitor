/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ExplicitMappingUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationExplicitMappingCase extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_AUTO_MAPPING_FALSE)
    public void annotationMapping_shouldMapOnlyAnnotatedColumnsWhenAutoMappingFalse() throws SQLException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ExplicitMappingUser.class, "", "explicit");
        TableMapping<?> mapping = registry.findBySpace("", "explicit");
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNull(mapping.getPropertyByName("age"));
        assertNull(mapping.getPropertyByName("email"));

        ExplicitMappingUser user = new ExplicitMappingUser();
        user.setId(baseId() + 41);
        user.setName("PolicyExplicit");
        user.setAge(28);
        user.setEmail("explicit@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(ExplicitMappingUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 41);

        assertEquals("PolicyExplicit", raw.getName());
        assertNull(raw.getAge());
        assertNull(raw.getEmail());
    }
}
