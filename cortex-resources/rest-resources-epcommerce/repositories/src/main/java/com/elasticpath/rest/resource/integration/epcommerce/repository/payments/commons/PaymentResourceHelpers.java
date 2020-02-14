/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.drools.core.util.StringUtils;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.impl.CustomerContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.DynamicInstructionsEntity;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.OrderRequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.definition.paymentinstructions.RequestInstructionsFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentAttributesEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;

/**
 * Provides common payment resource helper methods.
 */
@SuppressWarnings({"PMD.TooManyMethods"})
public final class PaymentResourceHelpers {

	/**
	 * Constant fake key to avoid encoding empty payment instrument creation instructions fields map into id.
	 */
	public static final String FAKE_INSTRUCTIONS_FIELD = "fake-field";

	private PaymentResourceHelpers() {
		// Empty constructor
	}

	/**
	 * Builds a {@link ProfilePaymentMethodsIdentifier} with the given parameter.
	 *
	 * @param userId unique user identifier
	 * @param scope  the scope for this identifier
	 * @return ProfilePaymentMethodsIdentifier
	 */
	public static ProfilePaymentMethodsIdentifier buildProfilePaymentMethodsIdentifier(final IdentifierPart<String> userId,
	                                                                                   final IdentifierPart<String> scope) {
		ProfileIdentifier profileIdentifier =
				ProfileIdentifier.builder().withProfileId(userId)
						.withScope(scope)
						.build();

		return buildProfilePaymentMethodsIdentifier(profileIdentifier);
	}

	/**
	 * Builds a {@link ProfilePaymentMethodsIdentifier} with the given parameter.
	 *
	 * @param profileIdentifier profile identifier
	 * @return ProfilePaymentMethodsIdentifier
	 */
	public static ProfilePaymentMethodsIdentifier buildProfilePaymentMethodsIdentifier(final ProfileIdentifier profileIdentifier) {
		return ProfilePaymentMethodsIdentifier.builder()
				.withProfile(profileIdentifier)
				.build();
	}

	/**
	 * Builds a {@link ProfilePaymentMethodIdentifier} with the given parameters.
	 *
	 * @param userId          unique user identifier
	 * @param scope           the scope for this identifier
	 * @param paymentMethodId the payment method id
	 * @return ProfilePaymentMethodIdentifier
	 */
	public static ProfilePaymentMethodIdentifier buildProfilePaymentMethodIdentifier(final IdentifierPart<String> userId,
	                                                                                 final IdentifierPart<String> scope,
	                                                                                 final IdentifierPart<String> paymentMethodId) {
		ProfileIdentifier profileIdentifier =
				ProfileIdentifier.builder().withProfileId(userId)
						.withScope(scope)
						.build();

		return buildProfilePaymentMethodIdentifier(profileIdentifier, paymentMethodId);
	}

	/**
	 * Builds a {@link ProfilePaymentMethodIdentifier} with the given parameters.
	 *
	 * @param profileIdentifier profile identifier
	 * @param paymentMethodId   the payment method id
	 * @return ProfilePaymentMethodIdentifier
	 */
	public static ProfilePaymentMethodIdentifier buildProfilePaymentMethodIdentifier(final ProfileIdentifier profileIdentifier,
	                                                                                 final IdentifierPart<String> paymentMethodId) {
		return ProfilePaymentMethodIdentifier.builder()
				.withProfilePaymentMethods(buildProfilePaymentMethodsIdentifier(profileIdentifier))
				.withPaymentMethodId(paymentMethodId)
				.build();
	}

	/**
	 * Builds a {@link PurchasePaymentMethodIdentifier} with the given parameters.
	 *
	 * @param purchasePaymentInstrumentIdentifier purchase payment instrument identifier
	 * @param paymentMethodId   the payment method id
	 * @return PurchasePaymentMethodIdentifier
	 */
	public static PurchasePaymentMethodIdentifier buildPurchasePaymentMethodIdentifier(final PurchasePaymentInstrumentIdentifier
																							   purchasePaymentInstrumentIdentifier,
																					 final IdentifierPart<String> paymentMethodId) {
		return PurchasePaymentMethodIdentifier.builder()
				.withPurchasePaymentInstrument(purchasePaymentInstrumentIdentifier)
				.withPaymentMethodId(paymentMethodId)
				.build();
	}

