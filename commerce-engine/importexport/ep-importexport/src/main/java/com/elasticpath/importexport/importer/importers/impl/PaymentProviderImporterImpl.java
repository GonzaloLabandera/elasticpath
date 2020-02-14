/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.elasticpath.common.dto.paymentprovider.PaymentProviderDTO;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.paymentprovider.PaymentProviderConfigDomainProxy;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Importer for {@link PaymentProviderDTO} and its associated domain class.
 */
public class PaymentProviderImporterImpl extends AbstractImporterImpl<PaymentProviderConfigDomainProxy, PaymentProviderDTO> {

	private static final Logger LOG = Logger.getLogger(PaymentProviderImporterImpl.class);

	private DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> paymentProviderAdapter;

	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	@Override
	public void initialize(final ImportContext context, final SavingStrategy<PaymentProviderConfigDomainProxy, PaymentProviderDTO> savingStrategy) {
		super.initialize(context, savingStrategy);

		final SavingManager<PaymentProviderConfigDomainProxy> paymentProviderConfigurationSavingManager =
				new SavingManager<PaymentProviderConfigDomainProxy>() {

					@Override
					public PaymentProviderConfigDomainProxy update(final PaymentProviderConfigDomainProxy persistable) {
						return new PaymentProviderConfigDomainProxy(paymentProviderConfigManagementService.saveOrUpdate(persistable));
					}

					@Override
					public void save(final PaymentProviderConfigDomainProxy persistable) {
						update(persistable);
					}
				};
		getSavingStrategy().setSavingManager(paymentProviderConfigurationSavingManager);
	}

	@Override
	public boolean executeImport(final PaymentProviderDTO paymentProviderDTO) {
		sanityCheck();
		setImportStatus(paymentProviderDTO);
		final PaymentProviderConfigDomainProxy paymentProviderConfigDomainProxy = findPersistentObject(paymentProviderDTO);

		if (paymentProviderConfigDomainProxy != null) {
			LOG.warn(new Message("IE-31400", paymentProviderDTO.getGuid()));
			return false;
		}

		getSavingStrategy().setDomainAdapter(paymentProviderAdapter);

		getSavingStrategy().populateAndSaveObject(paymentProviderConfigDomainProxy, paymentProviderDTO);

		return true;
	}

	@Override
	protected String getDtoGuid(final PaymentProviderDTO dto) {
		return dto.getGuid();
	}

	@Override
	protected DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> getDomainAdapter() {
		return paymentProviderAdapter;
	}

	@Override
	protected PaymentProviderConfigDomainProxy findPersistentObject(final PaymentProviderDTO dto) {
		final PaymentProviderConfigDTO paymentProviderConfigDTO = paymentProviderConfigManagementService.findByGuid(dto.getGuid());
		if (Objects.isNull(paymentProviderConfigDTO)) {
			return null;
		}
		return new PaymentProviderConfigDomainProxy(paymentProviderConfigDTO);
	}

	@Override
	protected void setImportStatus(final PaymentProviderDTO object) {
		getStatusHolder().setImportStatus(object.getGuid());
	}

	@Override
	public Class<? extends PaymentProviderDTO> getDtoClass() {
		return PaymentProviderDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return PaymentProviderDTO.ROOT_ELEMENT;
	}

	public void setPaymentProviderAdapter(final DomainAdapter<PaymentProviderConfigDomainProxy, PaymentProviderDTO> paymentProviderAdapter) {
		this.paymentProviderAdapter = paymentProviderAdapter;
	}

	public void setPaymentProviderConfigManagementService(final PaymentProviderConfigManagementService paymentProviderConfigManagementService) {
		this.paymentProviderConfigManagementService = paymentProviderConfigManagementService;
	}
}
