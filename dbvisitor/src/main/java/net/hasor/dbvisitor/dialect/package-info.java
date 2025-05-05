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
 * SQL方言包
 *
 * <p>提供数据库方言相关功能，包括：</p>
 * <ul>
 *   <li>基础SQL方言接口(SqlDialect)</li>
 *   <li>分页方言(PageSqlDialect)</li>
 *   <li>条件查询方言(ConditionSqlDialect)</li>
 *   <li>插入方言(InsertSqlDialect)</li>
 *   <li>序列方言(SeqSqlDialect)</li>
 *   <li>方言注册管理(SqlDialectRegister)</li>
 * </ul>
 *
 * @author 赵永春 (zyc@hasor.net)
 * @version 2017-03-23
 */
package net.hasor.dbvisitor.dialect;