/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.shopper.impl;

import java.util.UUID;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shopper.dao.ShopperDao;
import com.elasticpath.service.store.StoreService;

/** Service manipulating Shopper. */
public class ShopperServiceImpl implements ShopperService {

	private final BeanFactory beanFactory;

	private final ShopperDao shopperDao;
	private final StoreService storeService;

	/**
	 * Main constructor.
	 *
	 * @param beanFactory bean factory for constructing needed beans.
	 * @param shopperDao Dao used to manipulate and persist the shopping context.
	 * @param storeService store service used to fetch valid store code.
	 */
	public ShopperServiceImpl(final BeanFactory beanFactory, final ShopperDao shopperDao, final StoreService storeService) {
		super();
		this.beanFactory = beanFactory;
		this.shopperDao = shopperDao;
		this.storeService = storeService;
	}

	@Override
	public Shopper findByCustomerGuid(final String customerGuid) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuid(customerGuid);

		if (shopperMemento == null) {
			return null;
		}
		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findByCustomerGuidAndStoreCode(final String customerGuid, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuidAndStoreCode(customerGuid, storeCode);

		if (shopperMemento == null) {
			return null;
		}
		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findByCustomerGuidAndAccountSharedIdAndStore(final String customerGuid, final String accountSharedId, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerGuidAccountSharedIdAndStore(customerGuid, accountSharedId, storeCode);

		if (shopperMemento == null) {
			return null;
		}
		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findByCustomerSharedIdAndStoreCode(final String customerSharedId, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerSharedIdAndStore(customerSharedId, storeCode);

		if (shopperMemento == null) {
			return null;
		}
		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findByCustomerSharedIdAndAccountSharedIdAndStore(final String customerSharedId, final String accountSharedId,
																	final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerSharedIdAndAccountSharedIdAndStore(customerSharedId, accountSharedId, storeCode);

		if (shopperMemento == null) {
			return null;
		}
		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findOrCreateShopper(final Customer customer, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerAndStoreCode(customer, storeCode);

		if (shopperMemento == null) {
			// create a new one.
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			shopperMemento = createAndSaveNewShoppingMementoWithCustomerAndStore(customer, validStoreCode, null);
		}

		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	@Override
	public Shopper findOrCreateShopper(final Customer customer, final Customer account, final String storeCode) {
		ShopperMemento shopperMemento = shopperDao.findByCustomerAccountAndStore(customer, account, storeCode);

		if (shopperMemento == null) {
			final String validStoreCode = storeService.findValidStoreCode(storeCode);
			shopperMemento = createAndSaveNewShoppingMementoWithCustomerAndStore(customer, validStoreCode, account);
		}

		return createNewShopperFromMementoAndStore(shopperMemento, shopperMemento.getStoreCode());
	}

	private ShopperMemento createAndSaveNewShoppingMementoWithCustomerAndStore(final Customer customer, final String storeCode,
																			   final Customer account) {
		ShopperMemento shopperMemento = createShopperMemento();
		shopperMemento.setCustomer(customer);
		if (account != null) {
			shopperMemento.setAccount(account);
		}
		shopperMemento.setStoreCode(storeCode);
		return shopperDao.saveOrUpdate(shopperMemento);
	}

	@Override
	public Shopper createAndSaveShopper(final String storeCode) {
		final ShopperMemento shopperMemento = createShopperMemento();
		final Shopper shopper = createNewShopperFromMementoAndStore(shopperMemento, storeCode);
		shopper.setShopperMemento(shopperDao.saveOrUpdate(shopperMemento));

		return shopper;
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
		final ShopperMemento newShopperMemento = beanFactory.getPrototypeBean(ContextIdNames.SHOPPER_MEMENTO, ShopperMemento.class);
		final String mementoGuid = getNewGuid();
		newShopperMemento.setGuid(mementoGuid);
		return newShopperMemento;
	}

	private String getNewGuid() {
		final UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

}
