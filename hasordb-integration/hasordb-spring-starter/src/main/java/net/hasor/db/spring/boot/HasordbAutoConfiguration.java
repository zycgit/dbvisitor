package net.hasor.db.spring.boot;
import net.hasor.db.dal.dynamic.rule.RuleRegistry;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.mapping.resolve.MappingOptions;
import net.hasor.db.types.TypeHandlerRegistry;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass({ DalSession.class, DalRegistry.class })
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(HasordbProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class HasordbAutoConfiguration implements BeanClassLoaderAware, InitializingBean {
    private ClassLoader classLoader;
    //    private final HasordbProperties properties;
    //private final Interceptor[]     interceptors;
    //    private final ResourceLoader    resourceLoader;

    //    public HasordbAutoConfiguration(HasordbProperties properties, ResourceLoader resourceLoader) {
    //        this.properties = properties;
    //        this.interceptors = (Interceptor[]) interceptorsProvider.getIfAvailable();
    //        this.resourceLoader = resourceLoader;
    //    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void afterPropertiesSet() {
        //
        //hasordb.mapper-locations=classpath:hasordb/mapper/*.xml
        //hasordb.mapper-packages=com.example.demo.dao.*
        //
        //hasordb.type-handlers-packages=com.example.demo.dao.*
        //hasordb.type-handlers
    }

    @Bean
    @ConditionalOnMissingBean
    public DalRegistry dalRegistry(ObjectProvider<TypeHandlerRegistry> typeHandlersProvider, ObjectProvider<RuleRegistry> ruleRegistryProvider) {
        TypeHandlerRegistry typeHandlerRegistry = typeHandlersProvider.getIfAvailable();
        RuleRegistry ruleRegistry = ruleRegistryProvider.getIfAvailable();

        MappingOptions options = MappingOptions.buildNew();
        return new DalRegistry(this.classLoader, typeHandlerRegistry, ruleRegistry, options);
    }

    @Bean
    @ConditionalOnMissingBean
    public DalSession dalSession(DataSource dataSource, DalRegistry dalRegistry) throws Exception {
        return new DalSession(dataSource, dalRegistry);
    }

    //  jdbcTemplate name of the conflict
    //    @Bean
    //    @ConditionalOnMissingBean
    //    public JdbcTemplate jdbcTemplate(DataSource dataSource, ObjectProvider<TypeHandlerRegistry> typeHandlersProvider) {
    //        TypeHandlerRegistry typeHandlerRegistry = TypeHandlerRegistry.DEFAULT;
    //
    //        try {
    //            typeHandlerRegistry = typeHandlersProvider.getIfAvailable();
    //        } catch (Exception e) {
    //        }
    //
    //        return new JdbcTemplate(dataSource, typeHandlerRegistry);
    //    }

    @Bean
    @ConditionalOnMissingBean
    public LambdaTemplate lambdaTemplate(DataSource dataSource, ObjectProvider<TypeHandlerRegistry> typeHandlersProvider) {
        TypeHandlerRegistry typeHandlerRegistry = typeHandlersProvider.getIfAvailable();
        if (typeHandlerRegistry == null) {
            return new LambdaTemplate(dataSource);
        } else {
            return new LambdaTemplate(dataSource, typeHandlerRegistry);
        }
    }

}