	/**
	 * Builds an {@link OrderPaymentMethodsIdentifier} with the given parameters.
	 *
	 * @param scope   the scope for this identifier
	 * @param orderId the order id for this identifier
	 * @return OrderPaymentMethodsIdentifier
	 */
	private static OrderPaymentMethodsIdentifier buildOrderPaymentMethodsIdentifier(final IdentifierPart<String> scope,
	                                                                                final IdentifierPart<String> orderId) {
		return OrderPaymentMethodsIdentifier.builder()
				.withOrder(OrderIdentifier.builder()
						.withScope(scope)
						.withOrderId(orderId)
						.build())
				.build();
	}

	/**
	 * Builds an {@link OrderPaymentMethodIdentifier} with the given parameters.
	 *
	 * @param scope           the scope for this identifier
	 * @param paymentMethodId the payment method id
	 * @param orderId         the order id for this identifier
	 * @return OrderPaymentProviderIdentifier
	 */
	public static OrderPaymentMethodIdentifier buildOrderPaymentMethodIdentifier(final IdentifierPart<String> scope,
	                                                                             final IdentifierPart<String> paymentMethodId,
	                                                                             final IdentifierPart<String> orderId) {
		return OrderPaymentMethodIdentifier.builder()
				.withOrderPaymentMethods(buildOrderPaymentMethodsIdentifier(scope, orderId))
				.withPaymentMethodId(paymentMethodId)
				.build();
	}

	/**
	 * Builds an {@link PaymentInstructionsIdentifier} with the given parameters.
	 *
	 * @param profileIdentifier         profile identifier
	 * @param methodId                  the payment method id
	 * @param communicationInstructions communication instructions map
	 * @param payload                   payload map
	 * @return PaymentInstructionsIdentifier
	 */
	public static PaymentInstructionsIdentifier buildPaymentInstructionsIdentifier(
			final ProfileIdentifier profileIdentifier,
			final IdentifierPart<String> methodId,
			final Map<String, String> communicationInstructions,
			final Map<String, String> payload) {

		Map<String, String> communicationInstructionsWrapped = new HashMap<>(communicationInstructions);
		Map<String, String> payloadWrapped = new HashMap<>(payload);

		if (communicationInstructionsWrapped.isEmpty()) {
			communicationInstructionsWrapped.put(FAKE_INSTRUCTIONS_FIELD, StringUtils.EMPTY);
		}

		if (payloadWrapped.isEmpty()) {
			payloadWrapped.put(FAKE_INSTRUCTIONS_FIELD, StringUtils.EMPTY);
		}

		return PaymentInstructionsIdentifier.builder()
				.withCommunicationInstructionsId(CompositeIdentifier.of(communicationInstructionsWrapped))
				.withPayloadId(CompositeIdentifier.of(payloadWrapped))
				.withProfilePaymentMethod(buildProfilePaymentMethodIdentifier(profileIdentifier, methodId))
				.build();
	}

