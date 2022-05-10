/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.dbvisitor.dialect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQL Build
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class SqlBuilder implements BoundSql {
    protected final StringBuilder queryString = new StringBuilder();
    protected final List<Object>  argList     = new ArrayList<>();

    public boolean lastSpaceCharacter() {
        if (this.queryString.length() == 0) {
            return true;
        } else {
            char charAt = this.queryString.charAt(this.queryString.length() - 1);
            return charAt == ' ' || charAt == '\n' || charAt == '\t';
        }
    }

    public void appendSql(String sql, Object... args) {
        this.queryString.append(sql);
        this.argList.addAll(Arrays.asList(args));
    }

    public void appendSql(String sql) {
        this.queryString.append(sql);
    }

    public void appendSql(BoundSql boundSql) {
        this.queryString.append(boundSql.getSqlString());
    }

    public void appendBuilder(BoundSql boundSql) {
        if (boundSql instanceof SqlBuilder) {
            this.queryString.append(((SqlBuilder) boundSql).queryString);
            this.argList.addAll(((SqlBuilder) boundSql).argList);
        } else {
            this.queryString.append(boundSql.getSqlString());
            this.argList.addAll(Arrays.asList(boundSql.getArgs()));
        }
    }

    public void appendArgs(BoundSql boundSql) {
        this.argList.addAll(Arrays.asList(boundSql.getArgs()));
    }

    @Override
    public String getSqlString() {
        return this.queryString.toString();
    }

    @Override
    public Object[] getArgs() {
        return this.argList.toArray();
    }
}
