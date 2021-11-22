/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.ExecutionRetryHelper;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.cache.CacheRemove;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.chain.Assign;
import com.elasticpath.rest.chain.BrokenChainException;
import com.elasticpath.rest.chain.Ensure;
import com.elasticpath.rest.chain.ExecutionResultChain;
import com.elasticpath.rest.chain.OnFailure;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.dto.CustomerDTO;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.customer.UserAccountAssociationService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

/**
 * A repository for {@link Customer}s.
 */
@Singleton
@Named("customerRepository")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
public class CustomerRepositoryImpl implements CustomerRepository {

	private static final Logger LOG = LoggerFactory.getLogger(CustomerRepositoryImpl.class);
	private static final String CUSTOMER_WAS_NOT_FOUND = "Customer was not found.";
	private static final boolean IS_AUTHENTICATE = false;
	private static final String ACCOUNT_ROLE_FIELD_SETTING_PATH = "COMMERCE/SYSTEM/JWT/singleSessionUserRole";
	private static final int RETRY_COUNT = 1;

	private final CustomerService customerService;
	private final AccountTreeService accountTreeService;
	private final CustomerSessionService customerSessionService;
	private final BeanFactory coreBeanFactory;
	private final ReactiveAdapter reactiveAdapter;
	private final SettingsReader settingsReader;
	private final UserAccountAssociationService userAccountAssociationService;
	private final AddressService addressService;


	/**
	 * Constructor.
	 * @param customerService               The customer service
	 * @param accountTreeService            the account tree service
	 * @param customerSessionService        the customer session service
	 * @param coreBeanFactory               the bean factory
	 * @param reactiveAdapter               the reactive adapter
	 * @param settingsReader                the settings reader
	 * @param userAccountAssociationService the user account association service
	 * @param addressService the address service
	 */
	@Inject
	@SuppressWarnings({"checkstyle:parameternumber", "PMD.ExcessiveParameterList"})
	public CustomerRepositoryImpl(
			@Named("customerService") final CustomerService customerService,
			@Named("accountTreeService") final AccountTreeService accountTreeService,
			@Named("customerSessionService") final CustomerSessionService customerSessionService,
			@Named("coreBeanFactory") final BeanFactory coreBeanFactory,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter,
			@Named("settingsReader") final SettingsReader settingsReader,
			@Named("userAccountAssociationService") final UserAccountAssociationService userAccountAssociationService,
			@Named("addressService") final AddressService addressService) {

		this.customerService = customerService;
		this.accountTreeService = accountTreeService;
		this.customerSessionService = customerSessionService;
		this.coreBeanFactory = coreBeanFactory;
		this.reactiveAdapter = reactiveAdapter;
		this.settingsReader = settingsReader;
		this.userAccountAssociationService = userAccountAssociationService;
		this.addressService = addressService;
	}

	@Override
	public Customer createNewCustomerEntity() {
		return coreBeanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
	}

