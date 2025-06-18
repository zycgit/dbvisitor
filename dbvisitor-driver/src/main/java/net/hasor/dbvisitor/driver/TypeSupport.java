package net.hasor.dbvisitor.driver;

public interface TypeSupport {

    String getTypeName(int typeNumber);

    String getTypeClassName(String typeName);

    String getTypeName(Class<?> classType);

    int getTypeNumber(String typeName);

    TypeConvert findConvert(String typeName, Class<?> toType);
}
