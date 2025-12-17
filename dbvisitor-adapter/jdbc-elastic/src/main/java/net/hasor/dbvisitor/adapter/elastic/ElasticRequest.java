package net.hasor.dbvisitor.adapter.elastic;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class ElasticRequest extends AdapterRequest {
    private final String  commandBody;
    private final boolean preRead;

    public ElasticRequest(String commandBody, boolean preRead) {
        this.commandBody = commandBody;
        this.preRead = preRead;
    }

    public boolean isPreRead() {
        return this.preRead;
    }

    public String getCommandBody() {
        return commandBody;
    }
}
