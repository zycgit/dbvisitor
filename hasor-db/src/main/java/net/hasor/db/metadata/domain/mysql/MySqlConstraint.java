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
package net.hasor.db.metadata.domain.mysql;
/**
 * MySQL 约束
 * @version : 2021-03-30
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlConstraint {
    private String              schema;
    private String              name;
    private MySqlConstraintType constraintType;

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MySqlConstraintType getConstraintType() {
        return this.constraintType;
    }

    public void setConstraintType(MySqlConstraintType constraintType) {
        this.constraintType = constraintType;
    }
}
