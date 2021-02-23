/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentInstrumentIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPICFieldsRequestContext;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPICRequestContext;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentIdentifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentAttributesEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * The implementation of {@link PaymentInstrumentRepository} related operations.
 */
@Singleton
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.GodClass", "PMD.NPathComplexity"})
@Named("paymentInstrumentRepository")
public class PaymentInstrumentRepositoryImpl implements PaymentInstrumentRepository {

	/**
	 * Payment method is not found for the instrument.
	 */
	static final String PAYMENT_METHOD_IS_NOT_FOUND = "Payment method is not found for the instrument.";

	@Inject
	@Named("cartOrderPaymentInstrumentRepository")
	private CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;

	@Inject
	@Named("customerPaymentInstrumentRepository")
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;

	@Inject
	@Named("customerDefaultPaymentInstrumentRepository")
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;

	@Inject
	@Named("orderPaymentApiRepository")
	private OrderPaymentApiRepository orderPaymentApiRepository;

	@Inject
	@Named("coreBeanFactory")
	private BeanFactory beanFactory;

	@Inject
	@Named("resourceOperationContext")
	private ResourceOperationContext resourceOperationContext;

	@Inject
	@Named("cartOrderRepository")
	private CartOrderRepository cartOrderRepository;

	@Inject
	@Named("customerRepository")
	private CustomerRepository customerRepository;

	@Inject
	@Named("storePaymentProviderConfigRepository")
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Inject
	@Named("addressValidator")
	private AddressValidator addressValidator;

	@Inject
	@Named("conversionService")
	private ConversionService conversionService;

	@Inject
	@Named("filteredPaymentInstrumentService")
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Single<SubmitResult<OrderPaymentInstrumentIdentifier>> submitOrderPaymentInstrument(
			final IdentifierPart<String> scope,
			final OrderPaymentInstrumentForFormEntity formEntity) {

		String customerGuid = resourceOperationContext.getUserIdentifier();

		return customerRepository.getCustomer(customerGuid)
				.flatMap(customer -> resourceOperationContext.getResourceIdentifier()
						.map(resourceIdentifier -> ((OrderPaymentInstrumentFormIdentifier) resourceIdentifier))
						.map(formIdentifier -> createOrderPaymentInstrument(scope, formIdentifier, formEntity, customer))
						.orElse(Single.error(ResourceOperationFailure.notFound(PAYMENT_METHOD_IS_NOT_FOUND))));
	}

	@Override
	public Single<SubmitResult<PaymentInstrumentIdentifier>> submitProfilePaymentInstrument(
			final IdentifierPart<String> scope,
			final PaymentInstrumentForFormEntity formEntity) {

		String customerGuid = resourceOperationContext.getUserIdentifier();

		return customerRepository.getCustomer(customerGuid)
				.flatMap(customer -> resourceOperationContext.getResourceIdentifier()
						.map(resourceIdentifier -> ((ProfilePaymentInstrumentFormIdentifier) resourceIdentifier))
						.map(formIdentifier -> createProfilePaymentInstrument(scope, formIdentifier, formEntity, customer))
						.orElse(Single.error(ResourceOperationFailure.notFound(PAYMENT_METHOD_IS_NOT_FOUND))));
	}

	@Override
	public Single<SubmitResult<AccountPaymentInstrumentIdentifier>> submitAccountPaymentInstrument(
			final IdentifierPart<String> scope,
			final PaymentInstrumentForFormEntity formEntity) {

		String accountGuid = getAccountIdFromResourceOperationContext().getValue();

		return customerRepository.getCustomer(accountGuid)
				.flatMap(customer -> resourceOperationContext.getResourceIdentifier()
						.map(resourceIdentifier -> ((AccountPaymentInstrumentFormIdentifier) resourceIdentifier))
						.map(formIdentifier -> createAccountPaymentInstrument(scope, formIdentifier, formEntity, customer))
						.orElse(Single.error(ResourceOperationFailure.notFound(PAYMENT_METHOD_IS_NOT_FOUND))));
	}

	@Override
	public Single<PaymentInstrumentCreationFieldsDTO> getPaymentInstrumentCreationFieldsForProviderConfigGuid(final String storeProviderConfigGuid) {
		String userId = resourceOperationContext.getUserIdentifier();
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		return customerRepository.getCustomer(userId)
				.map(customer -> buildPICFieldsRequestContext(locale, currency, customer))
				.flatMap(requestContext -> storePaymentProviderConfigRepository.findByGuid(storeProviderConfigGuid)
						.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
						.flatMap(paymentProviderConfigGuid -> orderPaymentApiRepository.getPICFields(paymentProviderConfigGuid, requestContext)));
	}

