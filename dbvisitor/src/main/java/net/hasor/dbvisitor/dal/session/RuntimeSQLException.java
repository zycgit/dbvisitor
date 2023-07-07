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
package net.hasor.dbvisitor.dal.session;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 将 SQLException 转换为 RuntimeException
 * @version : 2021-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class RuntimeSQLException extends RuntimeException implements Iterable<Throwable> {
    
    protected SQLException getSQLException() {
        return (SQLException) this.getCause();
    }

    public RuntimeSQLException(SQLException e) {
        super(e);
    }

    public RuntimeSQLException(String s) {
        super(new SQLException(s));
    }

    /**
     * Retrieves the SQLState for this <code>SQLException</code> object.
     * @return the SQLState value
     */
    public String getSQLState() {
        return this.getSQLException().getSQLState();
    }

    /**
     * Retrieves the vendor-specific exception code for this <code>SQLException</code> object.
     * @return the vendor's error code
     */
    public int getErrorCode() {
        return this.getSQLException().getErrorCode();
    }

    /**
     * Returns an iterator over the chained SQLExceptions.
     * The iterator will be used to iterate over each SQLException and its underlying cause (if any).
     *
     * @return an iterator over the chained SQLExceptions and causes in the proper order
     * @since 1.6
     */
    public Iterator<Throwable> iterator() {
        return new Iterator<Throwable>() {
            SQLException firstException = RuntimeSQLException.this.getSQLException();
            SQLException nextException = firstException.getNextException();
            Throwable cause = firstException.getCause();

            public boolean hasNext() {
                return firstException != null || nextException != null || cause != null;
            }

            public Throwable next() {
                Throwable throwable = null;
                if (firstException != null) {
                    throwable = firstException;
                    firstException = null;
                } else if (cause != null) {
                    throwable = cause;
                    cause = cause.getCause();
                } else if (nextException != null) {
                    throwable = nextException;
                    cause = nextException.getCause();
                    nextException = nextException.getNextException();
                } else
                    throw new NoSuchElementException();
                return throwable;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}