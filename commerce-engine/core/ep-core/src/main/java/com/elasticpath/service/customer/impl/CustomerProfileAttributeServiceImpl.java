/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.AttributePolicyService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictor;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.validation.service.AttributeValueValidationService;

/**
 * Default implementation of <code>CustomerProfileAttributeService</code>.
 */
public class CustomerProfileAttributeServiceImpl implements CustomerProfileAttributeService {

	private AttributeService attributeService;

	private StoreService storeService;

	private FetchGroupLoadTuner fetchGroupLoadTuner;

	private StoreCustomerAttributeService storeCustomerAttributeService;

	private AttributePolicyService attributePolicyService;

	private PolicyKey defaultPolicyKey = PolicyKey.DEFAULT;

	private List<PolicyPermission> editPermissions = Lists.newArrayList(PolicyPermission.EDIT);

	private List<PolicyPermission> readOnlyPermissions = Lists.newArrayList(PolicyPermission.EMIT);

	private AttributeValueValidationService attributeValueValidationService;

	private Map<String, CustomerProfileAttributeValueRestrictor> valueRestrictors;

	private Map<String, PolicyKey> predefinedProfileAttributePolicies;

	@Override
	public Set<String> getCustomerEditableAttributeKeys(final String storeCode) {
		return getAttributeKeysByPermission(storeCode, getEditPermissions(), Collections.emptyList());
	}

	@Override
	public Map<String, Optional<CustomerProfileValue>> getCustomerEditableAttributes(final String storeCode, final Customer customer) {
		return getAttributesByPermission(storeCode, customer, getEditPermissions(), Collections.emptyList());
	}

	@Override
	public Map<String, Optional<CustomerProfileValue>> getCustomerReadOnlyAttributes(final String storeCode, final Customer customer) {
		return getAttributesByPermission(storeCode, customer, getReadOnlyPermissions(), getEditPermissions());
	}

	@Override
	public Map<String, Optional<CustomerProfileValue>> getAccountReadOnlyAttributes(final String storeCode, final Customer account) {
		return getAccountAttributesByPermission(storeCode, account, getReadOnlyPermissions(), getEditPermissions());
	}

	@Override
	public Map<String, Optional<CustomerProfileValue>> getAccountEditableAttributes(final String storeCode, final Customer account) {
		Map<String, Optional<CustomerProfileValue>> accountReadOnlyAttributes = getAccountReadOnlyAttributes(storeCode, account);
		Map<String, Optional<CustomerProfileValue>> accountAttributes = getAccountAttributes(account);
		accountReadOnlyAttributes.forEach((key, value) -> accountAttributes.remove(key));
		return accountAttributes;
	}

	@Override
	public Map<String, Optional<CustomerProfileValue>> getAccountAttributes(final Customer customer) {
		Map<String, Optional<CustomerProfileValue>> attributeValueMap = Maps.newHashMap();
		getAttributeKeysByUsage(AttributeUsageImpl.ACCOUNT_PROFILE_USAGE)
				.forEach(key -> attributeValueMap.put(key, Optional.ofNullable(customer.getProfileValueMap().get(key))));
		return attributeValueMap;
	}

	@Override
	public Collection<StructuredErrorMessage> validateAttributes(final Map<String, String> attributeValueMap,
																 final String storeCode, final AttributeUsage attributeUsage) {
		if (attributeValueMap.isEmpty()) {
			return Collections.emptySet();
		}

		Map<String, Attribute> attributeMap = attributeService.getCustomerProfileAttributesMap(attributeUsage);
		final CustomerProfileAttributeValueRestrictorContext context = getRestrictorContext(storeCode);

		Map<Attribute, Set<String>> referentAttributes = attributeMap.values().stream()
				.collect(Collectors.toMap(Function.identity(),
						attribute -> getRestrictedValues(attribute.getKey(), context)));

		return attributeValueValidationService.validate(attributeValueMap, referentAttributes);
	}

	/**
	 * Gets the restricted values for a given attribute.
	 *
	 * @param key     the attribute key
	 * @param context the customer profile attribute context
	 * @return the array of restricted values for the key
	 */
	protected Set<String> getRestrictedValues(final String key, final CustomerProfileAttributeValueRestrictorContext context) {
		if (valueRestrictors.get(key) != null) {
			return valueRestrictors.get(key).getRestrictedValues(context);
		}
		return Collections.emptySet();
	}

	/**
	 * Gets the context for restricted value lookup.
	 *
	 * @param storeCode the store code
	 * @return the context
	 */
	protected CustomerProfileAttributeValueRestrictorContext getRestrictorContext(final String storeCode) {
		return new CustomerProfileAttributeValueRestrictorContextImpl(getSharedLoginStores(storeCode));
	}