	@Override
	public Single<PaymentInstrumentCreationFieldsDTO> getAccountPaymentInstrumentCreationFieldsForProviderConfigGuid(
			final String storeProviderConfigGuid, final String accountId) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		return customerRepository.getCustomer(accountId)
				.map(customer -> buildPICFieldsRequestContext(locale, currency, customer))
				.flatMap(requestContext -> storePaymentProviderConfigRepository.findByGuid(storeProviderConfigGuid)
						.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
						.flatMap(paymentProviderConfigGuid -> orderPaymentApiRepository.getPICFields(paymentProviderConfigGuid, requestContext)));
	}

	@Override
	public Observable<PaymentInstrumentIdentifier> findAll(final IdentifierPart<String> scope) {
		final String customerGuid = resourceOperationContext.getUserIdentifier();
		return getCustomerPaymentInstrumentObservable(scope, customerGuid)
				.map(instrument -> buildPaymentInstrumentIdentifier(scope, StringIdentifier.of(instrument.getGuid())));
	}

	@Override
	public Observable<AccountPaymentInstrumentIdentifier> findAllAccountPaymentInstruments(final IdentifierPart<String> scope) {
		final IdentifierPart<String> accountId = getAccountIdFromResourceOperationContext();
		return getCustomerPaymentInstrumentObservable(scope, accountId.getValue())
				.map(instrument -> buildAccountPaymentInstrumentIdentifier(scope, accountId, StringIdentifier.of(instrument.getGuid())));
	}

	@Override
	public Observable<AccountPaymentInstrumentIdentifier> findAllAccountPaymentInstrumentsByAccountId(final String scope, final String accountId) {
		return customerRepository.getCustomer(accountId)
				.flatMapObservable(account -> reactiveAdapter.fromService(() -> filteredPaymentInstrumentService
						.findCustomerPaymentInstrumentsForCustomerAndStore(account, scope)))
				.flatMap(Observable::fromIterable)
				.map(instrument -> buildAccountPaymentInstrumentIdentifier(StringIdentifier.of(scope), StringIdentifier.of(accountId),
						StringIdentifier.of(instrument.getGuid())));
	}

	private Observable<CustomerPaymentInstrument> getCustomerPaymentInstrumentObservable(final IdentifierPart<String> scope,
																						 final String customerGuid) {
		return customerRepository.getCustomer(customerGuid)
				.flatMapObservable(customer -> reactiveAdapter.fromService(() -> filteredPaymentInstrumentService
						.findCustomerPaymentInstrumentsForCustomerAndStore(customer, scope.getValue())))
				.flatMap(Observable::fromIterable);
	}

	private Single<SubmitResult<OrderPaymentInstrumentIdentifier>> createOrderPaymentInstrument(
			final IdentifierPart<String> scope,
			final OrderPaymentInstrumentFormIdentifier formIdentifier,
			final OrderPaymentInstrumentForFormEntity formEntity,
			final Customer customer) {

		final IdentifierPart<String> orderId = formIdentifier.getOrderPaymentMethod().getOrderPaymentMethods().getOrder().getOrderId();
		final IdentifierPart<String> paymentMethodId = formIdentifier.getOrderPaymentMethod().getPaymentMethodId();

		return createCartOrderPaymentInstrument(scope, orderId, paymentMethodId, formEntity, customer)
				.map(instrumentGuid -> buildOrderPaymentInstrumentIdentifier(scope, orderId, StringIdentifier.of(instrumentGuid)))
				.map(instrumentIdentifier -> SubmitResult.<OrderPaymentInstrumentIdentifier>builder()
						.withIdentifier(instrumentIdentifier)
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	private Single<SubmitResult<PaymentInstrumentIdentifier>> createProfilePaymentInstrument(
			final IdentifierPart<String> scope,
			final ProfilePaymentInstrumentFormIdentifier formIdentifier,
			final PaymentInstrumentForFormEntity formEntity,
			final Customer customer) {

		final IdentifierPart<String> paymentMethodId = formIdentifier.getProfilePaymentMethod().getPaymentMethodId();

		return createCustomerPaymentInstrument(paymentMethodId, formEntity, customer)
				.map(instrumentGuid -> buildPaymentInstrumentIdentifier(scope, StringIdentifier.of(instrumentGuid)))
				.map(instrumentIdentifier -> SubmitResult.<PaymentInstrumentIdentifier>builder()
						.withIdentifier(instrumentIdentifier)
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	private Single<SubmitResult<AccountPaymentInstrumentIdentifier>> createAccountPaymentInstrument(
			final IdentifierPart<String> scope,
			final AccountPaymentInstrumentFormIdentifier formIdentifier,
			final PaymentInstrumentForFormEntity formEntity,
			final Customer account) {

		final IdentifierPart<String> accountPaymentMethodId = formIdentifier.getAccountPaymentMethod().getAccountPaymentMethodId();

		return createAccountPaymentInstrument(accountPaymentMethodId, formEntity, account)
				.map(instrumentGuid -> buildAccountPaymentInstrumentIdentifier(scope, StringIdentifier.of(account.getGuid()),
						StringIdentifier.of(instrumentGuid)))
				.map(instrumentIdentifier -> SubmitResult.<AccountPaymentInstrumentIdentifier>builder()
						.withIdentifier(instrumentIdentifier)
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	private Single<String> createCartOrderPaymentInstrument(final IdentifierPart<String> scope,
															final IdentifierPart<String> orderId,
															final IdentifierPart<String> paymentMethodId,
															final OrderPaymentInstrumentForFormEntity formEntity,
															final Customer customer) {

		return cartOrderRepository.findByGuid(scope.getValue(), orderId.getValue())
				.flatMap(cartOrder -> createCartOrderPaymentInstrument(formEntity, cartOrder)
						.flatMap(cartOrderPaymentInstrument -> storePaymentProviderConfigRepository.findByGuid(paymentMethodId.getValue())
								.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
								.flatMapCompletable(providerConfigGuid ->
										configurePaymentInstrumentForCartOrderPaymentInstrument(formEntity, customer,
												cartOrderPaymentInstrument, providerConfigGuid))
								//TODO [payments] when multi-selection enabled, remove "andThen(clearExistingCartOrderPaymentInstruments(cartOrder)".
								// We will no longer need to clear all other instruments.
								.andThen(clearExistingCartOrderPaymentInstruments(cartOrder))
								.andThen(cartOrderPaymentInstrumentRepository.saveOrUpdate(cartOrderPaymentInstrument))
								.map(GloballyIdentifiable::getGuid)));
	}

	private Completable clearExistingCartOrderPaymentInstruments(final CartOrder cartOrder) {
		return cartOrderPaymentInstrumentRepository.findByCartOrder(cartOrder)
				.flatMapCompletable(cartOrderInstrument -> cartOrderPaymentInstrumentRepository.remove(cartOrderInstrument));
	}

	private Completable configurePaymentInstrumentForCartOrderPaymentInstrument(final OrderPaymentInstrumentForFormEntity formEntity,
																				final Customer customer,
																				final CartOrderPaymentInstrument cartOrderPaymentInstrument,
																				final String paymentProviderConfigGuid) {
		return createPaymentInstrument(StringIdentifier.of(paymentProviderConfigGuid), formEntity, customer)
				.flatMapCompletable(instrumentGuid -> createParent(formEntity, instrumentGuid)
						.flatMapCompletable(parentGuid -> Completable.fromRunnable(() ->
								cartOrderPaymentInstrument.setPaymentInstrumentGuid(instrumentGuid))));
	}

	private Single<CartOrderPaymentInstrument> createCartOrderPaymentInstrument(final OrderPaymentInstrumentForFormEntity formEntity,
																				final CartOrder cartOrder) {
        CartOrderPaymentInstrument cartOrderPaymentInstrument = beanFactory.getPrototypeBean(
                CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class);
        cartOrderPaymentInstrument.setCartOrderUid(cartOrder.getUidPk());

        BigDecimal limitAmount = formEntity.getLimitAmount();

        if (limitAmount == null) {
            limitAmount = BigDecimal.ZERO;
        }
        cartOrderPaymentInstrument.setLimitAmount(limitAmount);
        cartOrderPaymentInstrument.setCurrency(SubjectUtil.getCurrency(resourceOperationContext.getSubject()));
        return Single.just(cartOrderPaymentInstrument);
    }

	private Single<String> createParent(final OrderPaymentInstrumentForFormEntity formEntity, final String instrumentGuid) {
		final boolean saveOnProfile = formEntity.isSaveOnProfile() == null ? Boolean.FALSE : formEntity.isSaveOnProfile();
		final boolean defaultOnProfile = formEntity.isDefaultOnProfile() == null ? Boolean.FALSE : formEntity.isDefaultOnProfile();
		if (saveOnProfile || defaultOnProfile) {
			return createCustomerPaymentInstrumentWithParent(instrumentGuid, defaultOnProfile);
		}
		return Single.just("");
	}

	private Single<String> createCustomerPaymentInstrument(final IdentifierPart<String> paymentMethodId,
														   final PaymentInstrumentForFormEntity formEntity,
														   final Customer customer) {
		final boolean isDefault = formEntity.isDefaultOnProfile() == null ? Boolean.FALSE : formEntity.isDefaultOnProfile();

		return storePaymentProviderConfigRepository.findByGuid(paymentMethodId.getValue())
				.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
				.flatMap(paymentProviderConfigGuid -> createPaymentInstrument(StringIdentifier.of(paymentProviderConfigGuid), formEntity, customer))
				.flatMap(paymentInstrumentGuid -> createCustomerPaymentInstrumentWithParent(paymentInstrumentGuid, isDefault));
	}

	private Single<String> createAccountPaymentInstrument(final IdentifierPart<String> paymentMethodId,
														  final PaymentInstrumentForFormEntity formEntity,
														  final Customer account) {
		final boolean isDefault = formEntity.isDefaultOnProfile() == null ? Boolean.FALSE : formEntity.isDefaultOnProfile();

		return storePaymentProviderConfigRepository.findByGuid(paymentMethodId.getValue())
				.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
				.flatMap(paymentProviderConfigGuid -> createPaymentInstrument(StringIdentifier.of(paymentProviderConfigGuid), formEntity, account))
				.flatMap(paymentInstrumentGuid -> createAccountPaymentInstrumentWithParent(paymentInstrumentGuid, isDefault));
	}

	private Single<String> createCustomerPaymentInstrumentWithParent(final String paymentInstrumentGuid, final boolean isDefault) {
		String customerGuid = resourceOperationContext.getUserIdentifier();

		CustomerPaymentInstrument customerPaymentInstrument = beanFactory.getPrototypeBean(
				ContextIdNames.CUSTOMER_PAYMENT_INSTRUMENT, CustomerPaymentInstrument.class);

		return customerRepository.getCustomer(customerGuid)
				.filter(customer -> !customer.isAnonymous())
				.switchIfEmpty(Single.error(ResourceOperationFailure.notFound(
						"Customer with id " + customerGuid + " is anonymous and cannot save instruments to profile")))
				.flatMap(customer -> configureCustomerPaymentInstrument(paymentInstrumentGuid, isDefault, customerPaymentInstrument, customer));
	}

	private Single<String> createAccountPaymentInstrumentWithParent(final String paymentInstrumentGuid, final boolean isDefault) {
		String accountGuid = getAccountIdFromResourceOperationContext().getValue();

		CustomerPaymentInstrument accountPaymentInstrument = beanFactory.getPrototypeBean(
				ContextIdNames.CUSTOMER_PAYMENT_INSTRUMENT, CustomerPaymentInstrument.class);

		return customerRepository.getCustomer(accountGuid)
				.filter(account -> !account.isAnonymous())
				.switchIfEmpty(Single.error(ResourceOperationFailure.notFound(
						"Account with id " + accountGuid + " is anonymous and cannot save instruments to profile")))
				.flatMap(account -> configureCustomerPaymentInstrument(paymentInstrumentGuid, isDefault, accountPaymentInstrument, account));
	}

	private Single<String> configureCustomerPaymentInstrument(final String paymentInstrumentGuid, final boolean isDefault,
															  final CustomerPaymentInstrument customerPaymentInstrument, final Customer customer) {
        return Completable.fromRunnable(() -> customerPaymentInstrument.setCustomerUid(customer.getUidPk()))
                .andThen(Completable.fromRunnable(() -> customerPaymentInstrument.setPaymentInstrumentGuid(paymentInstrumentGuid)))
                .andThen(customerPaymentInstrumentRepository.saveOrUpdate(customerPaymentInstrument))
                .flatMap(customerInstrument -> customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(customer)
                        .filter(hasDefault -> isDefault || !hasDefault)
                        .flatMapCompletable(hasDefault -> customerDefaultPaymentInstrumentRepository.saveAsDefault(customerPaymentInstrument))
                        .andThen(Single.just(customerInstrument.getGuid())));
    }

	private Single<String> createPaymentInstrument(final IdentifierPart<String> paymentProviderConfigId,
												   final PaymentInstrumentForFormEntity formEntity,
												   final Customer customer) {

		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		Map<String, String> paymentInstrumentForm = Collections.emptyMap();
		final PaymentInstrumentAttributesEntity data = formEntity.getPaymentInstrumentIdentificationForm();
		if (data != null) {
			paymentInstrumentForm = data.getDynamicProperties();
		}

		final Map<String, String> finalPaymentInstrumentForm = paymentInstrumentForm;

		AddressEntity billingAddressEntity = formEntity.getBillingAddress();

		if (billingAddressEntity != null) {
			return createPaymentInstrumentWithAddress(paymentProviderConfigId, customer, locale, currency,
					finalPaymentInstrumentForm, billingAddressEntity);
		}

		PICRequestContext picRequestContext = buildPICRequestContext(locale, currency, null, customer);
		return orderPaymentApiRepository.createPI(paymentProviderConfigId.getValue(), finalPaymentInstrumentForm, picRequestContext);
	}


	private Single<String> createPaymentInstrumentWithAddress(final IdentifierPart<String> paymentMethodId, final Customer customer,
															  final Locale locale, final Currency currency,
															  final Map<String, String> finalPaymentInstrumentForm,
															  final AddressEntity billingAddressEntity) {
		CustomerAddress customerAddress = conversionService.convert(billingAddressEntity, CustomerAddress.class);

		return addressValidator.validate(billingAddressEntity)
				.andThen(customerRepository.createAddressForCustomer(customer, customerAddress))
				.map(address -> conversionService.convert(address, AddressDTO.class))
				.map(addressDTO -> buildPICRequestContext(locale, currency, addressDTO, customer))
				.flatMap(picRequestContext ->
						orderPaymentApiRepository.createPI(paymentMethodId.getValue(), finalPaymentInstrumentForm, picRequestContext));
	}

	@Override
	public IdentifierPart<String> getAccountIdFromResourceOperationContext() {
		Optional<ResourceIdentifier> resourceIdentifierOptional = resourceOperationContext.getResourceIdentifier();
		if (resourceIdentifierOptional.isPresent()) {
			ResourceIdentifier resourceIdentifier = resourceIdentifierOptional.get();
			if (resourceIdentifier instanceof AccountPaymentInstrumentFormIdentifier) {
				return ((AccountPaymentInstrumentFormIdentifier) resourceIdentifier)
						.getAccountPaymentMethod()
						.getAccountPaymentMethods()
						.getAccount()
						.getAccountId();
			}
			if (resourceIdentifier instanceof AccountPaymentInstrumentsIdentifier) {
				return ((AccountPaymentInstrumentsIdentifier) resourceIdentifier)
						.getAccount()
						.getAccountId();
			}
			if (resourceIdentifier instanceof AccountDefaultPaymentInstrumentSelectorIdentifier) {
				return ((AccountDefaultPaymentInstrumentSelectorIdentifier) resourceIdentifier)
						.getAccountPaymentInstruments()
						.getAccount()
						.getAccountId();
			}
			if (resourceIdentifier instanceof AccountDefaultPaymentInstrumentSelectorChoiceIdentifier) {
				return ((AccountDefaultPaymentInstrumentSelectorChoiceIdentifier) resourceIdentifier)
						.getAccountPaymentInstrument()
						.getAccountPaymentInstruments()
						.getAccount()
						.getAccountId();
			}
			if (resourceIdentifier instanceof AccountIdentifier) {
				return ((AccountIdentifier) resourceIdentifier)
						.getAccountId();
			}
			if (resourceIdentifier instanceof AccountPaymentInstrumentIdentifier) {
				return ((AccountPaymentInstrumentIdentifier) resourceIdentifier)
						.getAccountPaymentInstruments()
						.getAccount()
						.getAccountId();
			}

			throw new UnsupportedOperationException(resourceIdentifier.getClass().getName() + " is not supported.");
		}
		throw new UnsupportedOperationException("Resource Identifier not found.");
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
