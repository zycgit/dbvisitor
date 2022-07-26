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
package net.hasor.dbvisitor.spring.annotation;
import net.hasor.dbvisitor.spring.support.DalRegistryBean;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Use this annotation to register dbVisitor mapper interfaces when using Java Config. It performs when same work as
 * {@link MappingScannerRegistrar} via {@link MappingScannerRegistrar}.
 *
 * <p>
 * Configuration example:
 * </p>
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;MapperScan("net.myproject.spring.sample.mapper")
 * public class AppConfig {
 *
 *   &#064;Bean
 *   public DataSource dataSource() {
 *     return new EmbeddedDatabaseBuilder().addScript("schema.sql").build();
 *   }
 *
 *   &#064;Bean
 *   public DalSession dalSession() throws Exception {
 *     return new DalSession(dataSource());
 *   }
 * }
 * </pre>
 *
 * @version 2022-04-29
 * @author 赵永春 (zyc@hasor.net)
 * @see MapperScannerRegistrar
 * @see DalRegistryBean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MapperScannerRegistrar.class)
@Repeatable(MappingScans.class)
public @interface MappingScan {
    /**
     * Specifies which {@code SqlSessionTemplate} to use in the case that there is more than one in the spring context.
     * Usually this is only needed when you have more than one datasource.
     *
     * @return the bean name of {@code DalSession}
     */
    String dalSessionRef() default "";

    String[] mapperLocations() default "";
}
