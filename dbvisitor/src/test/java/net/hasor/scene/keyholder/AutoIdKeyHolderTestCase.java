package net.hasor.scene.keyholder;
import net.hasor.dbvisitor.JdbcHelper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.wrapper.InsertWrapper;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import net.hasor.scene.keyholder.dto.UserDTO_32;
import net.hasor.scene.keyholder.dto.UserDTO_36;
import net.hasor.scene.keyholder.dto.UserDTO_KEYHOLDER;
import net.hasor.scene.keyholder.dto.UserDTO_SEQ;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class AutoIdKeyHolderTestCase {

    @Test
    public void autoUUID32TestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter wrapper = new WrapperAdapter(c);
            wrapper.deleteByEntity(UserDTO_32.class).allowEmptyWhere().doDelete();

            UserDTO_32 userData = new UserDTO_32();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertWrapper<UserDTO_32> insert = wrapper.insertByEntity(UserDTO_32.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() != null;
            assert userData.getName().length() == 32;
        }
    }

    @Test
    public void autoUUID36TestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter wrapper = new WrapperAdapter(c);
            wrapper.deleteByEntity(UserDTO_36.class).allowEmptyWhere().doDelete();

            UserDTO_36 userData = new UserDTO_36();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertWrapper<UserDTO_36> insert = wrapper.insertByEntity(UserDTO_36.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() != null;
            assert userData.getName().length() == 36;
        }
    }

    @Test
    public void autoSeqTestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            Options o = Options.of().defaultDialect(JdbcHelper.findDialect(c));
            WrapperAdapter wrapper = new WrapperAdapter(c, o);
            wrapper.deleteByEntity(UserDTO_SEQ.class).allowEmptyWhere().doDelete();

            UserDTO_SEQ userData = new UserDTO_SEQ();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertWrapper<UserDTO_SEQ> insert = wrapper.insertByEntity(UserDTO_SEQ.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() == 1;
            assert userData.getName().length() == 36;
        }
    }

    @Test
    public void myHolderTestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter wrapper = new WrapperAdapter(c);
            wrapper.deleteByEntity(UserDTO_KEYHOLDER.class).allowEmptyWhere().doDelete();

            UserDTO_KEYHOLDER userData = new UserDTO_KEYHOLDER();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertWrapper<UserDTO_KEYHOLDER> insert = wrapper.insertByEntity(UserDTO_KEYHOLDER.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() == 111111;
            assert userData.getName().length() == 36;
        }
    }
}
