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
package net.hasor.dbvisitor.mapper.def;
import net.hasor.cobble.StringUtils;

/**
 * 查询类型枚举
 * 定义了数据库操作的各种SQL语句类型
 */
public enum QueryType {
    /** 插入数据操作类型 */
    Insert("insert"),
    /** 删除数据操作类型 */
    Delete("delete"),
    /** 更新数据操作类型 */
    Update("update"),
    /** 任意类型SQL语句执行 */
    Execute("execute"),
    /** 查询数据操作类型 */
    Select("select"),
    /** SQL片段类型，可被其他SQL引用 */
    Segment("sql"),
    ;
    private final String xmlTag;

    QueryType(String xmlTag) {
        this.xmlTag = xmlTag;
    }

    /**
     * 获取XML标签名
     * @return 返回该类型对应的XML标签名
     */
    public String getXmlTag() {
        return this.xmlTag;
    }

    /**
     * 根据XML标签名查找对应的查询类型
     * @param xmlTag XML标签名
     * @return 匹配的查询类型，未找到返回null
     */
    public static QueryType valueOfTag(String xmlTag) {
        for (QueryType tableType : QueryType.values()) {
            if (StringUtils.equalsIgnoreCase(tableType.xmlTag, xmlTag)) {
                return tableType;
            }
        }
        return null;
    }
}
