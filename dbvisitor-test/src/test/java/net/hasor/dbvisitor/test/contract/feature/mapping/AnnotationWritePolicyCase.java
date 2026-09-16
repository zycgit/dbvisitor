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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.InsertExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadOnlyEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UpdateExcludedUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationWritePolicyCase extends AnnotationMappingPolicySupport {
    // 能力归属：对象映射 / 写入策略 / 只读字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_INSERT_FALSE, column = "mapping-keys/write-policies/fields")
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

        assertEquals(1, this.lambdaTemplate.update(InsertExcludedUser.class)//
                .eq(InsertExcludedUser::getId, baseId() + 1)//
                .updateTo(InsertExcludedUser::getEmail, "allowed-update@nxn.test")//
                .doUpdate());
        InsertExcludedUser loaded = this.lambdaTemplate.query(InsertExcludedUser.class)//
                .eq(InsertExcludedUser::getId, baseId() + 1)//
                .queryForObject();
        assertEquals("allowed-update@nxn.test", loaded.getEmail());
        assertEquals("allowed-update@nxn.test", queryRaw(baseId() + 1).getEmail());
    }

    // 能力归属：对象映射 / 写入策略 / 只读字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_UPDATE_FALSE, column = "mapping-keys/write-policies/fields")
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

        UpdateExcludedUser insert = new UpdateExcludedUser();
        insert.setId(baseId() + 12);
        insert.setName("PolicyUpdateFalseInsert");
        insert.setAge(31);
        insert.setEmail("allowed-insert@nxn.test");
        insert.setCreateTime(new Date());
        assertEquals(1, this.lambdaTemplate.insert(UpdateExcludedUser.class).applyEntity(insert).executeSumResult());
        assertEquals("allowed-insert@nxn.test", queryRaw(baseId() + 12).getEmail());
        UpdateExcludedUser loaded = this.lambdaTemplate.query(UpdateExcludedUser.class)//
                .eq(UpdateExcludedUser::getId, baseId() + 12)//
                .queryForObject();
        assertEquals("allowed-insert@nxn.test", loaded.getEmail());
    }

    // 能力归属：对象映射 / 写入策略 / 只读字段。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_READ_ONLY_FIELD, column = "mapping-keys/write-policies/fields")
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
