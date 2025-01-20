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
package net.hasor.dbvisitor.guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.dal.MapperRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Set;

/**
 * BeanFactory that enables injection of DalSession.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-29
 * @see Mapper
 */
class DalSessionSupplier implements Provider<DalSession> {
    private final ClassLoader              classLoader;
    private final MappingOptions           options;
    private final Key<DataSource>          dsInfo;
    private final Key<TypeHandlerRegistry> typeInfo;
    private final Key<RuleRegistry>        ruleInfo;
    private final Set<URI>                 mappers;

    private DalSession dalSession = null;

    public DalSessionSupplier(ClassLoader classLoader, MappingOptions options, Key<DataSource> dsInfo, Key<TypeHandlerRegistry> typeInfo, Key<RuleRegistry> ruleInfo, Set<URI> mappers) {
        this.classLoader = classLoader;
        this.options = options;
        this.dsInfo = dsInfo;
        this.typeInfo = typeInfo;
        this.ruleInfo = ruleInfo;
        this.mappers = mappers;
    }

    @Inject
    public void initDalSession(Injector injector) throws SQLException {
        TypeHandlerRegistry typeRegistry = injector.getInstance(this.typeInfo);
        RuleRegistry ruleRegistry = injector.getInstance(this.ruleInfo);

        DataSource dataSource = injector.getInstance(this.dsInfo);
        MapperRegistry dalRegistry = new MapperRegistry(this.classLoader, typeRegistry, ruleRegistry, this.options);

        if (!(this.mappers == null || this.mappers.isEmpty())) {
            try {
                for (URI mapper : this.mappers) {
                    dalRegistry.loadMapper(mapper.toString());
                }
            } catch (IOException e) {
                throw ExceptionUtils.toRuntime(e);
            }
        }

        this.dalSession = new DalSession(dataSource, dalRegistry);
    }

    @Override
    public DalSession get() {
        return this.dalSession;
    }
}
