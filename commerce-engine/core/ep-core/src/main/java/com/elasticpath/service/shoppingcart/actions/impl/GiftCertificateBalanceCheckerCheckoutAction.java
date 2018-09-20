/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.plugin.payment.exceptions.InsufficientGiftCertificateBalanceException;
import com.elasticpath.service.catalog.GiftCertificateNotFoundException;
import com.elasticpath.service.catalog.GiftCertificateService;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;

/**
 * <p>
 * Validates that there is sufficient Gift Certificate balance available to pay for the order.
 * </p>
 * <p>
 * The {@link ShoppingCart} maintains a running total of the amount to be paid by any customer-entered Gift Certificates.
 * During checkout, the customer is informed of the outstanding balance, if any. However, there is a chance that in the time between the customer
 * being presented with this balance and submitting the order, another order could be processed that uses up one or more of the applied Gift
 * Certificates.
 * </p>
 * <p>
 * This {@code CheckoutAction} confirms that the applied Gift Certificates still contain enough funds to complete the order.
 * </p>
 */
public class GiftCertificateBalanceCheckerCheckoutAction implements CheckoutAction {

	private GiftCertificateService giftCertificateService;

	@Override
	public void execute(final CheckoutActionContext context) throws EpSystemException {
		ShoppingCartPricingSnapshot shoppingCartPricingSnapshot = context.getShoppingCartTaxSnapshot().getShoppingCartPricingSnapshot();
		verifyGiftCertificateBalance(context.getShoppingCart(), shoppingCartPricingSnapshot);
	}

	/**
	 * Verifies that there is sufficient balance available to pay for the order.
	 *
	 * @param shoppingCart the shoppingcart containing the items to be purchased.
	 * @param pricingSnapshot the pricing snapshot
	 * @throws com.elasticpath.plugin.payment.exceptions.InsufficientGiftCertificateBalanceException if there is not enough balance available
	 */
	protected void verifyGiftCertificateBalance(final ShoppingCart shoppingCart, final ShoppingCartPricingSnapshot pricingSnapshot) {
		final Set<GiftCertificate> redeems = shoppingCart.getAppliedGiftCertificates();
		if (redeems != null && !redeems.isEmpty()) {
			BigDecimal totalAvailableAmount = BigDecimal.ZERO;
			for (Iterator<GiftCertificate> iterator = redeems.iterator(); iterator.hasNext();) {
				GiftCertificate giftCertificate = iterator.next();
				final GiftCertificate freshGc = this.giftCertificateService.findByGiftCertificateCode(giftCertificate.getGiftCertificateCode());
				if (freshGc == null) {
					throw new GiftCertificateNotFoundException(
						"No gift certificate found with the specified code",
						giftCertificate.getGiftCertificateCode());
				}

				final BigDecimal balance = this.giftCertificateService.getBalance(freshGc);
				if (balance.compareTo(BigDecimal.ZERO) <= 0) {
					iterator.remove();
				} else {
					totalAvailableAmount = totalAvailableAmount.add(balance);
				}
			}
			final BigDecimal totalRedeems = pricingSnapshot.getGiftCertificateDiscount();
			if (totalAvailableAmount.compareTo(totalRedeems) < 0) {
				throw new InsufficientGiftCertificateBalanceException("Not enough balance on gift certificate to process the payment.");
			}
		}
	}

	protected GiftCertificateService getGiftCertificateService() {
		return giftCertificateService;
	}

	public void setGiftCertificateService(final GiftCertificateService giftCertificateService) {
		this.giftCertificateService = giftCertificateService;
	}

}
