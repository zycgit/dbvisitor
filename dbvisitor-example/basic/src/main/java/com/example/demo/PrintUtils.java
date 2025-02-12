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
package com.example.demo;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.CharUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterUtils;

import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2014年7月11日
 */
public class PrintUtils {

    /** 打印列表内容 */
    public static <T> String printObjectList(final List<T> dataList) {
        return printObjectList(dataList, System.out);
    }

    /** 打印列表内容 */
    public static String printMapList(final List<Map<String, Object>> dataList) {
        return printMapList(dataList, System.out);
    }

    /** 打印列表内容 */
    public static <T> String printObjectList(final List<T> dataList, final PrintStream out) {
        List<Map<String, Object>> newDataList = new ArrayList<>();
        for (T obj : dataList) {
            if (obj instanceof Map) {
                newDataList.add((Map) obj);
            } else {
                List<String> keys = BeanUtils.getProperties(obj.getClass());
                Map<String, Object> newObj = new HashMap<>();
                for (String key : keys) {
                    newObj.put(key, BeanUtils.readProperty(obj, key));
                }
                newDataList.add(newObj);
            }
        }
        return printMapList(newDataList, out);
    }

    /** 打印列表内容 */
    public static String printMapList(final List<Map<String, Object>> dataList, final PrintStream out) {
        List<Map<String, String>> newValues = new ArrayList<>();
        Map<String, Integer> titleConfig = new LinkedHashMap<>();
        //1.转换
        for (Map<String, Object> mapItem : dataList) {
            Map<String, String> newVal = new HashMap<>();
            //
            for (Entry<String, Object> ent : mapItem.entrySet()) {
                //1.Title
                String key = ent.getKey();
                String val = ConverterUtils.convert(ent.getValue());
                val = val == null ? "" : val;
                Integer maxTitleLength = titleConfig.get(key);
                if (maxTitleLength == null) {
                    maxTitleLength = stringLength(key);
                }
                if (val.length() > maxTitleLength) {
                    maxTitleLength = stringLength(val);
                }
                titleConfig.put(key, maxTitleLength);
                //2.Value
                newVal.put(key, val);
            }
            //
            newValues.add(newVal);
        }
        //2.输出
        StringBuffer output = new StringBuffer();
        boolean first = true;
        int titleLength = 0;
        for (Map<String, String> row : newValues) {
            //1.Title
            if (first) {
                StringBuffer sb = new StringBuffer("");
                for (Entry<String, Integer> titleEnt : titleConfig.entrySet()) {
                    String title = StringUtils.rightPad(titleEnt.getKey(), titleEnt.getValue(), ' ');
                    sb.append(String.format("| %s ", title));
                }
                sb.append("|");
                titleLength = sb.length();
                sb.insert(0, String.format("/%s\\\n", StringUtils.center("", titleLength - 2, "-")));
                first = false;
                output.append(sb + "\n");
                output.append(String.format("|%s|\n", StringUtils.center("", titleLength - 2, "-")));
            }
            //2.Body
            StringBuffer sb = new StringBuffer("");
            for (String colKey : titleConfig.keySet()) {
                String val = row.get(colKey);
                String valueStr = StringUtils.rightPad(val, fixLength(val, titleConfig.get(colKey)), ' ');
                sb.append(String.format("| %s ", valueStr));
            }
            sb.append("|");
            output.append(sb.toString() + "\n");
        }
        output.append(String.format("\\%s/", StringUtils.center("", titleLength - 2, "-")));
        if (out != null) {
            out.println(output);
        }
        return output.toString();
    }

    private static int stringLength(final String str) {
        int length = 0;
        for (char c : str.toCharArray()) {
            if (CharUtils.isAscii(c)) {
                length++;
            } else {
                length = length + 2;
            }
        }
        return length;
    }

    /*修正长度*/
    private static int fixLength(final String str, int length) {
        for (char c : str.toCharArray()) {
            if (!CharUtils.isAscii(c)) {
                length--;
            }
        }
        return length;
    }
}