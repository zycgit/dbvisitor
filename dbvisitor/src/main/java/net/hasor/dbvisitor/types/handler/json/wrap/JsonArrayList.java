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
package net.hasor.dbvisitor.types.handler.json.wrap;
import java.util.ArrayList;
import java.util.Collection;
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/**
 * JSON ArrayList 包装类，用于支持通过 queryForObject 直接读取 JSON 数组为 List 结构。
 * <p>使用场景：</p>
 * <pre>{@code
 * String sql = "SELECT json_array_column FROM table WHERE id = ?";
 * JsonArrayList list = jdbcTemplate.queryForObject(sql, new Object[]{1}, JsonArrayList.class);
 * // list 会自动将 JSON 数组字符串反序列化为 ArrayList
 * // 示例 JSON: ["item1", "item2", "item3"]
 * // 或嵌套对象: [{"id":1,"name":"Alice"}, {"id":2,"name":"Bob"}]
 * }</pre>
 * <p>技术原理：</p>
 * <ul>
 *   <li>继承自 ArrayList，因此可以通过反射 newInstance() 创建实例</li>
 *   <li>TypeHandlerRegistry 会为此类型创建 JsonTypeHandler</li>
 *   <li>JsonTypeHandler 会将数据库的 JSON 数组字符串反序列化到此 List 中</li>
 *   <li>元素类型取决于 JSON 内容（Map、String、Number 等）</li>
 * </ul>
 * <p>泛型说明：</p>
 * 由于 JSON 反序列化时无法确定具体的元素类型（类型擦除），JsonArrayList 使用 {@code ArrayList<Object>}。
 * 实际元素类型取决于 JSON 库的解析结果：
 * <ul>
 *   <li>JSON 对象 → Map（通常是 LinkedHashMap）</li>
 *   <li>JSON 字符串 → String</li>
 *   <li>JSON 数字 → Integer/Long/Double</li>
 *   <li>JSON 布尔 → Boolean</li>
 *   <li>JSON null → null</li>
 * </ul>
 * @author 赵永春 (zyc@hasor.net)
 * @see JsonTypeHandler
 * @see java.util.ArrayList
 */
@BindTypeHandler(JsonTypeHandler.class)
public class JsonArrayList extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 默认构造函数
     */
    public JsonArrayList() {
        super();
    }

    /**
     * 指定初始容量的构造函数
     * @param initialCapacity 初始容量
     */
    public JsonArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 使用已有集合创建
     * @param c 集合
     */
    public JsonArrayList(Collection<?> c) {
        super(c);
    }
}
