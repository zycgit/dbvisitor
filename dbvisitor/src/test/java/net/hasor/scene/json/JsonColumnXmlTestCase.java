package net.hasor.scene.json;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.json.dto.Project1;
import net.hasor.scene.json.dto.Project2;
import net.hasor.scene.json.dto.ProjectFeature;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JsonColumnXmlTestCase {

    private Project1 newProject(String name, List<String> tags) {
        Project1 project = new Project1();
        project.setName(name);
        project.setFeature(new ProjectFeature());
        project.getFeature().setAge(23);
        project.getFeature().setDesc("this is desc");
        project.getFeature().setTimePoint(new Date());
        project.getFeature().setTags(new ArrayList<>());
        project.getFeature().getTags().addAll(tags);
        return project;
    }

    @Test
    public void result_1_TestCase() throws SQLException, IOException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/dbvisitor_scene/json/json-column-1.xml");
        dalRegistry.loadEntityToSpace(Project1.class);

        try (Connection c = DsUtils.mysqlConn()) {
            DalSession dalSession = new DalSession(c, dalRegistry);
            LambdaTemplate template = dalSession.lambdaTemplate();

            template.getJdbc().execute("drop table if exists `project`");
            template.getJdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project = newProject("abc", Arrays.asList("CN", "EN"));
            assert template.insertBySpace(Project1.class).applyEntity(project).executeSumResult() == 1;

            List<Object> list = dalSession.queryStatement("resultMap_test.selectProject", project);

            Project1 result = (Project1) list.get(0);
            assert result.getName().equals("abc");
            assert result.getFeature().getAge() == 23;
            assert result.getFeature().getDesc().equals("this is desc");
            assert result.getFeature().getTags().contains("CN");
            assert result.getFeature().getTags().contains("EN");
        }
    }

    @Test
    public void args_1_TestCase() throws SQLException, IOException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/dbvisitor_scene/json/json-column-1.xml");
        dalRegistry.loadEntityToSpace(Project1.class);

        try (Connection c = DsUtils.mysqlConn()) {
            DalSession dalSession = new DalSession(c, dalRegistry);
            LambdaTemplate template = dalSession.lambdaTemplate();

            template.getJdbc().execute("drop table if exists `project`");
            template.getJdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project1 = newProject("abc1", Arrays.asList("CN", "EN"));
            Project1 project2 = newProject("abc2", Arrays.asList("CN", "EN"));
            Project1 project3 = newProject("abc3", Arrays.asList("JP", "EN"));
            assert template.insertBySpace(Project1.class).applyEntity(project1, project2, project3).executeSumResult() == 3;

            List<Object> list = dalSession.queryStatement("resultMap_test.selectByJSON", project1);
            assert list.size() == 2;
            assert ((Project1) list.get(0)).getId() == 1;
            assert ((Project1) list.get(1)).getId() == 2;

            Project1 result = (Project1) list.get(0);
            assert result.getId() == 1;
            assert result.getName().equals("abc1");
            assert result.getFeature().getAge() == 23;
            assert result.getFeature().getDesc().equals("this is desc");
            assert result.getFeature().getTags().contains("CN");
            assert result.getFeature().getTags().contains("EN");
        }
    }

    @Test
    public void result_2_TestCase() throws SQLException, IOException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/dbvisitor_scene/json/json-column-2.xml");
        dalRegistry.loadEntityToSpace(Project1.class);

        try (Connection c = DsUtils.mysqlConn()) {
            DalSession dalSession = new DalSession(c, dalRegistry);
            LambdaTemplate template = dalSession.lambdaTemplate();

            template.getJdbc().execute("drop table if exists `project`");
            template.getJdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project = newProject("abc", Arrays.asList("CN", "EN"));
            assert template.insertBySpace(Project1.class).applyEntity(project).executeSumResult() == 1;

            List<Object> list = dalSession.queryStatement("resultMap_test.selectProject", project);

            Project2 result = (Project2) list.get(0);
            assert result.getName().equals("abc");
            assert result.getFeature().getAge() == 23;
            assert result.getFeature().getDesc().equals("this is desc");
            assert result.getFeature().getTags().contains("CN");
            assert result.getFeature().getTags().contains("EN");
        }
    }

    @Test
    public void args_2_TestCase() throws SQLException, IOException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/dbvisitor_scene/json/json-column-2.xml");
        dalRegistry.loadEntityToSpace(Project1.class);

        try (Connection c = DsUtils.mysqlConn()) {
            DalSession dalSession = new DalSession(c, dalRegistry);
            LambdaTemplate template = dalSession.lambdaTemplate();

            template.getJdbc().execute("drop table if exists `project`");
            template.getJdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project1 = newProject("abc1", Arrays.asList("CN", "EN"));
            Project1 project2 = newProject("abc2", Arrays.asList("CN", "EN"));
            Project1 project3 = newProject("abc3", Arrays.asList("JP", "EN"));
            assert template.insertBySpace(Project1.class).applyEntity(project1, project2, project3).executeSumResult() == 3;

            List<Object> list = dalSession.queryStatement("resultMap_test.selectByJSON", project1);
            assert list.size() == 2;
            assert ((Project2) list.get(0)).getId() == 1;
            assert ((Project2) list.get(1)).getId() == 2;

            Project2 result = (Project2) list.get(0);
            assert result.getId() == 1;
            assert result.getName().equals("abc1");
            assert result.getFeature().getAge() == 23;
            assert result.getFeature().getDesc().equals("this is desc");
            assert result.getFeature().getTags().contains("CN");
            assert result.getFeature().getTags().contains("EN");
        }
    }

}
