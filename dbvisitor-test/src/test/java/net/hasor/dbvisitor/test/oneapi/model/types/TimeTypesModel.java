package net.hasor.dbvisitor.test.oneapi.model.types;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Date;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * 时间日期类型测试模型 - 用户视角
 * 验证各种时间日期类型的自动推断和映射
 */
@Table("time_types_test")
public class TimeTypesModel {
    @Column(name = "id", primary = true)
    private Integer id;

    // java.util.Date 系列
    @Column(name = "util_date")
    private Date utilDate;

    // java.sql 系列
    @Column(name = "sql_date")
    private java.sql.Date sqlDate;
    @Column(name = "sql_time")
    private Time          sqlTime;
    @Column(name = "sql_timestamp")
    private Timestamp     sqlTimestamp;

    // Java 8 时间 API
    @Column(name = "local_date")
    private LocalDate      localDate;
    @Column(name = "local_time")
    private LocalTime      localTime;
    @Column(name = "local_datetime")
    private LocalDateTime  localDateTime;
    @Column(name = "offset_datetime")
    private OffsetDateTime offsetDateTime;
    @Column(name = "zoned_datetime")
    private ZonedDateTime  zonedDateTime;
    @Column(name = "instant")
    private Instant        instant;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getUtilDate() {
        return utilDate;
    }

    public void setUtilDate(Date utilDate) {
        this.utilDate = utilDate;
    }

    public java.sql.Date getSqlDate() {
        return sqlDate;
    }

    public void setSqlDate(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }

    public Time getSqlTime() {
        return sqlTime;
    }

    public void setSqlTime(Time sqlTime) {
        this.sqlTime = sqlTime;
    }

    public Timestamp getSqlTimestamp() {
        return sqlTimestamp;
    }

    public void setSqlTimestamp(Timestamp sqlTimestamp) {
        this.sqlTimestamp = sqlTimestamp;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }
}
