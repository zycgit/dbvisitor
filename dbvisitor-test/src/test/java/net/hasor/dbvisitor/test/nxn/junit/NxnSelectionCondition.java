/*
 * Copyright 2015-2026 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 */
package net.hasor.dbvisitor.test.nxn.junit;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.UniqueId;

/** Selects parameterized invocations after Jupiter has supplied their display names. */
public final class NxnSelectionCondition implements ExecutionCondition {
    public static final String FILTERED = "NxN filter: invocation not selected";
    private static final ThreadLocal<List<Pattern>> FILTERS = new ThreadLocal<>();

    public static void filters(List<Pattern> filters) {
        if (filters == null || filters.isEmpty()) {
            FILTERS.remove();
        } else {
            FILTERS.set(filters);
        }
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        List<Pattern> filters = FILTERS.get();
        if (filters == null || context.getTestMethod().isEmpty()) {
            return ConditionEvaluationResult.enabled("No NxN invocation filter");
        }
        String method = context.getRequiredTestMethod().getName();
        ExtensionContext parent = context.getParent().orElse(null);
        while (parent != null) {
            String segment = UniqueId.parse(parent.getUniqueId()).getLastSegment().getType();
            if (segment.equals("class-template-invocation")) {
                method += "[" + parent.getDisplayName() + "]";
            }
            parent = parent.getParent().orElse(null);
        }
        String segment = UniqueId.parse(context.getUniqueId()).getLastSegment().getType();
        if (segment.equals("test-template-invocation")) {
            method += "[" + context.getDisplayName() + "]";
        }
        String className = context.getRequiredTestClass().getName();
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        String name = method;
        boolean selected = filters.stream().anyMatch(pattern -> pattern.matcher(className).matches()
                || pattern.matcher(simpleName).matches()
                || pattern.matcher(className + "." + name).matches()
                || pattern.matcher(simpleName + "." + name).matches());
        return selected ? ConditionEvaluationResult.enabled("Selected NxN invocation")
                : ConditionEvaluationResult.disabled(FILTERED);
    }
}
