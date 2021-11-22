/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */

package com.elasticpath.cmclient.store.shipping.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.store.shipping.dialogs.ShippingLevelDialog;
import com.elasticpath.cmclient.store.shipping.views.ShippingLevelsSearchResultsView;
import com.elasticpath.commons.constants.EpShippingContextIdNames;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * Create shipping service level action.
 */
public class CreateShippingLevelAction extends Action {

	/** The logger. */
	private static final Logger LOG = LogManager.getLogger(CreateShippingLevelAction.class);

	/** ShippingLevelsSearchResultsView list view. */
	private final ShippingLevelsSearchResultsView listView;

	/**
	 * The constructor.
	 * 
	 * @param listView the shippingLevels list view.
	 * @param text the tool tip text.
	 * @param imageDescriptor the image is shown at the title.
	 */
	public CreateShippingLevelAction(final ShippingLevelsSearchResultsView listView, final String text, final ImageDescriptor imageDescriptor) {
		super(text, imageDescriptor);
		this.listView = listView;
	}

	@Override
	public void run() {
		LOG.debug("CreateShippingLevel Action called."); //$NON-NLS-1$

		ShippingServiceLevel shippingLevel = BeanLocator
				.getPrototypeBean(EpShippingContextIdNames.SHIPPING_SERVICE_LEVEL, ShippingServiceLevel.class);

		if (ShippingLevelDialog.openCreateDialog(listView.getSite().getShell(), shippingLevel)) {
			ShippingServiceLevelService shippingService = BeanLocator
					.getSingletonBean(EpShippingContextIdNames.SHIPPING_SERVICE_LEVEL_SERVICE, ShippingServiceLevelService.class);
			shippingService.add(shippingLevel);
			listView.refreshViewerInput();
		}
	}
}
