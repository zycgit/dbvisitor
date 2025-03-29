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
import net.hasor.dbvisitor.mapper.Mapper;
import net.hasor.dbvisitor.session.Session;

/**
 * BeanFactory that enables injection of user mapper interfaces.
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
 *     <bean id="oneMapper" class="net.hasor.dbvisitor.spring.support.MapperBean">
 *         <property name="session" ref="session"/>
 *         <property name="mapperInterface" value="net.hasor.dbvisitor.test.TestUserDAO"/>
 *     </bean>
 * }
 * </pre>
 * <p>
 * Note that this factory can only inject <em>interfaces</em>, not concrete classes.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see Mapper
 */
public class MapperBean extends AbstractSupportBean<Object> {
    private Session  session;
    private Class<?> mapperInterface;
    private Object   mapperObject;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.mapperInterface == null) {
            throw new NullPointerException("mapperInterface is null.");
        }
        if (this.session == null) {
            throw new IllegalStateException("dalSession is null.");
        }

        this.mapperObject = this.session.createMapper(this.mapperInterface);
    }

    @Override
    public Object getObject() {
        return this.mapperObject;
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setMapperInterface(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }
}