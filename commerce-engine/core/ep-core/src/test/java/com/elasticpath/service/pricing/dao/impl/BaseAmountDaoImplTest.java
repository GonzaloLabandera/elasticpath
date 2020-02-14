/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.pricing.dao.impl;

import static com.elasticpath.service.pricing.dao.impl.BaseAmountDaoImpl.BaseAmountJPQLBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.common.pricing.service.BaseAmountFilter;
import com.elasticpath.common.pricing.service.impl.BaseAmountFilterImpl;
import com.elasticpath.domain.pricing.BaseAmount;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Test for BaseAmountDaoImpl.
 */
public class BaseAmountDaoImplTest {
	
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	
	/**
	 * Test the JPQL query builder.
	 * @throws Exception on error
	 */
	@Test
	public void testQueryBuilder() {
		final BigDecimal quantityDbl = new BigDecimal("2.43");
		final BigDecimal list = new BigDecimal("9.9");
		final BigDecimal sale = new BigDecimal("3.14");
		final String objectGuid = "GUID2";
		final String objectType = "PROD";
		final String priceListDescriptorGuid = "PRICELISTGUID";

		BaseAmountFilter filter = new BaseAmountFilterImpl();
		filter.setPriceListDescriptorGuid(priceListDescriptorGuid);

		BaseAmountJPQLBuilder builder = new BaseAmountJPQLBuilder(filter);

		StringBuilder expectedQuery = new StringBuilder("SELECT baseAmount FROM BaseAmountImpl AS baseAmount WHERE ")
			.append("baseAmount.priceListDescriptorGuid = ?1");

		Object[] expectedParameters = Arrays.asList(priceListDescriptorGuid).toArray();

		assertThat(builder.getQueryString())
			.isEqualTo(expectedQuery.toString());
		assertThat(builder.getQueryParameters())
			.isEqualTo(expectedParameters);

		BaseAmountFilter filter2 = new BaseAmountFilterImpl();
		filter2.setObjectGuid(objectGuid);
		filter2.setQuantity(quantityDbl);
		filter2.setObjectType(objectType);
		filter2.setListValue(list);
		filter2.setPriceListDescriptorGuid(priceListDescriptorGuid);
		filter2.setSaleValue(sale);

		//test all fields
		BaseAmountJPQLBuilder builder2 = new BaseAmountJPQLBuilder(filter2);

		StringBuilder fullExpectedQuery = new StringBuilder("SELECT baseAmount FROM BaseAmountImpl AS baseAmount WHERE ")
			.append("baseAmount.objectGuid = ?1 AND ")
			.append("baseAmount.objectType = ?2 AND ")
			.append("baseAmount.priceListDescriptorGuid = ?3 AND ")
			.append("baseAmount.listValueInternal = ?4 AND ")
			.append("baseAmount.saleValueInternal = ?5 AND ")
			.append("baseAmount.quantityInternal = ?6");

		Object[] fullExpectedParameters = Arrays.asList(objectGuid, objectType, priceListDescriptorGuid, list, sale, quantityDbl).toArray();

		assertThat(builder2.getQueryString())
			.isEqualTo(fullExpectedQuery.toString());
		assertThat(builder2.getQueryParameters())
			.isEqualTo(fullExpectedParameters);
	}
	
	/**
	 * Test that guid/uidpk checking is done before updating a BaseAmount.
	 */
	@Test
	public void testUpdateGuidChecking() {
		final BaseAmount baseAmount = context.mock(BaseAmount.class);
		final PersistenceEngine engine = context.mock(PersistenceEngine.class);
		BaseAmountDaoImpl dao = new BaseAmountDaoImpl() {
			@Override
			public PersistenceEngine getPersistenceEngine() {
				return engine;
			}
		};
		context.checking(new Expectations() { {
			oneOf(baseAmount).isPersisted(); will(returnValue(true));
			oneOf(baseAmount).getGuid(); will(returnValue("GUID"));
			oneOf(engine).saveOrUpdate(baseAmount); will(returnValue(baseAmount));
		} });
		dao.update(baseAmount);
	}
	
	
	/**
	 * Test that if BaseAmount doesn't have a GUID, exception is thrown.
	 * Service won't be able to update a non existing guid.
	 */
	@Test(expected = EpPersistenceException.class)
	public void testUpdateExceptionNoGuid() {
		final BaseAmount baseAmount = context.mock(BaseAmount.class);
		BaseAmountDaoImpl dao = new BaseAmountDaoImpl() {
			@Override
			public BaseAmount findBaseAmountByGuid(final String guid) {
				return null;
			}
		};
		context.checking(new Expectations() { {
			oneOf(baseAmount).isPersisted(); will(returnValue(true));
			oneOf(baseAmount).getGuid(); will(returnValue(null));
		} });
		dao.update(baseAmount);
	}
	
	/**
	 * Test that if BaseAmount doesn't have a UIDPK, exception is thrown.
	 * Service won't be able to update a non persisted object.
	 */
	@Test(expected = EpPersistenceException.class)
	public void testUpdateExceptionNoUid() {
		final BaseAmount baseAmount = context.mock(BaseAmount.class);
		BaseAmountDaoImpl dao = new BaseAmountDaoImpl() {
			@Override
			public BaseAmount findBaseAmountByGuid(final String guid) {
				return null;
			}
		};
		context.checking(new Expectations() { {
			oneOf(baseAmount).isPersisted(); will(returnValue(false));
		} });
		dao.update(baseAmount);
	}
}
