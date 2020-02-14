/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.ExporterConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Tests <code>PaymentProviderExporterImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentProviderExporterImplTest {

	private static final String GUID1 = "GUID1";
	private static final String GUID1DNE = "GUID1-DNE";
	private static final String GUID2 = "GUID2";
	private static final String GUID3 = "GUID3";
	private static final int THREE = 3;
	@InjectMocks
	private PaymentProviderExporterImpl paymentProviderExporterImpl;
	@Mock
	private ImportExportSearcher importExportSearcher;
	@Mock
	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;
	@Mock
	private DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> paymentProviderAdapter;
	private ExportContext exportContext;

	/**
	 * All guids found during initialization should be exportable.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Test
	public void testInitialize() throws ConfigurationException {
		final PaymentProviderConfigDTO paymentProviderConfiguration1 = new PaymentProviderConfigDTO();
		paymentProviderConfiguration1.setGuid(GUID1);
		final PaymentProviderConfigDTO paymentProviderConfiguration2 = new PaymentProviderConfigDTO();
		paymentProviderConfiguration2.setGuid(GUID2);
		final PaymentProviderConfigDTO paymentProviderConfiguration3 = new PaymentProviderConfigDTO();
		paymentProviderConfiguration3.setGuid(GUID3);
		final List<PaymentProviderConfigDTO> paymentProviderConfigurations =
				Arrays.asList(paymentProviderConfiguration1, paymentProviderConfiguration2, paymentProviderConfiguration3);
		final List<String> foundGuids = Arrays.asList(GUID1, GUID2, GUID3);

		when(paymentProviderConfigManagementService.findAll()).thenReturn(paymentProviderConfigurations);

		exportContext = new ExportContext(new ExportConfiguration(), new SearchConfiguration());
		paymentProviderExporterImpl.initialize(exportContext);

		assertThat(CollectionUtils.isEqualCollection(foundGuids, paymentProviderExporterImpl.getListExportableIDs()))
				.isTrue();
	}

	/**
	 * Searching for a guids should return all guids regardless whether they were found during initialization.
	 *
	 * @throws ConfigurationException in case of errors
	 */
	@Test
	public void testFindByGuid() throws ConfigurationException {
		testInitialize();

		final PaymentProviderConfigDTO paymentProviderConfiguration1 = mock(PaymentProviderConfigDTO.class, "paymentProviderConfig-1");
		when(paymentProviderConfiguration1.getGuid()).thenReturn(GUID1);
		final PaymentProviderConfigDTO paymentProviderConfiguration2 = mock(PaymentProviderConfigDTO.class, "paymentProviderConfig-2");
		when(paymentProviderConfiguration2.getGuid()).thenReturn(GUID1DNE);

		final List<String> guidList = Arrays.asList(GUID1, GUID1DNE);
		final List<PaymentProviderConfigDTO> paymentProviderConfigurations =
				Arrays.asList(paymentProviderConfiguration1, paymentProviderConfiguration2);

		final List<PaymentProviderConfigDomainProxy> domainList = new ArrayList<>();
		for (String guid : guidList) {
			PaymentProviderConfigDomainProxy domain = new PaymentProviderConfigDomainProxy();
			domain.setGuid(guid);
			domainList.add(domain);
		}

		when(paymentProviderConfigManagementService.findAll()).thenReturn(paymentProviderConfigurations);
		List<PaymentProviderConfigDomainProxy> results = paymentProviderExporterImpl.findByIDs(guidList);

		assertThat(CollectionUtils.isEqualCollection(results, domainList))
				.as("Missing returned payment provider configuration")
				.isTrue();

		assertThat(results)
				.as("Extra payment provider configuration returned")
				.hasSize(2);
	}

	/**
	 * Test method for {@link PaymentProviderExporterImpl#getListExportableIDs()}.
	 *
	 * @throws ConfigurationException not expected exception.
	 */
	@Test
	public void testGetListExportableIDs() throws ConfigurationException {
		ExportConfiguration exportConfiguration = new ExportConfiguration();
		SearchConfiguration searchConfiguration = new SearchConfiguration();

		when(paymentProviderConfigManagementService.findAll()).thenReturn(Collections.emptyList());

		paymentProviderExporterImpl.initialize(new ExportContext(exportConfiguration, searchConfiguration));
		paymentProviderExporterImpl.getContext()
				.setDependencyRegistry(new DependencyRegistry(Arrays.asList(new Class<?>[]{PaymentProviderConfigDomainProxy.class})));

		ExporterConfiguration exporterConfiguration = new ExporterConfiguration();
		exportConfiguration.setExporterConfiguration(exporterConfiguration);

		paymentProviderExporterImpl.getContext().getDependencyRegistry().addGuidDependency(PaymentProviderConfigDomainProxy.class, GUID1);
		paymentProviderExporterImpl.getContext().getDependencyRegistry().addGuidDependency(PaymentProviderConfigDomainProxy.class, GUID2);
		paymentProviderExporterImpl.getContext().getDependencyRegistry().addGuidDependency(PaymentProviderConfigDomainProxy.class, GUID3);

		final List<String> listExportableIDs = paymentProviderExporterImpl.getListExportableIDs();

		assertThat(CollectionUtils.isEqualCollection(listExportableIDs, Arrays.asList(GUID1, GUID2, GUID3)))
				.as("Missing returned payment provider configuration")
				.isTrue();

		assertThat(listExportableIDs)
				.as("Extra payment provider configuration returned")
				.hasSize(THREE);
	}

	/**
	 * Ensures the proper {@link JobType} is returned.
	 */
	@Test
	public void testJobType() {
		assertThat(JobType.PAYMENTPROVIDER)
				.as("Incorrect job type returned.")
				.isEqualTo(paymentProviderExporterImpl.getJobType());
	}

	/**
	 * Tests getDependentClasses.
	 */
	@Test
	public void testDependantClasses() {
		assertThat(new Class<?>[]{PaymentProviderConfigDomainProxy.class})
				.as("Incorrect dependent classes returned.")
				.isEqualTo(paymentProviderExporterImpl.getDependentClasses());
	}

	/**
	 * Tests getDomainAdapter.
	 */
	@Test
	public void testGetDomainAdapter() {
		assertThat(paymentProviderAdapter)
				.as("Incorrect domain adapter returned.")
				.isEqualTo(paymentProviderExporterImpl.getDomainAdapter());
	}

	/**
	 * Tests getImportExportSearcher.
	 */
	@Test
	public void testImportExportSearcher() {
		assertThat(importExportSearcher)
				.as("Incorrect export searcher returned.")
				.isEqualTo(paymentProviderExporterImpl.getImportExportSearcher());
	}
}
