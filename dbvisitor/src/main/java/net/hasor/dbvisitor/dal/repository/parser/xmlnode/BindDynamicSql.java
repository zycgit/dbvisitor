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
package net.hasor.dbvisitor.dal.repository.parser.xmlnode;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.internal.OgnlUtils;

/**
 * <bind> 标签
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-24
 */
public class BindDynamicSql implements DynamicSql {
    private final String name;      // 名字
    private final String valueExpr; // 值

    public BindDynamicSql(String name, String valueExpr) {
        this.name = name;
        this.valueExpr = valueExpr;
    }

    @Override
    public boolean isHaveInjection() {
        return false;
    }

    @Override
    public void buildQuery(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder) {
        if (StringUtils.isNotBlank(this.name)) {
            Object testExprResult = OgnlUtils.evalOgnl(this.valueExpr, data);
            data.putValue(this.name, testExprResult);
        }
    }
}