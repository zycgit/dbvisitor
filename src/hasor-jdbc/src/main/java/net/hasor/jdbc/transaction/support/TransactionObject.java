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
package net.hasor.jdbc.transaction.support;
import net.hasor.jdbc.datasource.SavepointManager;
import net.hasor.jdbc.datasource.local.ConnectionHolder;
/**
 * 
 * @version : 2014-1-18
 * @author ������ (zyc@byshell.org)
 */
public class TransactionObject {
    public TransactionObject(ConnectionHolder holder) {
        // TODO Auto-generated constructor stub
    }
    public ConnectionHolder getHolder() {
        return null;
    }
    public SavepointManager getSavepointManager() {
        return this.getHolder();
    };
    public void rollback() {
        // TODO Auto-generated method stub
    }
    public void commit() {
        // TODO Auto-generated method stub
    }
    public boolean hasTransaction() {
        //AutoCommit�����Ϊ false ��ʾ����������
        //        return conn.getAutoCommit() == false ? true : false;
        return false;
    };
    public void begin() {
        //        if (autoMark == true)
        //            conn.setAutoCommit(false);//������autoCommit����Ϊfalse������Ϊ�ֶ��ݽ�����
    }
    public void requested() {
        // TODO Auto-generated method stub
    }
}