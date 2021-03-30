/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.metadata.mysql;
/**
 * MySQL 变量
 * @version : 2020-01-22
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlVariable {
    private String             name;
    private String             value;
    private MySqlVariableScope scope;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MySqlVariableScope getScope() {
        return scope;
    }

    public void setScope(MySqlVariableScope scope) {
        this.scope = scope;
    }
}
