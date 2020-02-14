/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation.impl.PICInstructionsFieldsValidatingRepositoryImpl;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * Test for
 * {@link PICInstructionsFieldsValidatingRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentInstructionFieldsValidationServiceImplTest {
	@Mock
	private CustomerService customerService;

	@Mock
	private StorePaymentProviderConfigService configService;

	@Mock
	private OrderPaymentApiService orderPaymentApiService;

	@InjectMocks
	private PICInstructionsFieldsValidatingRepositoryImpl validationService;

	@Test
	public void validateTest() {
		final String userId = "userId";
		final String methodId = "methodId";
		final String guidConfig = "guidConfig";

		final Customer customer = mock(Customer.class);
		final StorePaymentProviderConfig config = mock(StorePaymentProviderConfig.class);
		final PICInstructionsFieldsDTO dto = mock(PICInstructionsFieldsDTO.class);
		final List<StructuredErrorMessage> blockingFields = Collections.emptyList();

		when(customerService.findByGuid(userId)).thenReturn(customer);
		when(configService.findByGuid(methodId)).thenReturn(config);
		when(config.getPaymentProviderConfigGuid()).thenReturn(guidConfig);
		when(orderPaymentApiService.getPICInstructionsFields(eq(guidConfig), any(PICFieldsRequestContext.class))).thenReturn(dto);
		when(dto.getStructuredErrorMessages()).thenReturn(blockingFields);

		assertThat(validationService.validate(methodId, Currency.getInstance(Locale.CANADA), Locale.CANADA, userId)).isEqualTo(blockingFields);
		verify(customerService).findByGuid(userId);
		verify(config).getPaymentProviderConfigGuid();
	}
}
