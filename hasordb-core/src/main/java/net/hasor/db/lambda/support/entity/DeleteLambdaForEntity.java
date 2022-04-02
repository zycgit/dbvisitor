/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.db.lambda.support.entity;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.db.lambda.EntityDeleteOperation;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.lambda.core.AbstractDeleteLambda;
import net.hasor.db.mapping.def.TableMapping;

/**
 * 提供 lambda update 能力，是 LambdaUpdate 接口的实现类。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public class DeleteLambdaForEntity<T> extends AbstractDeleteLambda<EntityDeleteOperation<T>, T, SFunction<T>> implements EntityDeleteOperation<T> {

    public DeleteLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
    }

    @Override
    protected EntityDeleteOperation<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }

}
