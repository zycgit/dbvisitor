package net.hasor.dbvisitor.dal.session;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.test.dal.execute.PageExecuteDal;
import net.hasor.test.dto.UserInfo;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PageExecuteTest {

    public UserInfo buildData(int i) {
        UserInfo tbUser = new UserInfo();
        tbUser.setUserUuid(UUID.randomUUID().toString().replace("-", ""));
        tbUser.setName("user_" + i);
        tbUser.setLoginName("acc_" + i);
        tbUser.setLoginPassword("pwd_" + i);
        tbUser.setEmail(i + "_mail@mail.com");
        tbUser.setSeq(i);
        tbUser.setRegisterTime(new Date());
        return tbUser;
    }

    @Test
    public void listUserListPage_1() throws Exception {
        try (Connection con = DsUtils.mysqlConn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            dalRegistry.loadMapper(PageExecuteDal.class);

            DalSession dalSession = new DalSession(con, dalRegistry);
            PageExecuteDal dalExecute = dalSession.createMapper(PageExecuteDal.class);
            dalExecute.deleteAll();

            for (int i = 0; i < 50; i++) {
                dalExecute.createUser(buildData(i));
            }

            Page pageInfo = new PageObject();
            pageInfo.setPageSize(2);

            pageInfo.setCurrentPage(1);
            List<UserInfo> pageData1 = dalExecute.listByPage1("user_1", pageInfo);
            assert pageData1.size() == 2;
            assert pageData1.get(0).getName().equals("user_11");
            assert pageData1.get(1).getName().equals("user_12");

            pageInfo.setCurrentPage(2);
            List<UserInfo> pageData2 = dalExecute.listByPage1("user_1", pageInfo);
            assert pageData2.size() == 2;
            assert pageData2.get(0).getName().equals("user_13");
            assert pageData2.get(1).getName().equals("user_14");
        }
    }

    @Test
    public void listUserListPage_2() throws Exception {
        try (Connection con = DsUtils.mysqlConn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            dalRegistry.loadMapper(PageExecuteDal.class);

            DalSession dalSession = new DalSession(con, dalRegistry);
            PageExecuteDal dalExecute = dalSession.createMapper(PageExecuteDal.class);
            dalExecute.deleteAll();

            for (int i = 0; i < 50; i++) {
                dalExecute.createUser(buildData(i));
            }

            Page pageInfo = new PageObject();
            pageInfo.setPageSize(2);

            pageInfo.setCurrentPage(1);
            PageResult<UserInfo> pageData1 = dalExecute.listByPage2("user_1", pageInfo);
            assert pageData1.getData().size() == 2;
            assert pageData1.getData().get(0).getName().equals("user_11");
            assert pageData1.getData().get(1).getName().equals("user_12");
            assert pageData1.getPageSize() == 2;
            assert pageData1.getTotalCount() == 11;
            assert pageData1.getCurrentPage() == 1;
            assert pageData1.getTotalPage() == 6;

            pageInfo.setCurrentPage(2);
            PageResult<UserInfo> pageData2 = dalExecute.listByPage2("user_1", pageInfo);
            assert pageData2.getData().size() == 2;
            assert pageData2.getData().get(0).getName().equals("user_13");
            assert pageData2.getData().get(1).getName().equals("user_14");
            assert pageData2.getPageSize() == 2;
            assert pageData2.getTotalCount() == 11;
            assert pageData2.getCurrentPage() == 2;
            assert pageData2.getTotalPage() == 6;
        }
    }

    @Test
    public void listUserListPage_3() throws Exception {
        try (Connection con = DsUtils.mysqlConn()) {
            DalRegistry dalRegistry = new DalRegistry(MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
            dalRegistry.loadMapper(PageExecuteDal.class);

            DalSession dalSession = new DalSession(con, dalRegistry);
            PageExecuteDal dalExecute = dalSession.createMapper(PageExecuteDal.class);
            dalExecute.deleteAll();

            for (int i = 0; i < 50; i++) {
                dalExecute.createUser(buildData(i));
            }

            UserInfo tbUser2 = new UserInfo();
            tbUser2.setSeq(1);
            List<UserInfo> list = dalExecute.listBySample(tbUser2);
            assert list.size() == 1;
            assert list.get(0).getSeq() == 1;
            assert list.get(0).getLoginName().equals("acc_1");

            int delete = dalExecute.delete().eq("seq", 1).doDelete();
            assert delete == 1;

            list = dalExecute.listBySample(tbUser2);
            assert list.size() == 0;
        }
    }
}
