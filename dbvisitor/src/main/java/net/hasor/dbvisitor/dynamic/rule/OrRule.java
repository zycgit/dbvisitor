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
package net.hasor.dbvisitor.dynamic.rule;
/**
 * 如果参数不为空，则生成 'or column = ?' 或者 'column = ?' 。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public class OrRule extends ConditionRule {
    public static final SqlBuildRule INSTANCE = new OrRule(false);

    public OrRule(boolean usingIf) {
        super(usingIf, DEFAULT_TEST_PREFIX, "where", "where ", "or ");
    }

    @Override
    public String toString() {
        return (this.usingIf ? "ifor [" : "or [") + this.hashCode() + "]";
    }
}