	/**
	 * Gets a map of attribute key to attribute corresponding to a given set of allowed and disallowed permissions. Disallowed permissions
	 * take precedence.
	 *
	 * @param storeCode             the store code
	 * @param customer              the customer
	 * @param allowedPermissions    the allowed permissions
	 * @param disallowedPermissions the disallowed permissions
	 * @return the map of attribute key to customer profile value
	 */
	protected Map<String, Optional<CustomerProfileValue>> getAttributesByPermission(final String storeCode, final Customer customer,
																					final List<PolicyPermission> allowedPermissions,
																					final List<PolicyPermission> disallowedPermissions) {
		Map<String, Optional<CustomerProfileValue>> attributeValueMap = Maps.newHashMap();

		// using foreach instead of a Collector because values can be null
		getAttributeKeysByPermission(storeCode, allowedPermissions, disallowedPermissions)
				.stream().forEach(key -> attributeValueMap.put(key, Optional.ofNullable(customer.getProfileValueMap().get(key))));

		return attributeValueMap;
	}

	/**
	 * Gets the set of attribute keys corresponding to a given set of allowed and disallowed permissions.
	 * Disallowed permissions take precedence.
	 *
	 * @param storeCode             the store code
	 * @param allowedPermissions    the allowed permissions
	 * @param disallowedPermissions the disallowed permissions
	 * @return the set of permitted attribute keys
	 */
	protected Set<String> getAttributeKeysByPermission(final String storeCode,
													   final List<PolicyPermission> allowedPermissions,
													   final List<PolicyPermission> disallowedPermissions) {
		final Set<String> attributeKeys = Sets.newHashSet();
		final List<StoreCustomerAttribute> storeCustomerAttributes = getStoreCustomerAttributeService().findByStore(storeCode);
		Map<PolicyKey, Set<PolicyPermission>> policies = getPolicies();

		getAttributeService().getCustomerProfileAttributeKeys(AttributeUsageImpl.USER_PROFILE_USAGE).stream()
				.filter(key -> this.isAttributePermissible(key, storeCustomerAttributes, policies, allowedPermissions, disallowedPermissions))
				.forEach(attributeKeys::add);

		return attributeKeys;
	}

	/**
	 * Gets the set of account attribute keys corresponding to a given set of allowed and disallowed permissions.
	 * Disallowed permissions take precedence.
	 *
	 * @param storeCode             the store code
	 * @param allowedPermissions    the allowed permissions
	 * @param disallowedPermissions the disallowed permissions
	 * @return the set of permitted attribute keys
	 */
	protected Set<String> getAccountAttributeKeysByPermission(final String storeCode,
															  final List<PolicyPermission> allowedPermissions,
															  final List<PolicyPermission> disallowedPermissions) {
		final Set<String> attributeKeys = Sets.newHashSet();
		final List<StoreCustomerAttribute> storeCustomerAttributes = getStoreCustomerAttributeService().findByStore(storeCode);
		Map<PolicyKey, Set<PolicyPermission>> policies = getPolicies();

		getAttributeService().getCustomerProfileAttributeKeys(AttributeUsageImpl.ACCOUNT_PROFILE_USAGE).stream()
				.filter(key -> this.isAttributePermissible(key, storeCustomerAttributes, policies, allowedPermissions, disallowedPermissions))
				.forEach(attributeKeys::add);

		return attributeKeys;
	}

	/**
	 * Gets the set of attribute keys for a given attribute usage.
	 *
	 * @param usage the attribute usage
	 * @return the set of attribute keys
	 */
	protected Set<String> getAttributeKeysByUsage(final AttributeUsage usage) {
		final Set<String> attributeKeys = Sets.newHashSet();
		attributeKeys.addAll(getAttributeService().getCustomerProfileAttributeKeys(AttributeUsageImpl.ACCOUNT_PROFILE_USAGE));
		return attributeKeys;
	}

	/**
	 * Verifies if an attribute is permissible given the set of allowed and disallowed permissions.
	 * Disallowed permissions take precedence.
	 *
	 * @param attributeKey            the attribute key
	 * @param storeCustomerAttributes the store customer attributes
	 * @param policies                the set of attribute policies
	 * @param allowedPermissions      the allowed permissions
	 * @param disallowedPermissions   the disallowed permissions
	 * @return if the attribute is permissible
	 */
	protected boolean isAttributePermissible(final String attributeKey, final List<StoreCustomerAttribute> storeCustomerAttributes,
											 final Map<PolicyKey, Set<PolicyPermission>> policies, final List<PolicyPermission> allowedPermissions,
											 final List<PolicyPermission> disallowedPermissions) {
		// if a predefined policy exists it always takes precedence
		PolicyKey policyKey = getPredefinedProfileAttributePolicies().get(attributeKey);

		if (policyKey == null) {
			policyKey = getDefaultPolicyKey();
			Optional<StoreCustomerAttribute> storeCustomerAttribute = storeCustomerAttributes.stream()
					.filter(sca -> sca.getAttributeKey().equals(attributeKey))
					.findFirst();
			if (storeCustomerAttribute.isPresent()) {
				policyKey = storeCustomerAttribute.get().getPolicyKey();
			}
		}

		Set<PolicyPermission> permissions = policies.get(policyKey);

		// check disallowed permissions
		boolean disallowed = permissions.stream().anyMatch(disallowedPermissions::contains);
		boolean allowed = permissions.stream().anyMatch(allowedPermissions::contains);

		return !disallowed && allowed;
	}

