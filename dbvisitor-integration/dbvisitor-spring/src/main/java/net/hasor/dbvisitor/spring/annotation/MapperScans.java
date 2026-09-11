/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.spring.annotation;
import java.lang.annotation.*;
import net.hasor.dbvisitor.spring.annotation.ScannerRegistrar.RepeatingRegistrar;
import org.springframework.context.annotation.Import;

/**
 * The Container annotation that aggregates several {@link MapperScan} annotations.
 * <p>
 * Can be used natively, declaring several nested {@link MapperScan} annotations. Can also be used in conjunction with
 * Java 8's support for repeatable annotations, where {@link MapperScan} can simply be declared several times on the
 * same method, implicitly generating this container annotation.
 * @author Kazuki Shimizu
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-29
 * @see MapperScan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RepeatingRegistrar.class)
public @interface MapperScans {
    MapperScan[] value();
}
