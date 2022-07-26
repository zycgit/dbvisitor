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
 * 基于 UTF-16 的 基本 Latin 字符集
 *  https://codepoints.net/basic_multilingual_plane
 *       Basic Latin (U+0000 to U+007F)
 *       Latin-1 Supplement (U+0080 to U+00FF)
 *       Latin Extended-A (U+0100 to U+017F)
 *       Latin Extended-B (U+0180 to U+024F)
 *       Latin Extended Additional (U+1E00 to U+1EFF)
 *       Latin Extended-C (U+2C60 to U+2C7F)
 *       Latin Extended-D (U+A720 to U+A7FF)
 *       Latin Extended-E (U+AB30 to U+AB6F)
 */
public class LatinCharacters extends AbstractUTF16Characters {
    private final Range[] ranges = new Range[] { new Range(0x0000, 0x007F), // Basic Latin (U+0000 to U+007F)
            new Range(0x0080, 0x00FF), // Latin-1 Supplement (U+0080 to U+00FF)
            new Range(0x0100, 0x017F), // Latin Extended-A (U+0100 to U+017F)
            new Range(0x0180, 0x024F), // Latin Extended-B (U+0180 to U+024F)
            new Range(0x1E00, 0x1EFF), // Latin Extended Additional (U+1E00 to U+1EFF)
            new Range(0x2C60, 0x2C7F), // Latin Extended-C (U+2C60 to U+2C7F)
            new Range(0xA720, 0xA7FF), // Latin Extended-D (U+A720 to U+A7FF)
            new Range(0xAB30, 0xAB6F), // Latin Extended-E (U+AB30 to U+AB6F)
    };

    @Override
    protected Range[] getRanges() {
        return ranges;
    }
}
