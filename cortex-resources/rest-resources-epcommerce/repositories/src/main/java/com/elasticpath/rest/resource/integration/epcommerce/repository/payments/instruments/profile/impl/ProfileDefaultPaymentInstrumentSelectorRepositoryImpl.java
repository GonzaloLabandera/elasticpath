/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.profile.impl;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * Implement a selector repository for the Profile Payment Instrument Selector.
 *
 * @param <SI> The selector identifier
 * @param <CI> The Profile Payment Instrument Selector Choice Identifier
 */
@Component
public class ProfileDefaultPaymentInstrumentSelectorRepositoryImpl<
		SI extends ProfileDefaultPaymentInstrumentSelectorIdentifier,
		CI extends ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier>
		implements SelectorRepository<ProfileDefaultPaymentInstrumentSelectorIdentifier, ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> {

	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private ReactiveAdapter reactiveAdapter;
	private PaymentInstrumentRepository paymentInstrumentRepository;
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;

	@Override
	public Observable<SelectorChoice> getChoices(final ProfileDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier) {
		final IdentifierPart<String> scope = selectorIdentifier.getPaymentInstruments().getScope();
		final String storeCode = scope.getValue();

		return getCustomer()
				.flatMapMaybe(customer -> findDefaultPaymentInstrumentForCustomerAndStore(storeCode, customer))
				.map(CustomerPaymentInstrument::getGuid)
				.flatMapObservable(selectedPaymentInstrumentGuid ->
						getChoicesWithSelectedPaymentInstrument(selectorIdentifier, scope, selectedPaymentInstrumentGuid))
				.switchIfEmpty(getChoicesWithoutSelectedPaymentInstrument(selectorIdentifier, scope));
	}

	private Maybe<CustomerPaymentInstrument> findDefaultPaymentInstrumentForCustomerAndStore(final String storeCode, final Customer customer) {
		return reactiveAdapter.fromServiceAsMaybe(() ->
				filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, storeCode));
	}

	private Observable<SelectorChoice> getChoicesWithSelectedPaymentInstrument(
			final ProfileDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier,
			final IdentifierPart<String> scope,
			final String selectedPaymentInstrumentGuid) {
		return paymentInstrumentRepository.findAll(scope)
				.map(paymentInstrumentIdentifier -> buildSelectorChoice(selectorIdentifier, paymentInstrumentIdentifier,
						getChoiceStatus(paymentInstrumentIdentifier.getPaymentInstrumentId().getValue(), selectedPaymentInstrumentGuid)));
	}

	private Observable<SelectorChoice> getChoicesWithoutSelectedPaymentInstrument(
			final ProfileDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier,
			final IdentifierPart<String> scope) {
		return paymentInstrumentRepository.findAll(scope)
				.map(paymentInstrumentIdentifier -> buildSelectorChoice(selectorIdentifier, paymentInstrumentIdentifier, ChoiceStatus.CHOOSABLE));
	}

	private ChoiceStatus getChoiceStatus(final String paymentInstrumentId, final String selectedPaymentInstrument) {
		return selectedPaymentInstrument.equals(paymentInstrumentId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	@Override
	public Single<Choice> getChoice(final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		final ProfileDefaultPaymentInstrumentSelectorIdentifier selector = selectorChoiceIdentifier.getProfileDefaultPaymentInstrumentSelector();
		final IdentifierPart<String> paymentInstrumentId = selectorChoiceIdentifier.getPaymentInstrument().getPaymentInstrumentId();

		return Single.just(Choice.builder()
				.withDescription(PaymentInstrumentIdentifier.builder()
						.withPaymentInstruments(selector.getPaymentInstruments())
						.withPaymentInstrumentId(paymentInstrumentId)
						.build())
				.withAction(selectorChoiceIdentifier)
				.withStatus(ChoiceStatus.CHOOSABLE)
				.build());
	}

	@Override
	public Single<SelectResult<ProfileDefaultPaymentInstrumentSelectorIdentifier>> selectChoice(
			final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		final String storeCode = selectorChoiceIdentifier.getPaymentInstrument().getPaymentInstruments().getScope().getValue();
		final String selectedPaymentInstrumentId = selectorChoiceIdentifier.getPaymentInstrument().getPaymentInstrumentId().getValue();

		return getCustomer()
				.flatMap(customer -> getSelectStatus(storeCode, selectedPaymentInstrumentId, customer))
				.map(selectStatus -> SelectResult.<ProfileDefaultPaymentInstrumentSelectorIdentifier>builder()
						.withIdentifier(selectorChoiceIdentifier.getProfileDefaultPaymentInstrumentSelector())
						.withStatus(selectStatus)
						.build());
	}

	private Single<SelectStatus> getSelectStatus(final String storeCode, final String selectedPaymentInstrumentGuid, final Customer customer) {
		return findDefaultPaymentInstrumentForCustomerAndStore(storeCode, customer)
				.filter(paymentInstrument -> paymentInstrument.getPaymentInstrumentGuid().equals(selectedPaymentInstrumentGuid))
				.isEmpty()
				.flatMap(isNotFound -> {
					if (isNotFound) {
						return customerPaymentInstrumentRepository.findByGuid(selectedPaymentInstrumentGuid)
								.flatMap(this::savePaymentInstrumentSelection);
					}
					return Single.just(SelectStatus.EXISTING);
				});
	}

	private Single<SelectStatus> savePaymentInstrumentSelection(final CustomerPaymentInstrument customerPaymentInstrument) {
		return customerDefaultPaymentInstrumentRepository.saveAsDefault(customerPaymentInstrument)
				.andThen(Single.just(SelectStatus.SELECTED));
	}

	/**
	 * Build the selector choice.
	 *
	 * @param selectorIdentifier          profile payment instrument Selector Identifier
	 * @param paymentInstrumentIdentifier payment instrument identifier
	 * @param choiceStatus                choiceStatus
	 * @return the selector choice
	 */
	protected SelectorChoice buildSelectorChoice(
			final ProfileDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier,
			final PaymentInstrumentIdentifier paymentInstrumentIdentifier,
			final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(buildProfilePaymentInstrumentSelectorChoiceIdentifier(selectorIdentifier, paymentInstrumentIdentifier))
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the Profile Payment Instrument choice identifier.
	 *
	 * @param profilePaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier
	 * @param paymentInstrumentIdentifier                paymentInstrumentIdentifier
	 * @return the profile payment instrument choice identifier
	 */
	protected ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier buildProfilePaymentInstrumentSelectorChoiceIdentifier(
			final ProfileDefaultPaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier,
			final PaymentInstrumentIdentifier paymentInstrumentIdentifier) {
		return ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier.builder()
				.withPaymentInstrument(paymentInstrumentIdentifier)
				.withProfileDefaultPaymentInstrumentSelector(profilePaymentInstrumentSelectorIdentifier)
				.build();
	}

	private Single<Customer> getCustomer() {
		final String userGuid = resourceOperationContext.getUserIdentifier();
		return customerRepository.getCustomer(userGuid);
	}

	@Reference
	public void setCustomerPaymentInstrumentRepository(final CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository) {
		this.customerPaymentInstrumentRepository = customerPaymentInstrumentRepository;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setPaymentInstrumentRepository(final PaymentInstrumentRepository paymentInstrumentRepository) {
		this.paymentInstrumentRepository = paymentInstrumentRepository;
	}

	@Reference
	public void setFilteredPaymentInstrumentService(final FilteredPaymentInstrumentService filteredPaymentInstrumentService) {
		this.filteredPaymentInstrumentService = filteredPaymentInstrumentService;
	}

	@Reference
	public void setCustomerDefaultPaymentInstrumentRepository(
			final CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository) {
		this.customerDefaultPaymentInstrumentRepository = customerDefaultPaymentInstrumentRepository;
	}
}
