/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static com.elasticpath.commons.constants.ContextIdNames.STORE_PAYMENT_PROVIDER_CONFIG;
import static com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus.ACTIVE;
import static com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus.DISABLED;
import static com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus.DRAFT;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.common.dto.store.StoreDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;
import com.elasticpath.service.store.StoreService;

/**
 * Tests for StoreImporter.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreImporterTest {

	private static final String STORE_CODE = "test_store";
	private static final String PAYMENT_PROVIDER_PLUGIN_CONFIG_GUID = "test_payment_provider_guid";

	private final StoreDTO storeDTO = createStoreDto(STORE_CODE, Collections.singletonList(PAYMENT_PROVIDER_PLUGIN_CONFIG_GUID));

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private StoreService storeService;

	@Mock
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Mock
	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	@Mock
	private ImportStatusHolder importStatusHolder;

	@Mock
	private SavingStrategy<Store, StoreDTO> savingStrategy;

	@InjectMocks
	private StoreImporter storeImporter;

	@Mock
	private StorePaymentProviderConfig storePaymentProviderConfig;

	@Before
	public void setUp() {
		final Store existStoreDomainObject = null;
		final Store newStoreDomainObject = mock(Store.class);
		when(savingStrategy.populateAndSaveObject(existStoreDomainObject, storeDTO)).thenReturn(newStoreDomainObject);

		when(beanFactory.getPrototypeBean(STORE_PAYMENT_PROVIDER_CONFIG, StorePaymentProviderConfig.class)).thenReturn(storePaymentProviderConfig);
		when(storeService.findStoreWithCode(STORE_CODE)).thenReturn(existStoreDomainObject);
		doCallRealMethod().when(importStatusHolder).setImportStatus(any());
	}

	@Test
	public void shouldThrowEpSystemExceptionWhenPaymentProviderConfigDtoIsNull() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = null;
		when(paymentProviderConfigManagementService.findByGuid(PAYMENT_PROVIDER_PLUGIN_CONFIG_GUID)).thenReturn(paymentProviderConfigDTO);

		assertThatThrownBy(() -> storeImporter.executeImport(storeDTO)).isInstanceOf(EpSystemException.class);
	}

	@Test
	public void shouldThrowEpSystemExceptionWhenPaymentProviderConfigDtoHasStatusDraft() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = new PaymentProviderConfigDTO();
		paymentProviderConfigDTO.setStatus(DRAFT);

		when(paymentProviderConfigManagementService.findByGuid(PAYMENT_PROVIDER_PLUGIN_CONFIG_GUID)).thenReturn(paymentProviderConfigDTO);

		assertThatThrownBy(() -> storeImporter.executeImport(storeDTO)).isInstanceOf(EpSystemException.class);
	}

	@Test
	public void shouldThrowEpSystemExceptionWhenPaymentProviderConfigDtoHasStatusDisabled() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = new PaymentProviderConfigDTO();
		paymentProviderConfigDTO.setStatus(DISABLED);

		when(paymentProviderConfigManagementService.findByGuid(PAYMENT_PROVIDER_PLUGIN_CONFIG_GUID)).thenReturn(paymentProviderConfigDTO);

		assertThatThrownBy(() -> storeImporter.executeImport(storeDTO)).isInstanceOf(EpSystemException.class);
	}

	@Test
	public void shouldCallSaveOrUpdateStorePaymentProviderConfigWhenPaymentProviderConfigDtoHasStatusActive() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = new PaymentProviderConfigDTO();
		paymentProviderConfigDTO.setStatus(ACTIVE);

		when(paymentProviderConfigManagementService.findByGuid(PAYMENT_PROVIDER_PLUGIN_CONFIG_GUID)).thenReturn(paymentProviderConfigDTO);

		storeImporter.executeImport(storeDTO);

		verify(storePaymentProviderConfigService).saveOrUpdate(storePaymentProviderConfig);
	}

	private StoreDTO createStoreDto(final String storeCode, final List<String> paymentProviderConfigGuids) {
		final StoreDTO storeDTO = new StoreDTO();
		storeDTO.setCode(storeCode);
		storeDTO.setPaymentProviderConfigGuids(paymentProviderConfigGuids);

		return storeDTO;
	}

}