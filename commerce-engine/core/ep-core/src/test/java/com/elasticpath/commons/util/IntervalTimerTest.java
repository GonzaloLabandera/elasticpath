/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.awaitility.Duration;
import org.junit.Test;

/**
 * Tests the IntervalTimer class. 
 */
public class IntervalTimerTest {
	
	private static final int ONE_SECOND_AS_MS = 1000;

	/**
	 * Tests that an immediate call to hasIntervalPassed to a timer with a 1 second interval returns false.
	 */
	@Test
	public void testImmediateCallHasIntervalPassedReturnsFalse() {
		IntervalTimer intervalTimer = new IntervalTimer(ONE_SECOND_AS_MS);
		intervalTimer.setStartPointToNow();
		assertThat(intervalTimer.hasIntervalPassed()).as("interval should not have elapsed.").isFalse();
	}

	/**
	 * Tests that a call to hasIntervalPassed to a timer with a 1 second interval returns true,
	 * if the current thread sleeps for 3 seconds before calling hasIntervalPassed.
	 * @throws InterruptedException if the Thread.sleep call gets interrupted (it wont)
	 */
	@Test
	public void testCallHasIntervalPassedReturnsTrueAfterWaitingLongerThanInterval() 
		throws InterruptedException {

		IntervalTimer intervalTimer = new IntervalTimer(ONE_SECOND_AS_MS);
		intervalTimer.setStartPointToNow();

		await().atLeast(Duration.ONE_SECOND).until(() ->
			assertThat(intervalTimer.hasIntervalPassed()).as("interval should have elapsed.").isTrue());
	}
	
	/**
	 * Tests that a call to hasIntervalPassed to a timer with a 1 second interval returns true,
	 * if the current thread sleeps for 3 seconds before calling hasIntervalPassed. 
	 * Then tests that a call to startTiming resets the countdown timer.
	 * @throws InterruptedException if the Thread.sleep call gets interrupted (it wont)
	 */
	@Test
	public void testImmediateCallHasIntervalPassedReturnsFalseOnPreviouslyElapsedTimer() 
		throws InterruptedException {

		IntervalTimer intervalTimer = new IntervalTimer(ONE_SECOND_AS_MS);
		intervalTimer.setStartPointToNow();

		await().atLeast(Duration.ONE_SECOND).until(() ->
			assertThat(intervalTimer.hasIntervalPassed()).as("interval should have elapsed.").isTrue());

		intervalTimer.setStartPointToNow(); //restart the interval clock

		assertThat(intervalTimer.hasIntervalPassed()).as("interval should not have elapsed.").isFalse();
	}

}
