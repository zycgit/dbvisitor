package net.hasor.scene.types.json;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.types.json.dto.Project1;
import net.hasor.scene.types.json.dto.ProjectFeature;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class JsonColumnLambdaTestCase {

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
    public void insertTestCase() throws SQLException, IOException {
        try (Connection c = DsUtils.mysqlConn()) {
            LambdaTemplate template = new LambdaTemplate(c);
            template.jdbc().execute("drop table if exists `project`");
            template.jdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project = newProject("abc", Arrays.asList("CN", "EN"));
            assert template.insert(Project1.class).applyEntity(project).executeSumResult() == 1;
            assert project.getId() == 1;

            Project1 result = template.query(Project1.class).eq(Project1::getId, 1).queryForObject();
            assert result.getName().equals("abc");
            assert result.getFeature().getAge() == 23;
            assert result.getFeature().getDesc().equals("this is desc");
            assert result.getFeature().getTags().contains("CN");
            assert result.getFeature().getTags().contains("EN");
        }
    }

    @Test
    public void updateTestCase() throws SQLException, IOException {
        try (Connection c = DsUtils.mysqlConn()) {
            LambdaTemplate template = new LambdaTemplate(c);
            template.jdbc().execute("drop table if exists `project`");
            template.jdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project = newProject("abc", Arrays.asList("CN", "EN"));
            template.insert(Project1.class).applyEntity(project).executeSumResult();

            ProjectFeature feature = new ProjectFeature();
            feature.setDesc("this is desc2");
            feature.setTags(Arrays.asList("JP", "FR"));
            assert 1 == template.update(Project1.class).eq(Project1::getId, 1).updateTo(Project1::getFeature, feature).doUpdate();

            Project1 result = template.query(Project1.class).eq(Project1::getId, 1).queryForObject();
            assert result.getName().equals("abc");
            assert result.getFeature().getAge() == null;
            assert result.getFeature().getDesc().equals("this is desc2");
            assert result.getFeature().getTags().size() == 2;
            assert result.getFeature().getTags().contains("JP");
            assert result.getFeature().getTags().contains("FR");
        }
    }

    @Test
    public void whereTestCase() throws SQLException, IOException {
        try (Connection c = DsUtils.mysqlConn()) {
            LambdaTemplate template = new LambdaTemplate(c);
            template.jdbc().execute("drop table if exists `project`");
            template.jdbc().loadSQL("/dbvisitor_scene/project_for_mysql.sql");

            Project1 project1 = newProject("abc1", Arrays.asList("CN", "EN"));
            Project1 project2 = newProject("abc2", Arrays.asList("JP", "FR"));
            template.insert(Project1.class).applyEntity(project1).executeSumResult();
            template.insert(Project1.class).applyEntity(project2).executeSumResult();

            ProjectFeature feature = project2.getFeature();
            Project1 result = template.query(Project1.class).eq(Project1::getFeature, feature).queryForObject();
            assert result.getId() == 2;
            assert result.getName().equals("abc2");
            assert result.getFeature().getAge() == 23;
            assert result.getFeature().getDesc().equals("this is desc");
            assert result.getFeature().getTags().size() == 2;
            assert result.getFeature().getTags().contains("JP");
            assert result.getFeature().getTags().contains("FR");
        }
    }
}
