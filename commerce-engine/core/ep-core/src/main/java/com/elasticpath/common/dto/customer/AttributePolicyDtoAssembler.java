/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.customer;

import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;

/**
 * Assembles AttributePolicy domains and DTOs.
 */
public class AttributePolicyDtoAssembler
		extends AbstractDtoAssembler<AttributePolicyDTO, AttributePolicy> {

	private BeanFactory beanFactory;

	@Override
	public AttributePolicy getDomainInstance() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.ATTRIBUTE_POLICY, AttributePolicy.class);
	}

	@Override
	public void assembleDto(final AttributePolicy source, final AttributePolicyDTO target) {
		target.setGuid(source.getGuid());
		target.setPolicyKey(source.getPolicyKey().getName());
		target.setPolicyPermission(source.getPolicyPermission().getName());
	}

	@Override
	public void assembleDomain(final AttributePolicyDTO source, final AttributePolicy target) {
		target.setGuid(source.getGuid());
		target.setPolicyKey(PolicyKey.valueOf(source.getPolicyKey()));
		target.setPolicyPermission(PolicyPermission.valueOf(source.getPolicyPermission()));
	}

	@Override
	public AttributePolicyDTO getDtoInstance() {
		return new AttributePolicyDTO();
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
