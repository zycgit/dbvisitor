/*
 * Copyright 2002-2007 the original author or authors.
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
package net.hasor.jdbc.jdbc;
import java.sql.PreparedStatement;
import java.sql.SQLException;
/**
 * ��������ʱ��̬�������ýӿڡ�
 * @version : 2013-10-9
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author ������(zyc@hasor.net)
 */
public interface BatchPreparedStatementSetter {
    /** 
     * Set parameter values on the given PreparedStatement.
     * @param ps the PreparedStatement to invoke setter methods on
     * @param i index of the statement we're issuing in the batch, starting from 0
     * @throws SQLException if a SQLException is encountered (i.e. there is no need to catch SQLException)
     */
    public void setValues(PreparedStatement ps, int i) throws SQLException;
    /** 
     * Return the size of the batch.
     * @return the number of statements in the batch
     */
    public int getBatchSize();
}