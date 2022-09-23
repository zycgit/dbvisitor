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
/**
 * 试探性的生成各种数据，相比 carefully 更加激进增加了负数、有限的精度、以及中文字符
 *  - 1：数值范围固定在 +- 10^2、10^4、10^6、10^8
 *  - 2：时间范围：2000-01-01 00:00:00.000 ～ 2030-12-31 23:59:59.999
 */
package net.hasor.dbvisitor.faker.generator.provider.tentative;
