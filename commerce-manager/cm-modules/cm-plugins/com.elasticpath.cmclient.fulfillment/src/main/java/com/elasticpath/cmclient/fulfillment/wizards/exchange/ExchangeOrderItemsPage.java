/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.LoginManager;
import com.elasticpath.cmclient.core.binding.EpWizardPageSupport;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnSku;
import com.elasticpath.domain.shoppingcart.ExchangeItem;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;

/**
 * Order new items exchange wizard page.
 */
@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports", "PMD.ExcessiveClassLength", "PMD.TooManyMethods",
		"PMD.GodClass", "PMD.PrematureDeclaration"})
public class ExchangeOrderItemsPage extends AbstractEPWizardPage<ExchangeModel> {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LogManager.getLogger(ExchangeOrderItemsPage.class);

	private static final int CONTENT_MAX_HEIGHT = 550;
	private static final int BOTTOM_MARGIN = 30;

	private ShoppingCart shoppingCart;

	private ShippingInfoSectionPart shippingInfoSection;
	private SummarySectionPart summarySection;
	private OrderedItemsSectionPart orderedItemsSection;
	private IManagedForm managedForm;

	private boolean isConverted;

	private final ReturnAndExchangeService orderExchangeService;
	private PricingSnapshotService pricingSnapshotService;
	private TaxSnapshotService taxSnapshotService;

	/**
	 * The constructor.
	 *
	 * @param pageName the page name
	 */
	protected ExchangeOrderItemsPage(final String pageName) {
		super(1, true, pageName, new DataBindingContext());
		orderExchangeService = BeanLocator.getSingletonBean(ContextIdNames.ORDER_RETURN_SERVICE, ReturnAndExchangeService.class);
		setMessage(FulfillmentMessages.get().ExchangeWizard_OrderItemsExchangePage_Message);
	}

	@Override
	protected void bindControls() {
		// nothing to bind here.
		EpWizardPageSupport.create(this, getDataBindingContext());
	}

	@Override
	protected void populateControls() {
		// nothing to populate here.
		refreshSections();
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite parent) {
		IEpLayoutComposite composite = parent.addTableWrapLayoutComposite(1, true,
				parent.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true));

		managedForm = EpControlFactory.getInstance().createManagedForm(composite.getSwtComposite());
		final TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
		layoutData.maxHeight = CONTENT_MAX_HEIGHT;

		managedForm.getForm().setLayoutData(layoutData);
		final Composite formBody = managedForm.getForm().getBody();
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.bottomMargin = BOTTOM_MARGIN;
		formBody.setLayout(tableWrapLayout);

		final FormToolkit toolkit = managedForm.getToolkit();
		orderedItemsSection = new OrderedItemsSectionPart(this, formBody, toolkit);
		managedForm.addPart(orderedItemsSection);

		shippingInfoSection = new ShippingInfoSectionPart(this, formBody, toolkit);
		managedForm.addPart(shippingInfoSection);

		summarySection = new SummarySectionPart(this, formBody, toolkit);
		managedForm.addPart(summarySection);

