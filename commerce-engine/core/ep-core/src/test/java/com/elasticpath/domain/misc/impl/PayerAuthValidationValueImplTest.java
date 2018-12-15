/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.misc.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test of the Object on Domain/Misc.
 */

public class PayerAuthValidationValueImplTest {
	private PayerAuthValidationValueImpl payerAuthValidationValueImpl;

	@Before
	public void setUp() throws Exception {
		payerAuthValidationValueImpl = new PayerAuthValidationValueImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getAAV()'.
	 */
	@Test
	public void testGetAAV() {
		assertThat(payerAuthValidationValueImpl.getAAV()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setAAV()'.
	 */
	@Test
	public void testSetAAV() {
		final String aav = "test";
		payerAuthValidationValueImpl.setAAV(aav);
		assertThat(payerAuthValidationValueImpl.getAAV()).isEqualTo(aav);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getCAVV()'.
	 */
	@Test
	public void testGetCAVV() {
		assertThat(payerAuthValidationValueImpl.getCAVV()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setCAVV()'.
	 */
	@Test
	public void testSetCAVV() {
		final String cavv = "test cavv";
		payerAuthValidationValueImpl.setCAVV(cavv);
		assertThat(payerAuthValidationValueImpl.getCAVV()).isEqualTo(cavv);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getCommerceIndicator()'.
	 */
	@Test
	public void testGetCommerceIndicator() {
		assertThat(payerAuthValidationValueImpl.getCommerceIndicator()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setCommerceIndicator()'.
	 */
	@Test
	public void testSetCommerceIndicator() {
		final String commerceIndicator = "test CommerceIndicator";
		payerAuthValidationValueImpl.setCommerceIndicator(commerceIndicator);
		assertThat(payerAuthValidationValueImpl.getCommerceIndicator()).isEqualTo(commerceIndicator);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getECI()'.
	 */
	@Test
	public void testGetECI() {
		assertThat(payerAuthValidationValueImpl.getECI()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setECI()'.
	 */
	@Test
	public void testSetECI() {
		final String eci = "test ECI";
		payerAuthValidationValueImpl.setECI(eci);
		assertThat(payerAuthValidationValueImpl.getECI()).isEqualTo(eci);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getXID()'.
	 */
	@Test
	public void testGetXID() {
		assertThat(payerAuthValidationValueImpl.getXID()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setXID()'.
	 */
	@Test
	public void testSetXID() {
		final String xid = "test XID";
		payerAuthValidationValueImpl.setXID(xid);
		assertThat(payerAuthValidationValueImpl.getXID()).isEqualTo(xid);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.isValidated()'.
	 */
	@Test
	public void testIsValidated() {
		assertThat(payerAuthValidationValueImpl.isValidated()).isFalse();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setValidated()'.
	 */
	@Test
	public void testSetValidated() {
		payerAuthValidationValueImpl.setValidated(true);
		assertThat(payerAuthValidationValueImpl.isValidated()).isTrue();
	}
		
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getPaRES()'.
	 */
	@Test
	public void testGetPaRES() {
		assertThat(payerAuthValidationValueImpl.getPaRES()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setPaRES()'.
	 */
	@Test
	public void testSetMerchantData() {
		final String paRES = "pares test";
		payerAuthValidationValueImpl.setPaRES(paRES);
		assertThat(payerAuthValidationValueImpl.getPaRES()).isEqualTo(paRES);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.getUcafCollectionIndicator()'.
	 */
	@Test
	public void testGetUcafCollectionIndicator() {
		assertThat(payerAuthValidationValueImpl.getUcafCollectionIndicator()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthValidationValueImpl.setUcafCollectionIndicator()'.
	 */
	@Test
	public void testSetTermURL() {
		final String ucafCollectionIndicator = "UcafCollectionIndicator test";
		payerAuthValidationValueImpl.setUcafCollectionIndicator(ucafCollectionIndicator);
		assertThat(payerAuthValidationValueImpl.getUcafCollectionIndicator()).isEqualTo(ucafCollectionIndicator);
	}
}
