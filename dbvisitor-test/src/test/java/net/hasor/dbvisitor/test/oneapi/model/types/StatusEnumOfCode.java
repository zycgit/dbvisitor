package net.hasor.dbvisitor.test.oneapi.model.types;

import net.hasor.dbvisitor.types.handler.string.EnumOfCode;

/**
 * 实现 EnumOfCode 接口 - 自定义字符串代码映射
 * 将枚举映射到自定义字符串代码（而不是枚举名称）
 * 用于测试 EnumTypeHandler 对 EnumOfCode 接口的支持
 */
public enum StatusEnumOfCode implements EnumOfCode<StatusEnumOfCode> {
    ACTIVE("active"),
    INACTIVE("inactive"),
    DELETED("deleted");

    private final String code;

    StatusEnumOfCode(String code) {
        this.code = code;
    }

    @Override
    public String codeName() {
        return this.code;
    }

    @Override
    public StatusEnumOfCode valueOfCode(String codeString) {
        for (StatusEnumOfCode status : values()) {
            if (status.code.equals(codeString)) {
                return status;
            }
        }
        return null;
    }
}
