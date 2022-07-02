/*
 * Copyright 2008-2009 the original ������(zyc@hasor.net).
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
package net.hasor.jdbc.transaction;
import java.sql.Connection;
/**
 * ������뼶��
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public enum TransactionLevel {
    /**Ĭ��������뼶�𣬾���ʹ�õ����ݿ�������뼶���ɵײ������
     * @see java.sql.Connection*/
    ISOLATION_DEFAULT(-1),
    /**
     * ���
     * <p>�������ȡ������������¶�ʧ�����һ�������Ѿ���ʼд���ݣ�
     * ������һ������������ͬʱ����д����������������������������ݡ�
     * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
     */
    ISOLATION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /**
     * �����ظ���
     * <p>�������ظ���ȡ�������������ȡ����ȡ���ݵ�����������������������ʸ������ݣ�
     * ����δ�ύ��д���񽫻��ֹ����������ʸ��С�
     * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
     */
    ISOLATION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /**
     * ���ظ���ȡ 
     * <p>��ֹ�����ظ���ȡ�������������ʱ���ܳ��ֻ�Ӱ���ݡ�
     * ��ȡ���ݵ����񽫻��ֹд���񣨵���������񣩣�д�������ֹ�κ���������
     * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
     */
    ISOLATION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    /**
     * ͬ������
     * <p>�ṩ�ϸ��������롣��Ҫ���������л�ִ�У�����ֻ��һ������һ����ִ�У������ܲ���ִ�С�
     * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
     */
    ISOLATION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
    //
    private int value;
    TransactionLevel(int value) {
        this.value = value;
    }
    protected int value() {
        return this.value;
    }
}