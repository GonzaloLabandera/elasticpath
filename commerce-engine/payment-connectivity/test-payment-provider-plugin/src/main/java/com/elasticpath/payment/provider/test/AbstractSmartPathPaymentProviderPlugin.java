/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.payment.provider.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKeyBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;

/**
 * Smart path payment provider plugin, allowing to configure its behavior on create instrument step.
 */
public abstract class AbstractSmartPathPaymentProviderPlugin extends AbstractPaymentProviderPlugin {

	/**
	 * Capability states configuration.
	 */
	enum CapabilityConfig {
		/**
		 * Capability always succeeds.
		 */
		SUCCEEDS,

		/**
		 * Capability always fails.
		 */
		FAILS,

		/**
		 * Capability fails each 2nd time for the same reference number.
		 */
		FAILS_2ND_TIME;

		/**
		 * Finds capability configuration by transaction type from map. By default it's {@link CapabilityConfig#SUCCEEDS}.
		 *
		 * @param formData map of configurations
		 * @param type     transaction type
		 * @return capability configuration
		 */
		public static CapabilityConfig byTransactionType(final Map<String, String> formData, final TransactionType type) {
			final String config = formData.get(type.name());
			if (config != null && !"".equals(config)) {
				return CapabilityConfig.valueOf(config);
			}
			return SUCCEEDS;
		}
	}

	/**
	 * Map with currently processed reference number per each transaction.
	 * Supports only one reference number (order number) for transaction batch for better plugin performance.
	 * In integration/cucumber/Selenium test we are working with one order at a time anyway.
	 */
	private static final Map<TransactionType, String> CURRENT_REFERENCE_NUMBER_PER_TRANSACTION = new EnumMap<>(TransactionType.class);

	/**
	 * Clear currently processed reference number.
	 */
	static void clearCurrentReferenceNumberPerTransactionForTest() {
		CURRENT_REFERENCE_NUMBER_PER_TRANSACTION.clear();
	}

	/**
	 * Produces a success response or throws an exception depending on plugin configuration.
	 *
	 * @param request         payment capability request
	 * @param transactionType transaction type
	 * @return response builder
	 * @throws PaymentCapabilityRequestFailedException if capability is configured to fail
	 */
	PaymentCapabilityResponseBuilder createResponse(final PaymentCapabilityRequest request, final TransactionType transactionType)
			throws PaymentCapabilityRequestFailedException {

		final CapabilityConfig capabilityConfig = CapabilityConfig.byTransactionType(request.getPluginConfigData(), transactionType);

		if (capabilityConfig == CapabilityConfig.FAILS) {
			throw new PaymentCapabilityRequestFailedException(
					"Operation failed, because it was configured to do so", "Operation failure", false);
		}

		if (capabilityConfig == CapabilityConfig.FAILS_2ND_TIME) {
			final String currentlyProcessedOrderNumber = CURRENT_REFERENCE_NUMBER_PER_TRANSACTION.get(transactionType);
			final String orderNumber = request.getOrderContext().getOrderNumber();
			if (Objects.equals(currentlyProcessedOrderNumber, orderNumber)) {
				throw new PaymentCapabilityRequestFailedException(
						transactionType + " failed 2nd time for the reference number " + orderNumber
								+ ", because it was configured to do so", "Operation failure", false);
			} else {
				CURRENT_REFERENCE_NUMBER_PER_TRANSACTION.put(transactionType, orderNumber);
			}
		}

		return PaymentCapabilityResponseBuilder.aResponse()
				.withData(Collections.emptyMap())
				.withProcessedDateTime(LocalDateTime.now())
				.withRequestHold(false);
	}

	@Override
	public String getPaymentMethodId() {
		return "CARD";
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		final List<PluginConfigurationKey> configurationKeys = new ArrayList<>();
		if (hasCapability(ReserveCapability.class)) {
			configurationKeys.add(createPluginConfigurationKey(TransactionType.RESERVE.name(), TransactionType.RESERVE.name()));
		}
		if (hasCapability(ModifyCapability.class)) {
			configurationKeys.add(createPluginConfigurationKey(TransactionType.MODIFY_RESERVE.name(), TransactionType.MODIFY_RESERVE.name()));
		}
		if (hasCapability(CancelCapability.class)) {
			configurationKeys.add(createPluginConfigurationKey(TransactionType.CANCEL_RESERVE.name(), TransactionType.CANCEL_RESERVE.name()));
		}
		if (hasCapability(ChargeCapability.class)) {
			configurationKeys.add(createPluginConfigurationKey(TransactionType.CHARGE.name(), TransactionType.CHARGE.name()));
		}
		if (hasCapability(ReverseChargeCapability.class)) {
			configurationKeys.add(createPluginConfigurationKey(TransactionType.REVERSE_CHARGE.name(), TransactionType.REVERSE_CHARGE.name()));
		}
		if (hasCapability(CreditCapability.class)) {
			configurationKeys.add(createPluginConfigurationKey(TransactionType.CREDIT.name(), TransactionType.CREDIT.name()));
		}
		return configurationKeys;
	}

	private PluginConfigurationKey createPluginConfigurationKey(final String key, final String description) {
		return PluginConfigurationKeyBuilder.builder()
				.withKey(key)
				.withDescription(description)
				.build();
	}
}
