/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.dynamic.args;
import net.hasor.cobble.BeanUtils;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static net.hasor.test.utils.TestUtils.beanForData1;

/***
 *
 * @version : 2014-1-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class ParamsTest extends AbstractDbTest {
    @Test
    public void testParams_1() {
        UserInfo tb_user = beanForData1();
        BeanSqlArgSource beanParams = new BeanSqlArgSource(tb_user);
        //
        String[] parameterNames = beanParams.getParameterNames();
        Set<String> params = new HashSet<>(Arrays.asList(parameterNames));
        assert params.contains("userUuid");
        assert params.contains("name");
        assert params.contains("loginName");
        assert params.contains("loginPassword");
        assert params.contains("email");
        assert params.contains("seq");
        assert params.contains("registerTime");
        assert !params.contains("hashCode");
        //
        assert beanParams.hasValue("userUuid");
        assert beanParams.hasValue("name");
        assert beanParams.hasValue("loginName");
        assert beanParams.hasValue("loginPassword");
        assert beanParams.hasValue("email");
        assert beanParams.hasValue("seq");
        assert beanParams.hasValue("registerTime");
        assert !beanParams.hasValue("hashCode");
        //
        assert tb_user.getUserUuid().equals(beanParams.getValue("userUuid"));
        //
        beanParams.cleanupParameters();
    }

    @Test
    public void testParams_2() {
        UserInfo tb_user = beanForData1();
        Map<String, Object> dataMap = new HashMap<>();
        BeanUtils.copyProperties(dataMap, tb_user);
        //
        MapSqlArgSource beanParams = new MapSqlArgSource(dataMap);
        //
        String[] parameterNames = beanParams.getParameterNames();
        Set<String> params = new HashSet<>(Arrays.asList(parameterNames));
        assert params.contains("userUuid");
        assert params.contains("name");
        assert params.contains("loginName");
        assert params.contains("loginPassword");
        assert params.contains("email");
        assert params.contains("seq");
        assert params.contains("registerTime");
        assert !params.contains("hashCode");
        //
        assert beanParams.hasValue("userUuid");
        assert beanParams.hasValue("name");
        assert beanParams.hasValue("loginName");
        assert beanParams.hasValue("loginPassword");
        assert beanParams.hasValue("email");
        assert beanParams.hasValue("seq");
        assert beanParams.hasValue("registerTime");
        assert !beanParams.hasValue("hashCode");
        //
        assert tb_user.getUserUuid().equals(beanParams.getValue("userUuid"));
        //
        beanParams.cleanupParameters();
    }

    @Test
    public void testParams_3() {
        AtomicBoolean supplierValue = new AtomicBoolean();
        AtomicBoolean clearValue = new AtomicBoolean();
        Map<String, Object> map = new HashMap<>();
        map.put("supplier", (Supplier<String>) () -> {
            supplierValue.set(true);
            return null;
        });
        map.put("clear", (SqlArgDisposer) () -> clearValue.set(true));
        //
        MapSqlArgSource parameter = new MapSqlArgSource(map);
        //
        assert !supplierValue.get();
        assert !clearValue.get();
        //
        parameter.getValue("supplier");
        parameter.getValue("clear");
        assert supplierValue.get();
        assert !clearValue.get();
        //
        parameter.cleanupParameters();
        assert supplierValue.get();
        assert clearValue.get();
    }

    @Test
    public void testParams_4() {
        AtomicBoolean clearValue = new AtomicBoolean();
        Object objects = (SqlArgDisposer) () -> clearValue.set(true);
        BeanSqlArgSource parameter = new BeanSqlArgSource(objects);
        //
        parameter.cleanupParameters();
        //
        assert clearValue.get();
    }
}
