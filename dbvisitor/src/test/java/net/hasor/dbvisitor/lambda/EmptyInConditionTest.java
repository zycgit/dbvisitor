/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;

import java.util.Collections;
import net.hasor.dbvisitor.lambda.dto.AnnoUserInfoDTO;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class EmptyInConditionTest {
    @Test
    public void emptyIn_shouldFailWhileBuildingCondition() throws Exception {
        EntityQuery<AnnoUserInfoDTO> query = new LambdaTemplate().query(AnnoUserInfoDTO.class);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> query.in("uid", Collections.emptyList()));
        assertEquals("Assertion failed: build in failed, value is empty.", error.getMessage());
    }

    @Test
    public void emptyNotIn_shouldFailWhileBuildingCondition() throws Exception {
        EntityQuery<AnnoUserInfoDTO> query = new LambdaTemplate().query(AnnoUserInfoDTO.class);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> query.notIn("uid", Collections.emptyList()));
        assertEquals("Assertion failed: build notIn failed, value is empty.", error.getMessage());
    }

    @Test
    public void disabledEmptyInConditions_shouldLeaveQueryUnchanged() throws Exception {
        EntityQuery<AnnoUserInfoDTO> query = new LambdaTemplate().query(AnnoUserInfoDTO.class);
        String initialSql = query.getBoundSql().getSqlString();
        query.in(false, "uid", Collections.emptyList());
        query.notIn(false, "uid", Collections.emptyList());
        assertEquals(initialSql, query.getBoundSql().getSqlString());
        assertEquals(0, query.getBoundSql().getArgs().length);
    }
}
