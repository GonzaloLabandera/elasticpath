/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.common.dto.customer.AttributePolicyDTO;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.service.customer.AttributePolicyService;

/**
 * Attribute policy exporter.
 */
public class AttributePolicyExporter
		extends AbstractExporterImpl<AttributePolicy, AttributePolicyDTO, String> {

	private DomainAdapter<AttributePolicy, AttributePolicyDTO> attributePolicyAdapter;

	private AttributePolicyService attributePolicyService;

	private List<String> attributePolicyGuids;

	@Override
	protected void initializeExporter(final ExportContext context) {
		attributePolicyGuids = new ArrayList<>();
		attributePolicyService.findAll().forEach(policy -> attributePolicyGuids.add(policy.getGuid()));
	}

	@Override
	protected List<AttributePolicy> findByIDs(final List<String> subList) {
		return attributePolicyService.findByGuids(subList);
	}

	@Override
	protected DomainAdapter<AttributePolicy, AttributePolicyDTO> getDomainAdapter() {
		return attributePolicyAdapter;
	}

	@Override
	protected Class<? extends AttributePolicyDTO> getDtoClass() {
		return AttributePolicyDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		if (getContext().getDependencyRegistry().supportsDependency(AttributePolicy.class)) {
			attributePolicyGuids.addAll(getContext().getDependencyRegistry().getDependentGuids(AttributePolicy.class));
		}
		return attributePolicyGuids;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[]{AttributePolicy.class};
	}

	@Override
	public JobType getJobType() {
		return JobType.ATTRIBUTE_POLICY;
	}

	/**
	 * Set the attribute policy adapter.
	 *
	 * @param attributePolicyAdapter the attribute policy adapter
	 */
	public void setAttributePolicyAdapter(final DomainAdapter<AttributePolicy, AttributePolicyDTO>
												  attributePolicyAdapter) {
		this.attributePolicyAdapter = attributePolicyAdapter;
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
