package net.hasor.dbvisitor.test.oneapi.model.types;

import java.sql.Types;
import java.util.Map;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * JSON 类型显式映射模型 - 数据库视角
 * 验证不同 JSON 序列化库和数据库 JSON 类型的兼容性
 */
@Table("json_types_explicit_test")
public class JsonTypesExplicitModel {
    @Column(name = "id", primary = true, jdbcType = Types.INTEGER)
    private Integer id;

    // 使用 VARCHAR 存储 JSON（通用降级方案）
    @Column(name = "json_varchar", jdbcType = Types.VARCHAR)
    private Map<String, Object> jsonVarchar;

    // MySQL JSON 类型（5.7+）
    @Column(name = "json_mysql", jdbcType = Types.VARCHAR)
    private Map<String, Object> jsonMysql;

    // 嵌套对象测试
    @Column(name = "nested_json", jdbcType = Types.VARCHAR)
    private Map<String, Object> nestedJson;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, Object> getJsonVarchar() {
        return jsonVarchar;
    }

    public void setJsonVarchar(Map<String, Object> jsonVarchar) {
        this.jsonVarchar = jsonVarchar;
    }

    public Map<String, Object> getJsonMysql() {
        return jsonMysql;
    }

    public void setJsonMysql(Map<String, Object> jsonMysql) {
        this.jsonMysql = jsonMysql;
    }

    public Map<String, Object> getNestedJson() {
        return nestedJson;
    }

    public void setNestedJson(Map<String, Object> nestedJson) {
        this.nestedJson = nestedJson;
    }
}
