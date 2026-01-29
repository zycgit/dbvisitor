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
package net.hasor.dbvisitor.adapter.milvus;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.*;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

class MilvusCommandsForUser extends MilvusCommands {
    public static Future<?> execCreateUser(Future<Object> future, MilvusCmd milvusCmd, HintCommandContext h, CreateCmdContext createCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) {
        return failed(future, new UnsupportedOperationException("Not implemented yet"));
    }

    public static Future<?> execCreateRole(Future<Object> future, MilvusCmd milvusCmd, HintCommandContext h, CreateCmdContext createCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) {
        return failed(future, new UnsupportedOperationException("Not implemented yet"));
    }

    public static Future<?> execDropUser(Future<Object> future, MilvusCmd milvusCmd, HintCommandContext h, DropCmdContext dropCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) {
        return failed(future, new UnsupportedOperationException("Not implemented yet"));
    }

    public static Future<?> execDropRole(Future<Object> future, MilvusCmd milvusCmd, HintCommandContext h, DropCmdContext dropCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) {
        return failed(future, new UnsupportedOperationException("Not implemented yet"));
    }

    public static Future<?> execGrantCmd(Future<Object> future, MilvusCmd milvusCmd, HintCommandContext h, GrantCmdContext grantCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) {
        return failed(future, new UnsupportedOperationException("Not implemented yet"));
    }

    public static Future<?> execRevokeCmd(Future<Object> future, MilvusCmd milvusCmd, HintCommandContext h, RevokeCmdContext revokeCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) {
        return failed(future, new UnsupportedOperationException("Not implemented yet"));
    }
}
