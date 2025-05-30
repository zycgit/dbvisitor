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
package net.hasor.scene.transaction;
import net.hasor.cobble.dynamic.MethodInterceptor;
import net.hasor.cobble.dynamic.MethodInvocation;
import net.hasor.dbvisitor.transaction.Isolation;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 可以标记在：方法、类 上面
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-10-30
 */
public class TransactionalInterceptor implements MethodInterceptor {
    private final DataSource dataSource;

    public TransactionalInterceptor(DataSource dataSource) {
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
        DataSource dataSource = this.dataSource;
        TransactionManager manager = new LocalTransactionManager(dataSource);
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

    /** 在方法上找 Transactional ，如果找不到在到 类上找 Transactional ，如果依然没有，那么在所处的包(包括父包)上找 Transactional。 */
    private Transactional tranAnnotation(Method targetMethod) {
        Transactional tran = targetMethod.getAnnotation(Transactional.class);
        if (tran == null) {
            Class<?> declaringClass = targetMethod.getDeclaringClass();
            tran = declaringClass.getAnnotation(Transactional.class);
        }
        return tran;
    }
}
