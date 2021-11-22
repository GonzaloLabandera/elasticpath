package com.elasticpath.test.integration.deadlock;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.ShopperService;

/**
 * Service for testing deadlock scenarios.
 */
public class DeadlockService {
	private static final long DELAY = 1000;

	private CustomerService customerService;
	private ShopperService shopperService;
	private PersistenceEngine persistenceEngine;

	/**
	 * Update customer with uid customerUid and shopper with uid shopperUid in that order.
	 * @param customerUid customer uid to update
	 * @param shopperUid shopper uid to update
	 * @throws InterruptedException if thread is interrupted
	 */
	public void processDeadlockThreadA(final long customerUid, final long shopperUid) throws InterruptedException {
		Customer customer = customerService.get(customerUid);
		Shopper shopper = shopperService.get(shopperUid);
		customer.setSharedId("testA");
		customerService.update(customer);
		persistenceEngine.flush();
		Thread.sleep(DELAY);
		shopper.setGuid("testA");
		shopperService.save(shopper);
	}

	/**
	 * Update shopper with uid shopperUid and customer with uid customerUid in that order.
	 * @param customerUid customer uid to update
	 * @param shopperUid shopper uid to update
	 * @throws InterruptedException if thread is interrupted
	 */
	public void processDeadlockThreadB(final long customerUid, final long shopperUid) throws InterruptedException {
		Shopper shopper = shopperService.get(shopperUid);
		Customer customer = customerService.get(customerUid);
		shopper.setGuid("testB");
		shopperService.save(shopper);
		persistenceEngine.flush();
		Thread.sleep(DELAY);
		customer.setSharedId("testB");
		customerService.update(customer);
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	protected ShopperService getShopperService() {
		return shopperService;
	}

	public void setShopperService(final ShopperService shopperService) {
		this.shopperService = shopperService;
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}
}
