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
import net.hasor.dbvisitor.test.contract.material.model.annotation.InsertExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadOnlyEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UpdateExcludedUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationWritePolicyCase extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INSERT_FALSE)
    public void annotationMapping_shouldExcludeInsertFalseColumnFromInsert() throws SQLException {
        InsertExcludedUser user = new InsertExcludedUser();
        user.setId(baseId() + 1);
        user.setName("PolicyInsertFalse");
        user.setAge(25);
        user.setEmail("should-not-insert@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(InsertExcludedUser.class).applyEntity(user).executeSumResult();
        UserInfo raw = queryRaw(baseId() + 1);

        assertEquals("PolicyInsertFalse", raw.getName());
        assertEquals(Integer.valueOf(25), raw.getAge());
        assertNull(raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_UPDATE_FALSE)
    public void annotationMapping_shouldExcludeUpdateFalseColumnFromUpdate() throws SQLException {
        insertRaw(baseId() + 11, "PolicyUpdateFalse", 30, "before-update-false@nxn.test");

        UpdateExcludedUser update = new UpdateExcludedUser();
        update.setName("PolicyUpdateFalseChanged");
        update.setEmail("should-not-update@nxn.test");
        int rows = this.lambdaTemplate.update(UpdateExcludedUser.class) //
                .eq(UpdateExcludedUser::getId, baseId() + 11) //
                .updateToSample(update) //
                .doUpdate();

        UserInfo raw = queryRaw(baseId() + 11);
        assertEquals(1, rows);
        assertEquals("PolicyUpdateFalseChanged", raw.getName());
        assertEquals("before-update-false@nxn.test", raw.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_READ_ONLY_FIELD)
    public void annotationMapping_shouldTreatInsertFalseAndUpdateFalseAsReadOnly() throws SQLException {
        ReadOnlyEmailUser user = new ReadOnlyEmailUser();
        user.setId(baseId() + 21);
        user.setName("PolicyReadOnly");
        user.setAge(28);
        user.setEmail("should-not-write@nxn.test");
        user.setCreateTime(new Date());

        this.lambdaTemplate.insert(ReadOnlyEmailUser.class).applyEntity(user).executeSumResult();
        assertNull(queryRaw(baseId() + 21).getEmail());

        updateRawEmail(baseId() + 21, "db-value@nxn.test");

        ReadOnlyEmailUser update = new ReadOnlyEmailUser();
        update.setName("PolicyReadOnlyChanged");
        update.setEmail("should-not-overwrite@nxn.test");
        this.lambdaTemplate.update(ReadOnlyEmailUser.class) //
                .eq(ReadOnlyEmailUser::getId, baseId() + 21) //
                .updateToSample(update) //
                .doUpdate();

        UserInfo raw = queryRaw(baseId() + 21);
        assertEquals("PolicyReadOnlyChanged", raw.getName());
        assertEquals("db-value@nxn.test", raw.getEmail());

        ReadOnlyEmailUser loaded = this.lambdaTemplate.query(ReadOnlyEmailUser.class).eq(ReadOnlyEmailUser::getId, baseId() + 21).queryForObject();
        assertEquals("db-value@nxn.test", loaded.getEmail());
    }
}
