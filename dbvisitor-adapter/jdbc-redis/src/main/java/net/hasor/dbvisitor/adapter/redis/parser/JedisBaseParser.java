/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.parser;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

public abstract class JedisBaseParser extends Parser {
    protected char separatorChar = '\n';

    public JedisBaseParser(TokenStream input) {
        super(input);
    }

    public void setSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
    }
}
