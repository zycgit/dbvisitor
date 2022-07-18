/*
 * Copyright 2010-2021 the original author or authors.
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
package net.hasor.dbvisitor.spring.support;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.spring.adapter.AbstractDsAdapter;
import net.hasor.dbvisitor.spring.adapter.SpringDsAdapter;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * BeanFactory that enables injection of DalSession.
 * <p>
 * Sample configuration:
 *
 * <pre class="code">
 * {@code
 *     <bean id="dalRegistry" class="net.hasor.dbvisitor.spring.support.DalRegistryBean">
 *         <property name="mapperResources" value="classpath*:dbvisitor/mapper/*Mapper.xml"/>
 *     </bean>
 *
 *     <bean id="dalSession" class="net.hasor.dbvisitor.spring.support.DalSessionBean">
 *         <property name="dalRegistry" ref="dalRegistry"/>
 *         <property name="dialectName" value="mysql"/>
 *     </bean>
 * }
 * </pre>
 *
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
public class DalSessionBean extends AbstractSupportBean<DalSession> {
    private DalRegistry dalRegistry;
    private DalSession  dalSession;

    // - dsAdapter
    private AbstractDsAdapter dsAdapter;
    private Class<?>          dsAdapterClass;
    private String            dsAdapterName;
    private DataSource        dataSource;

    // dialect
    private PageSqlDialect dialect;
    private String         dialectName;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.dataSource == null) {
            this.dataSource = this.applicationContext.getBean(DataSource.class);
            if (this.dataSource == null) {
                throw new IllegalStateException("missing dataSource.");
            }
        }

        initDsAdapter();
        initDialect();

        if (this.dalRegistry == null) {
            this.dalSession = new DalSession(this.dsAdapter, DalRegistry.DEFAULT, this.dialect);
        } else {
            this.dalSession = new DalSession(this.dsAdapter, this.dalRegistry, this.dialect);
        }
    }

    private void initDsAdapter() throws Exception {
        if (this.dsAdapter == null) {
            if (this.dsAdapterClass != null) {
                this.dsAdapterName = this.dsAdapterClass.getName();
                this.dsAdapter = (AbstractDsAdapter) this.dsAdapterClass.newInstance();
            } else if (StringUtils.isNotBlank(this.dsAdapterName)) {
                this.dsAdapterClass = this.classLoader.loadClass(this.dsAdapterName);
                this.dsAdapter = (AbstractDsAdapter) this.dsAdapterClass.newInstance();
            } else {
                this.dsAdapterName = SpringDsAdapter.class.getName();
                this.dsAdapterClass = SpringDsAdapter.class;
                this.dsAdapter = new SpringDsAdapter();
            }
            this.dsAdapter.setDataSource(this.dataSource);
            return;
        }

        DataSource adapterDs = this.dsAdapter.getDataSource();
        DataSource targetDs = this.dataSource;
        if (adapterDs != null && targetDs != null && adapterDs != targetDs) {
            throw new IllegalArgumentException("DataSource configuration conflict.");
        }

        if (adapterDs == null) {
            this.dsAdapter.setDataSource(targetDs);
        }
    }

    private void initDialect() throws Exception {
        if (this.dialect == null && StringUtils.isNotBlank(this.dialectName)) {
            Class<?> dialectClass = this.classLoader.loadClass(dialectName);
            if (this.applicationContext != null) {
                this.dialect = (PageSqlDialect) createBeanByType(dialectClass, this.applicationContext);
            } else {
                this.dialect = (PageSqlDialect) dialectClass.newInstance();
            }
        }
    }

    @Override
    public DalSession getObject() {
        return Objects.requireNonNull(this.dalSession, "dalSession not init.");
    }

    @Override
    public Class<?> getObjectType() {
        return DalSession.class;
    }

    public void setDalRegistry(DalRegistry dalRegistry) {
        this.dalRegistry = dalRegistry;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDsAdapter(AbstractDsAdapter dsAdapter) {
        this.dsAdapter = dsAdapter;
    }

    public void setDsAdapterClass(Class<?> dsAdapterClass) {
        this.dsAdapterClass = dsAdapterClass;
    }

    public void setDsAdapterName(String dsAdapterName) {
        this.dsAdapterName = dsAdapterName;
    }

    public void setDialect(PageSqlDialect dialect) {
        this.dialect = dialect;
    }

    public void setDialectName(String dialectName) {
        this.dialectName = dialectName;
    }
}
