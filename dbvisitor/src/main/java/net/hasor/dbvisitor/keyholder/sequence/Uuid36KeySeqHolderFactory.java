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
package net.hasor.dbvisitor.keyholder.sequence;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.keyholder.KeySeqHolderFactory;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;

import java.sql.Connection;
import java.util.UUID;

/**
 * 使用 36 长度 UUID 作为默认 Key 值
 * @version : 2022-12-01
 * @author 赵永春 (zyc@hasor.net)
 */
public class Uuid36KeySeqHolderFactory implements KeySeqHolderFactory {
    @Override
    public KeySeqHolder createHolder(CreateContext context) {
        return new KeySeqHolder() {
            @Override
            public boolean onBefore() {
                return true;
            }

            @Override
            public Object beforeApply(Connection conn, Object entity, ColumnMapping mapping) {
                String genUUID = UUID.randomUUID().toString();
                mapping.getHandler().set(entity, genUUID);
                return genUUID;
            }
        };
    }
}