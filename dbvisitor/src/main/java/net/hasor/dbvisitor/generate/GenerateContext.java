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

import net.hasor.dbvisitor.metadata.CaseSensitivityType;

/**
 * DDL 生成过程中的环境信息
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class GenerateContext {
    /** default sensitive type if meta not be quot */
    private CaseSensitivityType plain;
    /** fetch sensitive type if meta be quot */
    private CaseSensitivityType delimited;

    public CaseSensitivityType getPlain() {
        return this.plain;
    }

    public void setPlain(CaseSensitivityType plain) {
        this.plain = plain;
    }

    public CaseSensitivityType getDelimited() {
        return this.delimited;
    }

    public void setDelimited(CaseSensitivityType delimited) {
        this.delimited = delimited;
    }
}