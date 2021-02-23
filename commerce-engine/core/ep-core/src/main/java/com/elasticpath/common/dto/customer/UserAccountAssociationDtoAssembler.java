/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.common.dto.customer;

import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.service.customer.CustomerService;

/**
 * The User Account Association DTO Assembler.
 */
public class UserAccountAssociationDtoAssembler extends AbstractDtoAssembler<UserAccountAssociationDTO, UserAccountAssociation> {

	private BeanFactory beanFactory;

	private CustomerService customerService;

	@Override
	public UserAccountAssociation getDomainInstance() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.USER_ACCOUNT_ASSOCIATION, UserAccountAssociation.class);
	}

	@Override
	public UserAccountAssociationDTO getDtoInstance() {
		return new UserAccountAssociationDTO();
	}

	@Override
	public void assembleDto(final UserAccountAssociation source, final UserAccountAssociationDTO target) {
		target.setGuid(source.getGuid());
		target.setAccountGuid(source.getAccountGuid());
		target.setUserGuid(source.getUserGuid());
		target.setRole(source.getAccountRole());
	}

	@Override
	public void assembleDomain(final UserAccountAssociationDTO source, final UserAccountAssociation target) {
		target.setGuid(source.getGuid());
		target.setAccountGuid(source.getAccountGuid());
		target.setUserGuid(source.getUserGuid());
		target.setAccountRole(source.getRole());
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected CustomerService getCustomerService() {
		return customerService;
	}

	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}
}
