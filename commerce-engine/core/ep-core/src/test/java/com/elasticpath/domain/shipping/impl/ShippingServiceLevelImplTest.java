/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.shipping.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.BrandLocalizedPropertyValueImpl;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.shipping.ShippingCostCalculationMethod;
import com.elasticpath.domain.shipping.ShippingRegion;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;

/** Test cases for <code>ShippingServiceLevelImpl</code>. */
public class ShippingServiceLevelImplTest {

	private ShippingServiceLevelImpl shippingServiceLevelImpl;

	private static final Locale DEFAULT_LOCALE = Locale.US;
	private static final String DISPLAYNAME_DEFAULT_LOCALE = "defaultDisplayName";
	private static final Locale OTHER_LOCALE = Locale.GERMANY;
	private static final String DISPLAYNAME_OTHER_LOCALE = "otherDisplayName";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Before
	public void setUp() throws Exception {
		this.shippingServiceLevelImpl = new ShippingServiceLevelImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.shipping.impl.ShippingServiceLevelImpl.getShippingRegion()'.
	 */
	@Test
	public void testGetSetShippingRegion() {
		final ShippingRegion shippingRegion = new ShippingRegionImpl();
		this.shippingServiceLevelImpl.setShippingRegion(shippingRegion);
		assertEquals(shippingRegion, this.shippingServiceLevelImpl.getShippingRegion());
	}

	/**
	 * Test method for 'com.elasticpath.domain.shipping.impl.ShippingServiceLevelImpl.getStore()'.
	 */
	@Test
	public void testGetSetStore() {
		final Store store = new StoreImpl();
		store.setCode("some code");
		this.shippingServiceLevelImpl.setStore(store);
		assertEquals(store, this.shippingServiceLevelImpl.getStore());
	}

	/**
	 * Test method for 'com.elasticpath.domain.shipping.impl.ShippingServiceLevelImpl.getShippingCostCalculationMethod()'.
	 */
	@Test
	public void testGetSetShippingCostCalculationMethod() {
		final ShippingCostCalculationMethod shippingCostCalculationMethod = new FixedBaseAndOrderTotalPercentageMethodImpl();
		this.shippingServiceLevelImpl.setShippingCostCalculationMethod(shippingCostCalculationMethod);
		assertEquals(shippingCostCalculationMethod, this.shippingServiceLevelImpl.getShippingCostCalculationMethod());
	}

	/**
	 * Test method for 'com.elasticpath.domain.shipping.impl.ShippingServiceLevelImpl.getCarrier()'.
	 */
	@Test
	public void testGetSetCarrier() {
		final String testCarrier = "Fed Ex";
		this.shippingServiceLevelImpl.setCarrier(testCarrier);
		assertEquals(testCarrier, this.shippingServiceLevelImpl.getCarrier());
	}
	
	private LocalizedProperties createLocalizedPropertiesWithDisplayNameInDefaultLocale() {
		//give it a reference to the Utility class so that it can broaden the locale
		LocalizedProperties localizedProperties = new LocalizedPropertiesImpl() {
			private static final long serialVersionUID = 7789997879611928448L;

			@Override
			protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
				return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
			}

			@Override
			public Utility getUtility() {
				return new UtilityImpl();
			}
		};
		localizedProperties.setValue(ShippingServiceLevel.LOCALIZED_PROPERTY_NAME, DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
		return localizedProperties;
	}
	
	private LocalizedProperties createLocalizedPropertiesWithDisplayNameInDefaultLocaleAndOtherLocale() {
		LocalizedProperties localizedProperties = new LocalizedPropertiesImpl() {
			private static final long serialVersionUID = 406419152977594590L;

			@Override
			protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
				return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
			}
		};
		localizedProperties.setValue(ShippingServiceLevel.LOCALIZED_PROPERTY_NAME, DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
		localizedProperties.setValue(ShippingServiceLevel.LOCALIZED_PROPERTY_NAME, OTHER_LOCALE, DISPLAYNAME_OTHER_LOCALE);
		return localizedProperties;
	}
	
	private ShippingServiceLevelImpl createShippingServiceLevelImplWithDisplayNameDefaultLocaleOnly() {
		return createShippingServiceLevelImplForTesting(createLocalizedPropertiesWithDisplayNameInDefaultLocale());
	}
	
	private ShippingServiceLevelImpl createShippingServiceLevelImplWithDisplayNameInBothLocales() {
		return createShippingServiceLevelImplForTesting(createLocalizedPropertiesWithDisplayNameInDefaultLocaleAndOtherLocale());
	}
	
	private ShippingServiceLevelImpl createShippingServiceLevelImplForTesting(final LocalizedProperties localizedProperties) {
		return new ShippingServiceLevelImpl() {
			private static final long serialVersionUID = 6032980036419946630L;
			
			@Override
			public LocalizedProperties getLocalizedProperties() {
				return localizedProperties;
			}
			
			@Override
			public Locale getStoreDefaultLocale() {
				return DEFAULT_LOCALE;
			}
		};
	}
	
	/**
	 * Test that getDisplayName falls back if necessary, but not if it has been forbidden.
	 */
	@Test
	public void testGetDisplayNameFallsBackIfNecessaryButNotIfForbidden() {
		ShippingServiceLevelImpl shippingServiceLevelImpl = createShippingServiceLevelImplWithDisplayNameDefaultLocaleOnly();
		assertEquals("Should fall back if necessary", DISPLAYNAME_DEFAULT_LOCALE, shippingServiceLevelImpl.getDisplayName(OTHER_LOCALE, true));
		assertEquals("Should not fall back if it's forbidden", null, shippingServiceLevelImpl.getDisplayName(OTHER_LOCALE, false));
	}

	/**
	 * Test that getDisplayName doesn't fall back if a match is found
	 * for the requested locale.
	 */
	@Test
	public void testGetDisplayNameDoesNotFallBackIfNotNecessary() {
		ShippingServiceLevelImpl shippingServiceLevelImpl = createShippingServiceLevelImplWithDisplayNameInBothLocales();
		assertEquals("Should not fall back if not necessary", DISPLAYNAME_OTHER_LOCALE, shippingServiceLevelImpl.getDisplayName(OTHER_LOCALE, true));
	}
	
	/**
	 */
	@Test
	public void testIsApplicableReturnsFalseWhenInactive() {
		ShippingServiceLevel shippingServiceLevel = new ShippingServiceLevelImpl();
		shippingServiceLevel.setEnabled(false);

		String anyStoreCode = "doesn't matter store code";
		Address anyAddress = context.mock(Address.class, "doesn't matter address");
		assertFalse(shippingServiceLevel.isApplicable(anyStoreCode, anyAddress));
	}

	/**
	 */
	@Test
	public void testIsApplicableReturnsFalseWhenStoreCodeMismatch() {
		ShippingServiceLevel shippingServiceLevel = new ShippingServiceLevelImpl();
		shippingServiceLevel.setEnabled(true);
		final Store store = createStore("test store code");
		shippingServiceLevel.setStore(store);

		Address anyAddress = context.mock(Address.class, "doesn't matter address");
		assertFalse(shippingServiceLevel.isApplicable("non-matching store code", anyAddress));
	}

	/**
	 */
	@Test
	public void testIsApplicableReturnsFalseWhenAddressNotInRegion() {
		ShippingServiceLevel shippingServiceLevel = new ShippingServiceLevelImpl();
		shippingServiceLevel.setEnabled(true);

		final String storeCode = "test store code";
		final Store store = createStore(storeCode);
		shippingServiceLevel.setStore(store);

		final ShippingRegion shippingRegion = createShippingRegion(false);
		shippingServiceLevel.setShippingRegion(shippingRegion);

		Address address = createAddress("US", "WA");
		assertFalse(shippingServiceLevel.isApplicable(storeCode, address));
	}

	/**
	 */
	@Test
	public void testIsApplicableReturnsTrueWhenEnabledMatchingStoreAndInRegion() {
		ShippingServiceLevel shippingServiceLevel = new ShippingServiceLevelImpl();
		shippingServiceLevel.setEnabled(true);

		final String storeCode = "test store code";
		final Store store = createStore(storeCode);
		shippingServiceLevel.setStore(store);

		final ShippingRegion shippingRegion = createShippingRegion(true);
		shippingServiceLevel.setShippingRegion(shippingRegion);

		Address address = createAddress("US", "WA");
		assertTrue(shippingServiceLevel.isApplicable(storeCode, address));
	}

	private Store createStore(final String storeCode) {
		final Store store = context.mock(Store.class);
		context.checking(new Expectations() {
			{
				oneOf(store).getCode();
				will(returnValue(storeCode));
			}
		});
		return store;
	}

	private Address createAddress(final String country, final String subCountry) {
		final Address address = context.mock(Address.class);
		context.checking(new Expectations() {
			{
				allowing(address).getCountry();
				will(returnValue(country));

				allowing(address).getSubCountry();
				will(returnValue(subCountry));
			}
		});
		return address;
	}

	private ShippingRegion createShippingRegion(final boolean inShippingRegion) {
		final ShippingRegion shippingRegion = context.mock(ShippingRegion.class);
		context.checking(new Expectations() {
			{
				oneOf(shippingRegion).isInShippingRegion(with(any(Address.class)));
				will(returnValue(inShippingRegion));
			}
		});
		return shippingRegion;
	}

}
