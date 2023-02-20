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
package net.hasor.dbvisitor.faker.dsl;
import net.hasor.dbvisitor.faker.dsl.model.DataModel;

import java.util.Map;

/**
 * 配置项
 * @version : 2023-02-10
 * @author 赵永春 (zyc@hasor.net)
 */
public class TypeProcessConf {
    private final String    confName;
    private final boolean   useAppend;
    private final DataModel configValue;

    public TypeProcessConf(String confName, boolean useAppend, DataModel configValue) {
        this.confName = confName;
        this.useAppend = useAppend;
        this.configValue = configValue;
    }

    public String getConfName() {
        return this.confName;
    }

    public boolean isUseAppend() {
        return this.useAppend;
    }

    public Object recover(Map<String, Object> variables, String throwMessage) {
        try {
            return configValue.recover(variables);
        } catch (Exception e) {
            throw new IllegalArgumentException(throwMessage + " configName is " + confName, e);
        }
    }

    @Override
    public String toString() {
        return confName + (useAppend ? " += " : " = ") + this.configValue;
    }

}