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
/**
 * {@link TransactionCallback}接口的无返回值。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2015年8月11日
 */
public interface TransactionCallbackWithoutResult extends TransactionCallback<Void> {
    default Void doTransaction(TransactionStatus tranStatus) throws Throwable {
        this.doTransactionWithoutResult(tranStatus);
        return null;
    }

    /***/
    void doTransactionWithoutResult(TransactionStatus tranStatus) throws Throwable;
}