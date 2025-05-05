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
package net.hasor.dbvisitor.dynamic;
import net.hasor.dbvisitor.dialect.BoundSql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQL构建器，用于动态构建SQL语句和参数。
 * 功能特点：
 * 1. 实现BoundSql接口，提供SQL字符串和参数绑定功能
 * 2. 支持SQL片段的拼接和参数收集
 * 3. 支持结果参数的收集和管理
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public class SqlBuilder implements BoundSql {
    protected final StringBuilder   queryString = new StringBuilder();
    protected final List<Object>    argList     = new ArrayList<>();
    protected final List<ResultArg> rules       = new ArrayList<>();

    /**
     * 检查最后一个字符是否为空白字符，true 表示最后一个字符是空白字符（空格、换行符、制表符）。
     */
    public boolean lastSpaceCharacter() {
        if (this.queryString.length() == 0) {
            return true;
        } else {
            char charAt = this.queryString.charAt(this.queryString.length() - 1);
            return charAt == ' ' || charAt == '\n' || charAt == '\t';
        }
    }

    /**
     * 添加结果参数
     * @param rule 结果参数对象
     */
    public void appendResult(ResultArg rule) {
        if (rule != null) {
            this.rules.add(rule);
        }
    }

    /**
     * 添加SQL片段和参数
     * @param sql SQL片段
     * @param args 参数数组
     */
    public void appendSql(String sql, Object... args) {
        this.queryString.append(sql);
        this.argList.addAll(Arrays.asList(args));
    }

    /**
     * 添加 SQL 片段
     * @param sql SQL片段
     */
    public void appendSql(String sql) {
        this.queryString.append(sql);
    }

    /**
     * 添加 {@link BoundSql} 对象的 SQL 片段
     * @param boundSql BoundSql 对象
     */
    public void appendSql(BoundSql boundSql) {
        this.queryString.append(boundSql.getSqlString());
    }

    /**
     * 添加 {@link BoundSql} 对象的参数
     * @param boundSql BoundSql对象
     */
    public void appendArgs(BoundSql boundSql) {
        this.argList.addAll(Arrays.asList(boundSql.getArgs()));
    }

    /**
     * 添加 {@link BoundSql} 对象的 SQL 片段和参数
     * @param boundSql BoundSql对象
     */
    public void appendBuilder(BoundSql boundSql) {
        if (boundSql instanceof SqlBuilder) {
            this.queryString.append(((SqlBuilder) boundSql).queryString);
            this.argList.addAll(((SqlBuilder) boundSql).argList);
        } else {
            this.queryString.append(boundSql.getSqlString());
            this.argList.addAll(Arrays.asList(boundSql.getArgs()));
        }
    }

    /** 获取构建的 SQL 字符串 */
    @Override
    public String getSqlString() {
        return this.queryString.toString();
    }

    /** 获取参数数组 */
    @Override
    public Object[] getArgs() {
        return this.argList.toArray();
    }

    /** 获取结果参数列表 */
    public List<ResultArg> getResultArgs() {
        return this.rules;
    }
}
