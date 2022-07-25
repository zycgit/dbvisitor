/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.faker.meta.JdbcSqlTypes;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedType;

/**
 * Column
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerColumn {
    private String       column;
    private boolean      ignore;
    private JdbcSqlTypes sqlType;
    private SeedType     seedType;
    private SeedConfig   seedConfig;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public JdbcSqlTypes getSqlType() {
        return sqlType;
    }

    public void setSqlType(JdbcSqlTypes sqlType) {
        this.sqlType = sqlType;
    }

    public SeedType getSeedType() {
        return seedType;
    }

    public void setSeedType(SeedType seedType) {
        this.seedType = seedType;
    }

    public SeedConfig getSeedConfig() {
        return seedConfig;
    }

    public void setSeedConfig(SeedConfig seedConfig) {
        this.seedConfig = seedConfig;
    }
}