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
package net.hasor.dbvisitor.spring.annotation;
import net.hasor.dbvisitor.spring.mapper.MapperScannerConfigurer;
import net.hasor.dbvisitor.spring.support.ConfigurationBean;
import net.hasor.dbvisitor.spring.support.MapperBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Use this annotation to register dbVisitor mapper interfaces when using Java Config. It performs when same work as
 * {@link MapperScannerConfigurer} via {@link ScannerRegistrar}.
 * <p>
 * Either {@link #basePackageClasses} or {@link #basePackages} (or its alias {@link #value}) may be specified to define
 * specific packages to scan. If specific packages are not defined, scanning will occur from the package of
 * the class that declares this annotation.
 * <p>
 * Configuration example:
 * </p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;MapperScan("net.myproject.spring.sample.mapper")
 * public class AppConfig {
 *   &#064;Bean
 *   public DataSource dataSource() {
 *     return new EmbeddedDatabaseBuilder().addScript("schema.sql").build();
 *   }
 *   &#064;Bean
 *   public DalSession dalSession() throws Exception {
 *     return new DalSession(dataSource());
 *   }
 * }
 * </pre>
 * @author Michael Lanyon
 * @author Eduardo Macarron
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see ScannerRegistrar
 * @see ConfigurationBean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ScannerRegistrar.class)
@Repeatable(MapperScans.class)
public @interface MapperScan {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @MapperScan("org.my.pkg")} instead of {@code @MapperScan(basePackages = "org.my.pkg"})}.
     * @return base package names
     */
    String[] value() default {};

    /**
     * Base packages to scan for dbVisitor interfaces. Note that only interfaces with at least one method will be
     * registered; concrete classes will be ignored.
     * @return base package names for scanning mapper interface
     */
    String[] basePackages() default {};

    String[] mapperLocations() default "";

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
     * package of each class specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that serves no purpose other than being
     * referenced by this attribute.
     * @return classes that indicate base package for scanning mapper interface
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * The {@link BeanNameGenerator} class to be used for naming detected components within the Spring container.
     * @return the class of {@link BeanNameGenerator}
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * This property specifies the annotation that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also have the specified annotation.
     * <p>
     * Note this can be combined with markerInterface.
     * @return the annotation that the scanner will search for
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * This property specifies the parent that the scanner will search for.
     * <p>
     * The scanner will register all interfaces in the base package that also have the specified interface class as a
     * parent.
     * <p>
     * Note this can be combined with annotationClass.
     * @return the parent that the scanner will search for
     */
    Class<?> markerInterface() default Class.class;

    /**
     * Specifies which {@code Session} to use in the case that there is more than one in the spring context.
     * Usually this is only needed when you have more than one datasource.
     * @return the bean name of {@code Session}
     */
    String sessionRef() default "";

    /**
     * Specifies a custom DalMapperBean to return a dbVisitor proxy as spring bean.
     * @return the class of {@code DalMapperBean}
     */
    Class<? extends MapperBean> factoryBean() default MapperBean.class;

    /**
     * Whether enable lazy init of mapper bean.
     * <p>
     * Default is {@code false}.
     * </p>
     * @return set {@code true} to enable lazy init
     * @since 2.0.2
     */
    String lazyInit() default "";

    /**
     * Specifies the default scope of scanned mappers.
     * <p>
     * Default is {@code ""} (equiv to singleton).
     * </p>
     * @return the default scope
     */
    String defaultScope() default AbstractBeanDefinition.SCOPE_DEFAULT;

    /** 是否禁用 mapper 的加载 只加载 mapperLocations*/
    boolean mapperDisabled() default false;
}
