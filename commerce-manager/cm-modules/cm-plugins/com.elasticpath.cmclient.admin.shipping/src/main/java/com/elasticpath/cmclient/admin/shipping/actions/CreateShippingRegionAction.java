/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.admin.shipping.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.elasticpath.cmclient.admin.shipping.dialogs.ShippingRegionDialog;
import com.elasticpath.cmclient.admin.shipping.views.ShippingRegionListView;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.EpShippingContextIdNames;
import com.elasticpath.domain.shipping.ShippingRegion;
import com.elasticpath.service.shipping.ShippingRegionService;

/**
 * Create shipping region action.
 */
public class CreateShippingRegionAction extends Action {

	private final ShippingRegionListView listView;

	/**
	 * The constructor.
	 * 
	 * @param listView owning view for this action.
	 * @param text action title.
	 * @param image descriptor for action icon image.
	 */
	public CreateShippingRegionAction(final ShippingRegionListView listView, final String text, final ImageDescriptor image) {
		super(text, image);
		this.listView = listView;
	}

	@Override
	public void run() {
		ShippingRegion shippingRegion = BeanLocator.getPrototypeBean(EpShippingContextIdNames.SHIPPING_REGION, ShippingRegion.class);

		if (ShippingRegionDialog.openCreateDialog(listView.getSite().getShell(), shippingRegion)) {
			ShippingRegionService shippingRegionService = BeanLocator.getSingletonBean(
					EpShippingContextIdNames.SHIPPING_REGION_SERVICE, ShippingRegionService.class);

			shippingRegionService.add(shippingRegion);
			listView.refreshViewerInput();
		}
	}
}
