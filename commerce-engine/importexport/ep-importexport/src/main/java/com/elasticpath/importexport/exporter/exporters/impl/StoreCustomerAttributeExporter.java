/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.StoreCustomerAttributeService;

/**
 * Store customer attribute exporter.
 */
public class StoreCustomerAttributeExporter extends AbstractExporterImpl<StoreCustomerAttribute, StoreCustomerAttributeDTO, String> {

	private DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> storeCustomerAttributeAdapter;

	private StoreCustomerAttributeService storeCustomerAttributeService;

	private AttributePolicyService attributePolicyService;

	private List<String> storeCustomerAttributeGuids;

	@Override
	protected void initializeExporter(final ExportContext context) {
		storeCustomerAttributeGuids = new ArrayList<>();
		storeCustomerAttributeService.findAll().forEach(attribute -> storeCustomerAttributeGuids.add(attribute.getGuid()));
	}

	@Override
	protected List<StoreCustomerAttribute> findByIDs(final List<String> subList) {
		return storeCustomerAttributeService.findByGuids(subList);
	}

	@Override
	protected DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> getDomainAdapter() {
		return storeCustomerAttributeAdapter;
	}

	@Override
	protected Class<? extends StoreCustomerAttributeDTO> getDtoClass() {
		return StoreCustomerAttributeDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		if (getContext().getDependencyRegistry().supportsDependency(StoreCustomerAttribute.class)) {
			storeCustomerAttributeGuids.addAll(getContext().getDependencyRegistry().getDependentGuids(StoreCustomerAttribute.class));
		}
		return storeCustomerAttributeGuids;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[]{StoreCustomerAttribute.class};
	}

	@Override
	protected void addDependencies(final List<StoreCustomerAttribute> storeCustomerAttributes, final DependencyRegistry dependencyRegistry) {
		storeCustomerAttributes.forEach(attribute -> {
			if (dependencyRegistry.supportsDependency(Store.class)) {
				dependencyRegistry.addGuidDependency(Store.class, attribute.getStoreCode());
			}

			if (dependencyRegistry.supportsDependency(Attribute.class)) {
				dependencyRegistry.addGuidDependency(Attribute.class, attribute.getAttributeKey());
			}

			if (dependencyRegistry.supportsDependency(AttributePolicy.class)) {
				final List<AttributePolicy> policies = attributePolicyService
						.findByPolicyKey(attribute.getPolicyKey());
				policies.forEach(policy -> dependencyRegistry.addGuidDependency(AttributePolicy.class, policy.getGuid()));
			}
		});
	}

	@Override
	public JobType getJobType() {
		return JobType.STORECUSTOMERATTRIBUTE;
	}

	/**
	 * Set the store customer attribute adapter.
	 *
	 * @param storeCustomerAttributeAdapter the store customer attribute adapter
	 */
	public void setStoreCustomerAttributeAdapter(final DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO>
														 storeCustomerAttributeAdapter) {
		this.storeCustomerAttributeAdapter = storeCustomerAttributeAdapter;
	}

	/**
	 * Set the store customer attribute service.
	 *
	 * @param storeCustomerAttributeService the store customer attribute service
	 */
	public void setStoreCustomerAttributeService(final StoreCustomerAttributeService storeCustomerAttributeService) {
		this.storeCustomerAttributeService = storeCustomerAttributeService;
	}

	/**
	 * Set the attribute policy service.
	 *
	 * @param attributePolicyService the attribute policy service
	 */
	public void setAttributePolicyService(final AttributePolicyService attributePolicyService) {
		this.attributePolicyService = attributePolicyService;
	}
}
