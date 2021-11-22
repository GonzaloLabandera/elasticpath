/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Implements an exporter for a {@link PaymentProviderConfigDomainProxy}s.
 */
public class PaymentProviderExporterImpl extends AbstractExporterImpl<PaymentProviderConfigDomainProxy, PaymentProviderDTO, String> {

	private static final Logger LOG = LogManager.getLogger(PaymentProviderExporterImpl.class);

	private ImportExportSearcher importExportSearcher;

	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	private DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> paymentProviderAdapter;

	private List<String> paymentProviders;

	@Override
	public JobType getJobType() {
		return JobType.PAYMENTPROVIDER;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[]{PaymentProviderConfigDomainProxy.class};
	}

	@Override
	protected void initializeExporter(final ExportContext context) throws ConfigurationException {
		List<PaymentProviderConfigDTO> paymentProviderConfigDTOs = paymentProviderConfigManagementService.findAll();
		paymentProviders = new ArrayList<>(paymentProviderConfigDTOs.size());

		for (PaymentProviderConfigDTO paymentProviderConfiguration : paymentProviderConfigDTOs) {
			paymentProviders.add(paymentProviderConfiguration.getGuid());
		}
		LOG.info("The list for " + paymentProviders.size() + " payment provider configuration(s) is retrieved from the database.");
	}

	@Override
	protected Class<? extends PaymentProviderDTO> getDtoClass() {
		return PaymentProviderDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		if (getContext().getDependencyRegistry().supportsDependency(PaymentProviderConfigDomainProxy.class)) {
			paymentProviders.addAll(getContext().getDependencyRegistry().getDependentGuids(PaymentProviderConfigDomainProxy.class));
		}

		return new ArrayList<>(paymentProviders);
	}

	@Override
	protected List<PaymentProviderConfigDomainProxy> findByIDs(final List<String> subList) {
		return paymentProviderConfigManagementService.findAll().stream()
				.map(config -> {
					PaymentProviderConfigDomainProxy domain = new PaymentProviderConfigDomainProxy();
					domain.setConfigurationName(config.getConfigurationName());
					domain.setGuid(config.getGuid());
					domain.setPaymentProviderPluginBeanName(config.getPaymentProviderPluginBeanName());
					domain.setPaymentConfigurationData(config.getPaymentConfigurationData());
					domain.setStatus(config.getStatus());
					domain.setLocalizedNames(config.getLocalizedNames());
					domain.setDefaultDisplayName(config.getDefaultDisplayName());
					return domain;
				}).collect(Collectors.toList());
	}

	@Override
	protected DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> getDomainAdapter() {
		return this.paymentProviderAdapter;
	}

	public void setPaymentProviderConfigManagementService(final PaymentProviderConfigManagementService paymentProviderConfigManagementService) {
		this.paymentProviderConfigManagementService = paymentProviderConfigManagementService;
	}

	public void setPaymentProviderAdapter(final DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> paymentProviderAdapter) {
		this.paymentProviderAdapter = paymentProviderAdapter;
	}

	public ImportExportSearcher getImportExportSearcher() {
		return importExportSearcher;
	}

	public void setImportExportSearcher(final ImportExportSearcher importExportSearcher) {
		this.importExportSearcher = importExportSearcher;
	}

}
