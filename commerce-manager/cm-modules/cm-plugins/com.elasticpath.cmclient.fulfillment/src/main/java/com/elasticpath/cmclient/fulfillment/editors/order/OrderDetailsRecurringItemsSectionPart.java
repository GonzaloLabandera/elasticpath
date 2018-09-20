/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.fulfillment.editors.order;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.catalog.editors.product.ProductEditor;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.ServiceLocator;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.editors.GuidEditorInput;
import com.elasticpath.cmclient.core.helpers.LocalProductSkuLookup;
import com.elasticpath.cmclient.core.service.AuthorizationService;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.OrderSkuComparatorFactory;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceSchedule;
import com.elasticpath.domain.catalog.PriceScheduleType;
import com.elasticpath.domain.catalog.PricingScheme;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.SimplePrice;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;

/**
 * Represents the UI of an order shipment.
 */
public class OrderDetailsRecurringItemsSectionPart extends AbstractCmClientEditorPageSectionPart 
	implements SelectionListener, ISelectionChangedListener {
	
	private static final Logger LOG = Logger.getLogger(OrderDetailsRecurringItemsSectionPart.class);

	private static final int COLUMN_WIDTH_IMAGE = 21;

	private static final int ORDER_SKU_TABLE_HEIGHT = 200;
	
	private static final int COLUMN_WIDTH_BUNDLE_NAME = 120;
	
	private static final int COLUMN_WIDTH_SKU_CODE = 84;

	private static final int COLUMN_WIDTH_PRODUCT_NAME = 141;

	private static final int COLUMN_WIDTH_SKU_OPTION = 80;

	private static final int COLUMN_WIDTH_LIST_PRICE = 80;
	
	private static final int COLUMN_WIDTH_PAYMENT_SCHEDULE = 140;

	private static final int COLUMN_WIDTH_INVOICE_PRICE = 80;

	private static final int COLUMN_WIDTH_QUANTITY = 40;

	private static final String ORDER_DETAILS_RECURRING_ITEMS_TABLE = "Order Details Recurring Items Table"; //$NON-NLS-1$

	private IEpTableViewer shipmentOrderSkuTable;

	private final OrderShipment shipment;

	private IEpLayoutComposite mainPane;

	
	private Button openProductButton;

	private final AbstractCmClientFormEditor editor;
	private ProductSkuLookup productSkuLookup;
	private PricingSnapshotService pricingSnapshotService;

	/**
	 * Constructor.
	 *
	 * @param formPage the formpage
	 * @param editor the CmClientFormEditor that contains the form
	 * @param shipment the order shipment that this section represents
	 */
	public OrderDetailsRecurringItemsSectionPart(final FormPage formPage, final AbstractCmClientFormEditor editor,
			final OrderShipment shipment) {
		super(formPage, editor, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		//this.order = (Order) editor.getModel();
		this.shipment = shipment;
		this.editor = editor;
	}

	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		mainPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		mainPane.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));

		final IEpLayoutData tableData = mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);

		shipmentOrderSkuTable = mainPane.addTableViewer(false, EpState.READ_ONLY, tableData, ORDER_DETAILS_RECURRING_ITEMS_TABLE);
		((TableWrapData) shipmentOrderSkuTable.getSwtTable().getLayoutData()).maxHeight = ORDER_SKU_TABLE_HEIGHT;
		
		final IEpTableColumn imageColumn = shipmentOrderSkuTable
				.addTableColumn(FulfillmentMessages.EMPTY_STRING, COLUMN_WIDTH_IMAGE);
		imageColumn.setLabelProvider(new ImageColumnLabelProvider());
		imageColumn.getSwtTableColumn().setResizable(false);
		
		final IEpTableColumn bundleNameColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_BundleName,
				COLUMN_WIDTH_BUNDLE_NAME);
		bundleNameColumn.setLabelProvider(new BundleNameLabelProvider());
		
		final IEpTableColumn skuCodeColumn = shipmentOrderSkuTable
				.addTableColumn(FulfillmentMessages.get().ShipmentSection_SkuCode, COLUMN_WIDTH_SKU_CODE);
		skuCodeColumn.setLabelProvider(new OrderSkuColumnLabelProvider());

		final IEpTableColumn productNameColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_ProductName,
				COLUMN_WIDTH_PRODUCT_NAME);
		productNameColumn.setLabelProvider(new ProductNameColumnLabelProvider());

		final IEpTableColumn skuOptionColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_SkuOption,
				COLUMN_WIDTH_SKU_OPTION);
		skuOptionColumn.setLabelProvider(new SkuOptionColumnLabelProvider());

		final IEpTableColumn skuListPriceColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_ListPrice,
				COLUMN_WIDTH_LIST_PRICE);
		skuListPriceColumn.setLabelProvider(new ListPriceColumnLabelProvider());

		final IEpTableColumn skuQuantityColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_Quantity,
				COLUMN_WIDTH_QUANTITY);
		skuQuantityColumn.setLabelProvider(new QuantityColumnLabelProvider());

		final IEpTableColumn skuInvoicePriceColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_InvoicePrice,
				COLUMN_WIDTH_INVOICE_PRICE);
		skuInvoicePriceColumn.setLabelProvider(new InvoicePriceColumnLabelProvider());

		final IEpTableColumn paymentScheduleColumn = shipmentOrderSkuTable.addTableColumn(FulfillmentMessages.get().ShipmentSection_PaymentSchedule,
				COLUMN_WIDTH_PAYMENT_SCHEDULE);
		paymentScheduleColumn.setLabelProvider(new PaymentScheduleColumnLabelProvider());
		
		
		shipmentOrderSkuTable.getSwtTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				
				final OrderSku orderSku = getSelectedOrderSku();
				final ProductSku productSku = getProductSkuLookup().findByGuid(orderSku.getSkuGuid());
				if (AuthorizationService.getInstance().isAuthorizedForAnyProductCatalog(productSku.getProduct())) {
	
					openProductButton.setEnabled(true);						

				}
				
			}
		});
		
		
		openProductButton = mainPane.addPushButton(FulfillmentMessages.get().ShipmentSection_OpenProductButton, CoreImageRegistry
				.getImage(CoreImageRegistry.PRODUCT), EpState.EDITABLE, mainPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING));
		openProductButton.setEnabled(false);
		openProductButton.addSelectionListener(this);
		
	}

	
	private OrderSku getSelectedOrderSku() {
		return (OrderSku) ((IStructuredSelection) shipmentOrderSkuTable.getSwtTableViewer().getSelection()).getFirstElement();
	}
	
	@Override
	protected String getSectionDescription() {
		return FulfillmentMessages.get().ElectronicShipmentSection_Description;
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().RecurringItemsSection_Title;
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// not used
	}

	/**
	 * Column label provider.
	 */
	private final class QuantityColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			final OrderSku orderSku = (OrderSku) element;
			return Integer.toString(orderSku.getQuantity());
		}
	}

	/**
	 * Column label provider.
	 */
	@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
	private final class InvoicePriceColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			final OrderSku orderSku = (OrderSku) element;

			Price price = getPricingSnapshotService().getPricingSnapshotForOrderSku(orderSku).getPrice();
			
			PricingScheme pricingScheme = price.getPricingScheme();
			
			if (pricingScheme != null) {
			
				Collection<PriceSchedule> schedules = pricingScheme.getSchedules(PriceScheduleType.RECURRING);
				
				for (PriceSchedule pricingSchedule : schedules) {

					SimplePrice simplePrice = pricingScheme.getSimplePriceForSchedule(pricingSchedule);

					Money lowestPrice = simplePrice.getLowestPrice(orderSku.getQuantity());

					return lowestPrice.getAmountUnscaled().toString();
				}
			
			}
			
			
			// by default return blank
			
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Column label provider.
	 */
	@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
	private final class ListPriceColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			final OrderSku orderSku = (OrderSku) element;

			Price price = getPricingSnapshotService().getPricingSnapshotForOrderSku(orderSku).getPrice();
			
			PricingScheme pricingScheme = price.getPricingScheme();
			
			if (pricingScheme != null) {
			
				Collection<PriceSchedule> schedules = pricingScheme.getSchedules(PriceScheduleType.RECURRING);
				
				for (PriceSchedule pricingSchedule : schedules) {

					SimplePrice simplePrice = pricingScheme.getSimplePriceForSchedule(pricingSchedule);

					Money listPrice = simplePrice.getListPrice(orderSku.getQuantity());

					return listPrice.getAmountUnscaled().toString();
				}
			
			}
			
			// by default return blank
			
			return ""; //$NON-NLS-1$
		}
	}
	
	
	/**
	 * Column label provider.
	 */
	private final class PaymentScheduleColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {

			String text = StringUtils.EMPTY;

			final OrderSku orderSku = (OrderSku) element;
			final ProductSku productSku = getProductSkuLookup().findByGuid(orderSku.getSkuGuid());
			Map<String, SkuOptionValue> optionValueMap = productSku.getOptionValueMap();
			if (optionValueMap != null) {
				final SkuOptionValue skuOptionValue = optionValueMap.get("Frequency"); //$NON-NLS-1$
				if (skuOptionValue != null) {
					text = skuOptionValue.getDisplayName(CorePlugin.getDefault().getDefaultLocale(), true);
				}
			}
			return text;
		}
	}

	/**
	 * Column label provider.
	 */
	private final class ProductNameColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			final OrderSku orderSku = (OrderSku) element;
			return orderSku.getDisplayName();
		}
	}
	
	/**
	 * Column label provider for displaying bundle name.
	 */
	private final class BundleNameLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			final OrderSku rootOrderSku = ((OrderSku) element).getRoot();
			if (rootOrderSku == null) {
				return emptyString();
			}

			final Product product = getProductSkuLookup().findByGuid(rootOrderSku.getSkuGuid()).getProduct();
			if (!isBundle(product)) {
				return emptyString();
			}

			final Order order = (Order) getEditor().getModel();
			return product.getDisplayName(order.getLocale());
		}

		private String emptyString() {
			return ""; //$NON-NLS-1$;
		}

		private boolean isBundle(final Product product) {
			return product instanceof ProductBundle;
		}
	}

	/**
	 * Column label provider.
	 */
	private final class OrderSkuColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			final OrderSku orderSku = (OrderSku) element;
			return orderSku.getSkuCode();
		}
	}

	/**
	 * Provider of text and image.
	 */
	private final class ImageColumnLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(final Object element) {
			String text = FulfillmentMessages.EMPTY_STRING;
			final OrderSku orderSku = (OrderSku) element;
			if (orderSku.getDigitalAsset() != null) {
				text = FulfillmentMessages.get().ShipmenSection_DigitalGood;
			}
			return text;
		}
	}

	/**
	 * Content provider for displaying the shipment's <code>OrderSku</code>s.
	 */
	private class OrderSkuContentProvider implements IStructuredContentProvider {

		/**
		 * @param inputElement the element containing the data to be displayed
		 * @return an array of the <code>OrderSku</code>s to be displayed
		 */
		@Override
		public Object[] getElements(final Object inputElement) {
			final List<OrderSku> orderSkus = (List<OrderSku>) inputElement;
			return orderSkus.toArray();
		}

		/**
		 *
		 */
		@Override
		public void dispose() {
			// not used

		}

		/**
		 * @param viewer the viewer
		 * @param oldInput the old input
		 * @param newInput the new input
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// not used

		}
	}

	@Override
	protected void populateControls() {
		shipmentOrderSkuTable.setContentProvider(new OrderSkuContentProvider());
		List<OrderSku> orderSkuList = new LinkedList<>(shipment.getShipmentOrderSkus());
		orderSkuList.sort(OrderSkuComparatorFactory.getOrderSkuCodeComparator());
		shipmentOrderSkuTable.setInput(orderSkuList);
	}

	/**
	 * Not used.
	 *
	 * @param event the event object
	 */
	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		// not used
	}

	/**
	 * Invoked on selection event.
	 *
	 * @param event the event object
	 */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		
		final OrderSku orderSku = (OrderSku) ((IStructuredSelection) shipmentOrderSkuTable.getSwtTableViewer().getSelection()).getFirstElement();
		
		try {
			
			IWorkbenchPage workbenchPage = editor.getSite().getPage();
			
			ProductSku productSku = getProductSkuLookup().findByGuid(orderSku.getSkuGuid());
			workbenchPage.openEditor(new GuidEditorInput(productSku.getProduct().getGuid(), Product.class), ProductEditor.PART_ID);
		
		} catch (final PartInitException exc) {
			
			LOG.error("Error opening the Product SKU Editor", exc); //$NON-NLS-1$
		}
	
		
	}
	
	@Override
	public void dispose() {
		getSection().dispose();
		super.dispose();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		//FIXME: [BB-778]


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
	 * Get the pricing snapshot service.
	 *
	 * @return the pricing snapshot service
	 */
	protected PricingSnapshotService getPricingSnapshotService() {
		if (pricingSnapshotService == null) {
			pricingSnapshotService = ServiceLocator.getService(ContextIdNames.PRICING_SNAPSHOT_SERVICE);
		}
		return pricingSnapshotService;
	}
}