	/**
	 * Builds an {@link OrderPaymentInstructionsIdentifier} with the given parameters.
	 *
	 * @param scope                     the scope for this identifier
	 * @param methodId                  the payment method id
	 * @param orderId                   the order id for this identifier
	 * @param communicationInstructions communication instructions map
	 * @param payload                   payload map
	 * @return OrderPaymentInstructionsIdentifier
	 */
	public static OrderPaymentInstructionsIdentifier buildOrderPaymentInstructionsIdentifier(
			final IdentifierPart<String> scope,
			final IdentifierPart<String> methodId,
			final IdentifierPart<String> orderId,
			final Map<String, String> communicationInstructions,
			final Map<String, String> payload) {

		Map<String, String> communicationInstructionsWrapped = new HashMap<>(communicationInstructions);
		Map<String, String> payloadWrapped = new HashMap<>(payload);

		if (communicationInstructionsWrapped.isEmpty()) {
			communicationInstructionsWrapped.put(FAKE_INSTRUCTIONS_FIELD, StringUtils.EMPTY);
		}

		if (payloadWrapped.isEmpty()) {
			payloadWrapped.put(FAKE_INSTRUCTIONS_FIELD, StringUtils.EMPTY);
		}

		return OrderPaymentInstructionsIdentifier.builder()
				.withCommunicationInstructionsId(CompositeIdentifier.of(communicationInstructionsWrapped))
				.withPayloadId(CompositeIdentifier.of(payloadWrapped))
				.withOrderPaymentMethod(buildOrderPaymentMethodIdentifier(scope, methodId, orderId))
				.build();
	}

	/**
	 * Builds a {@link RequestInstructionsFormIdentifier} with the given parameters.
	 *
	 * @param profileIdentifier profile identifier
	 * @param methodId          the payment method id
	 * @return RequestInstructionsFormIdentifier
	 */
	public static RequestInstructionsFormIdentifier buildRequestInstructionsForm(final ProfileIdentifier profileIdentifier,
	                                                                             final IdentifierPart<String> methodId) {
		return RequestInstructionsFormIdentifier.builder()
				.withProfilePaymentMethod(buildProfilePaymentMethodIdentifier(profileIdentifier, methodId))
				.build();
	}

	/**
	 * Builds an {@link OrderRequestInstructionsFormIdentifier} with the given parameters.
	 *
	 * @param scope    the scope for this identifier
	 * @param methodId the payment method id
	 * @param orderId  the order id for this identifier
	 * @return OrderRequestInstructionsFormIdentifier
	 */
	public static OrderRequestInstructionsFormIdentifier buildOrderRequestInstructionsFormIdentifier(final IdentifierPart<String> scope,
	                                                                                                 final IdentifierPart<String> methodId,
	                                                                                                 final IdentifierPart<String> orderId) {
		return OrderRequestInstructionsFormIdentifier.builder()
				.withOrderPaymentMethod(buildOrderPaymentMethodIdentifier(scope, methodId, orderId))
				.build();
	}

	/**
	 * Builds a {@link PaymentMethodConfigurationEntity} with the provided data.
	 *
	 * @param data                   the data to include in the {@link PaymentMethodConfigurationEntity}
	 * @param requiresBillingAddress indicates whether a billing address is required for payment instrument creation for this provider
	 * @return PaymentMethodConfigurationEntity containing the data.
	 */
	public static PaymentMethodConfigurationEntity buildPaymentMethodConfigurationEntity(final List<String> data,
	                                                                                     final boolean requiresBillingAddress) {
		return buildPaymentMethodConfigurationEntity(data.stream().collect(Collectors.toMap(Function.identity(), value -> "")),
				requiresBillingAddress);
	}

	private static PaymentMethodConfigurationEntity buildPaymentMethodConfigurationEntity(final Map<String, String> data,
	                                                                                      final boolean requiresBillingAddress) {
		PaymentMethodConfigurationEntity.Builder builder = PaymentMethodConfigurationEntity.builder();
		data.forEach(builder::addingProperty);

		if (requiresBillingAddress) {
			builder.withBillingAddress(buildEmptyCustomerAddressEntity());
		}

		return builder.build();
	}

