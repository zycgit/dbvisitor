/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * dbVisitor 事务管理器，支持 7 种传播属性。提供的操作数据库事务的接口，提供了7个不同的事务隔离级别。其实现思想来源于 Spring tx。
 * @author 赵永春 (zyc@hasor.net)
 */
package net.hasor.dbvisitor.transaction;
