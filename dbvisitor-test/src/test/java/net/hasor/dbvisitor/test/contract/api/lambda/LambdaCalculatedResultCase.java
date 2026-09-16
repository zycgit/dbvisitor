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
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaCalculatedResultCase extends LambdaResultHandlingSupport {

    protected String calculatedSelect() {
        return "age * 2 as doubled_age";
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_CALCULATED_COLUMN, column = "builder/queries/query")
    public void lambdaResult_shouldReadCalculatedColumnThroughRowMapper() throws SQLException {
        insertByJdbc(baseId() + 182, "LRCalc", 30, "lr-calc@test.com");
        Integer doubled = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 182)//
                .applySelect(calculatedSelect())//
                .queryForObject((rs, rowNum) -> rs.getInt("doubled_age"));
        assertEquals(Integer.valueOf(60), doubled);
    }
}
