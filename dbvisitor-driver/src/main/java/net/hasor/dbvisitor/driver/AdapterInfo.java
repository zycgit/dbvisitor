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
