package net.hasor.realdb.mongo.dto_bson;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.BsonTypeHandler;

@Table("bson_types_test")
public class BsonTypesDto {
    @Column(value = "_id", primary = true)
    private String id;

    @Column("str_val")
    private String stringValue;

    @Column("int_val")
    private Integer intValue;

    @Column("long_val")
    private Long longValue;

    @Column("double_val")
    private Double doubleValue;

    @Column("bool_val")
    private Boolean booleanValue;

    @Column("date_val")
    private Date dateValue;

    @Column("bytes_val")
    private byte[] bytesValue;

    @Column(value = "list_val", typeHandler = BsonTypeHandler.class)
    private List<String> listValue;

    @Column(value = "map_val", typeHandler = BsonTypeHandler.class)
    private Map<String, Object> mapValue;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStringValue() { return stringValue; }
    public void setStringValue(String stringValue) { this.stringValue = stringValue; }

    public Integer getIntValue() { return intValue; }
    public void setIntValue(Integer intValue) { this.intValue = intValue; }

    public Long getLongValue() { return longValue; }
    public void setLongValue(Long longValue) { this.longValue = longValue; }

    public Double getDoubleValue() { return doubleValue; }
    public void setDoubleValue(Double doubleValue) { this.doubleValue = doubleValue; }

    public Boolean getBooleanValue() { return booleanValue; }
    public void setBooleanValue(Boolean booleanValue) { this.booleanValue = booleanValue; }

    public Date getDateValue() { return dateValue; }
    public void setDateValue(Date dateValue) { this.dateValue = dateValue; }

    public byte[] getBytesValue() { return bytesValue; }
    public void setBytesValue(byte[] bytesValue) { this.bytesValue = bytesValue; }

    public List<String> getListValue() { return listValue; }
    public void setListValue(List<String> listValue) { this.listValue = listValue; }

    public Map<String, Object> getMapValue() { return mapValue; }
    public void setMapValue(Map<String, Object> mapValue) { this.mapValue = mapValue; }
}
