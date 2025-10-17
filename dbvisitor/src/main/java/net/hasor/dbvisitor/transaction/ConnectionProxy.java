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
package net.hasor.dbvisitor.transaction;
import java.io.Closeable;
import java.sql.Connection;

/**
 * 数据库连接代理接口，扩展了标准Connection接口
 * 提供了获取原始连接的能力，并继承了Closeable接口
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-3
 */
public interface ConnectionProxy extends Connection, Closeable {
    /**
     * 获取被代理的原始数据库连接对象
     * @return 底层原始连接对象（不会为null）
     */
    Connection getTargetConnection();
}