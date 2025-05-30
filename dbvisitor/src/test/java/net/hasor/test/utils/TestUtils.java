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
package net.hasor.test.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class TestUtils {
    public static final String   INSERT_ARRAY  = "insert into user_info (user_uuid,user_name,login_name,login_password,email,seq,register_time) values (?,?,?,?,?,?,?);";
    public static final String   INSERT_MAP    = "insert into user_info (user_uuid,user_name,login_name,login_password,email,seq,register_time) values (:userUUID,:name,:loginName,:loginPassword,:email,:index,:registerTime);";
    public static final Object[] DATA_1        = new Object[] { newID(), "默罕默德", "muhammad", "1", "muhammad@hasor.net", 1, new Date() };
    public static final Object[] DATA_2        = new Object[] { newID(), "安妮.贝隆", "belon", "2", "belon@hasor.net", 2, new Date() };
    public static final Object[] DATA_3        = new Object[] { newID(), "赵飞燕", "feiyan", "3", "feiyan@hasor.net", 3, new Date() };
    public static final Object[] DATA_4        = new Object[] { newID(), "赵子龙", "zhaoyun", "4", "zhaoyun@hasor.net", 4, new Date() };
    public static final Object[] DATA_5        = new Object[] { newID(), "诸葛亮", "wolong", "5", "wolong@hasor.net", 5, new Date() };
    public static final Object[] DATA_6        = new Object[] { newID(), "张果老", "guolao", null, "guolao@hasor.net", 6, new Date() };
    public static final Object[] DATA_7        = new Object[] { newID(), "吴广", "wuguang", null, "wuguang@hasor.net", 7, new Date() };
    //
    public static final String   INSERT_ARRAY3 = "insert into user_info3 (user_uuid,user_name,login_name,login_password,email,seq,register_time) values (?,?,?,?,?,?,?);";
    public static final String   INSERT_MAP3   = "insert into user_info3 (user_uuid,user_name,login_name,login_password,email,seq,register_time) values (:userUUID,:name,:loginName,:loginPassword,:email,:index,:registerTime);";
    public static final Object[] DATA_13       = new Object[] { "11", "默罕默德", "muhammad", "1", "muhammad@hasor.net", 1, new Date() };
    public static final Object[] DATA_23       = new Object[] { "11", "安妮.贝隆", "belon", "2", "belon@hasor.net", 2, new Date() };
    public static final Object[] DATA_33       = new Object[] { "22", "赵飞燕", "feiyan", "3", "feiyan@hasor.net", 3, new Date() };
    public static final Object[] DATA_43       = new Object[] { "22", "赵子龙", "zhaoyun", "4", "zhaoyun@hasor.net", 4, new Date() };
    public static final Object[] DATA_53       = new Object[] { "33", "诸葛亮", "wolong", "5", "wolong@hasor.net", 5, new Date() };
    public static final Object[] DATA_63       = new Object[] { "33", "张果老", "guolao", null, "guolao@hasor.net", 6, new Date() };

    public static String newID() {
        return UUID.randomUUID().toString();
    }

    private static UserInfo fillBean(Object[] data, UserInfo tbUser) {
        tbUser.setUserUuid((String) data[0]);
        tbUser.setName((String) data[1]);
        tbUser.setLoginName((String) data[2]);
        tbUser.setLoginPassword((String) data[3]);
        tbUser.setEmail((String) data[4]);
        tbUser.setSeq((Integer) data[5]);
        tbUser.setRegisterTime((Date) data[6]);
        return tbUser;
    }

    private static Map<String, Object> fillMap(Object[] data, Map<String, Object> map) {
        map.put("userUUID", data[0]);
        map.put("name", data[1]);
        map.put("loginName", data[2]);
        map.put("loginPassword", data[3]);
        map.put("email", data[4]);
        map.put("index", data[5]);
        map.put("registerTime", data[6]);
        return map;
    }

    public static UserInfo beanForData1() {
        return fillBean(DATA_1, new UserInfo());
    }

    public static Object[] arrayForData1() {
        return DATA_1;
    }

    public static Map<String, Object> mapForData1() {
        return fillMap(DATA_1, new HashMap<>());
    }

    public static UserInfo beanForData2() {
        return fillBean(DATA_2, new UserInfo());
    }

    public static Object[] arrayForData2() {
        return DATA_2;
    }

    public static Map<String, Object> mapForData2() {
        return fillMap(DATA_2, new HashMap<>());
    }

    public static UserInfo beanForData3() {
        return fillBean(DATA_3, new UserInfo());
    }

    public static Object[] arrayForData3() {
        return DATA_3;
    }

    public static Map<String, Object> mapForData3() {
        return fillMap(DATA_3, new HashMap<>());
    }

    public static UserInfo beanForData4() {
        return fillBean(DATA_4, new UserInfo());
    }

    public static Object[] arrayForData4() {
        return DATA_4;
    }

    public static Map<String, Object> mapForData4() {
        return fillMap(DATA_4, new HashMap<>());
    }

    public static UserInfo beanForData5() {
        return fillBean(DATA_5, new UserInfo());
    }

    public static Object[] arrayForData5() {
        return DATA_5;
    }

    public static Map<String, Object> mapForData5() {
        return fillMap(DATA_5, new HashMap<>());
    }

    public static UserInfo beanForData6() {
        return fillBean(DATA_6, new UserInfo());
    }

    public static Object[] arrayForData6() {
        return DATA_6;
    }

    public static Map<String, Object> mapForData6() {
        return fillMap(DATA_6, new HashMap<>());
    }

    public static UserInfo beanForData7() {
        return fillBean(DATA_7, new UserInfo());
    }

    public static Object[] arrayForData7() {
        return DATA_7;
    }

    public static Map<String, Object> mapForData7() {
        return fillMap(DATA_7, new HashMap<>());
    }
}