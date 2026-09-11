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

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoreOnMethodUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoreWithColumnUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.MultipleIgnoreUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationIgnoredFieldContractTest extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_LIFECYCLE)
    public void annotationMapping_shouldExcludeIgnoredFieldFromInsertSelectAndUpdate() throws SQLException {
        IgnoredEmailUser user = new IgnoredEmailUser();
        user.setId(baseId() + 31);
        user.setName("PolicyIgnore");
        user.setAge(25);
        user.setEmail("ignored-insert@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(IgnoredEmailUser.class).applyEntity(user).executeSumResult();
        assertNull(queryRaw(baseId() + 31).getEmail());

        updateRawEmail(baseId() + 31, "db-ignore@nxn.test");

        IgnoredEmailUser loaded = this.lambdaTemplate.query(IgnoredEmailUser.class).eq(IgnoredEmailUser::getId, baseId() + 31).queryForObject();
        assertNotNull(loaded);
        assertNull(loaded.getEmail());

        loaded.setName("PolicyIgnoreChanged");
        loaded.setAge(99);
        loaded.setEmail("ignored-update@nxn.test");
        this.lambdaTemplate.update(IgnoredEmailUser.class).eq(IgnoredEmailUser::getId, baseId() + 31).updateRow(loaded).doUpdate();

        UserInfo raw = queryRaw(baseId() + 31);
        assertEquals("PolicyIgnoreChanged", raw.getName());
        assertEquals(Integer.valueOf(99), raw.getAge());
        assertEquals("db-ignore@nxn.test", raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_MULTIPLE_FIELDS)
    public void annotationMapping_shouldExcludeMultipleIgnoredFields() throws SQLException {
        MultipleIgnoreUser user = new MultipleIgnoreUser();
        user.setId(baseId() + 32);
        user.setName("PolicyMultiIgnore");
        user.setAge(30);
        user.setEmail("multi-ignore@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(MultipleIgnoreUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 32);

        assertEquals("PolicyMultiIgnore", raw.getName());
        assertNull(raw.getAge());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_OVERRIDES_COLUMN)
    public void annotationMapping_shouldLetIgnoreOverrideColumnAnnotation() throws SQLException {
        IgnoreWithColumnUser user = new IgnoreWithColumnUser();
        user.setId(baseId() + 33);
        user.setName("PolicyIgnoreColumn");
        user.setAge(26);
        user.setEmail("ignore-column@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(IgnoreWithColumnUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 33);

        assertEquals("PolicyIgnoreColumn", raw.getName());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_ON_METHOD)
    public void annotationMapping_shouldHonorIgnoreOnGetterMethod() throws SQLException {
        IgnoreOnMethodUser user = new IgnoreOnMethodUser();
        user.setId(baseId() + 34);
        user.setName("PolicyIgnoreMethod");
        user.setAge(27);
        user.setEmail("ignore-method@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(IgnoreOnMethodUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 34);

        assertEquals("PolicyIgnoreMethod", raw.getName());
        assertNull(raw.getEmail());
    }
}
