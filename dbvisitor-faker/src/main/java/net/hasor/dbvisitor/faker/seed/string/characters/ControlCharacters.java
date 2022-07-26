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
package net.hasor.dbvisitor.faker.seed.string.characters;
/**
 * 基于 UTF-16 的 控制字母 字符集，对应 ASCII 0～1F
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class ControlCharacters extends AbstractUTF16Characters {
    private final Range[] ranges = new Range[] { new Range(0x0000, 0x001F), new Range(0x007F, 0x007F) };

    @Override
    protected Range[] getRanges() {
        return ranges;
    }

}
