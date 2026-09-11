/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.spring.support;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import net.hasor.cobble.ClassUtils;
import net.hasor.dbvisitor.mapper.Mapper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see Mapper
 */
public abstract class AbstractSupportBean<T> implements FactoryBean<T>, BeanClassLoaderAware, ApplicationContextAware, InitializingBean {
    protected ClassLoader        classLoader;
    protected ApplicationContext applicationContext;

    protected Object createBeanByType(Class<?> beanType, ApplicationContext applicationContext) throws Exception {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(beanType);
        if (beanNamesForType == null || beanNamesForType.length == 0) {
            return ClassUtils.newInstance(beanType);
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
