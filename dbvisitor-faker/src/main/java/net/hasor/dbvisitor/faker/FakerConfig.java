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
package net.hasor.dbvisitor.faker;
import net.hasor.cobble.RandomUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.RandomRatio;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.loader.DataLoaderFactory;
import net.hasor.dbvisitor.faker.generator.processor.DefaultTypeProcessorFactory;
import net.hasor.dbvisitor.faker.meta.JdbcFetchMeta;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

/**
 * Faker 全局配置
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerConfig {
    // generator
    private       ClassLoader                 classLoader;
    private       TypeHandlerRegistry         typeRegistry;
    private       DataLoaderFactory           dataLoaderFactory;
    private       SqlDialect                  sqlDialect;
    private       JdbcFetchMeta               customFetchMeta;
    private       DefaultTypeProcessorFactory typeProcessorFactory;
    private       String                      policy;
    private       boolean                     useQualifier;
    private       boolean                     keyChanges;
    private       boolean                     printSql;
    // one trans
    private       int                         minBatchSizePerOps;
    private       int                         maxBatchSizePerOps;
    private final RandomRatio<OpsType>        opsRatio;
    private       int                         minOpsCountPerTransaction;
    private       int                         maxOpsCountPerTransaction;
    // trans stream
    private       boolean                     transaction;
    private       int                         minPausePerTransactionMs;
    private       int                         maxPausePerTransactionMs;
    // worker
    private       ThreadFactory               threadFactory;
    private       int                         queueCapacity;
    private       int                         writeQps;
    private       int                         queryTimeout;
    private final Set<String>                 ignoreErrors;
    private       boolean                     ignoreAnyErrors;

    public FakerConfig() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.typeRegistry = TypeHandlerRegistry.DEFAULT;
        this.dataLoaderFactory = null;
        this.sqlDialect = null;
        this.typeProcessorFactory = null;
        this.useQualifier = true;
        //
        this.minBatchSizePerOps = 2;
        this.maxBatchSizePerOps = 5;
        this.opsRatio = RatioUtils.passerByConfig("INSERT#30;UPDATE#30;DELETE#30");
        this.minOpsCountPerTransaction = 5;
        this.maxOpsCountPerTransaction = 10;
        //
        this.transaction = true;
        this.minPausePerTransactionMs = 0;
        this.maxPausePerTransactionMs = 0;
        //
        this.queueCapacity = 4096;
        this.writeQps = -1;
        this.queryTimeout = -1;
        this.ignoreErrors = new HashSet<>(Arrays.asList("Duplicate", "duplicate"));
        this.ignoreAnyErrors = false;
    }

    public int randomOpsCountPerTrans() {
        return RandomUtils.nextInt(Math.min(1, this.minOpsCountPerTransaction), Math.max(1, this.maxOpsCountPerTransaction));
    }

    public int randomPausePerTransactionMs() {
        return RandomUtils.nextInt(Math.min(1, this.minPausePerTransactionMs), Math.max(1, this.maxPausePerTransactionMs));
    }

    public int randomBatchSizePerOps() {
        return RandomUtils.nextInt(Math.min(1, this.minBatchSizePerOps), Math.max(1, this.maxBatchSizePerOps));
    }

    public OpsType randomOps() {
        return this.opsRatio.getByRandom();
    }

    public boolean ignoreError(Exception e) {
        if (this.ignoreAnyErrors) {
            return true;
        }

        if (this.ignoreErrors.isEmpty()) {
            return false;
        }

        for (String term : this.ignoreErrors) {
            if (StringUtils.containsIgnoreCase(e.getMessage(), term)) {
                return true;
            }
        }
        return false;
    }

    public void addIgnoreError(String keyWords) {
        if (StringUtils.isNotBlank(keyWords)) {
            this.ignoreErrors.add(keyWords);
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public TypeHandlerRegistry getTypeRegistry() {
        return typeRegistry;
    }

    public void setTypeRegistry(TypeHandlerRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public DataLoaderFactory getDataLoaderFactory() {
        return dataLoaderFactory;
    }

    public void setDataLoaderFactory(DataLoaderFactory dataLoaderFactory) {
        this.dataLoaderFactory = dataLoaderFactory;
    }

    public String getPolicy() {
        return this.policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public boolean isUseQualifier() {
        return useQualifier;
    }

    public void setUseQualifier(boolean useQualifier) {
        this.useQualifier = useQualifier;
    }

    public boolean isKeyChanges() {
        return keyChanges;
    }

    public void setKeyChanges(boolean keyChanges) {
        this.keyChanges = keyChanges;
    }

    public boolean isPrintSql() {
        return printSql;
    }

    public void setPrintSql(boolean printSql) {
        this.printSql = printSql;
    }

    public SqlDialect getSqlDialect() {
        return sqlDialect;
    }

    public void setSqlDialect(SqlDialect sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    public JdbcFetchMeta getCustomFetchMeta() {
        return customFetchMeta;
    }

    public void setCustomFetchMeta(JdbcFetchMeta customFetchMeta) {
        this.customFetchMeta = customFetchMeta;
    }

    public DefaultTypeProcessorFactory getTypeProcessorFactory() {
        return typeProcessorFactory;
    }

    public void setTypeProcessorFactory(DefaultTypeProcessorFactory typeProcessorFactory) {
        this.typeProcessorFactory = typeProcessorFactory;
    }

    public RandomRatio<OpsType> getOpsRatio() {
        return opsRatio;
    }

    public void setOpsRatio(String opsRatio) {
        this.opsRatio.clearRatio();
        RatioUtils.fillByConfig(opsRatio, this.opsRatio);
    }

    public int getMinBatchSizePerOps() {
        return minBatchSizePerOps;
    }

    public void setMinBatchSizePerOps(int minBatchSizePerOps) {
        this.minBatchSizePerOps = minBatchSizePerOps;
    }

    public int getMaxBatchSizePerOps() {
        return maxBatchSizePerOps;
    }

    public void setMaxBatchSizePerOps(int maxBatchSizePerOps) {
        this.maxBatchSizePerOps = maxBatchSizePerOps;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public int getMinPausePerTransactionMs() {
        return minPausePerTransactionMs;
    }

    public void setMinPausePerTransactionMs(int minPausePerTransactionMs) {
        this.minPausePerTransactionMs = minPausePerTransactionMs;
    }

    public int getMaxPausePerTransactionMs() {
        return maxPausePerTransactionMs;
    }

    public void setMaxPausePerTransactionMs(int maxPausePerTransactionMs) {
        this.maxPausePerTransactionMs = maxPausePerTransactionMs;
    }

    public int getMinOpsCountPerTransaction() {
        return minOpsCountPerTransaction;
    }

    public void setMinOpsCountPerTransaction(int minOpsCountPerTransaction) {
        this.minOpsCountPerTransaction = minOpsCountPerTransaction;
    }

    public int getMaxOpsCountPerTransaction() {
        return maxOpsCountPerTransaction;
    }

    public void setMaxOpsCountPerTransaction(int maxOpsCountPerTransaction) {
        this.maxOpsCountPerTransaction = maxOpsCountPerTransaction;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getWriteQps() {
        return writeQps;
    }

    public void setWriteQps(int writeQps) {
        this.writeQps = writeQps;
    }

    public boolean isIgnoreAnyErrors() {
        return ignoreAnyErrors;
    }

    public void setIgnoreAnyErrors(boolean ignoreAnyErrors) {
        this.ignoreAnyErrors = ignoreAnyErrors;
    }
}