	/**
	 * Builds an {@link InstructionsEntity} using the provided maps of control data and
	 * payload data to create the internal {@link DynamicInstructionsEntity}s.
	 *
	 * @param controlData map of control data
	 * @param payloadData map of payload data
	 * @return {@link InstructionsEntity} containing the input maps as {@link DynamicInstructionsEntity}s
	 */
	public static InstructionsEntity buildInstructionsEntity(final Map<String, String> controlData, final Map<String, String> payloadData) {
		final DynamicInstructionsEntity controlDataEntity = buildDynamicInstructionsEntity(controlData);
		final DynamicInstructionsEntity payloadDataEntity = buildDynamicInstructionsEntity(payloadData);

		return InstructionsEntity.builder()
				.withCommunicationInstructions(controlDataEntity)
				.withPayload(payloadDataEntity)
				.build();
	}

	/**
	 * Builds an {@link OrderPaymentInstrumentEntity} with the provided parameters.
	 *
	 * @param limitAmount {@link CostEntity} representation of limit amount
	 * @param isDefault   flag indicating whether this instrument entity represents the customer's default
	 * @param isSaved     flag indicating whether the instrument is saved to the customer's profile
	 * @param data        payment instrument data
	 * @param name        payment instrument name
	 * @return {@link OrderPaymentInstrumentEntity}
	 */
	public static OrderPaymentInstrumentEntity buildOrderPaymentInstrumentEntity(final CostEntity limitAmount, final boolean isDefault,
	                                                                             final boolean isSaved, final Map<String, String> data,
	                                                                             final String name) {

		return OrderPaymentInstrumentEntity.builder()
				.withPaymentInstrumentIdentificationAttributes(buildPaymentInstrumentAttributesEntity(data))
				.withDefaultOnProfile(isDefault)
				.withSavedOnProfile(isSaved)
				.withLimitAmount(limitAmount)
				.withName(name)
				.build();
	}

	/**
	 * Builds an {@link OrderPaymentInstrumentForFormEntity} with the provided parameters.
	 *
	 * @param limitAmount             {@link BigDecimal} limit amount
	 * @param instrumentAttributeData payment instrument attribute data
	 * @param showSaveOnProfile       flag indicating whether to display the option to save to the profile
	 * @param requiresBillingAddress  flag indicating whether the corresponding payment provider requires a billing address for instrument creation
	 * @return {@link OrderPaymentInstrumentForFormEntity}
	 */
	@SuppressWarnings("squid:S2301")
	public static OrderPaymentInstrumentForFormEntity buildOrderPaymentInstrumentForFormEntity(final BigDecimal limitAmount,
	                                                                                           final List<String> instrumentAttributeData,
	                                                                                           final boolean showSaveOnProfile,
	                                                                                           final boolean requiresBillingAddress) {
		OrderPaymentInstrumentForFormEntity.Builder builder = OrderPaymentInstrumentForFormEntity.builder()
				.withPaymentInstrumentIdentificationForm(buildPaymentInstrumentAttributesEntity(instrumentAttributeData))
				.withLimitAmount(limitAmount);

		if (showSaveOnProfile) {
			builder.withSaveOnProfile(Boolean.FALSE)
					.withDefaultOnProfile(Boolean.FALSE);
		}

		if (requiresBillingAddress) {
			builder.withBillingAddress(buildEmptyCustomerAddressEntity());
		}

		return builder.build();
	}

	/**
	 * Builds a {@link ProfilePaymentInstrumentFormIdentifier} linked to the provided {@link ProfilePaymentMethodIdentifier}.
	 *
	 * @param identifier {@link ProfilePaymentMethodIdentifier}
	 * @return {@link ProfilePaymentInstrumentFormIdentifier}
	 */
	public static ProfilePaymentInstrumentFormIdentifier buildProfilePaymentInstrumentFormIdentifier(
			final ProfilePaymentMethodIdentifier identifier) {
		return ProfilePaymentInstrumentFormIdentifier.builder()
				.withProfilePaymentMethod(identifier)
				.build();
	}

	/**
	 * Builds {@link PaymentInstrumentsIdentifier} with the provided scope.
	 *
	 * @param scope scope tied to the created {@link PaymentInstrumentsIdentifier}
	 * @return {@link PaymentInstrumentsIdentifier}
	 */
	public static PaymentInstrumentsIdentifier buildPaymentInstrumentsIdentifier(final IdentifierPart<String> scope) {
		return PaymentInstrumentsIdentifier.builder()
				.withScope(scope)
				.build();
	}

