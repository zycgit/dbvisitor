/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.faker.seed.date;
import net.hasor.cobble.DateFormatType;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.time.format.DateTimeFormatter;

/**
 * 时间类型的 SeedConfig
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class DateSeedConfig extends SeedConfig {
    private GenType           genType;
    private DateType          dateType;
    private String            dateFormat;
    private DateTimeFormatter dateFormatter;
    // in random
    private String            rangeForm;
    private String            rangeTo;
    private String            zoneForm;     // see java.time.ZoneOffset.of(String offsetId)
    private String            zoneTo;       // see java.time.ZoneOffset.of(String offsetId)
    // in interval
    private String            startTime;
    private Integer           minInterval;
    private Integer           maxInterval;
    private IntervalScope     intervalScope;
    //
    private Integer           precision; //时间小数精度

    public DateTimeFormatter getDateTimeFormatter() {
        if (this.dateFormatter == null) {
            if (StringUtils.isNotBlank(this.dateFormat)) {
                this.dateFormatter = DateTimeFormatter.ofPattern(this.dateFormat);
            } else {
                this.dateFormatter = DateTimeFormatter.ofPattern(DateFormatType.d_yyyyMMdd_HHmmss.getDatePattern());
            }
        }
        return this.dateFormatter;
    }

    public final SeedType getSeedType() {
        return SeedType.Date;
    }

    @Override
    protected TypeHandler<?> defaultTypeHandler() {
        return TypeHandlerRegistry.DEFAULT.getDefaultTypeHandler();
    }

    public GenType getGenType() {
        return genType;
    }

    public void setGenType(GenType genType) {
        this.genType = genType;
    }

    public DateType getDateType() {
        return dateType;
    }

    public void setDateType(DateType dateType) {
        if (this.dateType != dateType) {
            this.dateType = dateType;
            this.setTypeHandler(TypeHandlerRegistry.DEFAULT.getTypeHandler(dateType.getDateType()));
        }
    }

    public String getRangeForm() {
        return rangeForm;
    }

    public void setRangeForm(String rangeForm) {
        this.rangeForm = rangeForm;
    }

    public String getRangeTo() {
        return rangeTo;
    }

    public void setRangeTo(String rangeTo) {
        this.rangeTo = rangeTo;
    }

    public String getZoneForm() {
        return zoneForm;
    }

    public void setZoneForm(String zoneForm) {
        this.zoneForm = zoneForm;
    }

    public String getZoneTo() {
        return zoneTo;
    }

    public void setZoneTo(String zoneTo) {
        this.zoneTo = zoneTo;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getMinInterval() {
        return minInterval;
    }

    public void setMinInterval(Integer minInterval) {
        this.minInterval = minInterval;
    }

    public Integer getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(Integer maxInterval) {
        this.maxInterval = maxInterval;
    }

    public IntervalScope getIntervalScope() {
        return intervalScope;
    }

    public void setIntervalScope(IntervalScope intervalScope) {
        this.intervalScope = intervalScope;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }
}