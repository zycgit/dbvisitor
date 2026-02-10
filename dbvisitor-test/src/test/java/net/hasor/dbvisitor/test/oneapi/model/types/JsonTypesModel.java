package net.hasor.dbvisitor.test.oneapi.model.types;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * JSON 类型测试模型 - 用户视角
 * 验证 JSON 类型的映射
 */
@Table("json_types_test")
public class JsonTypesModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // JSON 字段（存储为字符串）
    @Column(name = "json_data")
    private String jsonData;

    @Column(name = "json_array")
    private String jsonArray;

    @Column(name = "json_object")
    private String jsonObject;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(String jsonArray) {
        this.jsonArray = jsonArray;
    }

    public String getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(String jsonObject) {
        this.jsonObject = jsonObject;
    }
}
