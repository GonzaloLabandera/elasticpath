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
public class PayerAuthenticationEnrollmentResultImplTest {

	private PayerAuthenticationEnrollmentResultImpl payerAuthenticationEnrollmentResultImpl;

	@Before
	public void setUp() throws Exception {
		payerAuthenticationEnrollmentResultImpl = new PayerAuthenticationEnrollmentResultImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.getRaREQ()'.
	 */
	@Test
	public void testGetPaREQ() {
		assertThat(payerAuthenticationEnrollmentResultImpl.getPaREQ()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.setRaREQ()'.
	 */
	@Test
	public void testSetPaREQ() {
		final String pareq = "test";
		payerAuthenticationEnrollmentResultImpl.setPaREQ(pareq);
		assertThat(payerAuthenticationEnrollmentResultImpl.getPaREQ()).isEqualTo(pareq);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.getAcsURL()'.
	 */
	@Test
	public void testGetAcsURL() {
		assertThat(payerAuthenticationEnrollmentResultImpl.getAcsURL()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.setAcsURL()'.
	 */
	@Test
	public void testSetAcsURL() {
		final String acsURL = "www.citibank.com";
		payerAuthenticationEnrollmentResultImpl.setAcsURL(acsURL);
		assertThat(payerAuthenticationEnrollmentResultImpl.getAcsURL()).isEqualTo(acsURL);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.is3DSecureEnrolled()'.
	 */
	@Test
	public void testIs3DSecureEnrolled() {
		assertThat(payerAuthenticationEnrollmentResultImpl.is3DSecureEnrolled()).isFalse();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.setEnrolled()'.
	 */
	@Test
	public void testSetEnrolled() {
		payerAuthenticationEnrollmentResultImpl.setEnrolled(true);
		assertThat(payerAuthenticationEnrollmentResultImpl.is3DSecureEnrolled()).isTrue();
	}
		
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.getMerchantData()'.
	 */
	@Test
	public void testGetMerchantData() {
		assertThat(payerAuthenticationEnrollmentResultImpl.getMerchantData()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.setMerchantData()'.
	 */
	@Test
	public void testSetMerchantData() {
		final String merchantData = "123456789";
		payerAuthenticationEnrollmentResultImpl.setMerchantData(merchantData);
		assertThat(payerAuthenticationEnrollmentResultImpl.getMerchantData()).isEqualTo(merchantData);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.getTermURL()'.
	 */
	@Test
	public void testGetTermURL() {
		assertThat(payerAuthenticationEnrollmentResultImpl.getTermURL()).isNull();
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.misc.impl.PayerAuthenticationEnrollmentResultImpl.setTermURL()'.
	 */
	@Test
	public void testSetTermURL() {
		final String termURL = "www.elasticapth.com";
		payerAuthenticationEnrollmentResultImpl.setTermURL(termURL);
		assertThat(payerAuthenticationEnrollmentResultImpl.getTermURL()).isEqualTo(termURL);
	}
}
