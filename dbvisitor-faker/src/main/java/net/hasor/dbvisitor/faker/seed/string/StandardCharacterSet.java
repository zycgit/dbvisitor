/*
 * Copyright 2002-2010 the original author or authors.
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

import net.hasor.dbvisitor.faker.seed.string.characters.*;

/**
 * 预定义的字符集
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class StandardCharacterSet {
    public static final Characters ASCII   = new AsciiCharacters();
    public static final Characters LATIN   = new LatinCharacters();
    public static final Characters CONTROL = new ControlCharacters();
    public static final Characters SYMBOL  = new SymbolCharacters();

    public static final Characters CAPITAL_LETTER = new CapitalLetterCharacters();
    public static final Characters SMALL_LETTER   = new SmallLetterCharacters();
    public static final Characters NUMERIC        = new NumberCharacters();
    public static final Characters HEX            = new HexNumberCharacters();

    public static final Characters BASIC_LATIN = new GroupCharacters(CAPITAL_LETTER, SMALL_LETTER, NUMERIC, SYMBOL);

}
