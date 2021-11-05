/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.execute;
import net.hasor.db.dal.repository.config.SelectKeySqlConfig;

/**
 * 负责处理 SelectKey 的执行
 * @version : 2021-11-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class SelectKeyExecute implements SelectKeyHolder {

    public SelectKeyExecute(SelectKeySqlConfig keySqlConfig, AbstractStatementExecute<?> selectKeyExecute) {
        throw new UnsupportedOperationException("SelectKey Unsupported.");
    }
}