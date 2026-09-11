/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.parser;
/**
 * 解析异常。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-01-22
 */
public class QueryParseException extends RuntimeException {
    public QueryParseException(int line, int charPosition, String errorMessage) {
        super("parsing error, line number is " + line + ", char position in line number is " + charPosition + ", " + errorMessage);
    }
}
