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
/**
 * 列的 DDL 补充信息，用于补充生成 DDL 语句
 * @version : 2022-12-06
 * @author 赵永春 (zyc@hasor.net)
 */
public interface ColumnDescription {

    /** 列数据类型 */
    String getSqlType();

    /** 长度 */
    String getLength();

    /** 精度 */
    String getPrecision();

    /** 小数位数 */
    String getScale();

    /** 列字符集 */
    String getCharacterSet();

    /** 列排序规则 */
    String getCollation();

    /** 表示列是否允许为空 */
    boolean isNullable();

    /** 列上具有的默认值 */
    String getDefault();

    /** 列备注 */
    String getComment();

    /** 在生成建表语句的时候用于拼接的其它信息，开发者可以随意指定。会在 'create table' / 'alter table' 语句生成时自动追加 */
    String getOther();
}