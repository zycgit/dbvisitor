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
package net.hasor.dbvisitor.faker.provider.sqlserver;
import net.hasor.dbvisitor.faker.dsl.DslFunctionLoopUp;
import net.hasor.dbvisitor.faker.dsl.DslFunctionRegistry;
import net.hasor.dbvisitor.faker.generator.parameter.ParameterProcessorLookUp;
import net.hasor.dbvisitor.faker.generator.parameter.ParameterRegistry;

/**
 * DslFunctionLoopUp, ParameterProcessorLookUp 的 SQL SERVER 专属扩展。
 * @version : 2023-02-14
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlServerSpiRegistry implements DslFunctionLoopUp, ParameterProcessorLookUp {

    @Override
    public void loopUp(DslFunctionRegistry registry) {

    }

    @Override
    public void loopUp(ParameterRegistry registry) {

    }
}
