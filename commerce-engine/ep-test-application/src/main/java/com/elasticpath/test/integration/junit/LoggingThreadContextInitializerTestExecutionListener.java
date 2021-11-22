/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.junit;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Initialize a log4j Thread Context to improve logging capability.
 * <p>
 * Currently this class simply adds the System Property "testForkNumber" into the {@link ThreadContext} which allows us
 * to expose which test fork is logging a specific line, allowing the files to be more easily understood.
 * <p>
 * If the system property "testForkNumber" is not specific a random number is selected and used instead.
 */
public class LoggingThreadContextInitializerTestExecutionListener extends AbstractTestExecutionListener {

    private static final String TEST_FORK_NUMBER_KEY = "testForkNumber";
    /**
     * A constant to use as a prefix in log4j logs - to improve understandability.
     */
    private static final String TEST_FORK_NUMBER =
            "fork#" + System.getProperty(TEST_FORK_NUMBER_KEY, "" + (int) (Math.random() * 1000));

    /**
     * Sets the test fork number to the logging thread context to help trace individual forks' actions.
     * @param testContext ignored
     */
    @Override
    public void prepareTestInstance(TestContext testContext) {
        setTestForkToThreadContext();
    }

    /**
     * @return the order in which to apply this listener (this has high precedence)
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Surefire/failsafe plugins can use multiple forks and we tend to log to a shared log file, leading to intermingled
     * logging, this class sets a value for the key "testForkNumber" into {@link ThreadContext} which can be included in log4j logs when using
     * '%X{testForkNumber}' as part of the logging pattern.
     * <p>
     */
    public static void setTestForkToThreadContext() {
        // Copy the 'testForkNumber' into the logging thread context for logging so we can easily split logs
        // when running several forks logging to the same file (use a random value if not specified)
        ThreadContext.put(TEST_FORK_NUMBER_KEY, TEST_FORK_NUMBER);
    }

    public static String getTestForkNumber() {
        return TEST_FORK_NUMBER;
    }

}