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
package net.hasor.dbvisitor.spring.support;

import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.dal.session.DalSession;

import java.io.IOException;

/**
 * BeanFactory that enables injection of user mapper interfaces.
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
 *
 *     <bean id="oneMapper" class="net.hasor.dbvisitor.spring.support.DalMapperBean">
 *         <property name="dalSession" ref="dalSession"/>
 *         <property name="mapperInterface" value="net.hasor.dbvisitor.test.TestUserDAO"/>
 *     </bean>
 * }
 * </pre>
 * <p>
 * Note that this factory can only inject <em>interfaces</em>, not concrete classes.
 *
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
public class DalMapperBean extends AbstractSupportBean<Object> {
    private static final Logger     logger = LoggerFactory.getLogger(DalMapperBean.class);
    private              DalSession dalSession;
    private              Class<?>   mapperInterface;
    private              Object     mapperObject;

    @Override
    public void afterPropertiesSet() throws IOException {
        if (this.mapperInterface == null) {
            throw new NullPointerException("mapperInterface is null.");
        }
        if (this.dalSession == null) {
            throw new IllegalStateException("dalSession is null.");
        }

        RefMapper refMapper = this.mapperInterface.getAnnotation(RefMapper.class);
        if (refMapper != null) {
            logger.info("mapper '" + this.mapperInterface + "' using '" + refMapper.value() + "'");
            this.dalSession.getDalRegistry().loadMapper(this.mapperInterface);
        } else {
            logger.info("mapper '" + this.mapperInterface + "' using default.");
        }

        this.mapperObject = this.dalSession.createMapper(this.mapperInterface);
    }

    @Override
    public Object getObject() {
        return this.mapperObject;
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    public void setDalSession(DalSession dalSession) {
        this.dalSession = dalSession;
    }

    public void setMapperInterface(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }
}