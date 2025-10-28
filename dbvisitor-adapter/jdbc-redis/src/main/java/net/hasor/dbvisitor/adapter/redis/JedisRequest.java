package net.hasor.dbvisitor.adapter.redis;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class JedisRequest extends AdapterRequest {
    private final String commandBody;

    public JedisRequest(String commandBody) {
        this.commandBody = commandBody;
    }

    public String getCommandBody() {
        return commandBody;
    }
}
