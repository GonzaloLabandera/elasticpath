/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.reporting.ordersbystatus;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.elasticpath.cmclient.core.CmSingletonUtil;

/**
 * The activator class controls the plug-in life cycle.
 */
@SuppressWarnings({ "PMD.UseUtilityClass" })
public class ReportingOrdersByStatusPlugin extends AbstractUIPlugin {
	/** The plug-in ID. */
	public static final String PLUGIN_ID = "com.elasticpath.cmclient.reporting.ordersbystatus"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public ReportingOrdersByStatusPlugin() {
		// empty
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static ReportingOrdersByStatusPlugin getDefault() {
		return CmSingletonUtil.getSessionInstance(ReportingOrdersByStatusPlugin.class);
	}
}
