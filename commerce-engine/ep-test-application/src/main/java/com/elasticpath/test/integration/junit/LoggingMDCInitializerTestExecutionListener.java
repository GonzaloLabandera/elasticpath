/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.junit;

import org.apache.log4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Initialize a log4j Mapped Diagnostic Context to improve logging capability.
 *
 * Currently this class simply adds the System Property "testForkNumber" into the MDC which allows us
 * to expose which test fork is logging a specific line, allowing the files to be more easily understood.
 *
 * If the system property "testForkNumber" is not specific a random number is selected and used instead.
 */
public class LoggingMDCInitializerTestExecutionListener extends AbstractTestExecutionListener {

    /**
     * A constant to use as a prefix in log4j logs - to improve understandability.
     */
    private static final String TEST_FORK_NUMBER =
            "fork#" + System.getProperty("testForkNumber", "" + (int) (Math.random() * 1000));

    /**
     * Sets the test fork number to the logging MDC to help trace individual forks' actions.
     * @param testContext ignored
     */
    @Override
    public void prepareTestInstance(TestContext testContext) {
        setTestForkMDC();
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
     * logging, this class sets a MDC value for the key "testForkNumber" which can be included in log4j logs when using
     * '%X{testForkNumber}' as part of the logging pattern.
     * <p>
     */
    public static void setTestForkMDC() {
        // Copy the 'testForkNumber' into the MDC for logging so we can easily split logs
        // when running several forks logging to the same file (use a random value if not specified)
        MDC.put("testForkNumber", TEST_FORK_NUMBER);
    }

    public static String getTestForkNumber() {
        return TEST_FORK_NUMBER;
    }

}