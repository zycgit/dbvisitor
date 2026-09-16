/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class BaseMapperSampleCase extends BaseMapperCrudSupport {
    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_PARAMETER_SAMPLE, column = "mapper/base-mapper/operations")
    public void baseMapperSampleParameters_shouldMatchMultiplePropertiesAndIgnoreNulls() {
        this.mapper.insert(user(baseId() + 331, "BaseParamSample", 25, "sample1@basemapper.com"));
        this.mapper.insert(user(baseId() + 332, "BaseParamSample", 25, null));
        this.mapper.insert(user(baseId() + 333, "BaseParamSample", 30, "sample3@basemapper.com"));

        UserInfo sample = new UserInfo();
        sample.setName("BaseParamSample");
        sample.setAge(25);
        List<UserInfo> loaded = this.mapper.listBySample(sample);

        assertEquals(2, loaded.size());
        for (UserInfo user : loaded) {
            assertEquals("BaseParamSample", user.getName());
            assertEquals(Integer.valueOf(25), user.getAge());
        }
        assertEquals(2, this.mapper.countBySample(sample));
    }

    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_PARAMETER_MAP, column = "mapper/base-mapper/operations")
    public void baseMapperMapParameters_shouldCountAndDeleteByNamedValues() {
        this.mapper.insert(user(baseId() + 341, "BaseParamMap", 25, null));
        this.mapper.insert(user(baseId() + 342, "BaseParamMap", 25, null));
        this.mapper.insert(user(baseId() + 343, "BaseParamMapOther", 25, null));

        Map<String, Object> countByName = new HashMap<>();
        countByName.put("name", "BaseParamMap");
        assertEquals(2, this.mapper.countBySample(countByName));

        Map<String, Object> deleteOne = new HashMap<>();
        deleteOne.put("id", baseId() + 341);
        assertEquals(1, this.mapper.deleteByMap(deleteOne));
        assertNull(this.mapper.selectById(baseId() + 341));
        assertNotNull(this.mapper.selectById(baseId() + 342));
    }

    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_LIST_BY_SAMPLE, column = "mapper/base-mapper/operations")
    public void baseMapperListBySample_shouldFilterByNonNullFields() {
        this.mapper.insert(user(baseId() + 41, "BaseSample1", 51, null));
        this.mapper.insert(user(baseId() + 42, "BaseSample2", 52, null));
        this.mapper.insert(user(baseId() + 43, "BaseSample3", 51, null));

        UserInfo sample = new UserInfo();
        sample.setAge(51);

        List<UserInfo> loaded = this.mapper.listBySample(sample);

        assertEquals(2, loaded.size());
        for (UserInfo user : loaded) {
            assertEquals(Integer.valueOf(51), user.getAge());
        }
    }

    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_COUNT_BY_SAMPLE, column = "mapper/base-mapper/operations")
    public void baseMapperCountBySample_shouldCountMatchingRows() {
        this.mapper.insert(user(baseId() + 51, "BaseCount1", 61, null));
        this.mapper.insert(user(baseId() + 52, "BaseCount2", 62, null));
        this.mapper.insert(user(baseId() + 53, "BaseCount3", 61, null));

        UserInfo sample = new UserInfo();
        sample.setAge(61);

        int count = this.mapper.countBySample(sample);

        assertEquals(2, count);
    }
}
