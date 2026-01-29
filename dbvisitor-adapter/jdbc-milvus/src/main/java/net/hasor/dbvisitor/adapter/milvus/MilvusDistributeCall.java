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
import java.sql.SQLException;
import net.hasor.cobble.concurrent.future.Future;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser;
import net.hasor.dbvisitor.driver.AdapterReceive;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class MilvusDistributeCall {
    public static Future<?> execMilvusCmd(Future<Object> sync, MilvusCmd milvusCmd, MilvusParser.HintCommandContext h, AdapterRequest request, AdapterReceive receive, int startArgIdx, MilvusConn conn) throws SQLException {
        try {
            MilvusParser.CommandContext command = h.command();

            if (command.useCmd() != null) {
                return MilvusCommandsForDB.execUseCmd(sync, milvusCmd, h, request, receive, startArgIdx);
            }
            if (command.createCmd() != null) {
                return execCreateCmd(sync, milvusCmd, h, command.createCmd(), request, receive, startArgIdx);
            }
            if (command.alterCmd() != null) {
                return execAlterCmd(sync, milvusCmd, h, command.alterCmd(), request, receive, startArgIdx);
            }
            if (command.dropCmd() != null) {
                return execDropCmd(sync, milvusCmd, h, command.dropCmd(), request, receive, startArgIdx);
            }
            if (command.showCmd() != null) {
                return MilvusCommandsForShow.execShowCmd(sync, milvusCmd, h, command.showCmd(), request, receive, startArgIdx);
            }
            if (command.insertCmd() != null) {
                return MilvusCommandsForData.execInsertCmd(sync, milvusCmd, h, command.insertCmd(), request, receive, startArgIdx);
            }
            if (command.deleteCmd() != null) {
                return MilvusCommandsForData.execDeleteCmd(sync, milvusCmd, h, command.deleteCmd(), request, receive, startArgIdx);
            }
            if (command.grantCmd() != null) {
                return MilvusCommandsForUser.execGrantCmd(sync, milvusCmd, h, command.grantCmd(), request, receive, startArgIdx);
            }
            if (command.revokeCmd() != null) {
                return MilvusCommandsForUser.execRevokeCmd(sync, milvusCmd, h, command.revokeCmd(), request, receive, startArgIdx);
            }
            if (command.importCmd() != null) {
                return MilvusCommandsForData.execImportCmd(sync, milvusCmd, h, command.importCmd(), request, receive, startArgIdx);
            }
            if (command.loadCmd() != null) {
                return MilvusCommandsForTable.execLoadCmd(sync, milvusCmd, h, command.loadCmd(), request, receive, startArgIdx);
            }
            if (command.releaseCmd() != null) {
                return MilvusCommandsForTable.execReleaseCmd(sync, milvusCmd, h, command.releaseCmd(), request, receive, startArgIdx);
            }
            if (command.renameCmd() != null) {
                return MilvusCommandsForTable.execRenameCmd(sync, milvusCmd, h, command.renameCmd(), request, receive, startArgIdx);
            }
            if (command.selectCmd() != null) {
                return MilvusCommandsForData.execSelectCmd(sync, milvusCmd, h, command.selectCmd(), request, receive, startArgIdx);
            }

            sync.failed(new SQLException("unknown command."));
            return sync;
        } catch (Exception e) {
            sync.failed(readError(e, request));
            return sync;
        }
    }

    private static Future<?> execCreateCmd(Future<Object> future, MilvusCmd milvusCmd, MilvusParser.HintCommandContext h, MilvusParser.CreateCmdContext createCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        if (createCmd.DATABASE() != null) {
            return MilvusCommandsForDB.execCreateDatabase(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else if (createCmd.USER() != null) {
            return MilvusCommandsForUser.execCreateUser(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else if (createCmd.ROLE() != null) {
            return MilvusCommandsForUser.execCreateRole(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else if (createCmd.TABLE() != null && createCmd.PARTITION() == null && createCmd.INDEX() == null && createCmd.ALIAS() == null) {
            return MilvusCommandsForTable.execCreateTable(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else if (createCmd.PARTITION() != null) {
            return MilvusCommandsForTable.execCreatePartition(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else if (createCmd.INDEX() != null) {
            return MilvusCommandsForTable.execCreateIndex(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else if (createCmd.ALIAS() != null) {
            return MilvusCommandsForTable.execCreateAlias(future, milvusCmd, h, createCmd, request, receive, startArgIdx);
        } else {
            throw new SQLException("Unknown CREATE command");
        }
    }

    private static Future<?> execAlterCmd(Future<Object> future, MilvusCmd milvusCmd, MilvusParser.HintCommandContext h, MilvusParser.AlterCmdContext alterCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        if (alterCmd.DATABASE() != null) {
            return MilvusCommandsForDB.execAlterDatabase(future, milvusCmd, h, alterCmd, request, receive, startArgIdx);
        } else if (alterCmd.ALIAS() != null) {
            return MilvusCommandsForTable.execAlterAlias(future, milvusCmd, h, alterCmd, request, receive, startArgIdx);
        } else {
            throw new SQLException("Unknown ALTER command");
        }
    }

    private static Future<?> execDropCmd(Future<Object> future, MilvusCmd milvusCmd, MilvusParser.HintCommandContext h, MilvusParser.DropCmdContext dropCmd, AdapterRequest request, AdapterReceive receive, int startArgIdx) throws SQLException {
        if (dropCmd.DATABASE() != null) {
            return MilvusCommandsForDB.execDropDatabase(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else if (dropCmd.USER() != null) {
            return MilvusCommandsForUser.execDropUser(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else if (dropCmd.ROLE() != null) {
            return MilvusCommandsForUser.execDropRole(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else if (dropCmd.TABLE() != null) {
            return MilvusCommandsForTable.execDropTable(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else if (dropCmd.PARTITION() != null) {
            return MilvusCommandsForTable.execDropPartition(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else if (dropCmd.INDEX() != null) {
            return MilvusCommandsForTable.execDropIndex(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else if (dropCmd.ALIAS() != null) {
            return MilvusCommandsForTable.execDropAlias(future, milvusCmd, h, dropCmd, request, receive, startArgIdx);
        } else {
            throw new SQLException("Unknown DROP command");
        }
    }

    private static SQLException readError(Exception e, AdapterRequest request) {
        if (e instanceof SQLException) {
            return (SQLException) e;
        } else {
            return new SQLException(e);
        }
    }
}
