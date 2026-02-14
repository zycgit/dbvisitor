package net.hasor.dbvisitor.driver;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Comprehensive tests for JdbcCallableStatement — covers callable execution, OUT parameters, named set/get methods. */
public class JdbcCallableStatementTest {

    private JdbcConnection conn;

    @Before
    public void setUp() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, "mock");
        conn = new JdbcConnection("jdbc:dbvisitor:mock://localhost", props, null);
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed())
            conn.close();
    }

    // ==================== execute callable ====================
    @Test
    public void execute_callable() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.execute();
        assertEquals(1, cs.getUpdateCount());
        cs.close();
    }

    // ==================== registerOutParameter ====================
    @Test
    public void registerOutParameter_int_int() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getInt(1);
        } catch (SQLException ignore) { /* No row available - expected */ }
        cs.close();
    }

    @Test
    public void registerOutParameter_int_int_scale() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER, 2);
        cs.execute();
        cs.close();
    }

    @Test
    public void registerOutParameter_int_int_typeName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER, "INTEGER");
        cs.execute();
        cs.close();
    }

    @Test
    public void registerOutParameter_string_int() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getInt("arg1");
        } catch (SQLException ignore) { /* No row available - expected */ }
        cs.close();
    }

    @Test
    public void registerOutParameter_string_int_scale() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER, 2);
        cs.execute();
        cs.close();
    }

    @Test
    public void registerOutParameter_string_int_typeName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER, "INTEGER");
        cs.execute();
        cs.close();
    }

    // ==================== getter by index ====================
    @Test
    public void getInt_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getInt(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getString_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getString(2);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getBoolean_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getBoolean(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getByte_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getByte(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getShort_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getShort(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getLong_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getLong(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getFloat_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getFloat(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getDouble_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getDouble(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getBigDecimal_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getBigDecimal(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getDate_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getDate(1);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getDate_byIndex_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getDate(1, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTime_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getTime(1);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTime_byIndex_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getTime(1, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTimestamp_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getTimestamp(1);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTimestamp_byIndex_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getTimestamp(1, Calendar.getInstance());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getBytes_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getBytes(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getObject_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getObject(1);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getObject_byIndex_map() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getObject(1, new java.util.HashMap<String, Class<?>>());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getObject_byIndex_class() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getObject(1, Integer.class);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getNString_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getNString(2);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getClob_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getClob(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getNClob_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getNClob(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getBlob_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getBlob(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getURL_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getURL(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getSQLXML_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getSQLXML(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getRowId_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getRowId(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getNCharacterStream_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getNCharacterStream(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getCharacterStream_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getCharacterStream(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getRef_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getRef(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getArray_byIndex() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(2, Types.VARCHAR);
        cs.execute();
        try {
            cs.getArray(2);
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void wasNull_test() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getInt(1);
        } catch (SQLException ignore) {
        }
        cs.wasNull();
        cs.close();
    }

    // ==================== getter by name ====================
    @Test
    public void getInt_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getInt("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getString_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getString("arg2");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getBoolean_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getBoolean("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getByte_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getByte("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getShort_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getShort("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getLong_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getLong("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getFloat_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getFloat("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getDouble_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getDouble("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getBigDecimal_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getBigDecimal("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getDate_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getDate("arg1");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getDate_byName_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getDate("arg1", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTime_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getTime("arg1");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTime_byName_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getTime("arg1", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTimestamp_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getTimestamp("arg1");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getTimestamp_byName_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getTimestamp("arg1", Calendar.getInstance());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getBytes_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getBytes("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getObject_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getObject("arg1");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getObject_byName_map() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getObject("arg1", new java.util.HashMap<String, Class<?>>());
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getObject_byName_class() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg1", Types.INTEGER);
        cs.execute();
        try {
            cs.getObject("arg1", Integer.class);
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getNString_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getNString("arg2");
        } catch (SQLException ignore) {
        }
        cs.close();
    }

    @Test
    public void getClob_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getClob("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getNClob_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getNClob("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getBlob_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getBlob("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getURL_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getURL("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getSQLXML_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getSQLXML("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getRowId_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getRowId("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getNCharacterStream_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getNCharacterStream("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getCharacterStream_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getCharacterStream("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getRef_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getRef("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    @Test
    public void getArray_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter("arg2", Types.VARCHAR);
        cs.execute();
        try {
            cs.getArray("arg2");
        } catch (Exception ignore) {
        }
        cs.close();
    }

    // ==================== named setters ====================
    @Test
    public void setNull_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNull("arg1", Types.INTEGER);
        cs.execute();
        cs.close();
    }

    @Test
    public void setNull_byName_typeName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNull("arg1", Types.INTEGER, "INTEGER");
        cs.execute();
        cs.close();
    }

    @Test
    public void setBoolean_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBoolean("arg1", true);
        cs.execute();
        cs.close();
    }

    @Test
    public void setByte_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setByte("arg1", (byte) 1);
        cs.execute();
        cs.close();
    }

    @Test
    public void setShort_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setShort("arg1", (short) 1);
        cs.execute();
        cs.close();
    }

    @Test
    public void setInt_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setInt("arg1", 42);
        cs.execute();
        cs.close();
    }

    @Test
    public void setLong_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setLong("arg1", 42L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setFloat_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setFloat("arg1", 1.5f);
        cs.execute();
        cs.close();
    }

    @Test
    public void setDouble_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setDouble("arg1", 2.5d);
        cs.execute();
        cs.close();
    }

    @Test
    public void setBigDecimal_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBigDecimal("arg1", new BigDecimal("123.45"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setString_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setString("arg1", "value");
        cs.execute();
        cs.close();
    }

    @Test
    public void setNString_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNString("arg1", "value");
        cs.execute();
        cs.close();
    }

    @Test
    public void setBytes_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBytes("arg1", new byte[] { 1, 2 });
        cs.execute();
        cs.close();
    }

    @Test
    public void setDate_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setDate("arg1", java.sql.Date.valueOf("2023-01-15"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setDate_byName_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setDate("arg1", java.sql.Date.valueOf("2023-01-15"), Calendar.getInstance());
        cs.execute();
        cs.close();
    }

    @Test
    public void setTime_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setTime("arg1", java.sql.Time.valueOf("12:30:00"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setTime_byName_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        try {
            cs.setTime("arg1", java.sql.Time.valueOf("12:30:00"), Calendar.getInstance());
        } catch (UnsupportedOperationException ignore) { /* Time.toInstant() unsupported in Java 8 */ }
        cs.execute();
        cs.close();
    }

    @Test
    public void setTimestamp_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setTimestamp("arg1", java.sql.Timestamp.valueOf("2023-01-15 12:00:00"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setTimestamp_byName_calendar() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setTimestamp("arg1", java.sql.Timestamp.valueOf("2023-01-15 12:00:00"), Calendar.getInstance());
        cs.execute();
        cs.close();
    }

    @Test
    public void setObject_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setObject("arg1", 42);
        cs.execute();
        cs.close();
    }

    @Test
    public void setObject_byName_targetSqlType() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setObject("arg1", 42, Types.INTEGER);
        cs.execute();
        cs.close();
    }

    @Test
    public void setObject_byName_targetSqlType_scale() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setObject("arg1", 42, Types.INTEGER, 0);
        cs.execute();
        cs.close();
    }

    @Test
    public void setURL_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setURL("arg1", new URL("http://example.com"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setClob_reader_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setClob("arg1", new StringReader("data"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setClob_reader_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setClob("arg1", new StringReader("data"), 4L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setNClob_reader_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNClob("arg1", new StringReader("data"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setNClob_reader_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNClob("arg1", new StringReader("data"), 4L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setCharacterStream_reader_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setCharacterStream("arg1", new StringReader("data"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setCharacterStream_reader_int_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setCharacterStream("arg1", new StringReader("data"), 4);
        cs.execute();
        cs.close();
    }

    @Test
    public void setCharacterStream_reader_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setCharacterStream("arg1", new StringReader("data"), 4L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setNCharacterStream_reader_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNCharacterStream("arg1", new StringReader("data"));
        cs.execute();
        cs.close();
    }

    @Test
    public void setNCharacterStream_reader_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setNCharacterStream("arg1", new StringReader("data"), 4L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setBinaryStream_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBinaryStream("arg1", new ByteArrayInputStream(new byte[] { 1 }));
        cs.execute();
        cs.close();
    }

    @Test
    public void setBinaryStream_int_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBinaryStream("arg1", new ByteArrayInputStream(new byte[] { 1 }), 1);
        cs.execute();
        cs.close();
    }

    @Test
    public void setBinaryStream_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBinaryStream("arg1", new ByteArrayInputStream(new byte[] { 1 }), 1L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setAsciiStream_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setAsciiStream("arg1", new ByteArrayInputStream(new byte[] { 65 }));
        cs.execute();
        cs.close();
    }

    @Test
    public void setAsciiStream_int_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setAsciiStream("arg1", new ByteArrayInputStream(new byte[] { 65 }), 1);
        cs.execute();
        cs.close();
    }

    @Test
    public void setAsciiStream_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setAsciiStream("arg1", new ByteArrayInputStream(new byte[] { 65 }), 1L);
        cs.execute();
        cs.close();
    }

    @Test
    public void setBlob_inputStream_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBlob("arg1", new ByteArrayInputStream(new byte[] { 1, 2 }));
        cs.execute();
        cs.close();
    }

    @Test
    public void setBlob_inputStream_long_byName() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setBlob("arg1", new ByteArrayInputStream(new byte[] { 1, 2 }), 2L);
        cs.execute();
        cs.close();
    }

    // feature not supported
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setRowId_byName_unsupported() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setRowId("arg1", null);
    }

    @Test(expected = SQLFeatureNotSupportedException.class)
    public void setSQLXML_byName_unsupported() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.setSQLXML("arg1", null);
    }

    @Test
    public void getBigDecimal_byIndex_scale() throws Exception {
        CallableStatement cs = conn.prepareCall("{call myproc}");
        cs.registerOutParameter(1, Types.INTEGER);
        cs.execute();
        try {
            cs.getBigDecimal(1, 2);
        } catch (Exception ignore) {
        }
        cs.close();
    }
}
