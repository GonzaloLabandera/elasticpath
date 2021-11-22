/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFSession;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

/**
 * Converts {@code com.elasticpath.domain.shopper.Shopper} to {@code com.elasticpath.xpf.connectivity.context.Shopper}.
 */
public class ShopperConverter implements Converter<Shopper, XPFShopper> {

	private StoreService storeService;
	private CustomerConverter xpfCustomerConverter;
	private SessionConverter xpfSessionConverter;
	private StoreConverter xpfStoreConverter;

	@Override
	public XPFShopper convert(final Shopper shopper) {
		Store store = storeService.findStoreWithCode(shopper.getStoreCode());
		XPFSession xpfSession = shopper.getCustomerSession() == null ? null : xpfSessionConverter.convert(shopper.getCustomerSession());
		XPFStore xpfStore = xpfStoreConverter.convert(store);
		XPFCustomer xpfCustomer = shopper.getCustomer() == null ? null
				: xpfCustomerConverter.convert(new StoreDomainContext<>(shopper.getCustomer(), store));
		XPFCustomer xpfAccount = shopper.getAccount() == null ? null
				: xpfCustomerConverter.convert(new StoreDomainContext<>(shopper.getAccount(), store));
		return new XPFShopper(xpfSession, xpfStore, xpfCustomer, xpfAccount);
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setXpfCustomerConverter(final CustomerConverter xpfCustomerConverter) {
		this.xpfCustomerConverter = xpfCustomerConverter;
	}

	public void setXpfSessionConverter(final SessionConverter xpfSessionConverter) {
		this.xpfSessionConverter = xpfSessionConverter;
	}

	public void setXpfStoreConverter(final StoreConverter xpfStoreConverter) {
		this.xpfStoreConverter = xpfStoreConverter;
	}
}
