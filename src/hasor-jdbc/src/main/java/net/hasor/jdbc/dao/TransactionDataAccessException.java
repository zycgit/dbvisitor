/*
 * Copyright 2002-2006 the original author or authors.
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
package net.hasor.jdbc.dao;
/**
 * ���ݿ��������쳣�ĸ������ཫ��ȷ������������ԭ��
 * @version : 2013-10-12
 * @author ������(zyc@hasor.net)
 */
public abstract class TransactionDataAccessException extends DataAccessException {
    private static final long serialVersionUID = -5338007128104634937L;
    /**
    * ���ݿ��������쳣�ĸ������ཫ��ȷ������������ԭ��
     * @param msg the detail message
     */
    public TransactionDataAccessException(String msg) {
        super(msg);
    }
    /**
    * ���ݿ��������쳣�ĸ������ཫ��ȷ������������ԭ��
     * @param msg the detail message
     * @param cause the root cause from the data access API in use
     */
    public TransactionDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}