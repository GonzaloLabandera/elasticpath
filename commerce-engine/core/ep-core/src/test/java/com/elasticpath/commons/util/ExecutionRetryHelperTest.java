package com.elasticpath.commons.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;

/**
 * Tests {@link ExecutionRetryHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionRetryHelperTest {
	private static final Integer TEST_INTEGER = 0;
	public static final String TEST = "test";

	@SuppressWarnings("PMD.AbstractNaming")
	abstract class MockableClass {
		public abstract Integer retriableMethod();
    }

    @Mock
    private MockableClass mockableClass;

	@Test
	public void testRetryNoExceptionWithCount() {
		when(mockableClass.retriableMethod()).thenReturn(0);

		assertEquals(TEST_INTEGER, ExecutionRetryHelper.<Integer>withRetry(()-> mockableClass.retriableMethod(), 2, TEST,
				exception -> { throw new EpServiceException(exception.getLocalizedMessage()); }));
		verify(mockableClass, times(1)).retriableMethod();
	}

	@Test
	@SuppressWarnings("checkstyle:magicnumber")
	public void testRetryWithLimitedNumberOfExceptionRuns() {
		when(mockableClass.retriableMethod())
				.thenThrow(new RuntimeException(TEST))
				.thenThrow(new RuntimeException(TEST))
				.thenReturn(TEST_INTEGER);

		assertEquals(TEST_INTEGER, ExecutionRetryHelper.<Integer>withRetry(()-> mockableClass.retriableMethod(), 3, TEST,
				exception -> { throw new EpServiceException(exception.getLocalizedMessage()); }));
		verify(mockableClass, times(3)).retriableMethod();
	}

	@Test(expected = EpServiceException.class)
	public void testRetryWithExceptionAlways() {
		when(mockableClass.retriableMethod()).thenThrow(new RuntimeException(TEST));

		ExecutionRetryHelper.<Integer>withRetry(()-> mockableClass.retriableMethod(), 2, TEST,
				exception -> { throw new EpServiceException(exception.getLocalizedMessage()); });
	}
}