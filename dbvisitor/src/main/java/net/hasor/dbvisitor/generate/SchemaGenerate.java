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
package net.hasor.dbvisitor.generate;

import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.util.List;

/**
 * 生成建表脚本
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface SchemaGenerate {
    /** 生成建表语句 */
    List<String> buildCreate(TableMapping<?> tableMapping, GenerateContext context);
}