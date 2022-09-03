package net.hasor.dbvisitor.faker.seed.custom.special;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class MySqlTimeSeedFactoryTest {

    private final MySqlTimeSeedFactory f = new MySqlTimeSeedFactory();

    private String testConvertType(String str) {
        LocalDateTime dateTime = f.passerDateTime(str, LocalDateTime.now());
        return f.convertType(dateTime.atOffset(ZoneOffset.UTC), null).toString();
    }

    @Test
    public void convertTypeTest() {
        assert "838:59:59.123456789".equals(testConvertType("838:59:59.123456789"));
        assert "00:01:01.123".equals(testConvertType("00:01:01.123"));
        assert "00:00:00".equals(testConvertType("00:00"));
        assert "23:59:59.999999999".equals(testConvertType("23:59:59.999999999"));
        assert "52:59:59.999999999".equals(testConvertType("52:59:59.999999999"));
        assert "10000:59:59.999999999".equals(testConvertType("10000:59:59.999999999"));
        assert "-838:59:59.123456789".equals(testConvertType("-838:59:59.123456789"));
        assert "-01:01:01.123".equals(testConvertType("-01:01:01.123"));
        assert "-00:00:01".equals(testConvertType("-00:00:01"));
        assert "00:00:01".equals(testConvertType("00:00:01"));
    }

    @Test
    public void passerDateTimeTest() {
        assert "1970-01-02T14:59".equals(f.passerDateTime("38:59", LocalDateTime.now()).toString());
        assert "1970-01-02T14:59:59".equals(f.passerDateTime("38:59:59.0", LocalDateTime.now()).toString());
        assert "1970-01-02T14:59:59.123".equals(f.passerDateTime("38:59:59.123", LocalDateTime.now()).toString());
        assert "1970-01-02T14:59:59.123456789".equals(f.passerDateTime("38:59:59.123456789", LocalDateTime.now()).toString());

        assert "1969-12-30T09:01".equals(f.passerDateTime("-38:59", LocalDateTime.now()).toString());
        assert "1969-12-30T09:00:01".equals(f.passerDateTime("-38:59:59.0", LocalDateTime.now()).toString());
        assert "1969-12-30T09:00:00.877".equals(f.passerDateTime("-38:59:59.123", LocalDateTime.now()).toString());
        assert "1969-12-30T09:00:00.876543211".equals(f.passerDateTime("-38:59:59.123456789", LocalDateTime.now()).toString());

        assert "1970-02-04T22:59:59".equals(f.passerDateTime("838:59:59.000000", LocalDateTime.now()).toString());
        assert "1969-11-27T01:00:01".equals(f.passerDateTime("-838:59:59.000000", LocalDateTime.now()).toString());

        assert "1970-02-11T16:59:59".equals(f.passerDateTime("1000:59:59.000000", LocalDateTime.now()).toString());
        assert "1969-11-20T07:00:01".equals(f.passerDateTime("-1000:59:59.000000", LocalDateTime.now()).toString());
    }
}
