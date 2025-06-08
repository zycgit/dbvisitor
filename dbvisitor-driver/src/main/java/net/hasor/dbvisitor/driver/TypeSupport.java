package net.hasor.dbvisitor.driver;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public interface TypeSupport {

    String getTypeName(int typeNumber);

    String getTypeClassName(String typeName);

    String getTypeName(Class<?> classType);

    int getTypeNumber(String typeName);

    Date convertToData(Object o, Calendar cal);

    Time convertToTime(Object o, Calendar cal);

    Timestamp convertToTimestamp(Object o, Calendar cal);

    <T> T convertToType(Object o, Class<T> type);

    Object convertToType(Object o, Map<String, Class<?>> map);
}
