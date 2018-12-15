/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.tools.sync.job.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.tools.sync.job.Command;
import com.elasticpath.tools.sync.job.JobEntry;
import com.elasticpath.tools.sync.job.TransactionJobUnit;
import com.elasticpath.tools.sync.job.descriptor.TransactionJobDescriptor;
import com.elasticpath.tools.sync.job.descriptor.TransactionJobDescriptorEntry;
import com.elasticpath.tools.sync.job.descriptor.impl.TransactionJobDescriptorImpl;
import com.elasticpath.tools.sync.merge.configuration.EntityLocator;
import com.elasticpath.tools.sync.target.AssociatedDaoAdapter;
import com.elasticpath.tools.sync.target.DaoAdapter;
import com.elasticpath.tools.sync.target.DaoAdapterFactory;

/**
 * Tests that the TransactionJobBuilderImpl class behaves as expected.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionJobBuilderImplTest {

	private static final String COUPON_1 = "coupon1";
	private static final String COUPON_2 = "coupon2";

	@Mock
	private EntityLocator entityLocator;

	private TransactionJobBuilderImpl transactionJobBuilderImpl;

	private DaoAdapterFactory daoAdapterFactory;

	/**
	 * Setup test.
	 */
	@Before
	public void setUp() {
		daoAdapterFactory = new DaoAdapterFactory();
		transactionJobBuilderImpl = new TransactionJobBuilderImpl();
		transactionJobBuilderImpl.setEntityLocator(entityLocator);
		transactionJobBuilderImpl.setDaoAdapterFactory(daoAdapterFactory);
	}

	/**
	 * Tests retrieveObject() method.
	 */
	@Test
	public void testRetrieveObject() {

		final TransactionJobDescriptorEntry descriptorEntry = transactionJobBuilderImpl.createJobDescriptorEntry(Product.class, "dd", Command.REMOVE);

		final Product product = mock(Product.class);
		when(entityLocator.locatePersistence(descriptorEntry.getGuid(), descriptorEntry.getType())).thenReturn(product);

		Persistable result = transactionJobBuilderImpl.getObject(descriptorEntry);
		verify(entityLocator).locatePersistence(descriptorEntry.getGuid(), descriptorEntry.getType());
		assertThat(result)
			.as("The job builder object should be the expected product")
			.isEqualTo(product);
	}

	/**
	 * Test that associated job entries get created when required.
	 */
	@Test
	public void testAssociatedJobEntries() {
		@SuppressWarnings("unchecked") final DaoAdapter<? super Persistable> ruleAdaptor = mock(DaoAdapter.class, "ruleAdaptor");

		@SuppressWarnings("unchecked") final AssociatedDaoAdapter<? super Persistable> couponConfigAdapter = mock(AssociatedDaoAdapter.class, "couponConfigAdaptor");

		@SuppressWarnings("unchecked") final AssociatedDaoAdapter<? super Persistable> couponAdapter = mock(AssociatedDaoAdapter.class, "couponAdaptor");

		final Map<Class<?>, DaoAdapter<? super Persistable>> syncAdapters = new HashMap<>();
		syncAdapters.put(Rule.class, ruleAdaptor);
		syncAdapters.put(CouponConfig.class, couponConfigAdapter);
		syncAdapters.put(Coupon.class, couponAdapter);
		daoAdapterFactory.setSyncAdapters(syncAdapters);

		final TransactionJobDescriptorEntry entry = transactionJobBuilderImpl.createJobDescriptorEntry(Rule.class, "testrule", Command.UPDATE);
		final List<TransactionJobDescriptorEntry> jobDescriptorEntries = new ArrayList<>();
		jobDescriptorEntries.add(entry);

		final TransactionJobDescriptor transactionJobDescriptor = new TransactionJobDescriptorImpl();
		transactionJobDescriptor.setName("test");
		transactionJobDescriptor.setJobDescriptorEntries(jobDescriptorEntries);

		final Collection<Class<?>> assocaitedTypes = new ArrayList<>();
		assocaitedTypes.add(CouponConfig.class);
		assocaitedTypes.add(Coupon.class);

		final CouponConfig couponConfig = mock(CouponConfig.class);
		final List<String> couponConfigGuids = new ArrayList<>();
		couponConfigGuids.add("CONFIG1");

		final Coupon coupon1 = mock(Coupon.class, COUPON_1);
		final Coupon coupon2 = mock(Coupon.class, COUPON_2);
		final List<String> couponCodes = new ArrayList<>();
		couponCodes.add(COUPON_1);
		couponCodes.add(COUPON_2);

		final Rule rule = mock(Rule.class);
		when(entityLocator.locatePersistence(entry.getGuid(), entry.getType())).thenReturn(rule);
		when(entityLocator.locatePersistence("CONFIG1", CouponConfig.class)).thenReturn(couponConfig);
		when(entityLocator.locatePersistence(COUPON_1, Coupon.class)).thenReturn(coupon1);
		when(entityLocator.locatePersistence(COUPON_2, Coupon.class)).thenReturn(coupon2);

		doReturn(assocaitedTypes).when(ruleAdaptor).getAssociatedTypes();

		when(couponConfigAdapter.getAssociatedGuids(entry.getType(), entry.getGuid())).thenReturn(couponConfigGuids);
		doReturn(CouponConfig.class).when(couponConfigAdapter).getType();

		when(couponAdapter.getAssociatedGuids(entry.getType(), entry.getGuid())).thenReturn(couponCodes);
		doReturn(Coupon.class).when(couponAdapter).getType();
		TransactionJobUnit jobUnit = transactionJobBuilderImpl.createTransactionJobUnit(transactionJobDescriptor, true);

		final Collection<JobEntry> entries = new ArrayList<>(jobUnit.createJobEntries());

		final int expectedEntryCount = 4;

		assertThat(entries)
			.as("There should be a total of 4 entries - rule + couponConfig + 2 Coupons")
			.hasSize(expectedEntryCount);

		// unordered at this point
		assertEntriesHasExactly(entries, Rule.class, 1);
		assertEntriesHasExactly(entries, Coupon.class, 2);
		assertEntriesHasExactly(entries, CouponConfig.class, 1);

		verify(entityLocator).locatePersistence(entry.getGuid(), entry.getType());
		verify(entityLocator).locatePersistence("CONFIG1", CouponConfig.class);
		verify(entityLocator).locatePersistence(COUPON_1, Coupon.class);
		verify(entityLocator).locatePersistence(COUPON_2, Coupon.class);
		verify(ruleAdaptor).getAssociatedTypes();
		verify(couponConfigAdapter).getAssociatedGuids(entry.getType(), entry.getGuid());
		verify(couponConfigAdapter).getType();
		verify(couponAdapter).getAssociatedGuids(entry.getType(), entry.getGuid());
	}

	private static <T extends JobEntry> void assertEntriesHasExactly(final Collection<T> actual, final Class<?> clazz, final int number) {
		assertThat(actual)
			.as("Expected <" + number + "> JobEntry with <" + clazz + ">, got: " + actual)
			.filteredOn(jobEntry -> jobEntry.getType().equals(clazz))
			.hasSize(number);
	}
}
