/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaMutationCountCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 690000;
    }

    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_UPDATE_NO_MATCH, column = "builder/inserts-updates-and-deletes/writes")
    public void lambdaUpdate_shouldReturnZeroWhenNoRowMatches() throws SQLException {
        requiresNxnFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS);

        int updated = lambdaTemplate.update(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 21)//
                .updateTo(UserInfo::getAge, 100)//
                .doUpdate();

        assertEquals(0, updated);
    }

    // 能力归属：构造器 API / 写入操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_DELETE_NO_MATCH, column = "builder/inserts-updates-and-deletes/writes")
    public void lambdaDelete_shouldReturnZeroWhenNoRowMatches() throws SQLException {
        requiresNxnFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS);

        int deleted = lambdaTemplate.delete(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .doDelete();

        assertEquals(0, deleted);
    }
}
