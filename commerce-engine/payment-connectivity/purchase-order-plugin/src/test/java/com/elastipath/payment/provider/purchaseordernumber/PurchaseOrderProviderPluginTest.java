/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elastipath.payment.provider.purchaseordernumber;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.ErrorMessage;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;

@RunWith(MockitoJUnitRunner.class)
public class PurchaseOrderProviderPluginTest {

    private static final String PURCHASE_ORDER = "purchase-order";
    private static final String ORDER_VALUE = "orderValue";

    @Test
    public void ensureAllPluginCapabilitiesPresent() {
        assertTrue(ChargeCapability.class.isAssignableFrom(PurchaseOrderProviderPlugin.class));
        assertTrue(PICCapability.class.isAssignableFrom(PurchaseOrderProviderPlugin.class));
    }

    @Test
    public void testThatGetPaymentVendorIdReturnsRightValue() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        assertThat(plugin.getPaymentVendorId()).isEqualTo("ELASTICPATH");
    }

    @Test
    public void testThatGetPaymentMethodIdReturnsRightValue() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        assertThat(plugin.getPaymentMethodId()).isEqualTo("PURCHASE_ORDER");
    }

    @Test
    public void testThatGetConfigurationKeysReturnsEmptyValue() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        assertThat(plugin.getConfigurationKeys()).isEmpty();
    }

    @Test
    public void testThatCreatePaymentInstrumentReturnsRightValueAndNotThrowsExceptions() throws PaymentInstrumentCreationFailedException {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final PaymentInstrumentCreationRequest request = mock(PaymentInstrumentCreationRequest.class);
        when(request.getFormData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final Map<String, String> details = plugin.createPaymentInstrument(request).getDetails();
        assertThat(details).containsOnly(entry(PURCHASE_ORDER, ORDER_VALUE));
    }

    @Test
    public void testThatCreatePaymentInstrumentThrowsExceptionsWhenNoOrder() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final PaymentInstrumentCreationRequest request = mock(PaymentInstrumentCreationRequest.class);
        when(request.getFormData()).thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> plugin.createPaymentInstrument(request))
                .isInstanceOf(PaymentInstrumentCreationFailedException.class)
                .extracting(throwable -> ((PaymentInstrumentCreationFailedException) throwable).getStructuredErrorMessages().get(0))
                .extracting(data -> ((ErrorMessage) data).getData().get("field-name"))
                .isEqualTo(PURCHASE_ORDER);
    }

    @Test
    public void testThatGetPaymentInstrumentCreationFieldsReturnsRightValue() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();
        final PaymentInstrumentCreationFields data = plugin.getPaymentInstrumentCreationFields(mock(PICFieldsRequestContextDTO.class));

        assertThat(data.isSaveable()).isFalse();
        assertThat(data.getFields()).containsOnly(PURCHASE_ORDER);
        assertThat(data.getBlockingFields()).isEmpty();
    }

    @Test
    public void testSuccessfulCharge() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final ChargeCapabilityRequest request = mock(ChargeCapabilityRequest.class);
        when(request.getPaymentInstrumentData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final PaymentCapabilityResponse response = plugin.charge(request);
        checkResponse(response);
    }

    @Test
    public void testSuccessfulReverseCharge() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final ReverseChargeCapabilityRequest request = mock(ReverseChargeCapabilityRequest.class);
        when(request.getPaymentInstrumentData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final PaymentCapabilityResponse response = plugin.reverseCharge(request);
        checkResponse(response);
    }

    @Test
    public void testSuccessfulCredit() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final CreditCapabilityRequest request = mock(CreditCapabilityRequest.class);
        when(request.getPaymentInstrumentData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final PaymentCapabilityResponse response = plugin.credit(request);
        checkResponse(response);
    }

    @Test
    public void testSuccessfulCancel() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final CancelCapabilityRequest request = mock(CancelCapabilityRequest.class);
        when(request.getPaymentInstrumentData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final PaymentCapabilityResponse response = plugin.cancel(request);
        checkResponse(response);
    }

    @Test
    public void testSuccessfulModify() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final ModifyCapabilityRequest request = mock(ModifyCapabilityRequest.class);
        when(request.getPaymentInstrumentData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final PaymentCapabilityResponse response = plugin.modify(request);
        checkResponse(response);
    }

    @Test
    public void testSuccessfulReserve() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final ReserveCapabilityRequest request = mock(ReserveCapabilityRequest.class);
        when(request.getPaymentInstrumentData()).thenReturn(Collections.singletonMap(PURCHASE_ORDER, ORDER_VALUE));
        final PaymentCapabilityResponse response = plugin.reserve(request);
        checkResponse(response);
    }

    @Test
    public void testSuccessfulPaymentInstrumentCreationInstructionsFields() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final PICFieldsRequestContextDTO request = mock(PICFieldsRequestContextDTO.class);
        final PICInstructionsFields response = plugin.getPaymentInstrumentCreationInstructionsFields(request);
        assertThat(response).extracting(PICInstructionsFields::getFields).isEqualTo(Collections.emptyList());
    }

    @Test
    public void testSuccessfulPaymentInstrumentCreationInstructions() {
        final PurchaseOrderProviderPlugin plugin = new PurchaseOrderProviderPlugin();

        final PICInstructionsRequest request = mock(PICInstructionsRequest.class);
        final PICInstructions response = plugin.getPaymentInstrumentCreationInstructions(request);
        assertThat(response).extracting(PICInstructions::getCommunicationInstructions).isEqualTo(Collections.emptyMap());
        assertThat(response).extracting(PICInstructions::getPayload).isEqualTo(Collections.emptyMap());
    }

    private void checkResponse(final PaymentCapabilityResponse response) {
        assertThat(response.isRequestHold()).isFalse();
        assertThat(response.getData().get("PURCHASE_ORDER")).isEqualTo(ORDER_VALUE);
    }
}
