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

import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class AnnotationIgnoredSampleCase extends AnnotationMappingPolicySupport {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_IGNORE_SAMPLE)
    public void annotationMapping_shouldExcludeIgnoredFieldFromSampleConditions() throws SQLException {
        IgnoredEmailUser user = new IgnoredEmailUser();
        user.setId(baseId() + 31);
        user.setName("PolicyIgnore");
        user.setAge(25);
        user.setEmail("ignored-insert@nxn.test");
        user.setCreateTime(new Date());
        this.lambdaTemplate.insert(IgnoredEmailUser.class).applyEntity(user).executeSumResult();
        updateRawEmail(baseId() + 31, "db-ignore@nxn.test");

        IgnoredEmailUser sample = new IgnoredEmailUser();
        sample.setName("PolicyIgnore");
        sample.setEmail("wrong-email@nxn.test");
        IgnoredEmailUser bySample = this.lambdaTemplate.query(IgnoredEmailUser.class).eqBySample(sample).queryForObject();
        assertNotNull(bySample);
        assertEquals(Integer.valueOf(baseId() + 31), bySample.getId());
    }
}
