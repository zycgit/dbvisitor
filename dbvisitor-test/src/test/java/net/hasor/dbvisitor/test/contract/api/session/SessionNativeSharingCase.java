/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Session sharing through declared commands, independent of generated BaseMapper CRUD. */
@NxnContract
public abstract class SessionNativeSharingCase extends SessionMapperSupport {
    protected String xmlNamespace() {
        return SessionRefUserMapper.class.getName();
    }

    protected void prepareSharing(Session session) throws Exception {
        refMapper(session);
    }

    protected int annotationPut(Session session, int id, String value) throws Exception {
        return simpleMapper(session).insertUser(user(id, "NativeShared", 30, value));
    }

    protected String annotationGet(Session session, int id) throws Exception {
        UserInfo user = simpleMapper(session).selectById(id);
        return user == null ? null : user.getEmail();
    }

    protected int annotationRemove(Session session, int id) throws Exception {
        return simpleMapper(session).deleteById(id);
    }

    protected int xmlPut(Session session, int id, String value) throws Exception {
        return ((Number) session.executeStatement(xmlNamespace() + ".insertUser",
                user(id, "NativeShared", 30, value))).intValue();
    }

    protected List<String> xmlGet(Session session, int id) throws Exception {
        List<UserInfo> rows = session.queryStatement(xmlNamespace() + ".queryUserById", Collections.singletonMap("id", id));
        return rows.stream().map(UserInfo::getEmail).collect(Collectors.toList());
    }

    protected int xmlReplace(Session session, int id, String value) throws Exception {
        return ((Number) session.executeStatement(xmlNamespace() + ".updateUserEmail", Map.of("id", id, "email", value))).intValue();
    }

    protected String referenceGet(Session session, int id) throws Exception {
        UserInfo user = refMapper(session).queryUserById(id);
        return user == null ? null : user.getEmail();
    }

    protected String jdbcGet(Session session, int id) throws Exception {
        return session.jdbc().queryForString("SELECT email FROM user_info WHERE id = ?", id);
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_ANNOTATION_XML_SHARING)
    public void session_shouldShareAnnotationWritesWithXmlStatements() throws Exception {
        Session session = createSession();
        prepareSharing(session);
        int id = baseId() + 101;

        assertEquals(1, annotationPut(session, id, "first"));
        assertEquals("first", annotationGet(session, id));
        assertEquals(Collections.singletonList("first"), xmlGet(session, id));
        assertEquals(1, annotationRemove(session, id));
        assertNull(annotationGet(session, id));
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_JDBC_SHARING)
    public void session_shouldShareXmlWritesWithJdbcAndMapperInterfaces() throws Exception {
        Session session = createSession();
        prepareSharing(session);
        int id = baseId() + 102;

        assertEquals(1, xmlPut(session, id, "before"));
        assertEquals(Collections.singletonList("before"), xmlGet(session, id));
        assertEquals("before", jdbcGet(session, id));
        assertEquals(1, xmlReplace(session, id, "after"));
        assertEquals("after", annotationGet(session, id));
        assertEquals("after", referenceGet(session, id));
        assertEquals("after", jdbcGet(session, id));
    }
}