	/**
	 * Builds a {@link PaymentInstrumentIdentifier} with the provided scope and identifier.
	 *
	 * @param scope               scope tied to the created {@link PaymentInstrumentIdentifier}
	 * @param paymentInstrumentId unique payment instrument identifier
	 * @return {@link PaymentInstrumentIdentifier}
	 */
	public static PaymentInstrumentIdentifier buildPaymentInstrumentIdentifier(final IdentifierPart<String> scope,
	                                                                           final IdentifierPart<String> paymentInstrumentId) {
		return PaymentInstrumentIdentifier.builder()
				.withPaymentInstrumentId(paymentInstrumentId)
				.withPaymentInstruments(buildPaymentInstrumentsIdentifier(scope))
				.build();
	}

	/**
	 * Builds a {@link PaymentInstrumentForFormEntity} with the provided configuration and attribute data.
	 *
	 * @param attributeData          map representing the payment instrument attribute data to encapsulate
	 * @param showDefaultOnProfile   flag indicating whether to display the option to save as default to the profile
	 * @param requiresBillingAddress flag indicating whether the payment instrument requires a billing address for creation
	 * @return {@link PaymentInstrumentForFormEntity}
	 */
	@SuppressWarnings("squid:S2301")
	public static PaymentInstrumentForFormEntity buildPaymentInstrumentForFormEntity(final List<String> attributeData,
	                                                                                 final boolean showDefaultOnProfile,
	                                                                                 final boolean requiresBillingAddress) {
		final PaymentInstrumentForFormEntity.Builder builder = PaymentInstrumentForFormEntity.builder()
				.withPaymentInstrumentIdentificationForm(buildPaymentInstrumentAttributesEntity(attributeData));

		if (showDefaultOnProfile) {
			builder.withDefaultOnProfile(Boolean.FALSE);
		}

		if (requiresBillingAddress) {
			builder.withBillingAddress(buildEmptyCustomerAddressEntity());
		}

		return builder.build();
	}

	/**
	 * Builds a {@link PaymentInstrumentEntity} with the provided parameters.
	 *
	 * @param isDefault flag indicating whether this instrument entity represents the customer's default
	 * @param data      data map relevant to the corresponding payment instrument
	 * @param name      name payment instrument
	 * @return {@link PaymentInstrumentEntity}
	 */
	public static PaymentInstrumentEntity buildPaymentInstrumentEntity(final boolean isDefault, final Map<String, String> data,
	                                                                   final String name) {
		final PaymentInstrumentAttributesEntity.Builder dataBuilder = PaymentInstrumentAttributesEntity.builder();

		data.forEach(dataBuilder::addingProperty);

		return PaymentInstrumentEntity.builder()
				.withPaymentInstrumentIdentificationAttributes(dataBuilder.build())
				.withDefaultOnProfile(isDefault)
				.withName(name)
				.build();
	}

	/**
	 * Builds an {@link OrderPaymentInstrumentIdentifier} with the provided parameters.
	 *
	 * @param scope                    scope for this {@link OrderPaymentInstrumentIdentifier}
	 * @param orderId                  unique order identifier
	 * @param orderPaymentInstrumentId unique order payment instrument identifier
	 * @return {@link OrderPaymentInstrumentIdentifier}
	 */
	public static OrderPaymentInstrumentIdentifier buildOrderPaymentInstrumentIdentifier(final IdentifierPart<String> scope,
	                                                                                     final IdentifierPart<String> orderId,
	                                                                                     final IdentifierPart<String> orderPaymentInstrumentId) {
		return OrderPaymentInstrumentIdentifier.builder()
				.withOrder(OrderIdentifier.builder()
						.withScope(scope)
						.withOrderId(orderId)
						.build())
				.withPaymentInstrumentId(orderPaymentInstrumentId)
				.build();
	}

