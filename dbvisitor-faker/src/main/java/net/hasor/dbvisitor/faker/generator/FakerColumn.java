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
package net.hasor.dbvisitor.faker.generator;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.SeedType;

import java.util.Set;

/**
 * 要生成数据的列基本信息和配置信息
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerColumn {
    private String                          column;
    private boolean                         key;
    private Set<UseFor>                     ignoreAct;
    private boolean                         canBeCut;
    private Integer                         sqlType;
    private Class<?>                        javaType;
    private SeedType                        seedType;
    private SeedConfig                      seedConfig;
    private SeedFactory<SeedConfig, Object> seedFactory;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public Set<UseFor> getIgnoreAct() {
        return ignoreAct;
    }

    public void setIgnoreAct(Set<UseFor> ignoreAct) {
        this.ignoreAct = ignoreAct;
    }

    public boolean isCanBeCut() {
        return canBeCut;
    }

    public void setCanBeCut(boolean canBeCut) {
        this.canBeCut = canBeCut;
    }

    public Integer getSqlType() {
        return sqlType;
    }

    public void setSqlType(Integer sqlType) {
        this.sqlType = sqlType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
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

    public SeedFactory<SeedConfig, Object> getSeedFactory() {
        return seedFactory;
    }

    public void setSeedFactory(SeedFactory<SeedConfig, Object> seedFactory) {
        this.seedFactory = seedFactory;
    }
}