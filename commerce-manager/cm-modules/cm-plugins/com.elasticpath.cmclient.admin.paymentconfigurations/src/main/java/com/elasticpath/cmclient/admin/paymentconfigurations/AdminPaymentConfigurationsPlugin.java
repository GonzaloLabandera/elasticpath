/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations;

import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

import com.elasticpath.cmclient.core.service.AuthorizationService;
import com.elasticpath.cmclient.core.ui.AbstractEpUIPlugin;

/**
 * The activator class controls the plug-in life cycle.
 */
public class AdminPaymentConfigurationsPlugin extends AbstractEpUIPlugin {

	/**
	 * The plug-in ID.
	 */
	public static final String PLUGIN_ID = "com.elasticpath.cmclient.admin.paymentconfigurations"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public AdminPaymentConfigurationsPlugin() {
		super();
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Determines whether the current user is authorized to use this plugin.
	 *
	 * @return true if yes, else false.
	 */
	public static boolean isAuthorized() {
		return AuthorizationService.getInstance()
				.isAuthorizedWithPermission(AdminPaymentConfigurationsPermissions.ADMIN_PAYMENT_CONFIGURATIONS_MANAGE);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		try {
			AdminPaymentConfigurationsImageRegistry.disposeAllImages();
		} catch (ExceptionInInitializerError | IllegalStateException e) {
			// Do nothing.
		}
		super.stop(context);
	}

	@Override
	protected void loadLocalizedMessages() {
		AdminPaymentConfigurationMessages.get();
	}
}
