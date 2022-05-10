package net.hasor.dbvisitor.dal.dynamic;
import net.hasor.dbvisitor.dal.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.dal.dynamic.segment.SqlSegmentParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SegmentTester {

    @Test
    public void tokenTest_01() {
        Map<String, String> dat = new HashMap<>();

        DefaultSqlSegment sqlSegment = new DefaultSqlSegment() {
            @Override
            public void appendRuleExpr(String ruleName, String activateExpr, String exprString) {
                dat.put("NAME", ruleName);
                dat.put("ACT", activateExpr);
                dat.put("BODY", exprString);
            }
        };

        SqlSegmentParser.parserRule(sqlSegment, "a,b,c,d,e");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("b");
        assert dat.get("BODY").equals("c,d,e");

        SqlSegmentParser.parserRule(sqlSegment, "a");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("true");
        assert dat.get("BODY") == null;

        SqlSegmentParser.parserRule(sqlSegment, "a,b");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("b");
        assert dat.get("BODY") == null;

        SqlSegmentParser.parserRule(sqlSegment, "a,'b,c,d,e");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("'b,c,d,e");
        assert dat.get("BODY") == null;
    }

    @Test
    public void tokenTest_02() {
        Map<String, String> dat = new HashMap<>();

        DefaultSqlSegment sqlSegment = new DefaultSqlSegment() {
            @Override
            public void appendRuleExpr(String ruleName, String activateExpr, String exprString) {
                dat.put("NAME", ruleName);
                dat.put("ACT", activateExpr);
                dat.put("BODY", exprString);
            }
        };

        SqlSegmentParser.parserRule(sqlSegment, "a,'b,c,d,e");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("'b,c,d,e");
        assert dat.get("BODY") == null;

        SqlSegmentParser.parserRule(sqlSegment, "a,'b,c,'d,e");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("'b,c,'d");
        assert dat.get("BODY").equals("e");

        SqlSegmentParser.parserRule(sqlSegment, "a,'b','c,d',e");
        assert dat.get("NAME").equals("a");
        assert dat.get("ACT").equals("'b'");
        assert dat.get("BODY").equals("'c,d',e");
    }

    @Test
    public void tokenTest_03() {
        Map<String, String> dat = new HashMap<>();

        DefaultSqlSegment sqlSegment = new DefaultSqlSegment() {
            @Override
            public void appendRuleExpr(String ruleName, String activateExpr, String exprString) {
                dat.put("NAME", ruleName);
                dat.put("ACT", activateExpr);
                dat.put("BODY", exprString);
            }
        };

        SqlSegmentParser.parserRule(sqlSegment, "\\s,'b\\\',c,d,e");
        assert dat.get("NAME").equals("\\s");
        assert dat.get("ACT").equals("'b\\\',c,d,e");
        assert dat.get("BODY") == null;

        SqlSegmentParser.parserRule(sqlSegment, "\\s,'b\\\',c',d,e");
        assert dat.get("NAME").equals("\\s");
        assert dat.get("ACT").equals("'b\\\',c'");
        assert dat.get("BODY").equals("d,e");

    }

}
