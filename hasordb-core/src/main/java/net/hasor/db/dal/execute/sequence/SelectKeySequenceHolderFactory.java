/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.execute.sequence;
import net.hasor.cobble.StringUtils;
import net.hasor.db.dal.execute.AbstractStatementExecute;
import net.hasor.db.dal.execute.KeySequenceHolder;
import net.hasor.db.dal.execute.KeySequenceHolderFactory;
import net.hasor.db.dal.repository.config.SelectKeySqlConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 负责处理 SelectKey 的执行
 * @version : 2021-11-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class SelectKeySequenceHolderFactory implements KeySequenceHolderFactory {
    @Override
    public KeySequenceHolder createHolder(SelectKeySqlConfig keySqlConfig, AbstractStatementExecute<?> selectKeyExecute) {
        return new SelectKeySequenceHolder(keySqlConfig, selectKeyExecute);
    }

    private static class SelectKeySequenceHolder implements KeySequenceHolder {
        private final SelectKeySqlConfig          keySqlConfig;
        private final AbstractStatementExecute<?> selectKeyExecute;

        public SelectKeySequenceHolder(SelectKeySqlConfig keySqlConfig, AbstractStatementExecute<?> selectKeyExecute) {
            this.keySqlConfig = keySqlConfig;
            this.selectKeyExecute = selectKeyExecute;
        }

        public Object processSelectKey(Connection conn, Map<String, Object> parameter) throws SQLException {
            String keyColumn = this.keySqlConfig.getKeyColumn();
            Object resultValue = null;

            if (StringUtils.isBlank(keyColumn)) {
                // maybe is single value.
                resultValue = this.selectKeyExecute.execute(conn, this.keySqlConfig, parameter, null, false, null);
            } else {
                resultValue = this.selectKeyExecute.execute(conn, this.keySqlConfig, parameter, null, false, null, true);
            }

            if (resultValue instanceof List) {
                resultValue = ((List<?>) resultValue).get(0);
            }

            return resultValue;
        }
    }
}