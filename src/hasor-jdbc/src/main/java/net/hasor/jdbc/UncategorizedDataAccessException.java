/*
 * Copyright 2002-2006 the original author or authors.
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
package net.hasor.jdbc;
/**
 * ����ԭ��ĵײ� SQL �쳣��
 * @version : 2013-10-12
 * @author ������(zyc@hasor.net)
 */
public class UncategorizedDataAccessException extends DataAccessException {
    private static final long serialVersionUID = 447732094361475241L;
    /**����ԭ��ĵײ� SQL �쳣��*/
    public UncategorizedDataAccessException(String msg) {
        super(msg);
    }
    /**����ԭ��ĵײ� SQL �쳣��*/
    public UncategorizedDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }
}