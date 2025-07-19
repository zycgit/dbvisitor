/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
