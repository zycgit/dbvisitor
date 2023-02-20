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
package net.hasor.dbvisitor.faker.generator.parameter;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.generator.TypeProcessor;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.seed.SeedConfig;

/**
 * 自定义参数配置方式
 * @version : 2023-02-14
 * @author 赵永春 (zyc@hasor.net)
 */
public interface ParameterProcessor {
    void processor(FakerConfig fakerConfig, JdbcColumn colMeta, SettingNode colSetting, //
            SeedConfig seedConfig, TypeProcessor typeProcessor, boolean isAppend, Object parameter) throws ReflectiveOperationException;
}
