/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator.AccountAttributeValidator;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Account Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountEntityRepositoryImpl<E extends AccountEntity, I extends AccountIdentifier>
		implements Repository<AccountEntity, AccountIdentifier> {

	private ResourceOperationContext resourceOperationContext;

	private UserAccountAssociationService userAccountAssociationService;

	private CustomerService customerService;

	private CustomerProfileAttributeService customerProfileAttributeService;

	private CustomerProfileValueTransformer customerProfileValueTransformer;

	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	private AccountAttributeValidator accountAttributeValidator;

	private AttributeService attributeService;

	private CustomerRepository customerRepository;

	private DateForEditTransformer dateForEditTransformer;

	@Override
	public Single<AccountEntity> findOne(final AccountIdentifier identifier) {
		String accountId = identifier.getAccountId().getValue();
		return Single.just(convertCustomerToAccountEntity(identifier.getAccounts().getScope().getValue(), customerService.findByGuid(accountId)));
	}

	@Override
	public Observable<AccountIdentifier> findAll(final IdentifierPart<String> scope) {
		String userId = resourceOperationContext.getUserIdentifier();
		return Observable.fromIterable(userAccountAssociationService.findAssociationsForUser(userId).stream()
				.map(accountAssociation -> AccountIdentifier.builder()
						.withAccountId(StringIdentifier.of(accountAssociation.getAccountGuid()))
						.withAccounts(AccountsIdentifier.builder().withScope(scope).build())
						.build())
				.collect(Collectors.toList()));
	}

	@Override
	public Completable update(final AccountEntity entity, final AccountIdentifier accountIdentifier) {
		return accountAttributeValidator.validate(entity, accountIdentifier)
				.andThen(customerRepository.getCustomer(accountIdentifier.getAccountId().getValue())
						.map(account -> updateAccountData(entity, accountIdentifier, account))
						.flatMapCompletable(customerRepository::updateCustomer));
	}

	/**
	 * Updates the customer attributes with the profile entity.
	 *
	 * @param accountEntity     account entity
	 * @param accountIdentifier the account identifier
	 * @param account          customer to update
	 * @return the updated customer
	 */
	protected Customer updateAccountData(final AccountEntity accountEntity, final AccountIdentifier accountIdentifier, final Customer account) {
		final Map<String, Attribute> attributeMap = attributeService.getCustomerProfileAttributesMap(AttributeUsageImpl.ACCOUNT_PROFILE_USAGE);

		// replace map keys with internal keys
		Map<String, String> dynamicProperties = Maps.newHashMap();

		accountEntity.getDynamicProperties()
				.forEach((key, value) -> dynamicProperties.put(profileAttributeFieldTransformer.transformToAttributeKey(key),
						value));

		// add or update keys with non-empty values
		Set<String> updateKeys = dynamicProperties.entrySet()
				.stream()
				.filter(entry -> !StringUtils.isEmpty(entry.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		updateKeys.forEach(key ->
				this.updateCustomerAttribute(key, StringUtils.trimToNull(dynamicProperties.get(key)), account, attributeMap));

		// get set of editable attribute keys that haven't been updated - semantically this implies they should be removed
		List<String> attributeKeysForRemoval =
				customerProfileAttributeService.getCustomerEditableAttributeKeys(accountIdentifier.getAccounts().getScope().getValue())
						.stream()
						.filter(key -> !updateKeys.contains(key))
						.collect(Collectors.toList());

		removeAttributes(attributeKeysForRemoval, account);

		return account;
	}

	@SuppressWarnings({"PMD.MissingBreakInSwitch", "fallthrough"}) // PMD false positive bug - https://sourceforge.net/p/pmd/bugs/1262
	private void updateCustomerAttribute(final String attributeKey, final String value, final Customer customer,
										 final Map<String, Attribute> attributeMap) {
		final AttributeType attributeType = attributeMap.get(attributeKey).getAttributeType();
		switch (attributeType.getTypeId()) {
			case AttributeType.DATE_TYPE_ID:
			case AttributeType.DATETIME_TYPE_ID:
				customer.getCustomerProfile().setProfileValue(attributeKey,
						dateForEditTransformer.transformToDomain(attributeType, value)
								.orElseThrow(() -> new IllegalArgumentException("Date value is missing.")));
				break;
			default:
				customer.getCustomerProfile().setStringProfileValue(attributeKey, value);
		}
	}

	private void removeAttributes(final List<String> attributeKeysForRemoval, final Customer customer) {
		Map<String, CustomerProfileValue> profileValueMap = Maps.newHashMap(customer.getProfileValueMap());
		attributeKeysForRemoval.forEach(profileValueMap::remove);
		customer.setProfileValueMap(profileValueMap);
	}

	/**
	 * Converts given Customer to AccountEntity.
	 *
	 * @param scope    the scope
	 * @param customer customer to convert
	 * @return the converted AccountEntity
	 */
	protected AccountEntity convertCustomerToAccountEntity(final String scope, final Customer customer) {
		AccountEntity.Builder builder = AccountEntity.builder();
		Map<String, Optional<CustomerProfileValue>> attributes = customerProfileAttributeService
				.getAccountEditableAttributes(scope, customer);
		attributes.keySet().forEach(key -> addProperty(builder, key, attributes));
		return builder.build();
	}

	private void addProperty(final AccountEntity.Builder builder, final String key,
							 final Map<String, Optional<CustomerProfileValue>> attributeValueMap) {
		String value = null;
		Optional<CustomerProfileValue> optionalCustomerProfileValue = attributeValueMap.get(key);

		if (optionalCustomerProfileValue.isPresent()) {
			value = customerProfileValueTransformer.transformToString(optionalCustomerProfileValue.get());
		}
		builder.addingProperty(profileAttributeFieldTransformer.transformToFieldName(key), value);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	@Reference
	public void setCustomerProfileAttributeService(final CustomerProfileAttributeService customerProfileAttributeService) {
		this.customerProfileAttributeService = customerProfileAttributeService;
	}

	@Reference
	public void setCustomerProfileValueTransformer(final CustomerProfileValueTransformer customerProfileValueTransformer) {
		this.customerProfileValueTransformer = customerProfileValueTransformer;
	}

	@Reference
	public void setProfileAttributeFieldTransformer(final ProfileAttributeFieldTransformer profileAttributeFieldTransformer) {
		this.profileAttributeFieldTransformer = profileAttributeFieldTransformer;
	}

	@Reference
	public void setAccountAttributeValidator(final AccountAttributeValidator accountAttributeValidator) {
		this.accountAttributeValidator = accountAttributeValidator;
	}

	@Reference
	public void setDateForEditTransformer(final DateForEditTransformer dateForEditTransformer) {
		this.dateForEditTransformer = dateForEditTransformer;
	}

	@Reference
	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}


}
