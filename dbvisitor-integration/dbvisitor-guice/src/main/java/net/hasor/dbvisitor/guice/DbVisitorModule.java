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

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.name.Names;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.MatchUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.loader.ClassMatcher.ClassInfo;
import net.hasor.cobble.loader.ClassMatcher.ClassMatcherContext;
import net.hasor.cobble.loader.CobbleClassScanner;
import net.hasor.cobble.loader.ResourceLoader;
import net.hasor.cobble.loader.ResourceLoader.MatchType;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.setting.BasicSettings;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.cobble.setting.Settings;
import net.hasor.dbvisitor.dal.dynamic.rule.RuleRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.guice.provider.JdbcTemplateProvider;
import net.hasor.dbvisitor.guice.provider.LambdaTemplateProvider;
import net.hasor.dbvisitor.guice.provider.TransactionManagerProvider;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcAccessor;
import net.hasor.dbvisitor.jdbc.core.JdbcConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaOperations;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.transaction.*;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 */
public class DbVisitorModule implements com.google.inject.Module {
    private final Settings    settings;
    private final ClassLoader classLoader;

    public DbVisitorModule(Properties properties) {
        this.settings = new BasicSettings();
        this.classLoader = DbVisitorModule.class.getClassLoader();
        properties.forEach((key, value) -> settings.setSetting(key.toString(), value.toString()));
    }

    @Override
    public void configure(Binder binder) {
        String multipleDs = this.settings.getString(ConfigKeys.MultipleDataSource.getConfigKey());

        try {
            if (StringUtils.isNotBlank(multipleDs)) {
                String[] dsNames = multipleDs.split(",");
                for (String dbName : dsNames) {
                    configOneDb(dbName, binder);
                }
            } else {
                configOneDb(null, binder);
            }
        } catch (Exception e) {
            throw ExceptionUtils.toRuntime(e);
        }
    }

    private void configOneDb(String dbName, Binder binder) throws Exception {
        Key<DataSource> dsInfo = this.configDataSource(dbName, binder);
        Provider<DataSource> dsProvider = binder.getProvider(dsInfo);

        this.bindJdbc(dbName, binder, dsProvider);
        this.bindTrans(dbName, binder, dsProvider);

        Key<TypeHandlerRegistry> typeInfo = this.configTypeRegistry(dbName, binder);
        Key<RuleRegistry> ruleInfo = this.configRuleRegistry(dbName, binder);
        Key<DalSession> dalInfo = this.configDalSession(dbName, binder, dsInfo, typeInfo, ruleInfo);

        this.loadMapper(dbName, binder, dalInfo);
    }

    private Key<DataSource> configDataSource(String dbName, Binder binder) throws Exception {
        String configKey = ConfigKeys.DataSourceType.buildConfigKey(dbName);
        String dataSourceType = this.settings.getString(configKey, ConfigKeys.DataSourceType.getDefaultValue());
        DataSource dataSource;
        if (StringUtils.isBlank(dataSourceType)) {
            dataSource = new DefaultDataSource();
        } else {
            Class<?> dsClass = this.classLoader.loadClass(dataSourceType);
            dataSource = (DataSource) dsClass.newInstance();
        }

        SettingNode configNode = this.settings.getNode(configKey);
        String[] subKeys = configNode.getSubKeys();
        for (String key : subKeys) {
            String subValue = configNode.getSubValue(key);
            BeanUtils.writeProperty(dataSource, key, subValue);
        }

        Key<DataSource> key;
        if (StringUtils.isBlank(dbName)) {
            key = Key.get(DataSource.class);
        } else {
            key = Key.get(DataSource.class, Names.named(dbName));
        }

        binder.bind(key).toInstance(dataSource);
        return key;
    }

