/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.shopper.impl;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shopper.dao.ShopperDao;
import com.elasticpath.service.store.StoreService;

/** Service manipulating Shopper. */
public class ShopperServiceImpl implements ShopperService {

	private BeanFactory beanFactory;
	private ShopperDao shopperDao;
	private StoreService storeService;
	private CustomerService customerService;

	@Override
	public Shopper findOrCreateShopper(final Customer customer, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuidAndStore(customer.getGuid(), storeCode);

		if (shopperMemento == null) {
			// create a new one.
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			shopperMemento = createAndSaveNewShoppingMementoWithCustomerAndStore(customer, null, validStoreCode);
		}

		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findOrCreateShopper(final String customerGuid, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuidAndStore(customerGuid, storeCode);

		if (shopperMemento == null) {
			// create a new one.
			final Customer customer = customerService.findByGuid(customerGuid);
			if (customer == null) {
				throw new EpServiceException(String.format("Customer with guid %s not found", customerGuid));
			}
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			shopperMemento = createAndSaveNewShoppingMementoWithCustomerAndStore(customer, null, validStoreCode);
		}

		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findOrCreateShopper(final Customer customer, final Customer account, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuidAccountSharedIdAndStore(customer.getGuid(), account.getSharedId(), storeCode);

		if (shopperMemento == null) {
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			shopperMemento = createAndSaveNewShoppingMementoWithCustomerAndStore(customer, account, validStoreCode);
		}

		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findOrCreateShopper(final String customerGuid, final String accountSharedId, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuidAccountSharedIdAndStore(customerGuid, accountSharedId, storeCode);

		if (shopperMemento == null) {
			final Customer customer = customerService.findByGuid(customerGuid);
			if (customer == null) {
				throw new EpServiceException(String.format("Customer with guid %s not found", customerGuid));
			}
			final Customer account = customerService.findBySharedId(accountSharedId, CustomerType.ACCOUNT);
			if (account == null) {
				throw new EpServiceException(String.format("Account with shared ID %s not found", accountSharedId));
			}
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			shopperMemento = createAndSaveNewShoppingMementoWithCustomerAndStore(customer, account, validStoreCode);
		}

		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	private ShopperMemento createAndSaveNewShoppingMementoWithCustomerAndStore(final Customer customer, final Customer account,
																			   final String storeCode) {
		ShopperMemento shopperMemento = createShopperMemento();
		shopperMemento.setCustomer(customer);
		if (account != null) {
			shopperMemento.setAccount(account);
		}
		shopperMemento.setStoreCode(storeCode);
		return shopperDao.saveOrUpdate(shopperMemento);
	}

	@Override
	public Shopper get(final long uid) {
		final ShopperMemento retrievedShopperMemento = shopperDao.get(uid);
		if (retrievedShopperMemento == null) {
			return null;
		}

		return createNewShopperFromMemento(retrievedShopperMemento);
	}

	@Override
	public Shopper save(final Shopper shopper) {
		if (shopper != null) {
			final ShopperMemento persistedShopperMemento = shopperDao.saveOrUpdate(shopper.getShopperMemento());
			shopper.setShopperMemento(persistedShopperMemento);

			return shopper;
		}
		return null;
	}

	@Override
	public void remove(final Shopper shopper) {
		if (shopper != null && shopper.isPersisted()) {
			shopperDao.remove(shopper.getShopperMemento());
		}
	}

	private Shopper createNewShopperFromMemento(final ShopperMemento shopperMemento) {
		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	private Shopper createNewShopperFromMementoAndStore(final ShopperMemento shopperMemento, final String storeCode) {
		final Shopper shopper = beanFactory.getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		shopper.setShopperMemento(shopperMemento);
		shopper.setStoreCode(storeCode);

		return shopper;
	}

	private ShopperMemento createShopperMemento() {
		return beanFactory.getPrototypeBean(ContextIdNames.SHOPPER_MEMENTO, ShopperMemento.class);
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected ShopperDao getShopperDao() {
		return shopperDao;
	}

	public void setShopperDao(final ShopperDao shopperDao) {
		this.shopperDao = shopperDao;
	}

	protected StoreService getStoreService() {
		return storeService;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}
}
