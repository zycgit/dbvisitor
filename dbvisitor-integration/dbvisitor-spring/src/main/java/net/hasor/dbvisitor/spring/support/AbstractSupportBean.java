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
import net.hasor.dbvisitor.mapper.Mapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 *
 * @version : 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see Mapper
 */
public abstract class AbstractSupportBean<T> implements FactoryBean<T>, BeanClassLoaderAware, ApplicationContextAware, InitializingBean {
    protected ClassLoader        classLoader;
    protected ApplicationContext applicationContext;

    protected Object createBeanByType(Class<?> beanType, ApplicationContext applicationContext) throws Exception {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(beanType);
        if (beanNamesForType == null || beanNamesForType.length == 0) {
            return beanType.newInstance();
        } else {
            return applicationContext.getBean(beanType);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
