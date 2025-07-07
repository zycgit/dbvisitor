package net.hasor.dbvisitor.driver;

public interface AdapterReceive {
    void responseFailed(AdapterRequest request, Exception e);

    void responseResult(AdapterRequest request, AdapterCursor cursor);

    void responseUpdateCount(AdapterRequest request, long updateCount);

    void responseParameter(AdapterRequest request, String paramName, String paramType);

    void responseFinish();
}
