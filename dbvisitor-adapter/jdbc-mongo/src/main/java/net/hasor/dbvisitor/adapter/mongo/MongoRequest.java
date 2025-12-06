package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class MongoRequest extends AdapterRequest {
    private final String  commandBody;
    private final boolean preRead;

    public MongoRequest(String commandBody, boolean preRead) {
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
