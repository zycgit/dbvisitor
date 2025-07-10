package net.hasor.dbvisitor.driver;

public interface AdapterReceive {
    boolean responseFailed(AdapterRequest request, Exception e);

    boolean responseResult(AdapterRequest request, AdapterCursor cursor);

    boolean responseUpdateCount(AdapterRequest request, long updateCount);

    boolean responseParameter(AdapterRequest request, String paramName, String paramType, Object value);

    boolean responseNotify(AdapterRequest request);

    boolean responseFinish(AdapterRequest request);

    void responseFinish();
}