/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.transaction.interceptor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.persistence.api.Persistable;

/**
 * Extension of TransactionInterceptor that retries the operation within a new transaction if a deadlock is detected.
 */
public class TransactionInterceptorWithDeadlockRetry extends TransactionInterceptor {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(TransactionInterceptorWithDeadlockRetry.class);
	private static final Integer RETRY_COUNT = 3;

	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be {@code null}.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface.
		Class<?> targetClass = (invocation.getThis() == null ? null : AopUtils.getTargetClass(invocation.getThis()));

		List<Pair<Persistable, Long>> oids = extractOidsFromPersistables(invocation);
		AtomicInteger deadlockCounter = new AtomicInteger();
		Object result = null;
		while (shouldRetry(deadlockCounter)) {
			try {
				// Adapt to TransactionAspectSupport's invokeWithinTransaction...
				result = invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
				break;
			} catch (final RuntimeException exception) {
				handleException(exception, deadlockCounter);
				resetOidsOnPersistables(oids);
			}
		}
		return result;
	}

	private boolean shouldRetry(final AtomicInteger deadlockCounter) {
		return deadlockCounter.get() < RETRY_COUNT;
	}

	private List<Pair<Persistable, Long>> extractOidsFromPersistables(final MethodInvocation invocation) {
		return Arrays.stream(invocation.getArguments())
				.filter(argument -> argument instanceof Persistable)
				.map(argument -> new Pair<>(((Persistable) argument), ((Persistable) argument).getUidPk()))
				.collect(Collectors.toList());
	}

	private void resetOidsOnPersistables(final List<Pair<Persistable, Long>> oids) {
		oids.forEach(pair -> pair.getFirst().setUidPk(pair.getSecond()));
	}

	private void handleException(final RuntimeException exception, final AtomicInteger deadlockCounter) {
		if (isTopLevelTransactionInterceptor() && isDeadlock(exception)) {
			handleDeadlock(exception, deadlockCounter);
		} else {
			throw exception;
		}
	}

	private void handleDeadlock(final RuntimeException exception, final AtomicInteger deadlockCounter) {
		deadlockCounter.incrementAndGet();
		if (!shouldRetry(deadlockCounter)) {
			throw exception;
		}
		LOG.warn("Deadlock detected - retrying ({} of {}): {}", deadlockCounter, RETRY_COUNT, exception.getMessage());
	}

	private boolean isDeadlock(final RuntimeException exception) {
		if (exception.getMessage() != null && exception.getMessage().startsWith("Deadlock")) {
			return true;
		}
		if (exception.getCause() instanceof RuntimeException) {
			return isDeadlock((RuntimeException) exception.getCause());
		}
		return false;
	}

	/**
	 * When a deadlock exception is thrown inside a method that is wrapped by multiple transaction
	 * interceptors (i.e. two nested txProxyTemplate-wrapped service methods), we need to make sure that we
	 * only retry the outermost wrapped method, not the inner one. When the transaction interceptor detects
	 * an exception in a nested interceptor, it doesn't rollback the exception, it just puts it into rollback-only
	 * mode until the outer interceptor receives the exception.
	 *
	 * This method tests whether we're in the top-level interceptor or not by checking if the transaction
	 * is still active after the exception is thrown.
	 */
	private boolean isTopLevelTransactionInterceptor() {
		return !TransactionSynchronizationManager.isActualTransactionActive();
	}
}
