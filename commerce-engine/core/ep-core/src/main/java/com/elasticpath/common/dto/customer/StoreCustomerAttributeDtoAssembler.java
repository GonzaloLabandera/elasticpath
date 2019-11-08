/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.customer;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.springframework.util.CollectionUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.store.StoreService;

/**
 * Assembles StoreCustomerAttribute domains and DTOs.
 */
public class StoreCustomerAttributeDtoAssembler
		extends AbstractDtoAssembler<StoreCustomerAttributeDTO, StoreCustomerAttribute> {

	private BeanFactory beanFactory;

	private AttributeService attributeService;

	private StoreService storeService;

	private AttributePolicyService attributePolicyService;

	private CustomerProfileAttributeService customerProfileAttributeService;

	@Override
	public StoreCustomerAttribute getDomainInstance() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.STORE_CUSTOMER_ATTRIBUTE, StoreCustomerAttribute.class);
	}

	@Override
	public void assembleDto(final StoreCustomerAttribute source, final StoreCustomerAttributeDTO target) {
		target.setGuid(source.getGuid());
		target.setStoreCode(source.getStoreCode());
		target.setPolicyKey(source.getPolicyKey().getName());
		target.setAttributeKey(source.getAttributeKey());
	}

	@Override
	public void assembleDomain(final StoreCustomerAttributeDTO source, final StoreCustomerAttribute target) {
		target.setGuid(source.getGuid());

		final Store store = findStore(source.getStoreCode());
		target.setStoreCode(store.getCode());

		final Attribute attribute = findAttribute(source.getAttributeKey());
		validateAttributeIsNotPredefined(attribute);

		target.setAttributeKey(attribute.getKey());

		final PolicyKey policyKey = PolicyKey.valueOf(source.getPolicyKey());
		validatePolicyExists(policyKey);
		target.setPolicyKey(policyKey);
	}

	/**
	 * Returns the store associated with the given code.
	 *
	 * @param storeCode code the store code
	 * @return the store
	 * @throws EpServiceException if the {@link com.elasticpath.domain.store.Store} is not found.
	 */
	protected Store findStore(final String storeCode) {
		Optional<Store> optionalStore = ofNullable(storeService.findStoreWithCode(storeCode));
		return optionalStore.orElseThrow(() -> new EpServiceException("Store with code " + storeCode + " not found."));
	}

	/**
	 * Returns the attribute with the given key.
	 *
	 * @param attributeKey the attribute key
	 * @return the attribute
	 */
	protected Attribute findAttribute(final String attributeKey) {
		Optional<Attribute> optionalAttribute = ofNullable(attributeService.findByKey(attributeKey));
		return optionalAttribute.orElseThrow(() -> new EpServiceException("Attribute with key " + attributeKey + " not found."));
	}

	/**
	 * Validate the given policy exists.
	 *
	 * @param policyKey the policy key
	 */
	protected void validatePolicyExists(final PolicyKey policyKey) {
		if (CollectionUtils.isEmpty(attributePolicyService.findByPolicyKey(policyKey))) {
			throw new EpServiceException("Policy with key " + policyKey + " not found.");
		}
	}

	/**
	 * Validate that the attribute is not part of the predefined attribute policies.
	 *
	 * @param attribute the attribute
	 */
	protected void validateAttributeIsNotPredefined(final Attribute attribute) {
		if (getCustomerProfileAttributeService().getPredefinedProfileAttributePolicies()
				.keySet().contains(attribute.getKey())) {
			throw new EpServiceException("Attribute with key " + attribute.getKey() + " is predefined with a system policy.");
		}
	}

	@Override
	public StoreCustomerAttributeDTO getDtoInstance() {
		return new StoreCustomerAttributeDTO();
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	protected AttributeService getAttributeService() {
		return attributeService;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	protected StoreService getStoreService() {
		return storeService;
	}

	public void setAttributePolicyService(final AttributePolicyService attributePolicyService) {
		this.attributePolicyService = attributePolicyService;
	}

	protected AttributePolicyService getAttributePolicyService() {
		return attributePolicyService;
	}

	public CustomerProfileAttributeService getCustomerProfileAttributeService() {
		return customerProfileAttributeService;
	}

	public void setCustomerProfileAttributeService(final CustomerProfileAttributeService customerProfileAttributeService) {
		this.customerProfileAttributeService = customerProfileAttributeService;
	}
}
