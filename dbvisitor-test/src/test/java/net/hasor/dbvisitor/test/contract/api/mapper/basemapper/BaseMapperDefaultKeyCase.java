/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class BaseMapperDefaultKeyCase extends BaseMapperCrudSupport {
    // 能力归属：Mapper API / 主键策略。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_INSERT_DEFAULT_KEY, column = "mapper/key-strategies/strategies")
    public void baseMapperInsert_shouldAcceptUnspecifiedPrimaryKeyWhenSchemaSuppliesDefault() {
        UserInfo autoKey = user(null, "BaseAutoKey", 25, null);
        assertEquals(1, this.mapper.insert(autoKey));
    }
}
