/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.example;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Oracle System Change Number implementation
 *
 * @author Chris Cranford
 */
public class ScnTest {

    public static List<List<String>> dat1() {
        return new ArrayList<List<String>>() {{
            add(new ArrayList<>(Arrays.asList("1", "2", "3")));
            add(new ArrayList<>(Arrays.asList("4", "5", "6")));
            add(new ArrayList<>(Arrays.asList("2", "3", "4")));
            add(new ArrayList<>(Arrays.asList("9")));
            add(new ArrayList<>(Arrays.asList("17")));
            add(new ArrayList<>(Arrays.asList("17", "18")));
        }};
    }

    public static List<List<String>> dat2() {
        return new ArrayList<List<String>>() {{
            add(new ArrayList<>(Arrays.asList("1", "2", "3")));
            add(new ArrayList<>(Arrays.asList("4", "5", "6")));
            add(new ArrayList<>(Arrays.asList("7", "8", "9")));
        }};
    }

    public static List<List<String>> dat3() {
        return new ArrayList<List<String>>() {{
            add(new ArrayList<>(Arrays.asList("1", "2", "3")));
            add(new ArrayList<>(Arrays.asList("2", "3", "4")));
            add(new ArrayList<>(Arrays.asList("7", "8", "9")));
            add(new ArrayList<>(Arrays.asList("8", "9", "10")));
            add(new ArrayList<>(Arrays.asList("20", "21", "22")));
            add(new ArrayList<>(Arrays.asList("23", "24", "25")));
            add(new ArrayList<>(Arrays.asList("10", "20")));
        }};
    }

    public static void main(String[] args) {

        List<List<String>> dat = dat3();

        Result result = eval(dat);
        while (result.change) {
            result = eval(result.data);
        }

        System.out.println("OUT:\n" + JSON.toJSONString(result, true));

        //        Scn scn1 = Scn.valueOf(123);
        //        Scn scn2 = Scn.valueOf(224);
        //        System.out.println(scn1.compareTo(scn2));
    }

    public static Result eval(List<List<String>> data) {
        List<List<String>> outSet = new ArrayList<>();
        boolean change = false;

        for (List<String> inData : data) {
            if (outSet.isEmpty()) {
                outSet.add(inData);
                continue;
            }

            boolean merged = false;
            for (List<String> outDat : outSet) {
                if (containsAny(inData, outDat)) {
                    appendMerge(inData, outDat);
                    change = true;
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                outSet.add(inData);
            }
        }

        Result result = new Result();
        result.data = outSet;
        result.change = change;
        return result;
    }

    public static void appendMerge(final List<String> dat, final List<String> mergeTo) {
        for (String item : dat) {
            if (!mergeTo.contains(item)) {
                mergeTo.add(item);
            }
        }
    }

    public static boolean containsAny(final List<String> a, final List<String> b) {
        for (String item : a) {
            if (b.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static class Result {
        public List<List<String>> data;
        public boolean            change;
    }
}
