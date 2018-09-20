/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.common.dto.customer;

import org.apache.commons.lang.BooleanUtils;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.common.dto.assembler.customer.BuiltinFilters;
import com.elasticpath.common.dto.assembler.customer.CreditCardFilter;
import com.elasticpath.common.dto.customer.transformer.CreditCardDTOTransformer;
import com.elasticpath.common.dto.customer.transformer.PaymentTokenDTOTransformer;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.domain.customer.PaymentToken;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.service.customer.CustomerGroupService;

/**
 * <b>Does not copy credit card numbers.</b> <br>
 * This assembler is for Customers, but delegates to {@code CreditCardFilter} to assemble the credit card DTO.
 */
@SuppressWarnings("PMD.GodClass")
public class CustomerDtoAssembler extends AbstractDtoAssembler<CustomerDTO, Customer> {

	private BeanFactory beanFactory;

	private CustomerGroupService customerGroupService;

	private CreditCardFilter cardFilter;
	private CreditCardDTOTransformer credtCardDTOTransformer;
	private PaymentTokenDTOTransformer paymentTokenDTOTransformer;

	@Override
	public Customer getDomainInstance() {
		return getBeanFactory().getBean(ContextIdNames.CUSTOMER);
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

		target.setPassword(source.getPassword());
		target.setSalt(source.getCustomerAuthentication().getSalt());

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
		populateDtoPaymentMethods(source, target);
		populateDtoDefaultPaymentMethod(source, target);

		for (CustomerGroup group : source.getCustomerGroups()) {
			target.getGroups().add(group.getGuid());
		}

		for (String key : source.getProfileValueMap().keySet()) {
			AttributeValue value = source.getProfileValueMap().get(key);

			AttributeValueDTO avDto = getAttributeValueDto();
			avDto.setKey(key);
			avDto.setType(value.getAttributeType().toString());
			avDto.setValue(value.getStringValue());
			target.getProfileValues().add(avDto);
		}

	}

