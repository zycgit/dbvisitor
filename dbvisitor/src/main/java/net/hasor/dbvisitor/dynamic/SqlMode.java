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
package net.hasor.dbvisitor.dynamic;
/**
 * 参数模式
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public enum SqlMode {
    In(true, false),
    Out(false, true),
    Cursor(false, true),
    InOut(true, true);

    private final boolean out;
    private final boolean in;

    SqlMode(boolean in, boolean out) {
        this.in = in;
        this.out = out;
    }

    public boolean isIn() {
        return this.in;
    }

    public boolean isOut() {
        return this.out;
    }
}
