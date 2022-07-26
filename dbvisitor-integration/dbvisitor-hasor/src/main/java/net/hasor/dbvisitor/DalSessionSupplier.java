/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.core.AppContext;
import net.hasor.core.BindInfo;
import net.hasor.core.spi.AppContextAware;
import net.hasor.dbvisitor.dal.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.utils.function.ESupplier;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Set;

/**
 * BeanFactory that enables injection of DalSession.
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
class DalSessionSupplier implements ESupplier<DalSession, SQLException>, AppContextAware {
    private final BindInfo<DataSource>          dsInfo;
    private final BindInfo<TypeHandlerRegistry> typeInfo;
    private final BindInfo<RuleRegistry>        ruleInfo;
    private final Set<URL>                      mappers;

    private DataSource     dataSource  = null;
    private DalRegistry    dalRegistry = null;
    private PageSqlDialect dialect     = null;

    public DalSessionSupplier(BindInfo<DataSource> dsInfo, BindInfo<TypeHandlerRegistry> typeInfo, BindInfo<RuleRegistry> ruleInfo, Set<URL> mappers) {
        this.dsInfo = dsInfo;
        this.typeInfo = typeInfo;
        this.ruleInfo = ruleInfo;
        this.mappers = mappers;
    }

    @Override
    public void setAppContext(AppContext appContext) {
        TypeHandlerRegistry typeRegistry = appContext.getInstance(this.typeInfo);
        RuleRegistry ruleRegistry = appContext.getInstance(this.ruleInfo);
        MappingOptions options = MappingOptions.buildNew();

        this.dataSource = appContext.getInstance(this.dsInfo);
        this.dalRegistry = new DalRegistry(appContext.getClassLoader(), typeRegistry, ruleRegistry, options);
        this.dialect = null;// appContext.getInstance(this.ruleInfo);

        if (!(this.mappers == null || this.mappers.isEmpty())) {
            try {
                for (URL mapper : this.mappers) {
                    this.dalRegistry.loadMapper(mapper);
                }
            } catch (IOException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }
    }

    @Override
    public DalSession eGet() throws SQLException {
        return new DalSession(this.dataSource, this.dalRegistry, this.dialect);
    }
}
