package net.hasor.dbvisitor.test.contract.material.model.types;

import java.sql.Types;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 枚举类型测试模型
 * 映射表 enum_types_explicit_test，验证枚举类型的映射（存储为字符串或整数）
 */
@Table("enum_types_explicit_test")
public class EnumTypesModel {
    @Column(name = "id", primary = true)
    private Integer id;

    @Column(name = "status_string", jdbcType = Types.VARCHAR)
    private StatusEnum statusString;

    @Column(name = "status_enum_code", jdbcType = Types.VARCHAR)
    private StatusEnumOfCode statusEnumCode;

    @Column(name = "status_ordinal", jdbcType = Types.INTEGER)
    private StatusEnumOfValue statusOrdinal;

    @Column(name = "status_code", jdbcType = Types.INTEGER)
    private StatusEnumOfValue statusCode;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StatusEnum getStatusString() {
        return statusString;
    }

    public void setStatusString(StatusEnum statusString) {
        this.statusString = statusString;
    }

    public StatusEnumOfCode getStatusEnumCode() {
        return statusEnumCode;
    }

    public void setStatusEnumCode(StatusEnumOfCode statusEnumCode) {
        this.statusEnumCode = statusEnumCode;
    }

    public StatusEnumOfValue getStatusOrdinal() {
        return statusOrdinal;
    }

    public void setStatusOrdinal(StatusEnumOfValue statusOrdinal) {
        this.statusOrdinal = statusOrdinal;
    }

    public StatusEnumOfValue getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusEnumOfValue statusCode) {
        this.statusCode = statusCode;
    }
}
