/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.account;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
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
public class AccountDefaultPaymentInstrumentSelectorRepositoryImpl<
		SI extends AccountDefaultPaymentInstrumentSelectorIdentifier,
		CI extends AccountDefaultPaymentInstrumentSelectorChoiceIdentifier>
		implements SelectorRepository<AccountDefaultPaymentInstrumentSelectorIdentifier, AccountDefaultPaymentInstrumentSelectorChoiceIdentifier> {

	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private CustomerRepository customerRepository;
	private ReactiveAdapter reactiveAdapter;
	private PaymentInstrumentRepository paymentInstrumentRepository;
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;

	@Override
	public Observable<SelectorChoice> getChoices(final AccountDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier) {
		final IdentifierPart<String> scope = selectorIdentifier.getAccountPaymentInstruments().getAccount().getAccounts().getScope();
		final String storeCode = scope.getValue();

		return getAccount()
				.flatMapMaybe(account -> findDefaultPaymentInstrumentForAccountAndStore(storeCode, account))
				.map(CustomerPaymentInstrument::getGuid)
				.flatMapObservable(selectedPaymentInstrumentGuid ->
						getChoicesWithSelectedPaymentInstrument(selectorIdentifier, scope, selectedPaymentInstrumentGuid))
				.switchIfEmpty(getChoicesWithoutSelectedPaymentInstrument(selectorIdentifier, scope));
	}

	private Maybe<CustomerPaymentInstrument> findDefaultPaymentInstrumentForAccountAndStore(final String storeCode, final Customer account) {
		return reactiveAdapter.fromServiceAsMaybe(() ->
				filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(account, storeCode));
	}

	private Observable<SelectorChoice> getChoicesWithSelectedPaymentInstrument(
			final AccountDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier,
			final IdentifierPart<String> scope,
			final String selectedPaymentInstrumentGuid) {
		return paymentInstrumentRepository.findAllAccountPaymentInstruments(scope)
				.map(accountPaymentInstrumentIdentifier -> buildSelectorChoice(selectorIdentifier, accountPaymentInstrumentIdentifier,
						getChoiceStatus(accountPaymentInstrumentIdentifier.getAccountPaymentInstrumentId().getValue(),
								selectedPaymentInstrumentGuid)));
	}

	private Observable<SelectorChoice> getChoicesWithoutSelectedPaymentInstrument(
			final AccountDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier,
			final IdentifierPart<String> scope) {
		return paymentInstrumentRepository.findAllAccountPaymentInstruments(scope)
				.map(accountPaymentInstrumentIdentifier -> buildSelectorChoice(selectorIdentifier, accountPaymentInstrumentIdentifier,
						ChoiceStatus.CHOOSABLE));
	}

	private ChoiceStatus getChoiceStatus(final String paymentInstrumentId, final String selectedPaymentInstrument) {
		return selectedPaymentInstrument.equals(paymentInstrumentId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	@Override
	public Single<Choice> getChoice(final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		final AccountDefaultPaymentInstrumentSelectorIdentifier selector = selectorChoiceIdentifier.getAccountDefaultPaymentInstrumentSelector();
		final IdentifierPart<String> paymentInstrumentId = selectorChoiceIdentifier.getAccountPaymentInstrument().getAccountPaymentInstrumentId();

		return Single.just(Choice.builder()
				.withDescription(AccountPaymentInstrumentIdentifier.builder()
						.withAccountPaymentInstruments(selector.getAccountPaymentInstruments())
						.withAccountPaymentInstrumentId(paymentInstrumentId)
						.build())
				.withAction(selectorChoiceIdentifier)
				.withStatus(ChoiceStatus.CHOOSABLE)
				.build());
	}

	@Override
	public Single<SelectResult<AccountDefaultPaymentInstrumentSelectorIdentifier>> selectChoice(
			final AccountDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier) {
		final String storeCode = selectorChoiceIdentifier
				.getAccountPaymentInstrument()
				.getAccountPaymentInstruments()
				.getAccount()
				.getAccounts()
				.getScope()
				.getValue();
		final String selectedPaymentInstrumentId = selectorChoiceIdentifier.getAccountPaymentInstrument().getAccountPaymentInstrumentId().getValue();

		return getAccount()
				.flatMap(customer -> getSelectStatus(storeCode, selectedPaymentInstrumentId, customer))
				.map(selectStatus -> SelectResult.<AccountDefaultPaymentInstrumentSelectorIdentifier>builder()
						.withIdentifier(selectorChoiceIdentifier.getAccountDefaultPaymentInstrumentSelector())
						.withStatus(selectStatus)
						.build());
	}

	private Single<SelectStatus> getSelectStatus(final String storeCode, final String selectedPaymentInstrumentGuid, final Customer customer) {
		return findDefaultPaymentInstrumentForAccountAndStore(storeCode, customer)
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
			final AccountDefaultPaymentInstrumentSelectorIdentifier selectorIdentifier,
			final AccountPaymentInstrumentIdentifier paymentInstrumentIdentifier,
			final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(buildAccountPaymentInstrumentSelectorChoiceIdentifier(selectorIdentifier, paymentInstrumentIdentifier))
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the Profile Payment Instrument choice identifier.
	 *
	 * @param accountDefaultPaymentInstrumentSelectorIdentifier profilePaymentInstrumentSelectorIdentifier
	 * @param accountPaymentInstrumentIdentifier                paymentInstrumentIdentifier
	 * @return the profile payment instrument choice identifier
	 */
	protected AccountDefaultPaymentInstrumentSelectorChoiceIdentifier buildAccountPaymentInstrumentSelectorChoiceIdentifier(
			final AccountDefaultPaymentInstrumentSelectorIdentifier accountDefaultPaymentInstrumentSelectorIdentifier,
			final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier) {
		return AccountDefaultPaymentInstrumentSelectorChoiceIdentifier.builder()
				.withAccountPaymentInstrument(accountPaymentInstrumentIdentifier)
				.withAccountDefaultPaymentInstrumentSelector(accountDefaultPaymentInstrumentSelectorIdentifier)
				.build();
	}

	private Single<Customer> getAccount() {
		final String userGuid = paymentInstrumentRepository.getAccountIdFromResourceOperationContext().getValue();
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
