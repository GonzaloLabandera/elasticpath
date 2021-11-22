/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.domain.shopper.impl;

import java.util.Currency;
import java.util.Locale;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.shoppingcart.ShopperBrowsingActivity;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.tags.TagSet;

/**
 * The abstract aspects of Shoppers in the system, extensions
 * will add the specifics, e.g. identity, etc.
 */
public class ShopperImpl implements Shopper {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private ShopperMemento shopperMemento;

	private CustomerSession customerSession;

	private boolean customerSignedIn;

	private ShoppingCart shoppingCart;

	private WishList wishList;

	private transient ShopperBrowsingActivity browsingActivity;

	@Override
	public String getGuid() {
		return getShopperMemento().getGuid();
	}

	@Override
	public void setGuid(final String guid) {
		getShopperMemento().setGuid(guid);
	}

	@Override
	public long getUidPk() {
		return getShopperMemento().getUidPk();
	}

	@Override
	public void setUidPk(final long uidPk) {
		getShopperMemento().setUidPk(uidPk);
	}

	@Override
	public void setCustomerSession(final CustomerSession customerSession) {
		this.customerSession = customerSession;
	}

	@Override
	public CustomerSession getCustomerSession() {
		return customerSession;
	}

	@Override
	public Currency getCurrency() {
		return customerSession.getCurrency();
	}

	@Override
	public Locale getLocale() {
		return customerSession.getLocale();
	}

	@Override
	public String getStoreCode() {
		return getShopperMemento().getStoreCode();
	}

	@Override
	public void setStoreCode(final String storeCode) {
		getShopperMemento().setStoreCode(storeCode);
	}

	@Override
	public Customer getCustomer() {
		return shopperMemento.getCustomer();
	}

	@Override
	public void setCustomer(final Customer customer) {
		shopperMemento.setCustomer(customer);
	}

	@Override
	public Customer getAccount() {
		return shopperMemento.getAccount();
	}

	@Override
	public void setAccount(final Customer account) {
		shopperMemento.setAccount(account);
	}

	@Override
	public void setSignedIn(final boolean signedIn) {
		customerSignedIn = signedIn;
	}

	@Override
	public boolean isSignedIn() {
		return customerSignedIn;
	}

	@Override
	public PriceListStack getPriceListStack() {
		return customerSession.getPriceListStack();
	}

	@Override
	public void setPriceListStack(final PriceListStack priceListStack) {
		customerSession.setPriceListStack(priceListStack);
	}

	@Override
	public boolean isPriceListStackValid() {
		return customerSession.isPriceListStackValid();
	}

	@Override
	public ShoppingCart getCurrentShoppingCart() {
		return shoppingCart;
	}

	@Override
	public void setCurrentShoppingCart(final ShoppingCart shoppingCart) {
		this.shoppingCart = shoppingCart;
	}

	@Override
	public WishList getCurrentWishList() {
		return wishList;
	}

	@Override
	public void setCurrentWishList(final WishList wishList) {
		this.wishList = wishList;
	}

	@Override
	public TagSet getTagSet() {
		return customerSession.getCustomerTagSet();
	}

	@Override
	public ShopperMemento getShopperMemento() {
		return shopperMemento;
	}

	@Override
	public void setShopperMemento(final ShopperMemento shopperMemento) {
		this.shopperMemento = shopperMemento;
	}

	@Override
	public ShopperBrowsingActivity getBrowsingActivity() {
		return browsingActivity;
	}

	public void setBrowsingActivity(final ShopperBrowsingActivity browsingActivity) {
		this.browsingActivity = browsingActivity;
	}

	@Override
	public boolean isPersisted() {
		return shopperMemento.isPersisted();
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}

		if (other instanceof Shopper) {
			Shopper otherShopper = (Shopper) other;
			return getGuid().equals(otherShopper.getGuid());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getGuid().hashCode();
	}

}
