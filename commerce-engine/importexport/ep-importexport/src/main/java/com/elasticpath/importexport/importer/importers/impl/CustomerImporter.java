/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.importer.importers.impl;

import java.util.HashSet;
import java.util.Set;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.exception.runtime.ImportDuplicateEntityRuntimeException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.exporters.impl.CustomerCollectionsStrategy;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.persistence.support.impl.FetchGroupLoadTunerImpl;
import com.elasticpath.service.customer.CustomerService;

/**
 * Importer for {@link Customer} objects.
 */
public class CustomerImporter extends AbstractImporterImpl<Customer, CustomerDTO> {

	private DomainAdapter<Customer, CustomerDTO> customerAdapter;

	private CustomerService customerService;

	private Set<String> processedCompositeGuids;

	@Override
	protected CollectionsStrategy<Customer, CustomerDTO> getCollectionsStrategy() {
		return new CustomerCollectionsStrategy(getContext().getImportConfiguration().getImporterConfiguration(JobType.CUSTOMER), customerService);
	}

	@Override
	public String getImportedObjectName() {
		return CustomerDTO.ROOT_ELEMENT;
	}

	@Override
	protected String getDtoGuid(final CustomerDTO dto) {
		return dto.getGuid();
	}

	/**
	 * Gets the composite guid by concatenating customerType and sharedId of given DTO.
	 *
	 * @param dto the customer data transfer object
	 * @return the dto composite guid
	 */
	protected String getCompositeDtoGuid(final CustomerDTO dto) {
		return dto.getCustomerType() + dto.getSharedId();
	}

	/**
	 * Gets the composite username by concatenating customerType and username of given DTO.
	 *
	 * @param dto the customer data transfer object
	 * @return the dto composite username
	 */
	protected String getCompositeDtoUsername(final CustomerDTO dto) {
		return dto.getCustomerType() + dto.getUsername();
	}

	@Override
	protected DomainAdapter<Customer, CustomerDTO> getDomainAdapter() {
		return customerAdapter;
	}

	@Override
	protected Customer findPersistentObject(final CustomerDTO dto) {
		FetchGroupLoadTunerImpl loadtunerAll = new FetchGroupLoadTunerImpl();
		loadtunerAll.addFetchGroup(FetchGroupConstants.ALL);
		return customerService.findByGuid(dto.getGuid(), loadtunerAll);
	}

	@Override
	protected void checkDuplicateGuids(final CustomerDTO customerDTO, final Customer customer) {
		super.checkDuplicateGuids(customerDTO, customer);
		final String compositeGuid = getCompositeDtoGuid(customerDTO);
		final String compositeUsername = getCompositeDtoUsername(customerDTO);
		if (getSavingStrategy().isImportRequired(customer)) {
			if (compositeGuid != null && getProcessedCompositeGuids().contains(compositeGuid)) {
				throw new ImportDuplicateEntityRuntimeException("IE-30852", customerDTO.getSharedId());
			}

			if (compositeUsername != null && getProcessedCompositeGuids().contains(compositeUsername)) {
				throw new ImportDuplicateEntityRuntimeException("IE-30853", customerDTO.getUsername());
			}

			getProcessedCompositeGuids().add(compositeGuid);
		}
	}

	@Override
	protected void setImportStatus(final CustomerDTO object) {
		getStatusHolder().setImportStatus("(" + object.getGuid() + ")");
	}

	public void setCustomerAdapter(final DomainAdapter<Customer, CustomerDTO> customerAdapter) {
		this.customerAdapter = customerAdapter;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}


	public Set<String> getProcessedCompositeGuids() {
		return processedCompositeGuids;
	}

	public void setProcessedCompositeGuids(final Set<String> processedCompositeGuids) {
		this.processedCompositeGuids = processedCompositeGuids;
	}

	@Override
	public void initialize(final ImportContext context, final SavingStrategy<Customer, CustomerDTO> savingStrategy) {
		super.initialize(context, savingStrategy);
		setProcessedCompositeGuids(new HashSet<>());
	}

	@Override
	public Class<? extends CustomerDTO> getDtoClass() {
		return CustomerDTO.class;
	}
}