	private void populateDtoAddresses(final Customer source, final CustomerDTO target) {
		for (CustomerAddress sourceAddress : source.getAddresses()) {
			AddressDTO targetAddress = getAddressDto();

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

	private void populateDtoPaymentMethods(final Customer source, final CustomerDTO target) {
		for (PaymentMethod sourcePaymentMethod : source.getPaymentMethods().all()) {
			PaymentMethodDto paymentMethodDto = transformPaymentMethodToDto(sourcePaymentMethod);

			if (paymentMethodDto != null) {
				target.getPaymentMethods().add(paymentMethodDto);
			}
		}
	}

	private void populateDtoDefaultPaymentMethod(final Customer source, final CustomerDTO target) {
		PaymentMethod defaultPaymentMethod = source.getPaymentMethods().getDefault();
		if (defaultPaymentMethod != null) {
			PaymentMethodDto transformedDefaultPaymentMethodDto = transformPaymentMethodToDto(defaultPaymentMethod);
			if (transformedDefaultPaymentMethodDto != null) {
				DefaultPaymentMethodDTO defaultPaymentMethodDTO = new DefaultPaymentMethodDTO();
				defaultPaymentMethodDTO.setPaymentMethod(transformedDefaultPaymentMethodDto);
				target.setDefaultPaymentMethod(defaultPaymentMethodDTO);
			}
		}
	}

	@Override
	public void assembleDomain(final CustomerDTO source, final Customer target) {

		// Needed by credit card and preferred* below.
		populateDomainAddresses(source, target);

		target.setCreationDate(source.getCreationDate());
		target.setGuid(source.getGuid());
		target.setLastEditDate(source.getLastEditDate());

		target.setPassword(source.getPassword());
		target.getCustomerAuthentication().setSalt(source.getSalt());

		target.setPreferredBillingAddress(target.getAddressByGuid(source.getPreferredBillingAddressGuid()));
		target.setPreferredShippingAddress(target.getAddressByGuid(source.getPreferredShippingAddressGuid()));
		target.setStatus(source.getStatus());

		target.setStoreCode(source.getStoreCode());

		target.setUserId(source.getUserId());
		target.setFirstTimeBuyer(source.isFirstTimeBuyer());
		populateLegacyDomainCreditCards(source, target);
		populateDomainPaymentMethods(source, target);
		populateDefaultPaymentMethod(source, target);

		populateDomainCustomerGroup(source, target);

		for (AttributeValueDTO avDto : source.getProfileValues()) {
			target.getCustomerProfile().setStringProfileValue(avDto.getKey(), avDto.getValue());
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

	private void populateDomainPaymentMethods(final CustomerDTO source, final Customer target) {
		for (PaymentMethodDto sourcePaymentMethod : source.getPaymentMethods()) {
			PaymentMethod targetPaymentMethod = transformPaymentMethodDtoToDomain(sourcePaymentMethod);
			target.getPaymentMethods().add(targetPaymentMethod);
		}
	}

	private void populateDefaultPaymentMethod(final CustomerDTO source, final Customer target) {
		DefaultPaymentMethodDTO defaultPaymentMethod = source.getDefaultPaymentMethod();

		if (defaultPaymentMethod != null) {
			PaymentMethodDto defaultPaymentMethodSource = defaultPaymentMethod.getPaymentMethod();
			PaymentMethod targetDefaultPaymentMethod = transformPaymentMethodDtoToDomain(defaultPaymentMethodSource);
			target.getPaymentMethods().setDefault(targetDefaultPaymentMethod);
		}
	}

	private void populateLegacyDomainCreditCards(final CustomerDTO source, final Customer target) {
		if (source.getCreditCards() == null) {
			return;
		}

		for (LegacyCreditCardDTO sourceCard : source.getCreditCards()) {
			for (CustomerCreditCard card : target.getCreditCards()) {
				if (sourceCard.getGuid().equals(card.getGuid())) {
					target.getPaymentMethods().remove(card);
					break;
				}
			}

			CustomerCreditCard targetCard = getCredtCardDTOTransformer().transformToDomain(sourceCard);

			if (BooleanUtils.isTrue(sourceCard.isDefaultCard())) {
				target.getPaymentMethods().setDefault(targetCard);
			} else {
				target.getPaymentMethods().add(targetCard);
			}
		}
	}

	private PaymentMethod transformPaymentMethodDtoToDomain(final PaymentMethodDto paymentMethodDto) {
		PaymentMethod transformedPaymentMethod = null;
		if (paymentMethodDto instanceof CreditCardDTO) {
			transformedPaymentMethod = getCredtCardDTOTransformer().transformToDomain((CreditCardDTO) paymentMethodDto);
		} else if (paymentMethodDto instanceof PaymentTokenDto) {
			transformedPaymentMethod = getPaymentTokenDTOTransformer().transformToDomain((PaymentTokenDto) paymentMethodDto);
		}

		return transformedPaymentMethod;
	}

	private PaymentMethodDto transformPaymentMethodToDto(final PaymentMethod paymentMethod) {
		PaymentMethodDto transformedPaymentMethodDto = null;
		if (paymentMethod instanceof CustomerCreditCard) {
			CreditCardDTO nonFilteredCreditCardDto = getCredtCardDTOTransformer().transformToDto((CustomerCreditCard) paymentMethod);
			transformedPaymentMethodDto = getCardFilter().filter(nonFilteredCreditCardDto);
		} else if (paymentMethod instanceof PaymentToken) {
			transformedPaymentMethodDto = getPaymentTokenDTOTransformer().transformToDto((PaymentToken) paymentMethod);
		}

		return transformedPaymentMethodDto;
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
		return getBeanFactory().getBean(ContextIdNames.CUSTOMER_ADDRESS);
	}

	protected CustomerCreditCard getCustomerCreditCard() {
		return getBeanFactory().getBean(ContextIdNames.CUSTOMER_CREDIT_CARD);
	}

	public void setCardFilter(final CreditCardFilter filter) {
		cardFilter = filter;
	}

	/**
	 * Gets the card filter. <br>
	 * If a card filter not set then use the {@link BuiltinFilters.EMPTYING} filter.
	 *
	 * @return the card filter
	 */
	protected CreditCardFilter getCardFilter() {
		if (cardFilter == null) {
			cardFilter = BuiltinFilters.EMPTYING;
		}
		return cardFilter;
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

	public void setCreditCardDTOTransformer(final CreditCardDTOTransformer creditCardDTOTransformer) {
		this.credtCardDTOTransformer = creditCardDTOTransformer;
	}

	protected CreditCardDTOTransformer getCredtCardDTOTransformer() {
		return this.credtCardDTOTransformer;
	}

	public void setPaymentTokenDTOTransformer(final PaymentTokenDTOTransformer paymentTokenDTOTransformer) {
		this.paymentTokenDTOTransformer = paymentTokenDTOTransformer;
	}

	protected PaymentTokenDTOTransformer getPaymentTokenDTOTransformer() {
		return this.paymentTokenDTOTransformer;
	}
}
