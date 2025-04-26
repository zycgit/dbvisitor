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
package net.hasor.dbvisitor.transaction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事务注解，用于声明式事务管理。
 * 可以标记在方法或类上，被标记的方法或类中的所有方法将参与事务管理。
 * 需要通过 TransactionHelper 或 TransactionalInterceptor 来启用该注解的功能。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-07-18
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
    /**
     * 事务传播行为，定义事务如何传播。
     * 默认值：Propagation.REQUIRED（如果当前存在事务，则加入该事务；否则新建一个事务）
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务隔离级别，定义事务的隔离程度。
     * 默认值：Isolation.DEFAULT（使用数据库默认隔离级别）
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * 是否为只读事务。
     * 只读事务可以提供优化机会，某些数据库会根据此标志进行优化。
     * 默认值：false（非只读事务）
     */
    boolean readOnly() default false;

    /**
     * 指定遇到哪些异常时不回滚事务（按异常类名匹配）。
     * 默认值：空数组（所有异常都触发回滚）
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 指定遇到哪些异常时不回滚事务（按异常类全名匹配）。
     * 与noRollbackFor类似，但通过异常类名字符串匹配而非Class对象。
     * 默认值：空数组（所有异常都触发回滚）
     */
    String[] noRollbackForClassName() default {};
}