	/**
	 * Builds a {@link PICRequestContext} object for use during payment instrument creation.
	 *
	 * @param locale         {@link Locale}
	 * @param currency       {@link Currency}
	 * @param billingAddress {@link AddressDTO}
	 * @param customer       {@link Customer}
	 * @return {@link PICRequestContext} with the provided information
	 */
	public static PICRequestContext buildPICRequestContext(final Locale locale, final Currency currency, final AddressDTO billingAddress,
	                                                       final Customer customer) {

		CustomerContext customerContext = new CustomerContext(customer.getUserId(), customer.getFirstName(), customer.getLastName(),
				customer.getEmail());

		return new PICRequestContext(currency, locale, customerContext, billingAddress);
	}

	/**
	 * Builds a {@link PICFieldsRequestContext} object for querying plugin fields during payment instrument creation.
	 *
	 * @param locale   {@link Locale}
	 * @param currency {@link Currency}
	 * @param customer {@link Customer}
	 * @return {@link PICFieldsRequestContext} with the provided information
	 */
	public static PICFieldsRequestContext buildPICFieldsRequestContext(final Locale locale, final Currency currency, final Customer customer) {

		CustomerContext customerContext = new CustomerContext(customer.getUserId(), customer.getFirstName(), customer.getLastName(),
				customer.getEmail());

		return new PICFieldsRequestContext(currency, locale, customerContext);
	}

	/**
	 * Builds an empty {@link AddressEntity}.
	 *
	 * @return empty {@link AddressEntity}
	 */
	public static AddressEntity buildEmptyCustomerAddressEntity() {
		return AddressEntity.builder()
				.withAddress(buildEmptyBaseAddressEntity())
				.withPhoneNumber(StringUtils.EMPTY)
				.withOrganization(StringUtils.EMPTY)
				.withName(NameEntity.builder()
						.withFamilyName(StringUtils.EMPTY)
						.withGivenName(StringUtils.EMPTY)
						.build())
				.build();
	}

	/**
	 * Builds a {@link PaymentInstrumentAttributesEntity} using the provided map data.
	 *
	 * @param data map to populate the {@link PaymentInstrumentAttributesEntity}
	 * @return {@link PaymentInstrumentAttributesEntity}
	 */
	public static PaymentInstrumentAttributesEntity buildPaymentInstrumentAttributesEntity(final Map<String, String> data) {
		final PaymentInstrumentAttributesEntity.Builder dataBuilder = PaymentInstrumentAttributesEntity.builder();
		data.forEach(dataBuilder::addingProperty);
		return dataBuilder.build();
	}

	private static com.elasticpath.rest.definition.base.AddressEntity buildEmptyBaseAddressEntity() {
		return com.elasticpath.rest.definition.base.AddressEntity.builder()
				.withCountryName(StringUtils.EMPTY)
				.withExtendedAddress(StringUtils.EMPTY)
				.withLocality(StringUtils.EMPTY)
				.withPostalCode(StringUtils.EMPTY)
				.withRegion(StringUtils.EMPTY)
				.withStreetAddress(StringUtils.EMPTY)
				.build();
	}

	private static PaymentInstrumentAttributesEntity buildPaymentInstrumentAttributesEntity(final List<String> data) {
		final PaymentInstrumentAttributesEntity.Builder dataBuilder = PaymentInstrumentAttributesEntity.builder();
		data.forEach(datum -> dataBuilder.addingProperty(datum, ""));
		return dataBuilder.build();
	}

	private static DynamicInstructionsEntity buildDynamicInstructionsEntity(final Map<String, String> data) {
		final DynamicInstructionsEntity.Builder dataEntityBuilder = DynamicInstructionsEntity.builder();
		if (data != null) {
			data.forEach(dataEntityBuilder::addingProperty);
		}
		return dataEntityBuilder.build();
	}

}
