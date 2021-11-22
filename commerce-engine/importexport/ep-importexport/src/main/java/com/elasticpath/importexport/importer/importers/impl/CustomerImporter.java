/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.importer.importers.impl;

import static com.elasticpath.importexport.common.adapters.customer.CustomerAdapter.PREFERRED_BILLING_ADDRESS;
import static com.elasticpath.importexport.common.adapters.customer.CustomerAdapter.PREFERRED_SHIPPING_ADDRESS;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Pair;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.adapters.customer.CustomerAdapter;
import com.elasticpath.importexport.common.exception.runtime.ImportDuplicateEntityRuntimeException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.exporters.impl.CustomerCollectionsStrategy;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.importexport.importer.importers.SavingStrategy;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.persistence.support.impl.FetchGroupLoadTunerImpl;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Importer for {@link Customer} objects.
 */
public class CustomerImporter extends AbstractImporterImpl<Customer, CustomerDTO> {

	private DomainAdapter<Customer, CustomerDTO> customerAdapter;

	private CustomerService customerService;
	private PersistenceEngine persistenceEngine;
	private AccountTreeService accountTreeService;

	private AddressService addressService;

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

	public void setAccountTreeService(final AccountTreeService accountTreeService) {
		this.accountTreeService = accountTreeService;
	}
	public void setAddressService(final AddressService addressService) {
		this.addressService = addressService;
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

		final SavingManager<Customer> dataPolicySavingManager = new SavingManager<Customer>() {

			@Override
			public Customer update(final Customer customer) {
				return persistenceEngine.update(customer);
			}

			@Override
			public void save(final Customer customer) {
				persistenceEngine.save(customer);
				if (customer.getParentGuid() != null) {
					accountTreeService.insertClosures(customer.getGuid(), customer.getParentGuid());
				}
			}
		};
		getSavingStrategy().setSavingManager(dataPolicySavingManager);
	}

	@Override
	public Class<? extends CustomerDTO> getDtoClass() {
		return CustomerDTO.class;
	}

	/**
	 * CustomerImpl doesn't maintain the relationship with CustomerAddress instances due to performance issues.
	 * Prior calling this method, the Customer instances are saved *without* addresses. The non-persisted addresses are stored in the
	 * CustomerAdapter instance.
	 *
	 * Because addresses and customers can't be persisted in the same transaction, 2 additional transactions will be executed in this method.
	 *
	 * 1. fetch a list of Customer instances by GUIDs
	 * 2. link each CustomerAddress to Customer and *save* all addresses
	 * 3. set preferred billing and shipping addresses, if any, and *save* updated Customer instances.
	 */
	@Override
	public void postProcessingImportHandling() {
		CustomerAdapter customerAdapterImpl = (CustomerAdapter) getDomainAdapter();

		Multimap<String, Pair<Integer, CustomerAddress>> nonPersistedCustomerAddresses = customerAdapterImpl
				.getNonPersistedCustomerAddresses();

		if (nonPersistedCustomerAddresses.isEmpty()) {
			return;
		}

		List<Customer> customers = customerService.findByGuids(nonPersistedCustomerAddresses.keySet());

		Set<CustomerAddress> nonPersistedAddresses = new HashSet<>();
		customers.forEach(customer -> {
			Collection<Pair<Integer, CustomerAddress>> pairs = nonPersistedCustomerAddresses.get(customer.getGuid());

			for (Pair<Integer, CustomerAddress> pair : pairs) {
				CustomerAddress customerAddress = pair.getRight();
				customerAddress.setCustomerUidPk(customer.getUidPk());

				nonPersistedAddresses.add(customerAddress);

				if (pair.getLeft() == PREFERRED_BILLING_ADDRESS) {
					customer.setPreferredBillingAddress(customerAddress);
				} else if (pair.getLeft() == PREFERRED_SHIPPING_ADDRESS) {
					customer.setPreferredShippingAddress(customerAddress);
				}
			}
		});

		//validate customers with addresses - any violation will throw an exception and stop the import
		customerAdapterImpl.validateCustomers(customers);

		addressService.save(nonPersistedAddresses.toArray(new CustomerAddress[nonPersistedAddresses.size()]));
		customerService.updateCustomers(customers);

		nonPersistedCustomerAddresses.clear();
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}
}