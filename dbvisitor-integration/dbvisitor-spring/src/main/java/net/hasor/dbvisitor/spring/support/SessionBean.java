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
package net.hasor.dbvisitor.spring.support;
import java.util.Objects;
import javax.sql.DataSource;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.spring.adapter.AbstractDsAdapter;
import net.hasor.dbvisitor.spring.adapter.SpringDsAdapter;

/**
 * BeanFactory that enables injection of DalSession.
 * <p>
 * Sample configuration:
 * <pre class="code">
 * {@code
 *     <bean id="configuration" class="net.hasor.dbvisitor.spring.support.ConfigurationBean">
 *         <property name="mapperResources" value="classpath*:dbvisitor/mapper/*Mapper.xml"/>
 *     </bean>
 *     <bean id="session" class="net.hasor.dbvisitor.spring.support.SessionBean">
 *         <property name="configuration" ref="configuration"/>
 *     </bean>
 * }
 * </pre>
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see Mapper
 */
public class SessionBean extends AbstractSupportBean<Session> {
    private Configuration     configuration;
    // - dsAdapter
    private AbstractDsAdapter dsAdapter;
    private Class<?>          dsAdapterClass;
    private String            dsAdapterName;
    private DataSource        dataSource;
    //
    private Session           session;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.dataSource == null) {
            this.dataSource = this.applicationContext.getBean(DataSource.class);
            if (this.dataSource == null) {
                throw new IllegalStateException("missing dataSource.");
            }
        }

        initDsAdapter();

        this.session = this.configuration.newSession(this.dsAdapter);
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

    @Override
    public Session getObject() {
        return Objects.requireNonNull(this.session, "Session not init.");
    }

    @Override
    public Class<?> getObjectType() {
        return Session.class;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
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
}
