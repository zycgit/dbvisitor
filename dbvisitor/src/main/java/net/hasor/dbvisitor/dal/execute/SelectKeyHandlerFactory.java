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
package net.hasor.dbvisitor.dal.execute;
import net.hasor.dbvisitor.dal.repository.parser.xmlnode.SelectKeySqlConfig;

/**
 * 负责创建 SelectKeyHandler
 * @version : 2021-11-05
 * @author 赵永春 (zyc@hasor.net)
 */
public interface SelectKeyHandlerFactory {
    SelectKeyHandler createHandler(SelectKeySqlConfig keySqlConfig, AbstractStatementExecute<?> execute);
}