/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.importer.importers.impl;

import static com.elasticpath.commons.constants.ContextIdNames.STORE_PAYMENT_PROVIDER_CONFIG;
import static com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus.ACTIVE;

import java.util.List;
import java.util.Objects;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.common.dto.store.StoreDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;
import com.elasticpath.service.store.StoreService;

/**
 * An importer for {@link Store}s.
 */
public class StoreImporter extends AbstractImporterImpl<Store, StoreDTO> {

	private BeanFactory beanFactory;

	private DomainAdapter<Store, StoreDTO> storeAdapter;

	private StoreService storeService;

	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	@Override
	public boolean executeImport(final StoreDTO storeDTO) {
		if (super.executeImport(storeDTO)) {

			populatePaymentProviderConfigs(storeDTO);

			return true;
		}
		return false;
	}

	@Override
	protected CollectionsStrategy<Store, StoreDTO> getCollectionsStrategy() {
		return new StoreCollectionsStrategy(storePaymentProviderConfigService,
				getContext().getImportConfiguration().getImporterConfiguration(JobType.STORE));
	}

	@Override
	public String getImportedObjectName() {
		return StoreDTO.ROOT_ELEMENT;
	}

	@Override
	protected String getDtoGuid(final StoreDTO dto) {
		return dto.getCode();
	}

	@Override
	protected DomainAdapter<Store, StoreDTO> getDomainAdapter() {
		return storeAdapter;
	}

	@Override
	protected Store findPersistentObject(final StoreDTO dto) {
		return storeService.findStoreWithCode(dto.getCode());
	}

	@Override
	protected void setImportStatus(final StoreDTO object) {
		getStatusHolder().setImportStatus("(" + object.getCode() + ")");
	}

	public void setStoreAdapter(final DomainAdapter<Store, StoreDTO> storeAdapter) {
		this.storeAdapter = storeAdapter;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setStorePaymentProviderConfigService(final StorePaymentProviderConfigService storePaymentProviderConfigService) {
		this.storePaymentProviderConfigService = storePaymentProviderConfigService;
	}

	@Override
	public Class<? extends StoreDTO> getDtoClass() {
		return StoreDTO.class;
	}

	private void populatePaymentProviderConfigs(final StoreDTO source) {
		final List<String> paymentProviderPluginConfigGuids = source.getPaymentProviderPluginConfigGuids();

		for (final String paymentProviderPluginConfigGuid : paymentProviderPluginConfigGuids) {
			final PaymentProviderConfigDTO paymentProviderConfigDTO = paymentProviderConfigManagementService
					.findByGuid(paymentProviderPluginConfigGuid);

            if (Objects.isNull(paymentProviderConfigDTO)) {
                throw new EpSystemException("PaymentProviderConfig with guid " + paymentProviderPluginConfigGuid + " not exist");
            }

            if (!Objects.equals(paymentProviderConfigDTO.getStatus(), ACTIVE)) {
                throw new EpSystemException("PaymentProviderConfig with guid " + paymentProviderPluginConfigGuid + " is inactive");
            }

            final StorePaymentProviderConfig storePaymentProviderConfig = beanFactory
                    .getPrototypeBean(STORE_PAYMENT_PROVIDER_CONFIG, StorePaymentProviderConfig.class);
            storePaymentProviderConfig.setStoreCode(source.getCode());
            storePaymentProviderConfig.setPaymentProviderConfigGuid(paymentProviderPluginConfigGuid);

            storePaymentProviderConfigService.saveOrUpdate(storePaymentProviderConfig);
        }

	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public PaymentProviderConfigManagementService getPaymentProviderConfigManagementService() {
		return paymentProviderConfigManagementService;
	}

	public void setPaymentProviderConfigManagementService(final PaymentProviderConfigManagementService paymentProviderConfigManagementService) {
		this.paymentProviderConfigManagementService = paymentProviderConfigManagementService;
	}

}
