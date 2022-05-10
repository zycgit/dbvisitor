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
package net.hasor.dbvisitor.lambda.support.entity;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.AbstractInsertLambda;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * 提供 lambda insert 能力。是 InsertOperation 接口的实现类。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class InsertLambdaForEntity<T> extends AbstractInsertLambda<InsertOperation<T>, T, SFunction<T>> implements InsertOperation<T> {
    public InsertLambdaForEntity(Class<T> exampleType, TableMapping<T> tableMapping, LambdaTemplate jdbcTemplate) {
        super(exampleType, tableMapping, jdbcTemplate);
    }

    @Override
    protected InsertOperation<T> getSelf() {
        return this;
    }

    @Override
    protected String getPropertyName(SFunction<T> property) {
        return BeanUtils.toProperty(property);
    }
}
