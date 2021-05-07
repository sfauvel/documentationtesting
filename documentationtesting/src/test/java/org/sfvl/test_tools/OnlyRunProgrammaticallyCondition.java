package org.sfvl.test_tools;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class OnlyRunProgrammaticallyCondition implements ExecutionCondition {

    static public boolean RUN_TEST_PROGRAMATICALLY = false;

    public static boolean isEnabled() {
        return RUN_TEST_PROGRAMATICALLY;
    }

    public static void enable() {
        RUN_TEST_PROGRAMATICALLY = true;
    }

    public static void disable() {
        RUN_TEST_PROGRAMATICALLY = false;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        if (isEnabled()) {
            return ConditionEvaluationResult.enabled("Test run programmatically");
        } else {
            return ConditionEvaluationResult.disabled("Test could only be launched programmatically.");
        }
    }
}
