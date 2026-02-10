package net.hasor.dbvisitor.test.oneapi.model.types;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.time.JulianDayTypeHandler;

/**
 * 时间日期类型显式映射模型 - 数据库视角
 * 验证显式指定 jdbcType 的时间类型映射
 */
@Table("time_types_explicit_test")
public class TimeTypesExplicitModel {
    @Column(name = "id", primary = true, jdbcType = Types.INTEGER)
    private Integer id;

    // 显式指定 JDBC 时间类型
    @Column(name = "date_value", jdbcType = Types.DATE)
    private java.sql.Date dateValue;

    @Column(name = "time_value", jdbcType = Types.TIME)
    private Time timeValue;

    @Column(name = "timestamp_value", jdbcType = Types.TIMESTAMP)
    private Timestamp timestampValue;

    // 儒略日（Julian Day）- 使用 JulianDayTypeHandler 将 LocalDate 存储为 BIGINT
    @Column(name = "julian_day", jdbcType = Types.BIGINT, typeHandler = JulianDayTypeHandler.class)
    private LocalDate julianDay;

    // Java 8 时间 API 映射到 TIMESTAMP
    @Column(name = "local_date_ts", jdbcType = Types.TIMESTAMP)
    private LocalDate localDateTs;

    @Column(name = "local_time_ts", jdbcType = Types.TIMESTAMP)
    private LocalTime localTimeTs;

    @Column(name = "local_datetime_ts", jdbcType = Types.TIMESTAMP)
    private LocalDateTime localDateTimeTs;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public java.sql.Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(java.sql.Date dateValue) {
        this.dateValue = dateValue;
    }

    public Time getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(Time timeValue) {
        this.timeValue = timeValue;
    }

    public Timestamp getTimestampValue() {
        return timestampValue;
    }

    public void setTimestampValue(Timestamp timestampValue) {
        this.timestampValue = timestampValue;
    }

    public LocalDate getJulianDay() {
        return julianDay;
    }

    public void setJulianDay(LocalDate julianDay) {
        this.julianDay = julianDay;
    }

    public LocalDate getLocalDateTs() {
        return localDateTs;
    }

    public void setLocalDateTs(LocalDate localDateTs) {
        this.localDateTs = localDateTs;
    }

    public LocalTime getLocalTimeTs() {
        return localTimeTs;
    }

    public void setLocalTimeTs(LocalTime localTimeTs) {
        this.localTimeTs = localTimeTs;
    }

    public LocalDateTime getLocalDateTimeTs() {
        return localDateTimeTs;
    }

    public void setLocalDateTimeTs(LocalDateTime localDateTimeTs) {
        this.localDateTimeTs = localDateTimeTs;
    }
}
