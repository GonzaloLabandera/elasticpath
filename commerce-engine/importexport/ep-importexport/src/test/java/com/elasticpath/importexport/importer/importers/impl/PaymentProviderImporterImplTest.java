/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Test for <code>PaymentProviderImporterImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderImporterImplTest {

	private static final String GUID = "GUID";

	private PaymentProviderImporterImpl paymentProviderImporter;

	@Mock
	private DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> domainAdapter;

	@Mock
	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	private SavingStrategy<PaymentProviderConfigDomainProxy, PaymentProviderDTO> mockSavingStrategy;

	/**
	 * SetUps the test.
	 */
	@Before
	public void setUp() {
		paymentProviderImporter = new PaymentProviderImporterImpl();
		paymentProviderImporter.setStatusHolder(new ImportStatusHolder());
		paymentProviderImporter.setPaymentProviderConfigManagementService(paymentProviderConfigManagementService);
		paymentProviderImporter.setPaymentProviderAdapter(domainAdapter);

		mockSavingStrategy = AbstractSavingStrategy.createStrategy(ImportStrategyType.INSERT, new SavingManager<PaymentProviderConfigDomainProxy>() {
			@Override
			public void save(final PaymentProviderConfigDomainProxy persistable) {
				// do nothing
			}

			@Override
			public PaymentProviderConfigDomainProxy update(final PaymentProviderConfigDomainProxy persistable) {
				return null;
			}
		});
	}

	/**
	 * Check an import with non-initialized importer.
	 */
	public void testExecuteNonInitializedImport() {
		assertThatThrownBy(() -> paymentProviderImporter.executeImport(createPaymentProviderDTO()))
				.isInstanceOf(ImportRuntimeException.class)
				.hasMessageStartingWith("IE-30501");
	}

	/**
	 * Check an import of payment providers.
	 */
	@Test
	public void testExecuteImport() {
		ImportConfiguration importConfiguration = new ImportConfiguration();
		importConfiguration.setImporterConfigurationMap(new HashMap<>());

		when(paymentProviderConfigManagementService.findByGuid(GUID)).thenReturn(mock(PaymentProviderConfigDTO.class));

		paymentProviderImporter.initialize(new ImportContext(importConfiguration), mockSavingStrategy);
		paymentProviderImporter.executeImport(createPaymentProviderDTO());

		assertThat(PaymentProviderDTO.ROOT_ELEMENT)
				.isEqualTo(paymentProviderImporter.getImportedObjectName());
		assertThat(paymentProviderImporter.getSavingStrategy())
				.isNotNull();
	}

	/**
	 * Test method for {@link PaymentProviderImporterImpl#findPersistentObject(PaymentProviderDTO)}.
	 */
	@Test
	public void testFindPersistentObjectPaymentProviderDTO() {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = mock(PaymentProviderConfigDTO.class);

		when(paymentProviderConfigManagementService.findByGuid(GUID)).thenReturn(paymentProviderConfigDTO);

		final PaymentProviderConfigDomainProxy expectedPaymentProviderConfigDomainProxy =
				new PaymentProviderConfigDomainProxy(paymentProviderConfigDTO);
		final PaymentProviderConfigDomainProxy paymentProviderConfigDomainProxy =
				new PaymentProviderConfigDomainProxy(paymentProviderImporter.findPersistentObject(createPaymentProviderDTO()));

		assertThat(expectedPaymentProviderConfigDomainProxy).isEqualTo(paymentProviderConfigDomainProxy);
	}

	/**
	 * Test method for {@link com.elasticpath.importexport.importer.importers.impl.PaymentProviderImporterImpl#getDomainAdapter()}.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(domainAdapter)
				.isEqualTo(paymentProviderImporter.getDomainAdapter());
	}

	/**
	 * Test method for
	 * {@link com.elasticpath.importexport.importer.importers.impl.PaymentProviderImporterImpl
	 * #getDtoGuid{@link  com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;})}.
	 */
	@Test
	public void testGetDtoGuidtestFindPersistentObjectPaymentProviderDTO() {
		final PaymentProviderDTO dto = createPaymentProviderDTO();

		assertThat(GUID)
				.isEqualTo(paymentProviderImporter.getDtoGuid(dto));
	}

	/**
	 * Test method for {@link com.elasticpath.importexport.importer.importers.impl.PaymentProviderImporterImpl#getImportedObjectName()}.
	 */
	@Test
	public void testGetImportedObjectName() {
		assertThat(PaymentProviderDTO.ROOT_ELEMENT)
				.isEqualTo(paymentProviderImporter.getImportedObjectName());
	}

	/**
	 * The import classes should at least contain the DTO class we are operating on.
	 */
	@Test
	public void testDtoClass() {
		assertThat(PaymentProviderDTO.class)
				.isEqualTo(paymentProviderImporter.getDtoClass());
	}

	/**
	 * The auxiliary JAXB class list must not be null (can be empty).
	 */
	@Test
	public void testAuxiliaryJaxbClasses() {
		assertThat(paymentProviderImporter.getAuxiliaryJaxbClasses())
				.isNotNull();
	}

	private PaymentProviderDTO createPaymentProviderDTO() {
		PaymentProviderDTO paymentProviderConfigurationDTO = new PaymentProviderDTO();
		paymentProviderConfigurationDTO.setGuid(GUID);
		paymentProviderConfigurationDTO.setPaymentProviderPluginBeanName("paymentProviderPluginBeanName");
		paymentProviderConfigurationDTO.setName("configName");
		paymentProviderConfigurationDTO.setStatus("status");
		return paymentProviderConfigurationDTO;
	}
}
