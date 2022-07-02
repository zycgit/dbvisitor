/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.transaction;
/**
 * {@link TransactionCallback}接口的无返回值。
 * @version : 2015年8月11日
 * @author 赵永春(zyc@hasor.net)
 */
public abstract class TransactionCallbackWithoutResult implements TransactionCallback<Void> {
    public final Void doTransaction(TransactionStatus tranStatus) throws Throwable {
        this.doTransactionWithoutResult(tranStatus);
        return null;
    }
    /***/
    public abstract void doTransactionWithoutResult(TransactionStatus tranStatus) throws Throwable;
}