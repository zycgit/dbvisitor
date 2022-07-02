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
import java.sql.SQLException;
/**
 * ����Դ�������������
 * @version : 2013-10-30
 * @author ������(zyc@hasor.net)
 */
public interface TransactionManager {
    /**��������ʹ��Ĭ��������뼶��
     * @see net.hasor.jdbc.transaction.TransactionBehavior
     * @see net.hasor.jdbc.transaction.TransactionManager#getTransaction(TransactionBehavior, TransactionLevel)*/
    public TransactionStatus getTransaction(TransactionBehavior behavior) throws SQLException;
    /**��������
     * @see net.hasor.jdbc.transaction.TransactionBehavior
     * @see java.sql.Connection#setTransactionIsolation(int)*/
    public TransactionStatus getTransaction(TransactionBehavior behavior, TransactionLevel level) throws SQLException;
    /**�ݽ�����
     * <p>����ݽ������񲢲����������ջ���ˣ���ͬʱ�ݽ�������ĺ�����������*/
    public void commit(TransactionStatus status) throws SQLException;
    /**�ع�����*/
    public void rollBack(TransactionStatus status) throws SQLException;
    //
    /**�Ƿ����δ����������񣨰�������������񣩡�*/
    public boolean hasTransaction();
    /**��������״̬�Ƿ�λ��ջ����*/
    public boolean isTopTransaction(TransactionStatus status);
}