/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
public class AdapterInfo {
    private String         url;
    private String         userName;
    private AdapterVersion dbVersion;
    private AdapterVersion driverVersion;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public AdapterVersion getDbVersion() {
        return this.dbVersion;
    }

    public void setDbVersion(AdapterVersion dbVersion) {
        this.dbVersion = dbVersion;
    }

    public AdapterVersion getDriverVersion() {
        return this.driverVersion;
    }

    public void setDriverVersion(AdapterVersion driverVersion) {
        this.driverVersion = driverVersion;
    }
}
