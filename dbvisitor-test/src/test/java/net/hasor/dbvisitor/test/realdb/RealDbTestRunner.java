/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public final class RealDbTestRunner {
    private RealDbTestRunner() {
    }

    public static void run(Class<?> testClass) {
        // 使用与 Gradle 一致的 Jupiter 引擎执行指定数据库场景。
        var request = LauncherDiscoveryRequestBuilder.request() //
                .selectors(DiscoverySelectors.selectClass(testClass)) //
                .build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(request, listener);

        // 保留原始失败及退出语义，便于独立 main 入口诊断。
        TestExecutionSummary summary = listener.getSummary();
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            System.err.println(failure.getTestIdentifier().getDisplayName());
            failure.getException().printStackTrace(System.err);
        }
        if (summary.getTestsFoundCount() == 0) {
            throw new AssertionError("No real-database tests found in " + testClass.getName());
        }
        if (summary.getTotalFailureCount() != 0) {
            throw new AssertionError(summary.getTotalFailureCount() + " real-database test(s) failed in " + testClass.getName());
        }
        System.out.println(testClass.getName() + " passed: " + summary.getTestsSucceededCount());
    }
}