		setControl(parent.getSwtComposite());
		refreshSections();
	}

	private boolean isAbleToPopulate() {
		return shippingInfoSection.isShippingMethodSelected()
				&& !orderedItemsSection.getCartItems().isEmpty();
	}

	/**
	 * Populate shopping cart for exchange dialog step.
	 *
	 * @param step exchange dialog step
	 */
	void populateShoppingCart(final OrderPopulateStep step) {
		final OrderReturn orderReturn = getModel().getOrderReturn();
		if (isAbleToPopulate()) {
			int sizeBeforePopulating = orderedItemsSection.getCartItems().size();
			switch (step) {
				case SHIPPING_METHOD_MODIFIED:
				case SHOPPING_CART_MODIFIED:
					shoppingCart = orderExchangeService.populateShoppingCart(
							orderReturn, orderedItemsSection.getCartItems(),
							shippingInfoSection.getShippingOption(),
							shippingInfoSection.getShippingAddress());
					break;
				case SHIPMENT_DISCOUNT_MODIFIED:
				case SHIPPING_COST_MODIFIED:
					shoppingCart = orderExchangeService.populateShoppingCart(
							orderReturn, orderedItemsSection.getCartItems(),
							shippingInfoSection.getShippingOption(),
							summarySection.getShippingCost(),
							summarySection.getShipmentDiscount(),
							shippingInfoSection.getShippingAddress());
					break;
				default:
					break;
			}

			shoppingCart.setCmUserUID(LoginManager.getCmUserId());

			LOG.debug(sizeBeforePopulating + "   " + shoppingCart.getRootShoppingItems().size()); //$NON-NLS-1$
			// --- workaround decision for bug when CartItem was removed from shopping cart(if it is unavailable)
			if (shoppingCart.getRootShoppingItems().size() != sizeBeforePopulating) {
				MessageDialog.openInformation(this.getShell(),
						FulfillmentMessages.get().ExchangeWizard_ItemsUnavailableTitle,
						FulfillmentMessages.get().ExchangeWizard_ItemsUnavailableText);
				orderedItemsSection.updateShoppingItems(shoppingCart.getRootShoppingItems());
			}
			if (!shoppingCart.getRootShoppingItems().isEmpty()) {
				summarySection.updatePrices();
				setErrorMessage(null);
			}
		} else {
			summarySection.populateControls();
		}

		refreshSections();
	}

	@Override
	public boolean beforeFromPrev(final PageChangingEvent event) {
		List<ExchangeItem> exchangeItemsFromReturn = new ArrayList<>();
		if (!isConverted) {
			for (OrderReturnSku cartItem : getModel().getOrderReturn().getOrderReturnSkus()) {
				exchangeItemsFromReturn.add(convertOrderReturnSkuToCartItem(cartItem));
			}
			//important: the conversion of price from order to shopping item is performed only once
			//because we shouldn't lose changes to the price that we made if the Step2->Step1 and then
			//back to Step2 is done
			isConverted = true;
		}
		orderedItemsSection.updateExchangeItemsFromReturn(exchangeItemsFromReturn);
		refreshSections();
		return super.beforeFromPrev(event);
	}

	@Override
	public boolean beforeNext(final PageChangingEvent event) {
		return orderedItemsSection.validate() && shippingInfoSection.validate() && super.beforeNext(event);
	}

	/**
	 * Refresh all dialog sections.
	 */
	void refreshSections() {
		orderedItemsSection.refreshSection();
		shippingInfoSection.refresh();
		shippingInfoSection.getSection().pack();
		summarySection.refresh();
		summarySection.getSection().pack();
		managedForm.refresh();
		managedForm.reflow(true);
	}

	/*
	 * This method should convert OrderReturnSku to OrderReturnSku. IMPORTANT: quantity will be set to 1.
	 */
	private ExchangeItem convertOrderReturnSkuToCartItem(final OrderReturnSku orderReturnSku) {
		ExchangeItem exchangeItem = BeanLocator.getPrototypeBean(ContextIdNames.EXCHANGE_ITEM, ExchangeItem.class);
		exchangeItem.setSkuGuid(orderReturnSku.getSkuGuid());
		exchangeItem.setTaxAmount(orderReturnSku.getTax());

		ShoppingItemPricingSnapshot pricingSnapshot = getPricingSnapshotService().getPricingSnapshotForOrderSku(orderReturnSku.getOrderSku());
		Money unitPrice = pricingSnapshot.getPriceCalc().forUnitPrice().getMoney();
		Price price = BeanLocator.getPrototypeBean(ContextIdNames.PRICE, Price.class);
		price.setCurrency(getOrder().getCurrency());
		price.setListPrice(unitPrice);
		exchangeItem.setPrice(orderReturnSku.getQuantity(), price);
		return exchangeItem;
	}

	Order getOrder() {
		return getModel().getOrderReturn().getOrder();
	}

	ShoppingCart getShoppingCart() {
		return shoppingCart;
	}

	SummarySectionPart getSummarySection() {
		return summarySection;
	}

	/**
	 * Get the pricing snapshot service.
	 *
	 * @return the pricing snapshot service
	 */
	protected PricingSnapshotService getPricingSnapshotService() {
		if (pricingSnapshotService == null) {
			pricingSnapshotService = BeanLocator.getSingletonBean(ContextIdNames.PRICING_SNAPSHOT_SERVICE, PricingSnapshotService.class);
		}
		return pricingSnapshotService;
	}

	/**
	 * Get the tax snapshot service.
	 *
	 * @return the tax snapshot service
	 */
	protected TaxSnapshotService getTaxSnapshotService() {
		if (taxSnapshotService == null) {
			taxSnapshotService = BeanLocator.getSingletonBean(ContextIdNames.TAX_SNAPSHOT_SERVICE, TaxSnapshotService.class);
		}
		return taxSnapshotService;
	}
}
