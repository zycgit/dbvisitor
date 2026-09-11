/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.dynamic.args;
import java.util.Map;

/**
 * 一个 Map 到 SqlParameterSource 的桥，同时支持自动识别 Supplier 接口以获取具体参数。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2014-3-31
 */
public class MapSqlArgSource extends BindSqlArgSource {
    public MapSqlArgSource(final Map<String, ?> values) {
        super((Map<String, Object>) values);
    }
}
