/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.cartorder.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.cartorder.CartOrderShippingInformationSanitizer;
import com.elasticpath.service.cartorder.CartOrderShippingService;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * This Class can perform services for CartOrders related to shipping.
 */
public class CartOrderShippingServiceImpl implements CartOrderShippingService {

	private ShippingServiceLevelService shippingServiceLevelService;

	private CartOrderShippingInformationSanitizer cartOrderShippingInformationSanitizer;

	private CustomerAddressDao customerAddressDao;

	@Override
	public Boolean updateCartOrderShippingAddress(final String shippingAddressGuid, final CartOrder cartOrder, final String storeCode) {
		if (!isUpdateNeeded(shippingAddressGuid, cartOrder)) {
			return false;
		}

		cartOrder.setShippingAddressGuid(shippingAddressGuid);

		cartOrderShippingInformationSanitizer.sanitize(storeCode, cartOrder);

		Address shippingAddress = customerAddressDao.findByGuid(shippingAddressGuid);
		final List<ShippingServiceLevel> shippingServiceLevels = findShippingServiceLevels(storeCode, shippingAddress);

		if (CollectionUtils.isNotEmpty(shippingServiceLevels)) {
			String existingShippingServiceLevel = cartOrder.getShippingServiceLevelGuid();
			ShippingServiceLevel matchingShippingServiceLevel = getShippingServiceLevelMatchingGuid(shippingServiceLevels,
					existingShippingServiceLevel);

			boolean existingShippingLevelIsNoLongerValid = matchingShippingServiceLevel == null;
			if (existingShippingLevelIsNoLongerValid) {
				ShippingServiceLevel defaultShippingServiceLevel = shippingServiceLevels.get(0);
				cartOrder.setShippingServiceLevelGuid(defaultShippingServiceLevel.getGuid());
			}
		}
		return true;
	}

	/**
	 * Determine if an address update is necessary. We only need to update if the shipping address
	 * guid has changed or is null, or the shipping service level guid is null.
	 *
	 * @param shippingAddressGuid the new shipping address guid
	 * @param cartOrder the cart order
	 * @return true if an update is needed
	 */
	protected boolean isUpdateNeeded(final String shippingAddressGuid, final CartOrder cartOrder) {
		String cartOrderShippingAddressGuid = cartOrder.getShippingAddressGuid();
		return cartOrderShippingAddressGuid == null
			|| !cartOrderShippingAddressGuid.equals(shippingAddressGuid)
			|| cartOrder.getShippingServiceLevelGuid() == null;
	}

	@Override
	public ShoppingCart populateShoppingCartTransientFields(final ShoppingCart shoppingCart, final CartOrder cartOrder) {

		return populateAddressAndShippingFields(shoppingCart, cartOrder);
	}

	@Override
	public ShoppingCart populateAddressAndShippingFields(final ShoppingCart shoppingCart, final CartOrder cartOrder) {
		Address billingAddress = customerAddressDao.findByGuid(cartOrder.getBillingAddressGuid());
		shoppingCart.setBillingAddress(billingAddress);
		Address shippingAddress = customerAddressDao.findByGuid(cartOrder.getShippingAddressGuid());
		shoppingCart.setShippingAddress(shippingAddress);
		Store store = shoppingCart.getStore();

		final List<ShippingServiceLevel> shippingServiceLevels = findShippingServiceLevels(store.getCode(), shippingAddress);

		if (CollectionUtils.isNotEmpty(shippingServiceLevels)) {

			String shippingServiceLevelGuidFromCartOrder = cartOrder.getShippingServiceLevelGuid();
			ShippingServiceLevel matchingShippingServiceLevel = getShippingServiceLevelMatchingGuid(shippingServiceLevels,
					shippingServiceLevelGuidFromCartOrder);

			if (matchingShippingServiceLevel != null) {
				shoppingCart.setShippingServiceLevelList(shippingServiceLevels);
				shoppingCart.setSelectedShippingServiceLevelUid(matchingShippingServiceLevel.getUidPk());
			}
		}

		return shoppingCart;
	}

	@Override
	public List<ShippingServiceLevel> findShippingServiceLevels(final String storeCode, final Address shippingAddress) {
		return shippingServiceLevelService.retrieveShippingServiceLevel(storeCode, shippingAddress);
	}

	private ShippingServiceLevel getShippingServiceLevelMatchingGuid(final List<ShippingServiceLevel> shippingServiceLevels,
			final String shippingServiceLevelGuid) {
		for (ShippingServiceLevel currentShippingServiceLevel : shippingServiceLevels) {
			String currentShippingServiceLevelGuid = currentShippingServiceLevel.getGuid();
			if (currentShippingServiceLevelGuid.equals(shippingServiceLevelGuid)) {
				return currentShippingServiceLevel;
			}
		}
		return null;
	}

	protected ShippingServiceLevelService getShippingServiceLevelService() {
		return shippingServiceLevelService;
	}

	public void setShippingServiceLevelService(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}

	protected CartOrderShippingInformationSanitizer getCartOrderShippingInformationSanitizer() {
		return cartOrderShippingInformationSanitizer;
	}

	public void setCartOrderShippingInformationSanitizer(final CartOrderShippingInformationSanitizer cartOrderShippingInformationSanitizer) {
		this.cartOrderShippingInformationSanitizer = cartOrderShippingInformationSanitizer;
	}

	protected CustomerAddressDao getCustomerAddressDao() {
		return customerAddressDao;
	}

	public void setCustomerAddressDao(final CustomerAddressDao customerAddressDao) {
		this.customerAddressDao = customerAddressDao;
	}
}
