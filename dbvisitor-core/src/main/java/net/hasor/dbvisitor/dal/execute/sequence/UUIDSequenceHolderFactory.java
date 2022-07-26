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
package net.hasor.dbvisitor.dal.execute.sequence;
import net.hasor.dbvisitor.dal.execute.AbstractStatementExecute;
import net.hasor.dbvisitor.dal.execute.KeySequenceHolder;
import net.hasor.dbvisitor.dal.execute.KeySequenceHolderFactory;
import net.hasor.dbvisitor.dal.repository.config.SelectKeySqlConfig;

import java.util.UUID;

/**
 * 使用 UUID 作为默认 Key 值
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 */
public class UUIDSequenceHolderFactory implements KeySequenceHolderFactory {
    @Override
    public KeySequenceHolder createHolder(SelectKeySqlConfig keySqlConfig, AbstractStatementExecute<?> selectKeyExecute) {
        return (conn, parameter) -> UUID.randomUUID().toString();
    }
}