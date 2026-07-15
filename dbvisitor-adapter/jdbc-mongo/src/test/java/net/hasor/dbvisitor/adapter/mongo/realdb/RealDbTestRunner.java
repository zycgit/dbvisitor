package net.hasor.dbvisitor.adapter.mongo.realdb;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public final class RealDbTestRunner {
    private RealDbTestRunner() {
    }

    public static void run(Class<?> testClass) {
        Result result = JUnitCore.runClasses(testClass);
        for (Failure failure : result.getFailures()) {
            System.err.println(failure);
        }
        if (!result.wasSuccessful()) {
            throw new AssertionError(result.getFailureCount() + " real-database test(s) failed in " + testClass.getName());
        }
        System.out.println(testClass.getName() + " passed: " + result.getRunCount());
    }
}
