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
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;

import java.util.function.Function;

/**
 * All DML SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public abstract class DmlConfig extends SqlConfig {
    private SelectKeyConfig selectKey;

    public DmlConfig(DynamicSql target, Function<String, String> config) {
        super(target, config);
        this.processSelectKey(target);
    }

    public SelectKeyConfig getSelectKey() {
        return this.selectKey;
    }

    public void setSelectKey(SelectKeyConfig selectKey) {
        this.selectKey = selectKey;
    }

    protected void processSelectKey(DynamicSql target) {
        if (target instanceof ArrayDynamicSql) {
            for (DynamicSql dynamicSql : ((ArrayDynamicSql) target).getSubNodes()) {
                if (dynamicSql instanceof SelectKeyConfig) {
                    SelectKeyConfig selectKey = (SelectKeyConfig) dynamicSql;

                    this.selectKey = new SelectKeyConfig(selectKey.target, null);
                    this.selectKey.setStatementType(selectKey.getStatementType());
                    this.selectKey.setTimeout(selectKey.getTimeout());
                    this.selectKey.setResultMap(selectKey.getResultMap());
                    this.selectKey.setResultType(selectKey.getResultType());
                    this.selectKey.setFetchSize(selectKey.getFetchSize());
                    this.selectKey.setResultSetType(selectKey.getResultSetType());
                    this.selectKey.setBindOut(selectKey.getBindOut());

                    this.selectKey.setKeyProperty(selectKey.getKeyProperty());
                    this.selectKey.setKeyColumn(selectKey.getKeyColumn());
                    this.selectKey.setOrder(selectKey.getOrder());
                    this.selectKey.setHandler(selectKey.getHandler());

                    this.selectKey.setIgnoreBuild(false);
                }
            }
        }
    }
}