	/**
	 * Get account attributes by store and permissions.
	 *
	 * @param storeCode             the store code
	 * @param account               account
	 * @param allowedPermissions    allowed permission
	 * @param disallowedPermissions disallowed permissions
	 * @return the account attributes
	 */
	protected Map<String, Optional<CustomerProfileValue>> getAccountAttributesByPermission(
			final String storeCode, final Customer account,
			final List<PolicyPermission> allowedPermissions,
			final List<PolicyPermission> disallowedPermissions) {
		Map<String, Optional<CustomerProfileValue>> attributeValueMap = Maps.newHashMap();
		Set<String> accountAttributeKeysByPermission = getAccountAttributeKeysByPermission(storeCode, allowedPermissions, disallowedPermissions);
		accountAttributeKeysByPermission.forEach(key -> attributeValueMap.put(key, Optional.ofNullable(account.getProfileValueMap().get(key))));
		return attributeValueMap;
	}

	private Collection<Store> getSharedLoginStores(final String storeCode) {
		return storeService.getTunedStores(storeService.getTunedStore(storeCode, fetchGroupLoadTuner).getAssociatedStoreUids(),
				fetchGroupLoadTuner);
	}

	public AttributeService getAttributeService() {
		return attributeService;
	}

	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	public StoreCustomerAttributeService getStoreCustomerAttributeService() {
		return storeCustomerAttributeService;
	}

	public void setStoreCustomerAttributeService(final StoreCustomerAttributeService storeCustomerAttributeService) {
		this.storeCustomerAttributeService = storeCustomerAttributeService;
	}

	public AttributePolicyService getAttributePolicyService() {
		return attributePolicyService;
	}

	public void setAttributePolicyService(
			final AttributePolicyService attributePolicyService) {
		this.attributePolicyService = attributePolicyService;
	}

	public PolicyKey getDefaultPolicyKey() {
		return defaultPolicyKey;
	}

	public void setDefaultPolicyKey(final PolicyKey defaultPolicyKey) {
		this.defaultPolicyKey = defaultPolicyKey;
	}

	public List<PolicyPermission> getEditPermissions() {
		return editPermissions;
	}

	public void setEditPermissions(final List<PolicyPermission> editPermissions) {
		this.editPermissions = editPermissions;
	}

	public List<PolicyPermission> getReadOnlyPermissions() {
		return readOnlyPermissions;
	}

	public void setReadOnlyPermissions(final List<PolicyPermission> readOnlyPermissions) {
		this.readOnlyPermissions = readOnlyPermissions;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public FetchGroupLoadTuner getFetchGroupLoadTuner() {
		return fetchGroupLoadTuner;
	}

	public void setFetchGroupLoadTuner(final FetchGroupLoadTuner fetchGroupLoadTuner) {
		this.fetchGroupLoadTuner = fetchGroupLoadTuner;
	}

	public AttributeValueValidationService getAttributeValueValidationService() {
		return attributeValueValidationService;
	}

	public void setAttributeValueValidationService(final AttributeValueValidationService attributeValueValidationService) {
		this.attributeValueValidationService = attributeValueValidationService;
	}

	public Map<String, CustomerProfileAttributeValueRestrictor> getValueRestrictors() {
		return valueRestrictors;
	}

	public void setValueRestrictors(final Map<String, CustomerProfileAttributeValueRestrictor> valueRestrictors) {
		this.valueRestrictors = valueRestrictors;
	}

	private Map<PolicyKey, Set<PolicyPermission>> getPolicies() {
		Map<PolicyKey, Set<PolicyPermission>> policies = Maps.newHashMap();

		for (AttributePolicy policy : getAttributePolicyService().findAll()) {
			if (policies.get(policy.getPolicyKey()) == null) {
				policies.put(policy.getPolicyKey(), Sets.newHashSet());
			}
			policies.get(policy.getPolicyKey()).add(policy.getPolicyPermission());

		}
		return policies;
	}

	public Map<String, PolicyKey> getPredefinedProfileAttributePolicies() {
		return predefinedProfileAttributePolicies;
	}

	public void setPredefinedProfileAttributePolicies(final Map<String, PolicyKey> predefinedProfileAttributePolicies) {
		this.predefinedProfileAttributePolicies = predefinedProfileAttributePolicies;
	}
}
