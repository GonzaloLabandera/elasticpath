/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.paymentgateways.cybersource;

import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceRequestFields.CARD_ACCOUNT_NUMBER;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceRequestFields.CARD_EXPIRATION_MONTH;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceRequestFields.CARD_EXPIRATION_YEAR;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceRequestFields.CC_AUTH_SERVICE_RUN;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceRequestFields.PURCHASE_TOTALS_CURRENCY;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceRequestFields.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceResponseConstants.CC_AUTH_REPLY_AMOUNT;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceResponseConstants.REQUEST_ID;
import static com.elasticpath.paymentgateways.cybersource.constants.CyberSourceResponseConstants.REQUEST_TOKEN;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.elasticpath.plugin.payment.PaymentGatewayType;
import com.elasticpath.plugin.payment.capabilities.PreAuthorizeCapability;
import com.elasticpath.plugin.payment.dto.AddressDto;
import com.elasticpath.plugin.payment.dto.CardDetailsPaymentMethod;
import com.elasticpath.plugin.payment.dto.MoneyDto;
import com.elasticpath.plugin.payment.dto.OrderShipmentDto;
import com.elasticpath.plugin.payment.dto.OrderSkuDto;
import com.elasticpath.plugin.payment.dto.impl.MoneyDtoImpl;
import com.elasticpath.plugin.payment.transaction.AuthorizationTransactionRequest;
import com.elasticpath.plugin.payment.transaction.AuthorizationTransactionResponse;

/**
 * Cybersource credit card payment gateway implementation.
 *
 */
public class FakeCybersourceCreditCardPaymentGatewayPluginImpl extends AbstractFakeCybersourcePaymentGatewayPluginImpl
		implements PreAuthorizeCapability {
	private static final long serialVersionUID = 1L;

	@Override
	public String getPluginType() {
		return "paymentGatewayCybersource";
	}

	@Override
	public PaymentGatewayType getPaymentGatewayType() {
		return PaymentGatewayType.CREDITCARD;
	}
	
	@Override
	public AuthorizationTransactionResponse preAuthorize(final AuthorizationTransactionRequest authorizationTransactionRequest, 
			final AddressDto billingAddress, final OrderShipmentDto shipment) {
		final HashMap<String, String> request = new HashMap<>();
		
		CardDetailsPaymentMethod paymentMethod = (CardDetailsPaymentMethod) authorizationTransactionRequest.getPaymentMethod();
		MoneyDto money = authorizationTransactionRequest.getMoney();
		
		request.put(CC_AUTH_SERVICE_RUN, "true");
		request.put("businessRules_ignoreAVSResult", "true");
		
		setRequestReferenceCode(paymentMethod, request);

		// Billing Address
		request.put("billTo_firstName", billingAddress.getFirstName());
		request.put("billTo_lastName", billingAddress.getLastName());
		request.put("billTo_street1", billingAddress.getStreet1());
		request.put("billTo_city", billingAddress.getCity());

		if (billingAddress.getSubCountry() != null) {
			request.put("billTo_state", billingAddress.getSubCountry());
		}

		request.put("billTo_postalCode", billingAddress.getZipOrPostalCode());
		request.put("billTo_country", billingAddress.getCountry());
		request.put("billTo_email", paymentMethod.getEmail());

		// Card Information
		final String cardTypeCode = getDefaultSupportedCardTypes().get(paymentMethod.getCardType());
		if (cardTypeCode != null) {
			request.put("card_cardType", cardTypeCode);
		}
		request.put(CARD_ACCOUNT_NUMBER, paymentMethod.getUnencryptedCardNumber());
		request.put(CARD_EXPIRATION_MONTH, paymentMethod.getExpiryMonth());
		request.put(CARD_EXPIRATION_YEAR, paymentMethod.getExpiryYear());
		request.put(PURCHASE_TOTALS_CURRENCY, money.getCurrencyCode());
		request.put(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT,
				convertToString(money.getAmount()));
		if (this.isCvv2ValidationEnabled()) {
			request.put("card_cvNumber", paymentMethod.getCvv2Code());
		}

		if (shipment != null) {
			int counter = 0;
			for (final OrderSkuDto orderSku : shipment.getOrderSkuDtos()) {
				request.putAll(createItem(counter, orderSku.getDisplayName(), orderSku.getSkuCode(),
						orderSku.getQuantity(), orderSku.getTaxAmount(), orderSku.getUnitPrice()));
				counter++;
			}
			if (shipment.isPhysical()) {
				request.putAll(createItem(counter, shipment.getCarrier(), shipment.getServiceLevel(), 1,
						shipment.getShippingTax(), shipment.getShippingCost()));
			}

		}

		// The Client will get the merchantID from the CyberSource properties and insert it into
		// the request Map.
		Map<String, String> transactionReply = runTransaction(paymentMethod, request);
		
		
		/*
		 * if the authorization was successful, obtain the request id and request token
		 * for the follow-on capture later.
		 */
		String authorizationCode = transactionReply.get(REQUEST_ID);
		String requestToken = transactionReply.get(REQUEST_TOKEN);
		
		String authorizationAmount = transactionReply.get(CC_AUTH_REPLY_AMOUNT);
		String authorizationCurrrency = transactionReply.get("purchaseTotals_currency");
		
		return createAuthorizationResponse(null, authorizationCode, requestToken, null, new MoneyDtoImpl(new BigDecimal(authorizationAmount),
				authorizationCurrrency));
	}
}