    private void bindJdbc(String dbName, Binder binder, Provider<DataSource> dsProvider) {
        JdbcTemplateProvider tempProvider = new JdbcTemplateProvider(dsProvider);
        LambdaTemplateProvider lambdaProvider = new LambdaTemplateProvider(dsProvider);

        if (StringUtils.isBlank(dbName)) {
            binder.bind(JdbcAccessor.class).toProvider(tempProvider);
            binder.bind(JdbcConnection.class).toProvider(tempProvider);
            binder.bind(JdbcTemplate.class).toProvider(tempProvider);
            binder.bind(JdbcOperations.class).toProvider(tempProvider);
            binder.bind(LambdaTemplate.class).toProvider(lambdaProvider);
            binder.bind(LambdaOperations.class).toProvider(lambdaProvider);
        } else {
            binder.bind(JdbcAccessor.class).annotatedWith(Names.named(dbName)).toProvider(tempProvider);
            binder.bind(JdbcConnection.class).annotatedWith(Names.named(dbName)).toProvider(tempProvider);
            binder.bind(JdbcTemplate.class).annotatedWith(Names.named(dbName)).toProvider(tempProvider);
            binder.bind(JdbcOperations.class).annotatedWith(Names.named(dbName)).toProvider(tempProvider);
            binder.bind(LambdaTemplate.class).annotatedWith(Names.named(dbName)).toProvider(lambdaProvider);
            binder.bind(LambdaOperations.class).annotatedWith(Names.named(dbName)).toProvider(lambdaProvider);
        }
    }

    private void bindTrans(String dbName, Binder binder, Provider<DataSource> dsProvider) {
        Provider<TransactionManager> managerProvider = new TransactionManagerProvider(dsProvider);
        Provider<TransactionTemplate> templateProvider = () -> new TransactionTemplateManager(DataSourceUtils.getManager(dsProvider.get()));

        if (StringUtils.isBlank(dbName)) {
            binder.bind(TransactionManager.class).toProvider(managerProvider);
            binder.bind(TransactionTemplate.class).toProvider(templateProvider);
        } else {
            binder.bind(TransactionManager.class).annotatedWith(Names.named(dbName)).toProvider(managerProvider);
            binder.bind(TransactionTemplate.class).annotatedWith(Names.named(dbName)).toProvider(templateProvider);
        }

        TranInterceptor tranInter = new TranInterceptor(dsProvider);
        binder.bindInterceptor(new ClassAnnotationOf(Transactional.class), new MethodAnnotationOf(Transactional.class), tranInter);
    }

    private Key<TypeHandlerRegistry> configTypeRegistry(String dbName, Binder binder) {
        String configKey = ConfigKeys.NamedTypeRegistry.buildConfigKey(dbName);
        String namedTypeRegistry = this.settings.getString(configKey, ConfigKeys.NamedTypeRegistry.getDefaultValue());

        Key<TypeHandlerRegistry> bindInfo;
        if (StringUtils.isNotBlank(namedTypeRegistry)) {
            bindInfo = Key.get(TypeHandlerRegistry.class, Names.named(namedTypeRegistry));
        } else {
            bindInfo = Key.get(TypeHandlerRegistry.class);
        }

        binder.bind(bindInfo).toInstance(TypeHandlerRegistry.DEFAULT);
        return bindInfo;
    }

    private Key<RuleRegistry> configRuleRegistry(String dbName, Binder binder) {
        String configKey = ConfigKeys.NamedRuleRegistry.buildConfigKey(dbName);
        String namedRuleRegistry = this.settings.getString(configKey, ConfigKeys.NamedRuleRegistry.getDefaultValue());

        Key<RuleRegistry> bindInfo;
        if (StringUtils.isNotBlank(namedRuleRegistry)) {
            bindInfo = Key.get(RuleRegistry.class, Names.named(namedRuleRegistry));
        } else {
            bindInfo = Key.get(RuleRegistry.class);
        }

        binder.bind(bindInfo).toInstance(RuleRegistry.DEFAULT);
        return bindInfo;
    }

    private Key<DalSession> configDalSession(String dbName, Binder binder,//
            Key<DataSource> dsInfo, Key<TypeHandlerRegistry> typeInfo, Key<RuleRegistry> ruleInfo) throws IOException {
        String configKey = ConfigKeys.MapperLocations.buildConfigKey(dbName);
        String resources = this.settings.getString(configKey, ConfigKeys.MapperLocations.getDefaultValue());
        Set<URL> mappers = new HashSet<>();

        if (StringUtils.isNotBlank(resources)) {
            ClassPathResourceLoader classScannerLoader = new ClassPathResourceLoader(this.classLoader);

            for (String resourceURL : resources.split(",")) {
                String resMapper = resourceURL.trim();
                if (StringUtils.startsWithIgnoreCase(resMapper, "classpath:")) {
                    resMapper = resMapper.substring("classpath:".length());
                }

                resMapper = MatchUtils.wildToRegex(resMapper);
                List<URL> tmp = classScannerLoader.scanResources(MatchType.Regex, ResourceLoader.ScanEvent::getResource, new String[] { resMapper });
                mappers.addAll(tmp);
            }
        }

        DalSessionSupplier sessionSupplier = new DalSessionSupplier(this.classLoader, dsInfo, typeInfo, ruleInfo, mappers);
        binder.requestInjection(sessionSupplier);

        Key<DalSession> dalKey;
        if (StringUtils.isBlank(dbName)) {
            dalKey = Key.get(DalSession.class);
        } else {
            dalKey = Key.get(DalSession.class, Names.named(dbName));
        }

        binder.bind(dalKey).toProvider(sessionSupplier);
        return dalKey;
    }

