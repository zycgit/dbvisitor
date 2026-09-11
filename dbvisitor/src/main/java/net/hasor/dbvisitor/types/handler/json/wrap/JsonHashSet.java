/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types.handler.json.wrap;
import java.util.HashMap;
import java.util.HashSet;
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/**
 * JSON HashMap 包装类，用于支持通过 queryForObject 直接读取 JSON 为 Map 结构。
 * <p>使用场景：</p>
 * <pre>{@code
 * String sql = "SELECT json_column FROM table WHERE id = ?";
 * JsonHashMap map = jdbcTemplate.queryForObject(sql, new Object[]{1}, JsonHashMap.class);
 * // map 会自动将 JSON 字符串反序列化为 HashMap
 * }</pre>
 * <p>技术原理：</p>
 * <ul>
 *   <li>继承自 HashMap，因此可以通过反射 newInstance() 创建实例</li>
 *   <li>TypeHandlerRegistry 会为此类型创建 JsonTypeHandler</li>
 *   <li>JsonTypeHandler 会将数据库的 JSON 字符串反序列化到此 Map 中</li>
 * </ul>
 * @author 赵永春 (zyc@hasor.net)
 * @see JsonTypeHandler
 * @see HashMap
 */
@BindTypeHandler(JsonTypeHandler.class)
public class JsonHashSet extends HashSet<Object> {
    private static final long serialVersionUID = 1L;
}
