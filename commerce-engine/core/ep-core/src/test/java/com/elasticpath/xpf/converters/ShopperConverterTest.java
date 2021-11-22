/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFSession;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class ShopperConverterTest {
	@Mock
	private Shopper shopper;
	@Mock
	private CustomerSession customerSession;
	@Mock
	private Customer customer;
	@Mock
	private Customer account;
	@Mock
	private Store store;
	@Mock
	private XPFSession contextSession;
	@Mock
	private XPFStore contextStore;
	@Mock
	private XPFCustomer contextCustomer;
	@Mock
	private XPFCustomer contextAccount;
	@Mock
	private StoreService storeService;
	@Mock
	private StoreConverter storeConverter;
	@Mock
	private SessionConverter sessionConverter;
	@Mock
	private CustomerConverter customerConverter;

	@InjectMocks
	private ShopperConverter shopperConverter;

	@Test
	public void testConvert() {
		String storeCode = "storeCode";
		when(shopper.getStoreCode()).thenReturn(storeCode);
		when(shopper.getCustomerSession()).thenReturn(customerSession);
		when(shopper.getCustomer()).thenReturn(customer);
		when(shopper.getAccount()).thenReturn(account);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);
		when(storeConverter.convert(store)).thenReturn(contextStore);
		when(sessionConverter.convert(customerSession)).thenReturn(contextSession);
		when(customerConverter.convert(new StoreDomainContext<>(customer, store))).thenReturn(contextCustomer);
		when(customerConverter.convert(new StoreDomainContext<>(account, store))).thenReturn(contextAccount);

		XPFShopper contextShopper = shopperConverter.convert(shopper);
		assertEquals(contextStore, contextShopper.getStore());
		assertEquals(contextSession, contextShopper.getSession());
		assertEquals(contextCustomer, contextShopper.getUser());
		assertEquals(contextAccount, contextShopper.getAccount());
	}
}