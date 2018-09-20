/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.cartorder.impl;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.service.cartorder.CartOrderShippingInformationSanitizer;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * Implementation of {@link CartOrderShippingInformationSanitizer} that sets invalid GUIDs to null.
 */
public class ClearingCartOrderShippingInformationSanitizer implements CartOrderShippingInformationSanitizer {

	private CustomerAddressDao customerAddressDao;

	private ShippingServiceLevelService shippingServiceLevelService;

	@Override
	public boolean sanitize(final String storeCode, final CartOrder cartOrder) {
		if (cartOrder.getShippingAddressGuid() == null) {
			return false;
		}

		String shippingAddressGuid = cartOrder.getShippingAddressGuid();
		Address shippingAddress = getShippingAddress(shippingAddressGuid);
		if (shippingAddress == null) {
			cartOrder.setShippingAddressGuid(null);
			cartOrder.setShippingServiceLevelGuid(null);
			return true;
		}

		String shippingServiceLevelGuid = cartOrder.getShippingServiceLevelGuid();
		if (shippingServiceLevelGuid != null && !isShippingServiceLevelValid(shippingServiceLevelGuid, storeCode, shippingAddress)) {
			cartOrder.setShippingServiceLevelGuid(null);
			return true;
		}

		return false;
	}

	private boolean isShippingServiceLevelValid(final String shippingServiceLevelGuid, final String storeCode, final Address shippingAddress) {
		ShippingServiceLevel shippingServiceLevel = shippingServiceLevelService.findByGuid(shippingServiceLevelGuid);
		if (shippingServiceLevel == null) {
			return false;
		}

		return shippingServiceLevel.isApplicable(storeCode, shippingAddress);
	}

	private Address getShippingAddress(final String shippingAddressGuid) {
		return getCustomerAddressDao().findByGuid(shippingAddressGuid);
	}

	protected CustomerAddressDao getCustomerAddressDao() {
		return customerAddressDao;
	}

	public void setCustomerAddressDao(final CustomerAddressDao customerAddressDao) {
		this.customerAddressDao = customerAddressDao;
	}

	public void setShippingServiceLevelService(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}
}
