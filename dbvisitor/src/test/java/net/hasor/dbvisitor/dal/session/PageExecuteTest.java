package net.hasor.dbvisitor.dal.session;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.test.dal.execute.PageExecuteDal;
import net.hasor.test.dto.TbUser2;
import net.hasor.test.utils.DsUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PageExecuteTest {
    private DalSession dalSession;

    @Before
    public void loadMapping() throws IOException, SQLException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper(PageExecuteDal.class);

        this.dalSession = new DalSession(DsUtils.mysqlConn(), dalRegistry);

        this.beforeTest(this.dalSession.lambdaTemplate());
    }

    protected void beforeTest(JdbcTemplate jdbcTemplate) throws SQLException, IOException {
        jdbcTemplate.execute("drop table if exists test_user");
        jdbcTemplate.loadSplitSQL(";", StandardCharsets.UTF_8, "/net_hasor_db/dal_dynamic/execute/execute_for_mysql.sql");
    }

    public TbUser2 buildData(int i) {
        TbUser2 tbUser = new TbUser2();
        tbUser.setUid(UUID.randomUUID().toString().replace("-", ""));
        tbUser.setName("user_" + i);
        tbUser.setAccount("acc_" + i);
        tbUser.setPassword("pwd_" + i);
        tbUser.setMail(i + "_mail@mail.com");
        tbUser.setIndex(i);
        tbUser.setCreateTime(new Date());
        return tbUser;
    }

    @Test
    public void listUserListPage_1() {
        PageExecuteDal dalExecute = this.dalSession.createMapper(PageExecuteDal.class);
        dalExecute.deleteAll();

        for (int i = 0; i < 50; i++) {
            dalExecute.createUser(buildData(i));
        }

        Page pageInfo = new PageObject();
        pageInfo.setPageSize(2);

        pageInfo.setCurrentPage(1);
        List<TbUser2> pageData1 = dalExecute.listByPage1("user_1", pageInfo);
        assert pageData1.size() == 2;
        assert pageData1.get(0).getName().equals("user_11");
        assert pageData1.get(1).getName().equals("user_12");

        pageInfo.setCurrentPage(2);
        List<TbUser2> pageData2 = dalExecute.listByPage1("user_1", pageInfo);
        assert pageData2.size() == 2;
        assert pageData2.get(0).getName().equals("user_13");
        assert pageData2.get(1).getName().equals("user_14");
    }

    @Test
    public void listUserListPage_2() throws SQLException {
        PageExecuteDal dalExecute = this.dalSession.createMapper(PageExecuteDal.class);
        dalExecute.deleteAll();

        for (int i = 0; i < 50; i++) {
            dalExecute.createUser(buildData(i));
        }

        Page pageInfo = new PageObject();
        pageInfo.setPageSize(2);

        pageInfo.setCurrentPage(1);
        PageResult<TbUser2> pageData1 = dalExecute.listByPage2("user_1", pageInfo);
        assert pageData1.getData().size() == 2;
        assert pageData1.getData().get(0).getName().equals("user_11");
        assert pageData1.getData().get(1).getName().equals("user_12");
        assert pageData1.getPageSize() == 2;
        assert pageData1.getTotalCount() == 11;
        assert pageData1.getCurrentPage() == 1;
        assert pageData1.getTotalPage() == 6;

        pageInfo.setCurrentPage(2);
        PageResult<TbUser2> pageData2 = dalExecute.listByPage2("user_1", pageInfo);
        assert pageData2.getData().size() == 2;
        assert pageData2.getData().get(0).getName().equals("user_13");
        assert pageData2.getData().get(1).getName().equals("user_14");
        assert pageData2.getPageSize() == 2;
        assert pageData2.getTotalCount() == 11;
        assert pageData2.getCurrentPage() == 2;
        assert pageData2.getTotalPage() == 6;
    }

    @Test
    public void listUserListPage_3() throws SQLException {
        PageExecuteDal dalExecute = this.dalSession.createMapper(PageExecuteDal.class);
        dalExecute.deleteAll();

        for (int i = 0; i < 50; i++) {
            dalExecute.createUser(buildData(i));
        }

        TbUser2 tbUser2 = new TbUser2();
        tbUser2.setIndex(1);
        List<TbUser2> list = dalExecute.listBySample(tbUser2);
        assert list.size() == 1;
        assert list.get(0).getIndex() == 1;
        assert list.get(0).getAccount().equals("acc_1");

        int delete = dalExecute.delete().eq("index", 1).doDelete();
        assert delete == 1;

        list = dalExecute.listBySample(tbUser2);
        assert list.size() == 0;
    }
}
