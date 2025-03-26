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
 * 表的 Index 信息，用于补充生成 DDL 语句
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-06
 */
public interface IndexDescription {
    /** 索引名 */
    String getName();

    /** 是否是唯一索引 */
    boolean isUnique();

    /** 索引包含的列 */
    List<String> getColumns();

    /** 创建索引语句生成后在整个语句的自后添加的自定义代码 */
    String getOther();

    /** 索引备注 */
    String getComment();
}