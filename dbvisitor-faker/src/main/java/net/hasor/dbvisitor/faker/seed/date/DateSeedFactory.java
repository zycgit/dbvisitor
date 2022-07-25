/*
 * Copyright 2002-2010 the original author or authors.
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
import net.hasor.dbvisitor.faker.seed.RandomUtils;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;

import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

/**
 * 时间类型的 SeedFactory
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class DateSeedFactory implements SeedFactory<DateSeedConfig, Serializable> {

    @Override
    public SeedConfig newConfig() {
        return new DateSeedConfig();
    }

    @Override
    public Supplier<Serializable> createSeed(DateSeedConfig seedConfig) {
        boolean allowNullable = seedConfig.isAllowNullable();
        Float nullableRatio = seedConfig.getNullableRatio();
        if (allowNullable && nullableRatio == null) {
            throw new IllegalStateException("allowNullable is true but, nullableRatio missing.");
        }
        Supplier<Boolean> seedNull = () -> nullableRatio != null && RandomUtils.nextFloat(0, 100) < nullableRatio;

        switch (seedConfig.getGenType()) {
            case SysData:
                return seedSysData(seedConfig, seedNull);
            case Fixed:
                return seedFixed(seedConfig, seedNull);
            case Random:
                return seedRandom(seedConfig, seedNull);
            case Interval:
                return intervalSeed(seedConfig, seedNull);
            default:
                throw new UnsupportedOperationException("genType " + seedConfig.getGenType() + " Unsupported.");
        }
    }

    protected Supplier<Serializable> seedSysData(DateSeedConfig seedConfig, Supplier<Boolean> seedNull) {
        return () -> {
            if (seedNull.get()) {
                return null;
            } else {
                return convertType(ZonedDateTime.now(), seedConfig);
            }
        };
    }

    protected Supplier<Serializable> seedFixed(DateSeedConfig seedConfig, Supplier<Boolean> seedNull) {
        ZoneOffset zoneOffset = null;
        if (StringUtils.isNotBlank(seedConfig.getZoneForm())) {
            zoneOffset = passerZoned(seedConfig.getZoneForm(), ZonedDateTime.now().getOffset());
        } else if (StringUtils.isNotBlank(seedConfig.getZoneTo())) {
            zoneOffset = passerZoned(seedConfig.getZoneTo(), ZonedDateTime.now().getOffset());
        } else {
            zoneOffset = ZonedDateTime.now().getOffset();
        }

        ZonedDateTime passedTime = null;
        if (StringUtils.isNotBlank(seedConfig.getZoneForm())) {
            passedTime = passerDateTime(seedConfig.getRangeForm(), ZonedDateTime.now(), zoneOffset);
        } else if (StringUtils.isNotBlank(seedConfig.getZoneTo())) {
            passedTime = passerDateTime(seedConfig.getZoneTo(), ZonedDateTime.now(), zoneOffset);
        } else {
            passedTime = ZonedDateTime.of(LocalDateTime.now(), zoneOffset);
        }

        Serializable dateTime = convertType(passedTime, seedConfig);
        return () -> {
            if (seedNull.get()) {
                return null;
            } else {
                return dateTime;
            }
        };
    }

    protected Supplier<Serializable> seedRandom(DateSeedConfig seedConfig, Supplier<Boolean> seedNull) {
        ZoneOffset startZoned = passerZoned(seedConfig.getZoneForm(), ZonedDateTime.now().getOffset());
        ZoneOffset endZoned = passerZoned(seedConfig.getZoneTo(), ZonedDateTime.now().getOffset());
        ZonedDateTime startTime = passerDateTime(seedConfig.getRangeForm(), eraZero(), null);
        ZonedDateTime endTime = passerDateTime(seedConfig.getRangeTo(), ZonedDateTime.now().plusYears(100), null);

        int minZoned = startZoned.getTotalSeconds();
        int maxZoned = endZoned.getTotalSeconds();
        long minTime = startTime.toInstant().toEpochMilli();
        long maxTime = endTime.toInstant().toEpochMilli();

        return () -> {
            if (seedNull.get()) {
                return null;
            }

            int randomZoned = 0;
            long randomTime = 0;

            if (RandomUtils.nextBoolean()) {
                randomZoned = -RandomUtils.nextInt(0, Math.abs(minZoned));
            } else {
                randomZoned = RandomUtils.nextInt(0, Math.abs(maxZoned + 1));
            }
            if (RandomUtils.nextBoolean()) {
                randomTime = -RandomUtils.nextLong(0, Math.abs(minTime));
            } else {
                randomTime = RandomUtils.nextLong(0, Math.abs(maxTime + 1));
            }

            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(randomZoned);
            ZonedDateTime passedTime = Instant.ofEpochMilli(randomTime).atZone(zoneOffset);
            return convertType(passedTime, seedConfig);
        };
    }

    protected Supplier<Serializable> intervalSeed(DateSeedConfig seedConfig, Supplier<Boolean> seedNull) {
        ZonedDateTime startTime = passerDateTime(seedConfig.getStartTime(), eraZero(), null);
        int maxInterval = seedConfig.getMaxInterval();
        IntervalScope intervalScope = seedConfig.getIntervalScope();

        return () -> {
            if (seedNull.get()) {
                return null;
            }

            ZonedDateTime passedTime = plusInterval(startTime, maxInterval, intervalScope);
            return convertType(passedTime, seedConfig);
        };
    }

    private ZonedDateTime plusInterval(ZonedDateTime startTime, int maxInterval, IntervalScope scope) {
        long interval = RandomUtils.nextInt(0, maxInterval);
        switch (scope) {
            case Year:
                return startTime.plusYears(interval);
            case Month:
                return startTime.plusMonths(interval);
            case Day:
                return startTime.plusDays(interval);
            case Week:
                return startTime.plusWeeks(interval);
            case Hours:
                return startTime.plusHours(interval);
            case Minute:
                return startTime.plusMinutes(interval);
            case Second:
                return startTime.plusSeconds(interval);
            case Milli:
                return startTime.plusNanos(interval * 1000000);
            case Micro:
                return startTime.plusNanos(interval * 1000);
            default:
                throw new UnsupportedOperationException("intervalScope " + scope + " Unsupported.");
        }
    }

    protected static ZoneOffset passerZoned(String zonedStr, ZoneOffset defaultValue) {
        if (StringUtils.isNotBlank(zonedStr)) {
            return ZoneOffset.of(zonedStr);
        } else {
            return defaultValue;
        }
    }

    protected static ZonedDateTime passerDateTime(String dateStr, ZonedDateTime defaultDate, ZoneOffset zoned) {
        ZonedDateTime passedTime = defaultDate;
        if (StringUtils.isNotBlank(dateStr)) {
            try {
                passedTime = ZonedDateTime.parse(dateStr);
            } catch (DateTimeParseException e1) {
                try {
                    DateFormatType formatType = DateFormatType.passerType(dateStr);
                    passedTime = ZonedDateTime.of(formatType.toLocalDateTime(dateStr), ZoneId.systemDefault());
                } catch (Exception ignored) {
                }
            }
        }

        if (zoned != null) {
            return passedTime.toInstant().atZone(zoned);
        } else {
            return passedTime;
        }
    }

    protected static ZonedDateTime eraZero() {
        return ZonedDateTime.of(LocalDate.of(0, 1, 1), LocalTime.of(0, 0, 0), ZoneOffset.UTC);
    }

    private static Serializable convertType(ZonedDateTime defaultValue, DateSeedConfig seedConfig) {
        if (seedConfig.getDateType() == null) {
            return defaultValue;
        }
        switch (seedConfig.getDateType()) {
            case JavaDate:
                return new java.util.Date(defaultValue.toInstant().toEpochMilli());
            case JavaLong:
                return defaultValue.toInstant().toEpochMilli();
            case SqlDate:
                return new java.sql.Date(defaultValue.toInstant().toEpochMilli());
            case SqlTime:
                return new java.sql.Time(defaultValue.toInstant().toEpochMilli());
            case SqlTimestamp:
                return new java.sql.Timestamp(defaultValue.toInstant().toEpochMilli());
            case LocalDate:
                return defaultValue.toLocalDateTime().toLocalDate();
            case LocalTime:
                return defaultValue.toLocalDateTime().toLocalTime();
            case LocalDateTime:
                return defaultValue.toLocalDateTime();
            case Year:
                return Year.of(defaultValue.getYear());
            case YearMonth:
                return YearMonth.of(defaultValue.getYear(), defaultValue.getMonth());
            case Month:
                return defaultValue.getMonth();
            case MonthDay:
                return MonthDay.of(defaultValue.getMonth(), defaultValue.getDayOfMonth());
            case DayOfWeek:
                return defaultValue.getDayOfWeek();
            case OffsetTime:
                return defaultValue.toOffsetDateTime().toOffsetTime();
            case OffsetDateTime:
                return defaultValue.toOffsetDateTime();
            case Instant:
                return defaultValue.toInstant();
            case StrWithFormat:
                return seedConfig.getDateTimeFormatter().format(defaultValue);
            case ZonedDateTime:
            default:
                return defaultValue;
        }
    }
}