    //    private void loadResources(String dbName, ApiBinder apiBinder, Settings settings,//
    //            final BindInfo<TypeHandlerRegistry> typeInfo, final BindInfo<RuleRegistry> ruleInfo) throws IOException {
    //        HasorUtils.autoAware(apiBinder.getEnvironment(), appContext -> {
    //            TypeHandlerRegistry handlerRegistry = appContext.getInstance(typeInfo);
    //            RuleRegistry ruleRegistry = appContext.getInstance(ruleInfo);
    //            //
    //            // TODO setup.
    //        });
    //    }

    private void loadMapper(String dbName, Binder binder, Key<DalSession> dalInfo) throws ClassNotFoundException {
        String configMapperDisabled = ConfigKeys.MapperDisabled.buildConfigKey(dbName);
        String configMapperPackages = ConfigKeys.MapperPackages.buildConfigKey(dbName);
        String configMapperScope = ConfigKeys.MapperScope.buildConfigKey(dbName);
        String configScanMarkerAnnotation = ConfigKeys.ScanMarkerAnnotation.buildConfigKey(dbName);
        String configScanMarkerInterface = ConfigKeys.ScanMarkerInterface.buildConfigKey(dbName);

        Boolean mapperDisabled = this.settings.getBoolean(configMapperDisabled, Boolean.parseBoolean(ConfigKeys.MapperDisabled.getDefaultValue()));
        String mapperPackageConfig = this.settings.getString(configMapperPackages, ConfigKeys.MapperPackages.getDefaultValue());
        String mapperScope = this.settings.getString(configMapperScope, ConfigKeys.MapperScope.getDefaultValue());
        String scanMarkerAnnotation = this.settings.getString(configScanMarkerAnnotation, ConfigKeys.ScanMarkerAnnotation.getDefaultValue());
        String scanMarkerInterface = this.settings.getString(configScanMarkerInterface, ConfigKeys.ScanMarkerInterface.getDefaultValue());

        if (mapperDisabled) {
            return;
        }

        if (StringUtils.isBlank(mapperPackageConfig)) {
            return;
        }

        String[] mapperPackages = mapperPackageConfig.split(",");
        Set<Class<?>> finalResult = new HashSet<>();
        CobbleClassScanner scanner = new CobbleClassScanner(new ClassPathResourceLoader(this.classLoader));

        if (StringUtils.isNotBlank(scanMarkerAnnotation)) {
            Class<?> scanAnnotationType = this.classLoader.loadClass(scanMarkerAnnotation);
            Set<Class<?>> result1 = scanner.getClassSet(mapperPackages, c -> testClass(c, scanAnnotationType));
            finalResult.addAll(result1);
            finalResult.remove(scanAnnotationType);
        }

        if (StringUtils.isNotBlank(scanMarkerInterface)) {
            Class<?> scanInterfaceType = this.classLoader.loadClass(scanMarkerInterface);
            Set<Class<?>> result2 = scanner.getClassSet(mapperPackages, c -> testClass(c, scanInterfaceType));
            finalResult.addAll(result2);
            finalResult.remove(scanInterfaceType);
        }

        for (Class<?> mapper : finalResult) {
            Class<Object> mapperCast = (Class<Object>) mapper;

            DalMapperSupplier dalMapper = new DalMapperSupplier(mapperCast, dalInfo);
            binder.requestInjection(dalMapper);

            Key<Object> mapperKey;
            if (StringUtils.isBlank(dbName)) {
                mapperKey = Key.get(mapperCast);
            } else {
                mapperKey = Key.get(mapperCast, Names.named(dbName));
            }

            if (StringUtils.isNotBlank(mapperScope)) {
                Class<?> scopeType = this.classLoader.loadClass(mapperScope);
                binder.bind(mapperKey).toProvider(dalMapper).in((Class<? extends Annotation>) scopeType);
            } else {
                binder.bind(mapperKey).toProvider(dalMapper).asEagerSingleton();
            }
        }
    }

