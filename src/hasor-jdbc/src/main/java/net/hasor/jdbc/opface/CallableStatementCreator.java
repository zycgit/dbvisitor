/*
 * Copyright 2002-2005 the original author or authors.
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
import java.sql.Connection;
import java.sql.SQLException;
/**
 * �ýӿ����ڴ��� CallableStatement ����
 * @version : 2013-10-9
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author ������(zyc@hasor.net)
 */
public interface CallableStatementCreator {
    /**ʹ�ò��������������Ӵ��� CallableStatement ���� */
    public CallableStatement createCallableStatement(Connection con) throws SQLException;
}