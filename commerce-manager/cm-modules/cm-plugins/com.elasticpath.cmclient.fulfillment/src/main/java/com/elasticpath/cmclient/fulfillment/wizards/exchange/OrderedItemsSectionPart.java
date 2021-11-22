/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import static com.elasticpath.cmclient.fulfillment.wizards.exchange.OrderPopulateStep.SHOPPING_CART_MODIFIED;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.helpers.LocalProductSkuLookup;
import com.elasticpath.cmclient.core.helpers.PriceHelper;
import com.elasticpath.cmclient.core.ui.dialog.SkuFinderDialog;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.ProductSkuChecker;
import com.elasticpath.cmclient.fulfillment.editors.order.dialog.InlinePriceEditingSupport;
import com.elasticpath.common.dto.pricing.BaseAmountDTO;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ExchangeItem;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * This section contains Cart Items to be ordered.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.GodClass"})
public class OrderedItemsSectionPart extends AbstractCmClientFormSectionPart {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LogManager.getLogger(OrderedItemsSectionPart.class);

	private static final int ORDER_SKU_TABLE_HEIGHT = 100;
	private static final int ORDER_SKU_TABLE_WIDTH = 760;

	private static final int COLUMN_WIDTH_IMAGE = 25;
	private static final int COLUMN_WIDTH_SKU_CODE = 150;
	private static final int COLUMN_WIDTH_PRODUCT_NAME = 240;
	private static final int COLUMN_WIDTH_SKU_OPTIONS = 100;
	private static final int COLUMN_WIDTH_ORDER_QUANTITY = 80;
	private static final int COLUMN_WIDTH_UNIT_PRICE = 140;

	private static final String EXCHANGE_ORDER_ITEMS_TABLE = "Exchange Order Items Table"; //$NON-NLS-1$

	private final ExchangeOrderItemsPage parentPage;

	/**
	 * Ordered Items IEpTableViewer.
	 */
	private IEpTableViewer orderedItemsTableViewer;

	/**
	 * List of cart items to getOrder().
	 */
	private final List<ExchangeItem> cartItems;

	private ProductSkuLookup productSkuLookup;

	private Button createItemButton;
	private Button deleteItemButton;
	private Action createItemAction;
	private Action deleteItemAction;

	private final Locale locale;

	/**
	 * Constructor.
	 *
	 * @param parentPage parent page
	 * @param parent     the parent form
	 * @param toolkit    the form toolkit
	 */
	public OrderedItemsSectionPart(final ExchangeOrderItemsPage parentPage, final Composite parent, final FormToolkit toolkit) {
		super(parent, toolkit, parentPage.getDataBindingContext(), ExpandableComposite.TITLE_BAR);
		this.parentPage = parentPage;
		this.locale = parentPage.getOrder().getLocale();
		this.cartItems = new ArrayList<>();
	}

	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		IEpLayoutComposite mainPane = CompositeFactory.createTableWrapLayoutComposite(getSection(), 1, false);
		createOrderSkuTableControl(mainPane);
		createTableButtons(mainPane);

