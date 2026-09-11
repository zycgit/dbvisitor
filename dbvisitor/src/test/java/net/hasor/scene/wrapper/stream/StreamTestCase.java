/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.scene.wrapper.stream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.dto.UserInfo2;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-3-22
 */
public class StreamTestCase extends AbstractDbTest {
    @Test
    public void lambdaQuery_stream_page_0() throws Throwable {
        Configuration conf = new Configuration(Options.of().dialect(new H2Dialect()));
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = conf.newLambda(c);
            //
            List<String> userIds = new ArrayList<>();
            Iterator<UserInfo2> userIterator = wrapper.query(UserInfo2.class).iteratorForLimit(-1, 1);
            while (userIterator.hasNext()) {
                userIds.add(userIterator.next().getUid());
            }

            assert wrapper.query(UserInfo2.class).queryForCount() == userIds.size();
        }
    }

    @Test
    public void lambdaQuery_stream_page_1() throws Throwable {
        Configuration conf = new Configuration(Options.of().dialect(new H2Dialect()));
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = conf.newLambda(c);
            //
            List<String> userIds = new ArrayList<>();
            Iterator<UserInfo2> userIterator = wrapper.query(UserInfo2.class).iteratorForLimit(2, 1);
            while (userIterator.hasNext()) {
                userIds.add(userIterator.next().getUid());
            }

            assert userIds.size() == 2;
        }
    }
}
