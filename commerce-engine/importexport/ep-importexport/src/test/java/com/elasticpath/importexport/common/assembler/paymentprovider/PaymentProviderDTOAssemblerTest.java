/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.assembler.paymentprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * Tests for PaymentProviderDTOAssembler.
 */
public class PaymentProviderDTOAssemblerTest {

	private static final String DEFAULT_DISPLAY_NAME = "displayName";
	private static final String LANGUAGE = "fr_CA";
	private static final String LOCALIZED_PROPERTIES_VALUE = "localizedValue";

	@Test
	public void paymentProviderDTOAssemblerGetDomainInstanceShouldReturnPaymentProviderConfigDomainProxy() {
		final PaymentProviderDTOAssembler paymentProviderDTOAssembler = new PaymentProviderDTOAssembler();

		assertThat(paymentProviderDTOAssembler.getDomainInstance()).isInstanceOf(PaymentProviderConfigDomainProxy.class);
	}

	@Test
	public void assembleDtoShouldSetIntoPaymentProviderDtoDefaultDisplayNameAndLocalizedNames() {
		final PaymentProviderConfigDomainProxy paymentProviderConfigDomainProxy = createPaymentProviderConfigDomainProxy();
		final PaymentProviderDTO paymentProviderDTO = new PaymentProviderDTO();

		final PaymentProviderDTOAssembler paymentProviderDTOAssembler = new PaymentProviderDTOAssembler();

		paymentProviderDTOAssembler.assembleDto(paymentProviderConfigDomainProxy, paymentProviderDTO);

		assertThat(paymentProviderDTO.getDefaultDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
		assertThat(paymentProviderDTO.getLocalizedNames()).extracting(DisplayValue::getLanguage).containsOnly(LANGUAGE);
		assertThat(paymentProviderDTO.getLocalizedNames()).extracting(DisplayValue::getValue).containsOnly(LOCALIZED_PROPERTIES_VALUE);
	}

	@Test
	public void assembleDomainShouldSetIntoPaymentProviderConfigDomainProxyDefaultDisplayNameAndLocalizedNames() {
		final PaymentProviderConfigDomainProxy paymentProviderConfigDomainProxy = new PaymentProviderConfigDomainProxy();
		final PaymentProviderDTO paymentProviderDTO = createPaymentProviderDTO();

		final PaymentProviderDTOAssembler paymentProviderDTOAssembler = new PaymentProviderDTOAssembler();

		paymentProviderDTOAssembler.assembleDomain(paymentProviderDTO, paymentProviderConfigDomainProxy);

		assertThat(paymentProviderConfigDomainProxy.getDefaultDisplayName()).isEqualTo(DEFAULT_DISPLAY_NAME);
		assertThat(paymentProviderConfigDomainProxy.getLocalizedNames().keySet()).containsOnly(LANGUAGE);
		assertThat(paymentProviderConfigDomainProxy.getLocalizedNames().values()).containsOnly(LOCALIZED_PROPERTIES_VALUE);
	}

	private PaymentProviderConfigDomainProxy createPaymentProviderConfigDomainProxy() {
		final PaymentProviderConfigDomainProxy paymentProviderConfigDomainProxy = new PaymentProviderConfigDomainProxy();
		paymentProviderConfigDomainProxy.setStatus(PaymentProviderConfigurationStatus.ACTIVE);
		paymentProviderConfigDomainProxy.setDefaultDisplayName(DEFAULT_DISPLAY_NAME);
		paymentProviderConfigDomainProxy.setLocalizedNames(Collections.singletonMap(LANGUAGE, LOCALIZED_PROPERTIES_VALUE));
		paymentProviderConfigDomainProxy.setPaymentConfigurationData(Collections.emptyMap());

		return paymentProviderConfigDomainProxy;
	}

	private PaymentProviderDTO createPaymentProviderDTO() {
		final PaymentProviderDTO paymentProviderDTO = new PaymentProviderDTO();
		paymentProviderDTO.setStatus("ACTIVE");
		paymentProviderDTO.setDefaultDisplayName(DEFAULT_DISPLAY_NAME);

		final DisplayValue displayValue = new DisplayValue(LANGUAGE, LOCALIZED_PROPERTIES_VALUE);
		paymentProviderDTO.setLocalizedNames(Collections.singletonList(displayValue));

		return paymentProviderDTO;
	}

}