	@Override
	@CacheResult(uniqueIdentifier = "findCustomerBySharedId")
	public ExecutionResult<Customer> findCustomerBySharedId(final String storeCode, final String sharedId) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				// If storeCode is null, then the search is for account
				Customer customer;
				if (storeCode == null) {
					customer = customerService.findBySharedId(sharedId, CustomerType.ACCOUNT);
				} else {
					customer = customerService.findBySharedId(sharedId, CustomerType.SINGLE_SESSION_USER);
				}
				Ensure.notNull(customer, OnFailure.returnNotFound(CUSTOMER_WAS_NOT_FOUND));
				customer.setStoreCode(storeCode);
				return ExecutionResultFactory.createReadOK(customer);
			}
		}.execute();
	}

	@Override
	public ExecutionResult<Customer> findOrCreateUser(final CustomerDTO customerDTO, final String scope,
													  final String sharedId, final String accountSharedId) {

		// Retry execution of findOrCreateUser to handle race condition scenario.
		return ExecutionRetryHelper.withRetry(
				() -> findOrCreateUserInternal(customerDTO, scope, sharedId, accountSharedId),
				RETRY_COUNT,
				"findOrCreateUser",
				exception -> {
					throw processException(exception);
				});
	}

	private BrokenChainException processException(final Exception exception) {
		return exception instanceof BrokenChainException
				? (BrokenChainException) exception
				: new BrokenChainException(exception.getMessage());
	}

	private ExecutionResult<Customer> findOrCreateUserInternal(final CustomerDTO customerDTO, final String scope,
															   final String sharedId, final String accountSharedId) {
		final ExecutionResult<Customer> userExecutionResult = findCustomerBySharedId(scope, sharedId);

		if (isSingleSessionUserExistsInDatabase(userExecutionResult)) {
			return userExecutionResult;
		}

		final Customer account = Assign.ifSuccessful(findAccount(accountSharedId));
		final Customer user = Assign.ifSuccessful(createUser(customerDTO));

		if (Objects.nonNull(account) && Objects.nonNull(user)) {
			final String accountRole = getAccountRoleSettingValue(scope);
			userAccountAssociationService.associateUserToAccount(user, account, accountRole);
		}

		return ExecutionResultFactory.createReadOK(user);
	}

	/**
	 * Derives the Account role for given scope using corresponding setting path.
	 *
	 * @param scope scope
	 * @return accountRoleSettingValue
	 */
	protected String getAccountRoleSettingValue(final String scope) {
		final SettingValue accountRoleSettingValue = settingsReader.getSettingValue(ACCOUNT_ROLE_FIELD_SETTING_PATH, scope);
		return accountRoleSettingValue.getValue();
	}

	private ExecutionResult<Customer> findAccount(final String accountSharedId) {
		if (Objects.isNull(accountSharedId)) {
			return createFailedExecutionResult("JWT.token.missing.account.identifier", "JWT token must contain account identifier");
		}

		ExecutionResult<Customer> account = findCustomerBySharedId(null, accountSharedId);

		if (account.isFailure()) {
			return createFailedExecutionResult("authentication.account.not.found", "No account found for the provided shared ID.");
		}

		return account;
	}

	private ExecutionResult<Customer> createFailedExecutionResult(final String messageId, final String debugMessage) {
		return ExecutionResult.<Customer>builder()
				.withStructuredErrorMessages(Collections.singletonList(Message.builder()
						.withId(messageId)
						.withDebugMessage(debugMessage)
						.build()))
				.withResourceStatus(ResourceStatus.BAD_REQUEST_BODY)
				.build();
	}

	private ExecutionResult<Customer> createUser(final CustomerDTO customerDTO) {
		final Customer customerToAuthenticate = createCustomer(customerDTO);
		Customer singleSessionCustomer = addUnauthenticatedUserInternal(customerToAuthenticate);

		if (Objects.isNull(singleSessionCustomer)) {
			return ExecutionResultFactory.createServerError("Error occurred while adding unauthenticated user.");
		}

		return ExecutionResultFactory.createReadOK(singleSessionCustomer);
	}

	/**
	 * Creates customer by getting values from customer data transfer object.
	 *
	 * @param customerDTO customer data transfer object
	 * @return {@link Customer}
	 */
	private Customer createCustomer(final CustomerDTO customerDTO) {
		final Customer customer = createNewCustomerEntity();
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);
		customer.setStoreCode(customerDTO.getStoreCode());
		customer.setSharedId(customerDTO.getSharedId());
		customer.setEmail(customerDTO.getEmail());
		customer.setFirstName(customerDTO.getFirstName());
		customer.setLastName(customerDTO.getLastName());
		customer.setUsername(customerDTO.getUsername());
		customer.setCompany(customerDTO.getUserCompany());

		return customer;
	}

	private boolean isSingleSessionUserExistsInDatabase(final ExecutionResult<Customer> customerResult) {
		return customerResult.isSuccessful() && isSingleSessionUserType(customerResult.getData());
	}

	private boolean isSingleSessionUserType(final Customer customer) {
		return customer.getCustomerType().equals(CustomerType.SINGLE_SESSION_USER);
	}

	@Override
	@CacheResult
	public ExecutionResult<Customer> findCustomerByGuidAndStoreCode(final String customerGuid, final String storeCode) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				Customer customer = customerService.findByGuid(customerGuid);
				Ensure.notNull(customer, OnFailure.returnNotFound(CUSTOMER_WAS_NOT_FOUND));
				customer.setStoreCode(storeCode);
				return ExecutionResultFactory.createReadOK(customer);
			}
		}.execute();
	}

	@Override
	@CacheResult
	public ExecutionResult<List<Customer>> findCustomersByProfileAttributeKeyAndValue(final String profileAttributeKey,
																					  final String profileAttributeValue) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				try {
					List<Customer> customers =
							customerService.findCustomersByProfileAttributeKeyAndValue(profileAttributeKey, profileAttributeValue);

					return ExecutionResultFactory.createReadOK(customers);
				} catch (EpServiceException e) {
					LOG.error("Error finding customer by profile value text", e);
					return ExecutionResultFactory.createServerError("Server error when finding customer");
				}
			}
		}.execute();
	}

	@Override
	@CacheResult
	public ExecutionResult<Boolean> isCustomerGuidExists(final String guid) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final boolean customerExists = customerService.isCustomerGuidExists(guid);
				if (!customerExists) {
					return ExecutionResultFactory.createNotFound();
				}
				return ExecutionResultFactory.createReadOK(Boolean.TRUE);
			}
		}.execute();
	}

	@Override
	@CacheResult
	public ExecutionResult<Long> getCustomerCountByProfileAttributeKeyAndValue(final String profileAttributeKey,
																			   final String profileAttributeValue) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final Long customerCount = customerService.getCustomerCountByProfileAttributeKeyAndValue(profileAttributeKey, profileAttributeValue);
				if (customerCount == 0) {
					return ExecutionResultFactory.createNotFound();
				}
				return ExecutionResultFactory.createReadOK(customerCount);
			}
		}.execute();
	}

	@Override
	@CacheResult
	public ExecutionResult<Boolean> isCustomerExistsBySharedIdAndStoreCode(final CustomerType customerType, final String sharedId) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final boolean customerExists = customerService.isCustomerExistsBySharedId(sharedId, customerType);
				if (!customerExists) {
					return ExecutionResultFactory.createNotFound();
				}
				return ExecutionResultFactory.createReadOK(Boolean.TRUE);
			}
		}.execute();
	}

	@Override
	@CacheResult
	public ExecutionResult<String> findCustomerGuidBySharedId(final CustomerType customerType, final String sharedId,
															  final String customerIdentifierKey) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final String customerGuid = customerService.findCustomerGuidBySharedId(sharedId, customerType);
				if (StringUtils.isEmpty(customerGuid)) {
					return ExecutionResultFactory.createNotFound();
				}
				return ExecutionResultFactory.createReadOK(customerGuid);
			}
		}.execute();
	}

	@Override
	@CacheResult
	public ExecutionResult<String> findCustomerGuidByProfileAttributeKeyAndValue(final String profileAttributeKey,
																				 final String profileAttributeValue) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final String customerGuid = customerService.findCustomerGuidByProfileAttributeKeyAndValue(profileAttributeKey,
						profileAttributeValue);
				if (StringUtils.isEmpty(customerGuid)) {
					return ExecutionResultFactory.createNotFound();
				}
				return ExecutionResultFactory.createReadOK(customerGuid);
			}
		}.execute();
	}

	@Override
	@CacheResult(uniqueIdentifier = "findCustomerByUsername")
	public ExecutionResult<Customer> findCustomerByUsername(final String username, final String storeCode) {
		return new ExecutionResultChain() {
			@Override
			public ExecutionResult<?> build() {
				final Customer customer = Assign.ifSuccessful(findCustomerByUserNameWithoutException(username, storeCode));
				Ensure.notNull(customer, OnFailure.returnNotFound(CUSTOMER_WAS_NOT_FOUND));
				customer.setStoreCode(storeCode);
				return ExecutionResultFactory.createReadOK(customer);

			}
		}.execute();

	}

	private ExecutionResult<Customer> findCustomerByUserNameWithoutException(final String username, final String storeCode) {
		Customer customer = null;
		try {
			customer = customerService.findCustomerByUserName(username, storeCode);
		} catch (final Exception e) {
			LOG.error(String.format("Error when finding customer by store code %s and user name %s", storeCode, username), e);
			return ExecutionResultFactory.createServerError("Server error when finding customer by user name");
		}

		return ExecutionResultFactory.createReadOK(customer);
	}


	@Override
	@CacheResult
	public Single<Customer> getCustomer(final String guid) {
		return reactiveAdapter.fromServiceAsSingle(() -> customerService.findByGuid(guid), CUSTOMER_WAS_NOT_FOUND);
	}

	@Override
	public String getCustomerGuid(final String userGuid, final Subject subject) {
		return ObjectUtils.firstNonNull(getAccountGuid(subject), userGuid);
	}

	@Override
	public String getAccountGuid(final Subject subject) {
		return Optional.ofNullable(SubjectUtil.getAccountSharedId(subject))
				.map(sharedId-> customerService.findCustomerGuidBySharedId(sharedId, CustomerType.ACCOUNT))
				.orElse(null);
	}

	@Override
	@CacheRemove(typesToInvalidate = Customer.class)
	public Completable updateCustomer(final Customer customer) {
		return reactiveAdapter.fromServiceAsCompletable(() -> customerService.update(customer));
	}

	@Override
	@CacheRemove(typesToInvalidate = Customer.class)
	public Single<Customer> addAddress(final Customer customer, final CustomerAddress address) {
		return reactiveAdapter.fromServiceAsSingle(() -> customerService.addOrUpdateAddress(customer, address));
	}

	@Override
	@CacheRemove(typesToInvalidate = Customer.class)
	public Completable updateAddress(final Customer customer, final CustomerAddress address) {
		return reactiveAdapter.fromServiceAsCompletable(() -> customerService.addOrUpdateAddress(customer, address));
	}

	@Override
	@CacheRemove(typesToInvalidate = Customer.class)
	public Single<Customer> update(final Customer updatedCustomer) {
		return reactiveAdapter.fromServiceAsSingle(() -> customerService.update(updatedCustomer));
	}

	@Override
	public ExecutionResult<Object> mergeCustomer(final Shopper singleSessionShopper,
												 final Customer registeredCustomer,
												 final String validatedStoreCode) {
		try {
			customerSessionService.changeFromSingleSessionToRegisteredCustomer(singleSessionShopper, registeredCustomer, validatedStoreCode);
		} catch (final Exception exception) {
			LOG.error("Error merging customer session", exception);
			return ExecutionResultFactory.createServerError("Server error when merging customer session");
		}
		return ExecutionResultFactory.createUpdateOK();
	}

	@Override
	public ExecutionResult<Customer> addUnauthenticatedUser(final Customer customer) {
		try {
			final Customer authenticatedCustomer = addUnauthenticatedUserInternal(customer);
			return ExecutionResultFactory.createReadOK(authenticatedCustomer);
		} catch (EpServiceException e) {
			return ExecutionResultFactory.createServerError("Error occurred while adding unauthenticated user. " + e.getLocalizedMessage());
		}
	}

	@CacheRemove(typesToInvalidate = Customer.class)
	private Customer addUnauthenticatedUserInternal(final Customer customer) {
		return customerService.addByAuthenticate(customer, IS_AUTHENTICATE);
	}

	@Override
	public boolean isFirstTimeBuyer(final Customer customer) {
		return customer.isFirstTimeBuyer();
	}

	@Override
	public Single<CustomerAddress> createAddressForCustomer(final Customer customer, final CustomerAddress customerAddress) {
		customerAddress.setCustomerUidPk(customer.getUidPk());
		CustomerAddress existingAddress = addressService.findByAddress(customer.getUidPk(), customerAddress);
		if (existingAddress == null) {
			return addAddress(customer, customerAddress)
					.map(updatedCustomer -> customerAddress);
		}

		return Single.just(existingAddress);
	}

	@Override
	public List<String> findDescendants(final String accountGuid) {
		return accountTreeService.findDescendantGuids(accountGuid);
	}

	@Override
	public List<String> findPaginatedChildren(final String accountId, final int pageStartIndex, final int pageSize) {
		return accountTreeService.findChildGuidsPaginated(accountId, pageStartIndex, pageSize);
	}

}
