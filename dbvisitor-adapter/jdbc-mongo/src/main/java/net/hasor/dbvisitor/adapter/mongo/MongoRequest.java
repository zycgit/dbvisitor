package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class MongoRequest extends AdapterRequest {
    private final String commandBody;

    public MongoRequest(String commandBody) {
        this.commandBody = commandBody;
    }

    public String getCommandBody() {
        return commandBody;
    }
}
