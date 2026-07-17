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
package net.hasor.dbvisitor.hasor.session;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.sql.DataSource;
import net.hasor.cobble.MatchUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;
import net.hasor.cobble.loader.MatchType;
import net.hasor.cobble.loader.ScanEvent;
import net.hasor.cobble.loader.providers.ClassPathResourceLoader;
import net.hasor.cobble.setting.Settings;
import net.hasor.core.ApiBinder;
import net.hasor.core.BindInfo;
import net.hasor.core.HasorUtils;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaOperations;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.TransactionTemplate;
import net.hasor.dbvisitor.transaction.Transactional;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

/** Registers dbVisitor runtime services for a data source that is already bound to Hasor. */
class SessionBindingModule implements net.hasor.core.Module {
    private final String sourceName;

    SessionBindingModule(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public void loadModule(ApiBinder apiBinder) throws Exception {
        BindInfo<DataSource> dataSource = apiBinder.findBindingRegister(this.bindingName(), DataSource.class);
        if (dataSource == null) {
            throw new IllegalStateException("DataSource '" + this.bindingName() + "' is not registered.");
        }

        Supplier<DataSource> dataSourceProvider = apiBinder.getProvider(dataSource);
        this.bindJdbc(apiBinder, dataSourceProvider);
        this.bindTransaction(apiBinder, dataSourceProvider);
        BindInfo<Configuration> configuration = this.bindConfiguration(apiBinder);
        this.bindSession(apiBinder, dataSource, configuration);
    }

    private void bindJdbc(ApiBinder apiBinder, Supplier<DataSource> dataSourceProvider) {
        JdbcTemplateProvider jdbcTemplate = new JdbcTemplateProvider(dataSourceProvider);
        LambdaTemplateProvider lambdaTemplate = new LambdaTemplateProvider(dataSourceProvider);
        if (StringUtils.isBlank(this.sourceName)) {
            apiBinder.bindType(JdbcTemplate.class).toProvider(jdbcTemplate);
            apiBinder.bindType(JdbcOperations.class).toProvider(jdbcTemplate);
            apiBinder.bindType(LambdaTemplate.class).toProvider(lambdaTemplate);
            apiBinder.bindType(LambdaOperations.class).toProvider(lambdaTemplate);
        } else {
            apiBinder.bindType(JdbcTemplate.class).nameWith(this.sourceName).toProvider(jdbcTemplate);
            apiBinder.bindType(JdbcOperations.class).nameWith(this.sourceName).toProvider(jdbcTemplate);
            apiBinder.bindType(LambdaTemplate.class).nameWith(this.sourceName).toProvider(lambdaTemplate);
            apiBinder.bindType(LambdaOperations.class).nameWith(this.sourceName).toProvider(lambdaTemplate);
        }
    }

    private void bindTransaction(ApiBinder apiBinder, Supplier<DataSource> dataSourceProvider) {
        Supplier<TransactionManager> transactionManager = new TransactionManagerProvider(dataSourceProvider);
        Supplier<TransactionTemplate> transactionTemplate = new TransactionTemplateProvider(dataSourceProvider);
        if (StringUtils.isBlank(this.sourceName)) {
            apiBinder.bindType(TransactionManager.class).toProvider(transactionManager);
            apiBinder.bindType(TransactionTemplate.class).toProvider(transactionTemplate);
        } else {
            apiBinder.bindType(TransactionManager.class).nameWith(this.sourceName).toProvider(transactionManager);
            apiBinder.bindType(TransactionTemplate.class).nameWith(this.sourceName).toProvider(transactionTemplate);
        }
        apiBinder.bindInterceptor(new ClassAnnotationOf(Transactional.class), new MethodAnnotationOf(Transactional.class), new TransactionInterceptor(dataSourceProvider));
    }

    private BindInfo<Configuration> bindConfiguration(ApiBinder apiBinder) {
        Settings settings = apiBinder.getSettings();
        SessionSettings configurationReader = new SessionSettings(settings, this.sourceName);
        Options options = Options.of();
        this.setBoolean(options::setAutoMapping, configurationReader.getAutoMapping());
        this.setBoolean(options::setMapUnderscoreToCamelCase, configurationReader.getCamelCase());
        this.setBoolean(options::setCaseInsensitive, configurationReader.getCaseInsensitive());
        this.setBoolean(options::setUseDelimited, configurationReader.getUseDelimited());
        this.setBoolean(options::setIgnoreNonExistStatement, configurationReader.getIgnoreNonExistStatement());
        String dialect = configurationReader.getSqlDialect();
        if (StringUtils.isNotBlank(dialect)) {
            options.setDialect(SqlDialectRegister.findOrCreate(dialect, apiBinder.getClassLoader()));
        }
        if (StringUtils.isBlank(this.sourceName)) {
            return apiBinder.bindType(Configuration.class).toInstance(new Configuration(options)).toInfo();
        } else {
            return apiBinder.bindType(Configuration.class).nameWith(this.sourceName).toInstance(new Configuration(options)).toInfo();
        }
    }

    private void setBoolean(java.util.function.Consumer<Boolean> setter, String value) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(Boolean.parseBoolean(value));
        }
    }

    private BindInfo<Session> bindSession(ApiBinder apiBinder, BindInfo<DataSource> dataSource, BindInfo<Configuration> configuration) throws Exception {
        Set<URI> mapperResources = this.findMapperResources(apiBinder);
        SessionSupplier sessionSupplier = HasorUtils.autoAware(apiBinder.getEventContext(), new SessionSupplier(configuration, dataSource, mapperResources));
        if (StringUtils.isBlank(this.sourceName)) {
            return apiBinder.bindType(Session.class).toProvider(sessionSupplier).toInfo();
        } else {
            return apiBinder.bindType(Session.class).nameWith(this.sourceName).toProvider(sessionSupplier).toInfo();
        }
    }

    private Set<URI> findMapperResources(ApiBinder apiBinder) throws Exception {
        SessionSettings settings = new SessionSettings(apiBinder.getSettings(), this.sourceName);
        String resources = settings.getMapperLocations();
        Set<URI> mapperResources = new HashSet<>();
        if (StringUtils.isBlank(resources)) {
            return mapperResources;
        }
        ClassPathResourceLoader resourceLoader = new ClassPathResourceLoader(apiBinder.getClassLoader());
        for (String resource : resources.split(",")) {
            String resourcePattern = resource.trim();
            if (StringUtils.startsWithIgnoreCase(resourcePattern, "classpath:")) {
                resourcePattern = resourcePattern.substring("classpath:".length());
            }
            resourcePattern = MatchUtils.wildToRegex(resourcePattern);
            List<URI> matches = resourceLoader.scanResources(MatchType.Regex, ScanEvent::getResource, new String[] { resourcePattern });
            mapperResources.addAll(matches);
        }
        return mapperResources;
    }

    private String bindingName() {
        return this.sourceName == null ? "" : this.sourceName;
    }

    private static class TransactionInterceptor implements MethodInterceptor {
        private final Supplier<DataSource> dataSource;

        TransactionInterceptor(Supplier<DataSource> dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource, "dataSource Provider is null.");
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Transactional transactional = this.findTransactional(invocation.getMethod());
            if (transactional == null) {
                return invocation.proceed();
            }
            TransactionManager manager = TransactionHelper.txManager(this.dataSource.get());
            TransactionStatus status = manager.begin(transactional.propagation(), transactional.isolation());
            if (transactional.readOnly()) {
                status.setReadOnly();
            }
            try {
                return invocation.proceed();
            } catch (Throwable e) {
                if (!this.isNoRollback(transactional, e)) {
                    status.setRollback();
                }
                throw e;
            } finally {
                if (!status.isCompleted()) {
                    manager.commit(status);
                }
            }
        }

        private Transactional findTransactional(Method method) {
            Transactional transactional = method.getAnnotation(Transactional.class);
            return transactional != null ? transactional : method.getDeclaringClass().getAnnotation(Transactional.class);
        }

        private boolean isNoRollback(Transactional transactional, Throwable error) {
            for (Class<? extends Throwable> type : transactional.noRollbackFor()) {
                if (type.isInstance(error)) {
                    return true;
                }
            }
            for (String typeName : transactional.noRollbackForClassName()) {
                if (error.getClass().getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ClassAnnotationOf implements Predicate<Class<?>> {
        private final Class<? extends Annotation> annotationType;

        ClassAnnotationOf(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean test(Class<?> type) {
            if (type.isAnnotationPresent(this.annotationType)) {
                return true;
            }
            return Arrays.stream(type.getMethods()).flatMap((Function<Method, Stream<?>>) method -> Arrays.stream(method.getAnnotationsByType(this.annotationType))).findAny().isPresent() || Arrays.stream(type.getDeclaredMethods()).flatMap((Function<Method, Stream<?>>) method -> Arrays.stream(method.getAnnotationsByType(this.annotationType))).findAny().isPresent();
        }
    }

    private static class MethodAnnotationOf implements Predicate<Method> {
        private final Class<? extends Annotation> annotationType;

        MethodAnnotationOf(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        @Override
        public boolean test(Method method) {
            return method.isAnnotationPresent(this.annotationType) || method.getDeclaringClass().isAnnotationPresent(this.annotationType);
        }
    }
}
