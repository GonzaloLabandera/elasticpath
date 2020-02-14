/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.ui.framework.impl;

/**
 * This allows for text to be disabled(greyed out) in the EpTableColoringLabelProvider class.
 */
public interface EpDisabledColorTextDecorator {
	/**
	 * Should the row be disabled so the text should be greyed out.
	 * @return is the row disabled
	 */
	boolean isDisabled();
}
