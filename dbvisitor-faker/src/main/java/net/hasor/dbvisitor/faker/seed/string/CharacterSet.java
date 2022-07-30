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
package net.hasor.dbvisitor.faker.seed.string;

import net.hasor.dbvisitor.faker.seed.string.characters.AbstractUTF16Characters.Range;
import net.hasor.dbvisitor.faker.seed.string.characters.GroupCharacters;
import net.hasor.dbvisitor.faker.seed.string.characters.UTF16Range;

/**
 * 预定义的字符集
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class CharacterSet {
    // 完整 ASCII 字符集
    public static final Characters ASCII                  = UTF16Range.ofRanges(new Range(0x0000, 0x007F));
    // 基本 LATIN 字符集
    public static final Characters LATIN                  = UTF16Range.ofRanges(//
            new Range(0x0000, 0x007F), // Basic Latin (U+0000 to U+007F)
            new Range(0x0080, 0x00FF), // Latin-1 Supplement (U+0080 to U+00FF)
            new Range(0x0100, 0x017F), // Latin Extended-A (U+0100 to U+017F)
            new Range(0x0180, 0x024F), // Latin Extended-B (U+0180 to U+024F)
            new Range(0x1E00, 0x1EFF), // Latin Extended Additional (U+1E00 to U+1EFF)
            new Range(0x2C60, 0x2C7F), // Latin Extended-C (U+2C60 to U+2C7F)
            new Range(0xA720, 0xA7FF), // Latin Extended-D (U+A720 to U+A7FF)
            new Range(0xAB30, 0xAB6F)  // Latin Extended-E (U+AB30 to U+AB6F)
    );
    // emoji 表情符 https://codepoints.net/emoticons
    //    public static final Characters EMOTICONS              = RangeCharacters.ofRanges(new Range(0xD83DDE00, 0xD83DDE4F));
    // 中文/日文 汉子 字符集 https://codepoints.net/cjk_unified_ideographs?page=79
    public static final Characters CJK_UNIFIED_IDEOGRAPHS = UTF16Range.ofRanges(new Range(0x4E00, 0x9FCC));
    // Bit 字符集(只含有 0，1 两个字符)
    public static final Characters BIT                    = UTF16Range.ofRanges(new Range(0x0030, 0x0031));
    // 大写字母 字符集
    public static final Characters CAPITAL_LETTER         = UTF16Range.ofRanges(new Range(0x0041, 0x005A));
    // 小写字母 字符集
    public static final Characters SMALL_LETTER           = UTF16Range.ofRanges(new Range(0x0061, 0x007A));
    // 数字 字符集
    public static final Characters NUMBER                 = UTF16Range.ofRanges(new Range(0x0030, 0x0039));
    // 字符集中只含有 0-9，a-f，共计 16 个字符，可以用于生成 16 进制形式的 bytes 数据。
    public static final Characters HEX                    = UTF16Range.ofRanges(new Range(0x0030, 0x0039), new Range(0x0041, 0x0046));
    // ASCII 中定义的符号
    public static final Characters SYMBOL                 = UTF16Range.ofRanges(new Range(0x0021, 0x002f), new Range(0x003a, 0x0040), new Range(0x005b, 0x0060), new Range(0x007b, 0x007e));
    // ASCII 中定义的控制字符
    public static final Characters CONTROL                = UTF16Range.ofRanges(new Range(0x0000, 0x001F), new Range(0x007F, 0x007F));
    // 字母 + 数字
    public static final Characters LETTER_NUMBER          = new GroupCharacters(CAPITAL_LETTER, SMALL_LETTER, NUMBER);

}