    private static class TranInterceptor implements MethodInterceptor {
        private final Provider<DataSource> dataSource;

        public TranInterceptor(Provider<DataSource> dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource Provider is null.");
        }

        /*是否不需要回滚:true表示不要回滚*/
        private boolean testNoRollBackFor(Transactional tranAnno, Throwable e) {
            //1.test Class
            Class<? extends Throwable>[] noRollBackType = tranAnno.noRollbackFor();
            for (Class<? extends Throwable> cls : noRollBackType) {
                if (cls.isInstance(e)) {
                    return true;
                }
            }
            //2.test Name
            String[] noRollBackName = tranAnno.noRollbackForClassName();
            String errorType = e.getClass().getName();
            for (String name : noRollBackName) {
                if (errorType.equals(name)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public final Object invoke(final MethodInvocation invocation) throws Throwable {
            Method targetMethod = invocation.getMethod();
            Transactional tranInfo = tranAnnotation(targetMethod);
            if (tranInfo == null) {
                return invocation.proceed();
            }
            //0.准备事务环境
            DataSource dataSource = this.dataSource.get();
            TransactionManager manager = DataSourceUtils.getManager(dataSource);
            Propagation behavior = tranInfo.propagation();
            Isolation level = tranInfo.isolation();
            TransactionStatus tranStatus = manager.begin(behavior, level);
            //1.只读事务
            if (tranInfo.readOnly()) {
                tranStatus.setReadOnly();
            }
            //2.事务行为控制
            try {
                return invocation.proceed();
            } catch (Throwable e) {
                if (!this.testNoRollBackFor(tranInfo, e)) {
                    tranStatus.setRollback();
                }
                throw e;
            } finally {
                if (!tranStatus.isCompleted()) {
                    manager.commit(tranStatus);
                }
            }
        }

        /** 在方法上找 Transactional ，如果找不到在到 类上找 Transactional ，如果依然没有，那么在所处的包(包括父包)上找 Transactional。*/
        private Transactional tranAnnotation(Method targetMethod) {
            Transactional tran = targetMethod.getAnnotation(Transactional.class);
            if (tran == null) {
                Class<?> declaringClass = targetMethod.getDeclaringClass();
                tran = declaringClass.getAnnotation(Transactional.class);
            }
            return tran;
        }

    }

    /** 匹配类或类方法上标记的注解 */
    private static class ClassAnnotationOf extends AbstractMatcher<Class<?>> {
        private final Class<? extends Annotation> annotationType;

        public ClassAnnotationOf(final Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean matches(final Class<?> matcherType) {
            Object[] annoByType1 = matcherType.getAnnotationsByType(this.annotationType);
            if (annoByType1.length > 0) {
                return true;
            }

            Object[] annoByType2 = Arrays.stream(matcherType.getMethods()).flatMap((Function<Method, Stream<?>>) method -> {
                return Arrays.stream(method.getAnnotationsByType(annotationType));
            }).toArray(Object[]::new);
            if (annoByType2.length > 0) {
                return true;
            }

            Object[] annoByType3 = Arrays.stream(matcherType.getDeclaredMethods()).flatMap((Function<Method, Stream<?>>) method -> {
                return Arrays.stream(method.getAnnotationsByType(annotationType));
            }).toArray(Object[]::new);

            return annoByType3.length > 0;
        }
    }

    /** 匹配方法上的注解 */
    private static class MethodAnnotationOf extends AbstractMatcher<Method> {
        private final Class<? extends Annotation> annotationType;

        public MethodAnnotationOf(final Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean matches(final Method matcherType) {
            if (matcherType.isAnnotationPresent(this.annotationType)) {
                return true;
            } else {
                return matcherType.getDeclaringClass().isAnnotationPresent(this.annotationType);
            }
        }
    }

    private static boolean testClass(ClassMatcherContext matcherContext, Class<?> compareType) {
        ClassInfo classInfo = matcherContext.getClassInfo();
        String compareTypeName = compareType.getName();

        if (classInfo.className.equals(compareTypeName) || classInfo.superName.equals(compareTypeName)) {
            return true;
        }

        for (String castType : classInfo.castType) {
            if (castType.equals(compareTypeName)) {
                return true;
            }
        }
        for (String face : classInfo.annos) {
            if (face.equals(compareTypeName)) {
                return true;
            }
        }

        return false;
    }
}
