package net.hasor.dbvisitor.driver;

public interface TypeConvert {

    Object convert(Class<?> targetType, Object value);
}