		orderedItemsTableViewer.setContentProvider(new ArrayContentProvider());
		orderedItemsTableViewer.getSwtTableViewer().addSelectionChangedListener(event -> {
			final IStructuredSelection strSelection = (IStructuredSelection) event.getSelection();
			Object firstSelection = strSelection.getFirstElement();
			deleteItemButton.setEnabled(firstSelection != null);
		});
		getSection().setClient(mainPane.getSwtComposite());
	}

	private void createOrderSkuTableControl(final IEpLayoutComposite mainPane) {
		final IEpLayoutData tableData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, false, true);
		orderedItemsTableViewer = mainPane.addTableViewer(false, EpControlFactory.EpState.EDITABLE, tableData, EXCHANGE_ORDER_ITEMS_TABLE);
		TableWrapData layout = new TableWrapData();
		layout.maxWidth = ORDER_SKU_TABLE_WIDTH;
		layout.maxHeight = ORDER_SKU_TABLE_HEIGHT;
		orderedItemsTableViewer.getSwtTable().setLayoutData(layout);
		initializeTableViewer();
	}

	private void createTableButtons(final IEpLayoutComposite mainPane) {
		final IEpLayoutData layoutData = mainPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.END, false, true);
		IEpLayoutComposite buttonsPane = mainPane.addGridLayoutComposite(2, true, layoutData);
		createItemButton = addPushButton(buttonsPane, layoutData, FulfillmentMessages.get().ExchangeWizard_AddItem_Button,
				CoreImageRegistry.IMAGE_ADD, true);
		createItemAction = new AddCartItemAction();
		createItemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				createItemAction.run();
			}
		});

		deleteItemButton = addPushButton(buttonsPane, layoutData, FulfillmentMessages.get().ExchangeWizard_RemoveItem_Button,
				CoreImageRegistry.IMAGE_REMOVE, false);
		deleteItemAction = new RemoveCartItemAction();
		deleteItemButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				deleteItemAction.run();
			}
		});
	}

	/**
	 * Refresh cart items list view.
	 */
	void refreshSection() {
		orderedItemsTableViewer.getSwtTableViewer().refresh();
		getSection().pack();
	}

	List<ExchangeItem> getCartItems() {
		return cartItems;
	}

	/**
	 * Update shopping items in exchange cart.
	 *
	 * @param shoppingItems shopping items
	 */
	void updateShoppingItems(final List<ShoppingItem> shoppingItems) {
		orderedItemsTableViewer.setInput(shoppingItems);
		parentPage.refreshSections();
	}

	/**
	 * Update exchange items from order return.
	 *
	 * @param exchangeItemsFromReturn exchange items
	 */
	void updateExchangeItemsFromReturn(final List<ExchangeItem> exchangeItemsFromReturn) {
		for (ExchangeItem exchangeItem : exchangeItemsFromReturn) {
			final ProductSku productSku = getProductSku(exchangeItem);
			final ExchangeItem cartItem = getCartItemByProductSkuUid(productSku.getUidPk());
			if (cartItem == null) {
				//In case when current product sku doesn't exist in our cart and the qty is positive than we should add it.
				if (exchangeItem.getQuantity() > 0) {
					cartItems.add(exchangeItem);
				}
			} else {
				//In case when current product sku already exist in cart we should set quantity that was changed on first page.
				cartItem.setPrice(exchangeItem.getQuantity(), exchangeItem.getPrice());
			}
		}
		parentPage.refreshSections();
	}

	/*
	 * This method initialize table viewer.
	 */
	@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.CyclomaticComplexity", "squid:MaximumInheritanceDepth"})
	private void initializeTableViewer() {
		final IEpTableColumn skuImage = orderedItemsTableViewer.addTableColumn("", COLUMN_WIDTH_IMAGE);
		skuImage.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				return FulfillmentImageRegistry.getImage(FulfillmentImageRegistry.ICON_ORDERTABLE_ITEM);
			}
		});
		final IEpTableColumn skuCodeColumn = orderedItemsTableViewer.addTableColumn(
				FulfillmentMessages.get().ExchangeWizard_SKUCode_Column, COLUMN_WIDTH_SKU_CODE);
		skuCodeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final ShoppingItem cartItem = (ShoppingItem) element;
				return getProductSku(cartItem).getSkuCode();
			}
		});
		final IEpTableColumn productNameColumn = orderedItemsTableViewer.addTableColumn(
				FulfillmentMessages.get().ExchangeWizard_ProductName_Column, COLUMN_WIDTH_PRODUCT_NAME);
		productNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final ShoppingItem cartItem = (ShoppingItem) element;
				final ProductSku productSku = getProductSkuLookup().findByGuid(cartItem.getSkuGuid());
				return productSku.getProduct().getDisplayName(locale);
			}
		});
		final IEpTableColumn skuOptionsColumn = orderedItemsTableViewer.addTableColumn(
				FulfillmentMessages.get().ExchangeWizard_SKUOptions_Column, COLUMN_WIDTH_SKU_OPTIONS);
		skuOptionsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				final ShoppingItem cartItem = (ShoppingItem) element;
				return getItemOptions(cartItem);
			}

			private String getItemOptions(final ShoppingItem item) {
				StringBuilder builder = new StringBuilder();
				for (SkuOptionValue option : getProductSku(item).getOptionValues()) {
					builder.append(option.getDisplayName(locale, true));
					builder.append(FulfillmentMessages.SPACE);
				}
				return builder.toString();
			}
		});
		final IEpTableColumn skuQuantityColumn = orderedItemsTableViewer.addTableColumn(
				FulfillmentMessages.get().ExchangeWizard_OrderQty_Column, COLUMN_WIDTH_ORDER_QUANTITY);
		skuQuantityColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				return CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT);
			}

			@Override
			public String getText(final Object element) {
				final ShoppingItem cartItem = (ShoppingItem) element;
				return String.valueOf(cartItem.getQuantity());
			}
		});
		skuQuantityColumn.setEditingSupport(new SkuQuantityEditingSupport());
		final IEpTableColumn skuPriceColumn = orderedItemsTableViewer.addTableColumn(
				FulfillmentMessages.get().ExchangeWizard_UnitPrice_Column, COLUMN_WIDTH_UNIT_PRICE);
		skuPriceColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(final Object element) {
				return CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT);
			}

			@Override
			public String getText(final Object element) {
				final ExchangeItem shoppingItem = (ExchangeItem) element;
				if (shoppingItem.getSaleUnitPrice() != null) {
					return shoppingItem.getSaleUnitPrice().getAmount().toString();
				}
				if (shoppingItem.getListUnitPrice() != null) {
					return shoppingItem.getListUnitPrice().getAmount().toString();
				}
				return StringUtils.EMPTY;
			}
		});
		skuPriceColumn.setEditingSupport(new InlinePriceEditingSupport(orderedItemsTableViewer,
				parentPage.getModel().getOrderReturn().getOrderShipmentForReturn(),
				new Action() {
					@Override
					public void run() {
						parentPage.populateShoppingCart(SHOPPING_CART_MODIFIED);
					}
				},
				getBindingContext()));
	}

	/**
	 * Add <code>CartItem</code> to the <code>ShoppingCart</code> Action.
	 */
	protected class AddCartItemAction extends Action {

		@Override
		public void run() {
			SkuFinderDialog dialog = new SkuFinderDialog(parentPage.getShell(),
					parentPage.getOrder().getStore().getCatalog(), true);
			if (dialog.open() == Window.OK) {
				final ExchangeItem cartItem = returnCartItemWithProductSku(dialog);
				if (cartItem == null) {
					return;
				}
				ExchangeItem item = getCartItemByProductSkuUid(getProductSku(cartItem).getUidPk());
				if (item == null) {
					LOG.debug("we got new sku: " + getProductSku(cartItem).getSkuCode()); //$NON-NLS-1$
					enrichItemWithDialogSelectedPrice(cartItem, dialog.getSelectedItemPriceSummary(),
							Currency.getInstance(dialog.getCurrentCurrencyCode()));
					cartItems.add(cartItem);
					refreshSection();
				} else { // when ProductSku is already exists in cartItems we should increase it quantity
					int newQuantity = item.getQuantity() + 1;
					LOG.debug("quantity was increased: " + newQuantity); //$NON-NLS-1$
					orderedItemsTableViewer.getSwtTableViewer().update(item, null);
				}
				parentPage.populateShoppingCart(SHOPPING_CART_MODIFIED);
			}
		}

		private void enrichItemWithDialogSelectedPrice(final ExchangeItem item, final BaseAmountDTO dto, final Currency priceCurrency) {
			final PriceHelper priceHelper = new PriceHelper();
			item.setPrice(dto.getQuantity().intValue(), priceHelper.createPriceFromBaseAmountDto(dto, priceCurrency));
		}

		/*
		 * This method retrieves ProductSku from SkuFinderDialog and returns cart item instance. IMPORTANT: in case if Product was retrieved default
		 * product SKU Item will be set for the cart item.
		 */
		private ExchangeItem returnCartItemWithProductSku(final SkuFinderDialog dialog) {
			ProductSku productSku = null;
			if (dialog.getSelectedObject() instanceof Product) {
				Product product = (Product) dialog.getSelectedObject();
				LOG.debug("product was returned: " + product.getDisplayName(locale)); //$NON-NLS-1$
				productSku = product.getDefaultSku();
			}

			if (dialog.getSelectedObject() instanceof ProductSku) {
				productSku = (ProductSku) dialog.getSelectedObject();
				LOG.debug("productSku was returned: " + productSku.getDisplayName(locale)); //$NON-NLS-1$

				ProductSkuChecker psc = new ProductSkuChecker(parentPage.getOrder());
				if (psc.isRecurringSku(productSku)) {
					MessageDialog.openError(null, FulfillmentMessages.get().Exchange_AddingItem_NoRecurring_Title,
							FulfillmentMessages.get().Exchange_AddingItem_NoRecurring_Message);
					return null;
				}
			}
			if (productSku == null) {
				return null;
			}
			final ExchangeItem cartItem = BeanLocator.getPrototypeBean(ContextIdNames.EXCHANGE_ITEM, ExchangeItem.class);
			cartItem.setSkuGuid(productSku.getGuid());
			return cartItem;
		}
	}

	/**
	 * Remove <code>CartItem</code> from the <code>ShoppingCart</code> Action.
	 */
	protected class RemoveCartItemAction extends Action {

		@Override
		public void run() {
			ShoppingItem selectedItem = getSelectedCartItem();
			if (selectedItem == null) {
				return;
			}
			final ProductSku productSku = getProductSku(selectedItem);
			boolean confirmed = MessageDialog.openConfirm(parentPage.getShell(),
					FulfillmentMessages.get().ExchangeWizard_RemoveLineItemConfirmTitle,
					NLS.bind(FulfillmentMessages.get().ExchangeWizard_RemoveLineItemConfirmText,
							productSku.getProduct().getDisplayName(locale)));
			if (confirmed) {
				cartItems.remove(selectedItem);
				parentPage.populateShoppingCart(SHOPPING_CART_MODIFIED);
			}
		}

		/**
		 * Get selected Cart Item.
		 *
		 * @return the selected cart item
		 */
		private ShoppingItem getSelectedCartItem() {
			IStructuredSelection selection = (IStructuredSelection) orderedItemsTableViewer.getSwtTableViewer().getSelection();
			if (selection.isEmpty()) {
				return null;
			}
			return (ShoppingItem) selection.getFirstElement();
		}

	}

	private Button addPushButton(final IEpLayoutComposite composite, final IEpLayoutData layout, final String text,
								 final ImageDescriptor descriptor, final boolean enabled) {
		Button taxButton = composite.addPushButton("", EpControlFactory.EpState.EDITABLE, layout); //$NON-NLS-1$
		taxButton.setImage(CoreImageRegistry.getImage(descriptor));
		taxButton.setText(text);
		taxButton.setEnabled(enabled);
		return taxButton;
	}

	@Override
	protected void populateControls() {
		orderedItemsTableViewer.setInput(cartItems);
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().ExchangeWizard_ItemsToBeOrdered_Section;
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// nothing to bound here.
	}

	/**
	 * Validate this section.
	 *
	 * @return true if section is valid
	 */
	public boolean validate() {
		if (cartItems.isEmpty()) {
			parentPage.setErrorMessage(FulfillmentMessages.get().ExchangeWizard_NoSkusToExchange_Message);
			return false;
		}
		for (ExchangeItem orderSku : cartItems) {
			if (orderSku.getListUnitPrice() == null && orderSku.getSaleUnitPrice() == null) {
				parentPage.setErrorMessage(
						NLS.bind(FulfillmentMessages.get().Exchange_AddingItem_Message_NoPrice,
								new Object[]{getProductSku(orderSku).getSkuCode()}));
				return false;
			}
		}
		return true;
	}

	/*
	 * Returns CartItem from cartItems by uidPk.
	 */
	private ExchangeItem getCartItemByProductSkuUid(final long uidPk) {
		for (ExchangeItem item : cartItems) {
			if (getProductSku(item).getUidPk() == uidPk) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Returns the given cart item's product sku.
	 *
	 * @param cartItem the cart item
	 * @return the given cart item's product sku
	 */
	protected ProductSku getProductSku(final ShoppingItem cartItem) {
		return getProductSkuLookup().findByGuid(cartItem.getSkuGuid());
	}

	/**
	 * Lazy loads a ProductSkuLookup.
	 *
	 * @return a product sku reader.
	 */
	protected ProductSkuLookup getProductSkuLookup() {
		if (productSkuLookup == null) {
			productSkuLookup = new LocalProductSkuLookup();
		}

		return productSkuLookup;
	}

	/**
	 * Editing Support for SKU quantity.
	 */
	protected class SkuQuantityEditingSupport extends EditingSupport {

		private final CellEditor editor;

		/**
		 * The constructor.
		 */
		public SkuQuantityEditingSupport() {
			super(orderedItemsTableViewer.getSwtTableViewer());
			editor = new TextCellEditor(orderedItemsTableViewer.getSwtTable());
		}

		@Override
		protected boolean canEdit(final Object element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			return editor;
		}

		@Override
		protected Object getValue(final Object element) {
			return String.valueOf(((ShoppingItem) element).getQuantity());
		}

		@Override
		protected void setValue(final Object element, final Object value) {
			doSetValue(element, value);
			getViewer().update(element, null);
		}

		/**
		 * Set value.
		 *
		 * @param element exchange item
		 * @param value   sku quantity
		 */
		protected void doSetValue(final Object element, final Object value) {
			ExchangeItem item = (ExchangeItem) element;
			try {
				Integer orderQuantity = Integer.valueOf(value.toString());
				if (EpValidatorFactory.POSITIVE_INTEGER.validate(orderQuantity) == ValidationStatus.ok()
						&& orderQuantity >= getProductSku(item).getProduct().getMinOrderQty()) {
					item.setPrice(orderQuantity, item.getPrice());
					parentPage.populateShoppingCart(SHOPPING_CART_MODIFIED);
				}
			} catch (NumberFormatException exception) {
				LOG.debug("wrong format", exception); //$NON-NLS-1$
			}
		}
	}
}
