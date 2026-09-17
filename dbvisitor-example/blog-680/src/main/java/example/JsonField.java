/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

public class JsonField {
    @BindTypeHandler(JsonTypeHandler.class)
    public static class Preferences {
        private String       theme;
        private List<String> channels;

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public List<String> getChannels() {
            return channels;
        }

        public void setChannels(List<String> channels) {
            this.channels = channels;
        }
    }

    @Table("blog_user_profile")
    public static class UserProfile {
        @Column(primary = true)
        private Integer             id;
        private Preferences         preferences;
        @Column(typeHandler = JsonTypeHandler.class, specialJavaType = LinkedHashMap.class)
        private Map<String, Object> attributes;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Preferences getPreferences() {
            return preferences;
        }

        public void setPreferences(Preferences preferences) {
            this.preferences = preferences;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:blog_json"); Statement ddl = conn.createStatement()) {
            ddl.executeUpdate("CREATE TABLE blog_user_profile(id INT PRIMARY KEY, preferences VARCHAR(2000), attributes VARCHAR(2000))");
            Preferences prefs = new Preferences();
            prefs.setTheme("dark");
            prefs.setChannels(List.of("email", "app"));
            UserProfile profile = new UserProfile();
            profile.setId(1);
            profile.setPreferences(prefs);
            profile.setAttributes(Map.of("language", "zh-CN"));
            LambdaTemplate lambda = new LambdaTemplate(conn);
            lambda.insert(UserProfile.class).applyEntity(profile).executeSumResult();
            UserProfile loaded = lambda.query(UserProfile.class).eq(UserProfile::getId, 1).queryForObject();
            System.out.println("theme=" + loaded.getPreferences().getTheme());
            System.out.println("channels=" + loaded.getPreferences().getChannels());
            System.out.println("language=" + loaded.getAttributes().get("language"));
            loaded.getPreferences().setTheme("light");
            lambda.update(UserProfile.class).eq(UserProfile::getId, 1).updateTo(UserProfile::getPreferences, loaded.getPreferences()).doUpdate();
            System.out.println("updated=" + lambda.query(UserProfile.class).eq(UserProfile::getId, 1).queryForObject().getPreferences().getTheme());
        }
    }
}
