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
package net.hasor.dbvisitor.mapping.def;
import java.util.List;

/**
 * 列的 DDL 补充信息，用于补充生成 DDL 语句
 * @version : 2022-12-06
 * @author 赵永春 (zyc@hasor.net)
 */
public interface ColumnDescription {
    /** 列备注 */
    String getComment();

    /** 列数据类型 */
    String getDbType();

    /** 长度 */
    String getLength();

    /** 精度 */
    String getPrecision();

    /** 小数位数 */
    String getScale();

    /** 列上具有的默认值 */
    String getDefault();

    /** 表示列是否允许为空 */
    Boolean getNullable();

    /** 这个列属于哪些索引（如果某个索引含有多个列，那么这些列的 belongIndex 属性都会含有这个索引的名字） */
    List<String> getBelongIndex();

    /** 这个列属于哪些唯一索引（如果某个索引含有多个列，那么这些列的 belongIndex 属性都会含有这个索引的名字） */
    List<String> getBelongUnique();
}