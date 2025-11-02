package net.hasor.dbvisitor.adapter.redis;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class JedisRequest extends AdapterRequest {
    private final String  commandBody;
    private       boolean numKeysCheck = true;

    public JedisRequest(String commandBody) {
        this.commandBody = commandBody;
    }

    public String getCommandBody() {
        return commandBody;
    }

    public boolean isNumKeysCheck() {
        return this.numKeysCheck;
    }

    public void setNumKeysCheck(boolean numKeysCheck) {
        this.numKeysCheck = numKeysCheck;
    }
}
