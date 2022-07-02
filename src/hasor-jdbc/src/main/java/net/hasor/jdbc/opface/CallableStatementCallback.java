/*
 * Copyright 2002-2008 the original author or authors.
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
package net.hasor.jdbc.opface;
import java.sql.CallableStatement;
import java.sql.SQLException;
import net.hasor.jdbc.DataAccessException;
/**
 * ͨ�õĻص��ӿڡ�����ִ�л��� {@link CallableStatement}�ϵ��������������������ݿ������
 * @version : 2013-10-9
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author ������(zyc@hasor.net)
 */
public interface CallableStatementCallback<T> {
    /**
     * ִ��һ�� JDBC �����������߲���Ҫ�������ݿ����ӵ�״̬������
     * @param con һ�����õ� JDBC ���ݿ�����
     * @return ���ز���ִ�е����ս����
     */
    public T doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException;
}