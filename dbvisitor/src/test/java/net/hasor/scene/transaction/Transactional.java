/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.scene.transaction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;

/**
 * 可以标记在：方法、类 上面。 通过 TransactionHelper 或者 TransactionalInterceptor 来使用该注解。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    /** 传播属性 */
    Propagation propagation() default Propagation.REQUIRED;

    /** 隔离级别 */
    Isolation isolation() default Isolation.DEFAULT;

    /** 是否为只读事务 */
    boolean readOnly() default false;

    /** 遇到下列异常继续事务递交 */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /** 遇到下列异常继续事务递交 */
    String[] noRollbackForClassName() default {};
}
