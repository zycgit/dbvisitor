package net.hasor.dbvisitor.test.oneapi.model.types;

import net.hasor.dbvisitor.types.handler.string.EnumOfValue;

/**
 * 实现 EnumOfValue 接口 - 数值代码映射
 * 将枚举映射到整数代码
 * 用于测试 EnumTypeHandler 对 EnumOfValue 接口的支持
 */
public enum StatusEnumOfValue implements EnumOfValue<StatusEnumOfValue> {
    ACTIVE(1),
    INACTIVE(0),
    DELETED(-1);

    private final int code;

    StatusEnumOfValue(int code) {
        this.code = code;
    }

    @Override
    public int codeValue() {
        return this.code;
    }

    @Override
    public StatusEnumOfValue valueOfCode(int codeValue) {
        for (StatusEnumOfValue status : values()) {
            if (status.code == codeValue) {
                return status;
            }
        }
        return null;
    }
}
