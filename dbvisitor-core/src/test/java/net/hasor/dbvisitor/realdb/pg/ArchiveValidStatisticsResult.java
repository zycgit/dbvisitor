package net.hasor.dbvisitor.realdb.pg;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

import java.sql.Timestamp;

@Table(name = "archive_valid_statistics_result", schema = "temp", autoMapping = false)
public class ArchiveValidStatisticsResult {
    @Column(name = "archive_type", primary = true)
    private String archiveType;

    @Column(name = "has_name")
    private Long hasName = 0L;

    @Column(name = "has_not_name")
    private Long hasNotName = 0L;

    @Column(name = "unique_id_15")
    private Long uniqueId15 = 0L;

    @Column(name = "unique_id_18")
    private Long uniqueId18 = 0L;

    @Column(name = "unique_id_10")
    private Long uniqueId10 = 0L;

    @Column(name = "illegal_rowkey")
    private Long illegalRowKey = 0L;

    @Column(name = "update_time", insert = false)
    private Timestamp updateTime;

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public Long getHasName() {
        return hasName;
    }

    public void setHasName(Long hasName) {
        this.hasName = hasName;
    }

    public Long getHasNotName() {
        return hasNotName;
    }

    public void setHasNotName(Long hasNotName) {
        this.hasNotName = hasNotName;
    }

    public Long getUniqueId15() {
        return uniqueId15;
    }

    public void setUniqueId15(Long uniqueId15) {
        this.uniqueId15 = uniqueId15;
    }

    public Long getUniqueId18() {
        return uniqueId18;
    }

    public void setUniqueId18(Long uniqueId18) {
        this.uniqueId18 = uniqueId18;
    }

    public Long getUniqueId10() {
        return uniqueId10;
    }

    public void setUniqueId10(Long uniqueId10) {
        this.uniqueId10 = uniqueId10;
    }

    public Long getIllegalRowKey() {
        return illegalRowKey;
    }

    public void setIllegalRowKey(Long illegalRowKey) {
        this.illegalRowKey = illegalRowKey;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}