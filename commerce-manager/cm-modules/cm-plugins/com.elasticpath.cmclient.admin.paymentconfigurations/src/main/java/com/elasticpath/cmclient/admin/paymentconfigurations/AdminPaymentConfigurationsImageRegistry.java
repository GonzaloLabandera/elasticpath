/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.elasticpath.cmclient.core.AbstractImageRegistry;

/**
 * Provides an image registry for the AdminPayment plugin. Caches the images so that they don't have to be loaded more than once.
 */
public final class AdminPaymentConfigurationsImageRegistry extends AbstractImageRegistry {

	private static final String PLUGIN_ID = AdminPaymentConfigurationsPlugin.PLUGIN_ID;

	/**
	 * Payment Configurations image.
	 */
	public static final ImageDescriptor IMAGE_PAYMENT_CONFIGURATIONS =
			getImageDescriptor(PLUGIN_ID, "payment-configurations_default_22.png"); //$NON-NLS-1$

	/**
	 * Payment Configuration create image.
	 */
	public static final ImageDescriptor IMAGE_PAYMENT_CONFIGURATIONS_CREATE =
			getImageDescriptor(PLUGIN_ID, "add_default_22.png"); //$NON-NLS-1$

	/**
	 * Payment Configuration edit image.
	 */
	public static final ImageDescriptor IMAGE_PAYMENT_CONFIGURATIONS_EDIT = getImageDescriptor(PLUGIN_ID, "edit_default_22.png"); //$NON-NLS-1$

	/**
	 * Payment Configuration disable image.
	 */
	public static final ImageDescriptor IMAGE_PAYMENT_CONFIGURATIONS_ACTIVATE
			= getImageDescriptor(PLUGIN_ID, "activate_default_22.png"); //$NON-NLS-1$

	/**
	 * Payment Configuration disable image.
	 */
	public static final ImageDescriptor IMAGE_PAYMENT_CONFIGURATIONS_DISABLE
			= getImageDescriptor(PLUGIN_ID, "disable_default_22.png"); //$NON-NLS-1$

	private AdminPaymentConfigurationsImageRegistry() {
		// utility class
	}

	/**
	 * Returns and instance of <code>Image</code> of an <code>ImageDescriptor</code>.
	 *
	 * @param imageDescriptor the image descriptor
	 * @return instance of an <code>Image</code>
	 */
	public static Image getImage(final ImageDescriptor imageDescriptor) {
		return getImage(imageDescriptor, PLUGIN_ID);
	}

	/**
	 * Disposes all the images in the <code>HashMap</code>. Should be called by the Plugin's stop method.
	 */
	public static void disposeAllImages() {
		disposeAllImages(PLUGIN_ID);
	}
}
