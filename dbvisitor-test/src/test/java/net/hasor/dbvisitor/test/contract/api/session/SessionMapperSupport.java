/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.math.BigDecimal;
import java.util.Date;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class SessionMapperSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 812000;
    }

    protected Session createSession() throws Exception {
        Configuration configuration = newConfiguration();
        return configuration.newSession(dataSource);
    }

    protected UserInfo user(Integer id, String name, Integer age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        return user;
    }

    protected UserOrder order(Integer id, Integer userId, String orderNo, String amount) {
        UserOrder order = new UserOrder();
        order.setId(id);
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setAmount(new BigDecimal(amount));
        order.setCreateTime(new Date());
        return order;
    }
}
