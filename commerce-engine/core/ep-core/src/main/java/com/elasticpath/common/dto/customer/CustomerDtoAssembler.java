/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.common.dto.customer;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.service.customer.CustomerGroupService;

/**
 * <b>Does not copy credit card numbers.</b> <br>
 * This assembler is for Customers, but delegates to {@code CreditCardFilter} to assemble the credit card DTO.
 */
@SuppressWarnings("PMD.GodClass")
public class CustomerDtoAssembler extends AbstractDtoAssembler<CustomerDTO, Customer> {

	private BeanFactory beanFactory;

	private CustomerGroupService customerGroupService;

	@Override
	public Customer getDomainInstance() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
	}

	@Override
	public CustomerDTO getDtoInstance() {
		return new CustomerDTO();
	}

	@Override
	public void assembleDto(final Customer source, final CustomerDTO target) {
		target.setCreationDate(source.getCreationDate());
		target.setGuid(source.getGuid());
		target.setLastEditDate(source.getLastEditDate());

		if (source.getCustomerAuthentication() != null) {
			target.setPassword(source.getCustomerAuthentication().getPassword());
			target.setSalt(source.getCustomerAuthentication().getSalt());
		}

		if (source.getPreferredBillingAddress() != null) {
			target.setPreferredBillingAddressGuid(source.getPreferredBillingAddress().getGuid());
		}
		if (source.getPreferredShippingAddress() != null) {
			target.setPreferredShippingAddressGuid(source.getPreferredShippingAddress().getGuid());
		}
		target.setStatus(source.getStatus());
		target.setStoreCode(source.getStoreCode());
		target.setUserId(source.getUserId());
		target.setFirstTimeBuyer(source.isFirstTimeBuyer());

		populateDtoAddresses(source, target);

		for (CustomerGroup group : source.getCustomerGroups()) {
			target.getGroups().add(group.getGuid());
		}

		for (String key : source.getProfileValueMap().keySet()) {
			CustomerProfileValue value = source.getProfileValueMap().get(key);

			AttributeValueDTO avDto = getAttributeValueDto();
			avDto.setKey(key);
			avDto.setType(value.getAttributeType().toString());
			avDto.setCreationDate(value.getCreationDate());
			avDto.setValue(value.getStringValue());
			target.getProfileValues().add(avDto);
		}
	}

	private void populateDtoAddresses(final Customer source, final CustomerDTO target) {
		for (CustomerAddress sourceAddress : source.getAddresses()) {
			AddressDTO targetAddress = getAddressDto();

			targetAddress.setCreationDate(sourceAddress.getCreationDate());
			targetAddress.setLastModifiedDate(sourceAddress.getLastModifiedDate());
			targetAddress.setCity(sourceAddress.getCity());
			targetAddress.setCommercialAddress(sourceAddress.isCommercialAddress());
			targetAddress.setCountry(sourceAddress.getCountry());
			targetAddress.setFaxNumber(sourceAddress.getFaxNumber());
			targetAddress.setFirstName(sourceAddress.getFirstName());
			targetAddress.setGuid(sourceAddress.getGuid());
			targetAddress.setLastName(sourceAddress.getLastName());
			targetAddress.setPhoneNumber(sourceAddress.getPhoneNumber());
			targetAddress.setStreet1(sourceAddress.getStreet1());
			targetAddress.setStreet2(sourceAddress.getStreet2());
			targetAddress.setSubCountry(sourceAddress.getSubCountry());
			targetAddress.setZipOrPostalCode(sourceAddress.getZipOrPostalCode());
			targetAddress.setOrganization(sourceAddress.getOrganization());

			target.getAddresses().add(targetAddress);
		}
	}

	@Override
	public void assembleDomain(final CustomerDTO source, final Customer target) {

		// Needed by credit card and preferred* below.
		populateDomainAddresses(source, target);

		target.setCreationDate(source.getCreationDate());
		target.setGuid(source.getGuid());
		target.setLastEditDate(source.getLastEditDate());

		target.setPassword(source.getPassword(), source.getSalt());

		target.setPreferredBillingAddress(target.getAddressByGuid(source.getPreferredBillingAddressGuid()));
		target.setPreferredShippingAddress(target.getAddressByGuid(source.getPreferredShippingAddressGuid()));
		target.setStatus(source.getStatus());

		target.setStoreCode(source.getStoreCode());

		target.setUserId(source.getUserId());
		target.setFirstTimeBuyer(source.isFirstTimeBuyer());

		populateDomainCustomerGroup(source, target);

		for (AttributeValueDTO avDto : source.getProfileValues()) {
			target.getCustomerProfile().setStringProfileValue(avDto.getKey(), avDto.getValue(), avDto.getCreationDate());
		}

	}

	private void populateDomainAddresses(final CustomerDTO source, final Customer target) {

		// Remove all addresses in the target which are also in the source.
		// We do it this way since AbstractAddressImpl overrides equals and doesn't use guid.
		for (AddressDTO sourceAddress : source.getAddresses()) {
			CustomerAddress targetAddress;

			targetAddress = target.getAddressByGuid(sourceAddress.getGuid());

			if (targetAddress == null) {
				targetAddress = getCustomerAddress();
				target.getAddresses().add(targetAddress);
			}

			targetAddress.setCreationDate(sourceAddress.getCreationDate());
			targetAddress.setLastModifiedDate(sourceAddress.getLastModifiedDate());
			targetAddress.setCity(sourceAddress.getCity());
			targetAddress.setCommercialAddress(sourceAddress.isCommercialAddress());
			targetAddress.setCountry(sourceAddress.getCountry());
			targetAddress.setFaxNumber(sourceAddress.getFaxNumber());
			targetAddress.setFirstName(sourceAddress.getFirstName());
			targetAddress.setGuid(sourceAddress.getGuid());
			targetAddress.setLastName(sourceAddress.getLastName());
			targetAddress.setPhoneNumber(sourceAddress.getPhoneNumber());
			targetAddress.setStreet1(sourceAddress.getStreet1());
			targetAddress.setStreet2(sourceAddress.getStreet2());
			targetAddress.setSubCountry(sourceAddress.getSubCountry());
			targetAddress.setZipOrPostalCode(sourceAddress.getZipOrPostalCode());
			targetAddress.setOrganization(sourceAddress.getOrganization());
		}
	}

	private void populateDomainCustomerGroup(final CustomerDTO source, final Customer target) {
		final CustomerGroup defaultCustomerGroup = getCustomerGroupService().findByGroupName(CustomerGroup.DEFAULT_GROUP_NAME);
		target.addCustomerGroup(defaultCustomerGroup);

		for (String guid : source.getGroups()) {
			final CustomerGroup customerGroup = getCustomerGroupService().findByGuid(guid);
			target.addCustomerGroup(customerGroup);
		}
	}

	protected AddressDTO getAddressDto() {
		return new AddressDTO();
	}

	protected AttributeValueDTO getAttributeValueDto() {
		return new AttributeValueDTO();
	}

	protected CustomerAddress getCustomerAddress() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER_ADDRESS, CustomerAddress.class);
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setCustomerGroupService(final CustomerGroupService customerGroupService) {
		this.customerGroupService = customerGroupService;
	}

	protected CustomerGroupService getCustomerGroupService() {
		return customerGroupService;
	}

}
