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
 * 基于 UTF-16 的 字符集，字符集中只含有 0-9，a-f，共计 16 个字符，可以用于生成 16 进制形式的 bytes 数据。
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class HexNumberCharacters extends AbstractUTF16Characters {
    private final Range[] ranges = new Range[] { new Range(0x0030, 0x0039), new Range(0x0041, 0x0046) };

    @Override
    protected Range[] getRanges() {
        return ranges;
    }

}
