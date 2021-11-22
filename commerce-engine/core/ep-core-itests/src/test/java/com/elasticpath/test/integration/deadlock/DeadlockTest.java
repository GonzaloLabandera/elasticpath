package com.elasticpath.test.integration.deadlock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.CustomerScenario;

/**
 * Test TransactionProxyFactoryBeanWithDeadlockRetry to ensure it properly recovers from deadlocks.
 */
public class DeadlockTest extends BasicSpringContextTest {
	@Autowired
	private DeadlockService deadlockService;

	private Customer customer;
	private Shopper shopper;

	@Before
	public void setUp() {
		CustomerScenario scenario = getTac().useScenario(CustomerScenario.class);
		customer = scenario.getCustomer();
		shopper = scenario.getShopper();
	}

	/**
	 * Intentionally triggers a deadlock to ensure that the TransactionProxyFactoryBeanWithDeadlockRetry
	 * recovers properly.
	 * @throws ExecutionException if an unexpected exception occurs
	 * @throws InterruptedException if a thread is interrupted
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings({"PMD.PrematureDeclaration"})
	public void testDeadlockRetryHandling() throws ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		Future<Void> thread1Result = executorService.submit(() -> {
			deadlockService.processDeadlockThreadA(customer.getUidPk(), shopper.getUidPk());
			return null;
		});
		Future<Void> thread2Result = executorService.submit(() -> {
			deadlockService.processDeadlockThreadB(customer.getUidPk(), shopper.getUidPk());
			return null;
		});
		thread1Result.get();
		thread2Result.get();
		executorService.shutdown();
	}
}
