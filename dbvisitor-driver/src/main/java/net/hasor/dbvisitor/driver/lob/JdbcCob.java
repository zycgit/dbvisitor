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
package net.hasor.dbvisitor.driver.lob;
import java.io.*;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import net.hasor.dbvisitor.driver.JdbcErrorCode;

public class JdbcCob implements Clob, NClob, JdbcOutputStreamWatcher, JdbcWriterWatcher {
    protected String  charData;
    private   boolean freed;

    private void checkOpen() throws SQLException {
        if (this.freed) {
            throw new SQLException("Clob has been freed.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
    }

    public JdbcCob(String charDataInit) {
        this.charData = charDataInit;
    }

    @Override
    public InputStream getAsciiStream() throws SQLException {
        this.checkOpen();
        if (this.charData != null) {
            return new ByteArrayInputStream(this.charData.getBytes());
        }

        return null;
    }

    @Override
    public Reader getCharacterStream() throws SQLException {
        this.checkOpen();
        if (this.charData != null) {
            return new StringReader(this.charData);
        }

        return null;
    }

    @Override
    public String getSubString(long startPos, int length) throws SQLException {
        this.checkRange(startPos, length);

        int adjustedStartPos = (int) startPos - 1;
        int adjustedEndIndex = adjustedStartPos + length;
        if (this.charData != null) {
            return this.charData.substring(adjustedStartPos, adjustedEndIndex);
        } else {
            return null;
        }
    }

    @Override
    public long length() throws SQLException {
        this.checkOpen();
        if (this.charData != null) {
            return this.charData.length();
        } else {
            return 0;
        }
    }

    @Override
    public long position(java.sql.Clob arg0, long arg1) throws SQLException {
        this.checkOpen();
        return position(arg0.getSubString(1L, (int) arg0.length()), arg1);
    }

    @Override
    public long position(String stringToFind, long startPos) throws SQLException {
        this.checkOpen();
        if (startPos < 1) {
            throw new SQLException("Illegal starting position for search, '" + startPos + "'", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        if (this.charData != null) {
            if ((startPos - 1) > this.charData.length()) {
                throw new SQLException("Starting position for search is past end of CLOB", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
            }
            int pos = this.charData.indexOf(stringToFind, (int) (startPos - 1));
            return (pos == -1) ? (-1) : (pos + 1);
        } else {
            return -1;
        }
    }

    @Override
    public OutputStream setAsciiStream(long indexToWriteAt) throws SQLException {
        this.checkRange(indexToWriteAt, 0);

        JdbcWatchableOutputStream bytesOut = new JdbcWatchableOutputStream();
        bytesOut.setWatcher(this);
        bytesOut.write(this.charData.getBytes(), 0, (int) (indexToWriteAt - 1));
        return bytesOut;
    }

    @Override
    public Writer setCharacterStream(long indexToWriteAt) throws SQLException {
        this.checkRange(indexToWriteAt, 0);

        JdbcWatchableWriter writer = new JdbcWatchableWriter();
        writer.setWatcher(this);
        //
        // Don't call write() if nothing to write...
        //
        if (indexToWriteAt > 1) {
            writer.write(this.charData, 0, (int) (indexToWriteAt - 1));
        }
        return writer;
    }

    @Override
    public int setString(long pos, String str) throws SQLException {
        this.checkOpen();
        if (str == null) {
            throw new SQLException("String to set can not be NULL", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        return this.setString(pos, str, 0, str.length());
    }

    @Override
    public int setString(long pos, String str, int offset, int len) throws SQLException {
        this.checkRange(pos, 0);
        if (str == null) {
            throw new SQLException("Invalid CLOB write range.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        long sourceEnd = (long) offset + len;
        long writeStart = pos - 1;
        long writeEnd = writeStart + len;
        boolean invalidSourceRange = offset < 0 || len < 0 || sourceEnd > str.length();
        boolean writeOverflow = writeEnd > Integer.MAX_VALUE;
        if (invalidSourceRange || writeOverflow) {
            throw new SQLException("Invalid CLOB write range.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        StringBuilder charBuf = new StringBuilder(this.charData);
        try {
            String replaceString = str.substring(offset, (int) sourceEnd);
            charBuf.replace((int) writeStart, (int) writeEnd, replaceString);
        } catch (StringIndexOutOfBoundsException e) {
            throw new SQLException(e.getMessage(), JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT, e);
        }

        this.charData = charBuf.toString();
        return len;
    }

    @Override
    public void truncate(long length) throws SQLException {
        this.checkOpen();
        if (length < 0 || length > this.charData.length()) {
            throw new SQLException("Cannot truncate CLOB of length " + this.charData.length() + " to length of " + length);
        }

        this.charData = this.charData.substring(0, (int) length);
    }

    @Override
    public void writerClosed(JdbcWatchableWriter out) {
        if (this.freed) {
            return;
        }

        int dataLength = out.size();
        if (dataLength < this.charData.length()) {
            out.write(this.charData, dataLength, this.charData.length() - dataLength);
        }

        this.charData = out.toString();
    }

    @Override
    public void streamClosed(JdbcWatchableStream out) {
        if (this.freed) {
            return;
        }

        int streamSize = out.size();
        if (streamSize < this.charData.length()) {
            out.write(this.charData.getBytes(), streamSize, this.charData.length() - streamSize);
        }

        byte[] byteArray = out.toByteArray();
        char[] charArray = new char[byteArray.length];
        int readpoint = 0;
        int readLength = byteArray.length;
        for (int i = 0; i < readLength; i++) {
            charArray[i] = (char) byteArray[readpoint];
            readpoint++;
        }

        this.charData = new String(charArray);
    }

    @Override
    public void free() {
        this.freed = true;
        this.charData = null;
    }

    @Override
    public Reader getCharacterStream(long pos, long length) throws SQLException {
        this.checkRange(pos, length);
        String value = getSubString(pos, (int) length);
        return value == null ? null : new StringReader(value);
    }

    private void checkRange(long pos, long length) throws SQLException {
        long size = this.length();
        if (pos < 1 || length < 0 || pos - 1 > size || length > size - (pos - 1)) {
            throw new SQLException("Invalid CLOB range.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
    }
}
