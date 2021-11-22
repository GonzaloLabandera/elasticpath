/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.shoppingcart.extension.impl;

import java.util.Map;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFOrderDataPopulatorContext;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderDataPopulator;

/**
 * Cart Modifier Order Data Populator.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.ORDER_DATA_POPULATOR, priority = 1010)
public class CartModifierOrderDataPopulator extends XPFExtensionPointImpl implements OrderDataPopulator {

	@Override
	public Map<String, String> collectOrderData(final XPFOrderDataPopulatorContext orderDataPopulationContext) {
		return orderDataPopulationContext.getShoppingCart().getModifierFields();
	}

}
