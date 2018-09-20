/**
 * Copyright (c) Elastic Path Software Inc., 2007-2014
 */
package com.elasticpath.cmclient.fulfillment.editors.order;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.LoginManager;
import com.elasticpath.cmclient.core.ServiceLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.order.dialog.OrderAddEditAddressDialog;
import com.elasticpath.cmclient.fulfillment.util.AddressUtil;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * Represents the physical shipment shipping information sub section.
 */
public class OrderDetailsPhysicalShipmentSubSectionShippingInfo implements IPropertyListener, SelectionListener {

	private CCombo shippingServiceLevelComboBox;

	private CCombo shippingAddressComboBox;

	private final boolean editMode;

	private final PhysicalOrderShipment shipment;

	private final Order order;

	private final List<Address> addressList;

	private List<ShippingServiceLevel> shippingServiceLevels;

	private Button editShipAddressButton;

	private final AbstractCmClientFormEditor editor;

	private final Composite mainPane; //NOPMD

	/**
	 * Constructor.
	 *
	 * @param shipment the physical shipment
	 * @param editor the editor
	 * @param editMode if the shipping info should be editable
	 * @param mainPane the parent of the parent of this section
	 */
	public OrderDetailsPhysicalShipmentSubSectionShippingInfo(final PhysicalOrderShipment shipment, final AbstractCmClientFormEditor editor,
			final boolean editMode, final Composite mainPane) {
		this.shipment = shipment;
		this.editMode = editMode;
		this.order = (Order) editor.getModel();
		this.editor = editor;
		this.mainPane = mainPane;
		addressList = new ArrayList<>();
		// create copies of shipment address and customer addresses so we are not working with the actual instances
		OrderAddress shipmentAddress = ServiceLocator.getService(ContextIdNames.ORDER_ADDRESS);
		shipmentAddress.init(shipment.getShipmentAddress());
		addressList.add(shipmentAddress);
		for (Address address : order.getCustomer().getAddresses()) {
			OrderAddress orderAddress = ServiceLocator.getService(ContextIdNames.ORDER_ADDRESS);
			orderAddress.init(address);
			addressList.add(orderAddress);
		}
	}

	private EpState getStateFromPermissions() {
		EpState state;
		if (editMode) {
			state = EpState.EDITABLE;
		} else {
			state = EpState.READ_ONLY;
		}
		return state;
	}

	private EpState getShippingAddressButtonState() {
		EpState state;
		if (((OrderEditor) editor).isAuthorizedAndAvailableForEdit()
				&& (shipment.getShipmentStatus() == OrderShipmentStatus.AWAITING_INVENTORY
						|| shipment.getShipmentStatus() == OrderShipmentStatus.INVENTORY_ASSIGNED)
				&& !shipment.getOrder().isExchangeOrder()) {
			state = EpState.EDITABLE;
		} else {
			state = EpState.READ_ONLY;
		}

		return state;
	}

	/**
	 * Creates the controls.
	 *
	 * @param client the composite
	 * @param toolkit the form tool kit
	 */
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		final Section section = toolkit.createSection(client, ExpandableComposite.TITLE_BAR);
		section.setText(getSectionTitle());
		final IEpLayoutComposite infoPane = CompositeFactory.createTableWrapLayoutComposite(section, 2, false);
		infoPane.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		final IEpLayoutData buttonData = infoPane.createLayoutData();
		final IEpLayoutComposite shippingInfoPane = CompositeFactory.createTableWrapLayoutComposite(section, 2, false);
		shippingInfoPane.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		createShippingInfoControls(shippingInfoPane, buttonData);

		section.setClient(shippingInfoPane.getSwtComposite());
	}

	private void createShippingInfoControls(final IEpLayoutComposite shippingInfoPane, final IEpLayoutData buttonData) {
		final IEpLayoutData shippingInfoPaneData = shippingInfoPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER);

		final IEpLayoutData shippingInfoData = shippingInfoPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER);
		shippingInfoPane.addLabelBold(FulfillmentMessages.get().ShipmentSection_ShippingAddress, shippingInfoPaneData);
		shippingInfoPane.addEmptyComponent(shippingInfoPaneData);
		this.shippingAddressComboBox = shippingInfoPane.addComboBox(getStateFromPermissions(), shippingInfoData);
		this.shippingAddressComboBox.addSelectionListener(this);
		final IEpLayoutData addressButtonPaneData = shippingInfoPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL);
		final IEpLayoutComposite addressButtonsPane = shippingInfoPane.addTableWrapLayoutComposite(1, true, addressButtonPaneData);
		this.editShipAddressButton = addressButtonsPane.addPushButton(FulfillmentMessages.get().ShipmentSection_EditAddressButton, CoreImageRegistry
				.getImage(CoreImageRegistry.IMAGE_ADDRESS_EDIT), getShippingAddressButtonState(), buttonData);

		this.editShipAddressButton.addSelectionListener(this);
		shippingInfoPane.addLabelBold(FulfillmentMessages.get().ShipmentSection_ShippingMethod, shippingInfoPaneData);
		shippingInfoPane.addEmptyComponent(shippingInfoPaneData);
		this.shippingServiceLevelComboBox = shippingInfoPane.addComboBox(getStateFromPermissions(), shippingInfoData);
		this.shippingServiceLevelComboBox.addSelectionListener(this);
	}

	/**
	 * Populate the controls with their initial values.
	 */
	protected void populateControls() {
		this.shippingAddressComboBox.setItems(getShippingAddressListAsArray());
		this.shippingAddressComboBox.select(0);

		this.shippingServiceLevelComboBox.setItems(getShippingMethodListAsArray(shipment.getShipmentAddress()));
		this.shippingServiceLevelComboBox.add(FulfillmentMessages.get().ShipmentSection_ShippingMethodComboBoxSelectAMethod, 0);
		this.shippingServiceLevelComboBox.select(getSelectedShipMethodIndex());

	}

	/**
	 * Binds the shipping method combo box to validator.
	 *
	 * @param context the binding context
	 */
	protected void bindControl(final DataBindingContext context) {
		// bind the combo box
		final EpControlBindingProvider bindingProvider = EpControlBindingProvider.getInstance();

		bindingProvider.bind(context, shippingServiceLevelComboBox, EpValidatorFactory.REQUIRED_COMBO_FIRST_ELEMENT_NOT_VALID, null,
				new ObservableUpdateValueStrategy() {
					@Override
					protected IStatus doSet(final IObservableValue observableValue, final Object value) {
						final int selectedIndex = (Integer) value;
						if (selectedIndex == 0) {
							editor.controlModified();
							return Status.CANCEL_STATUS;
						}
						final ShippingServiceLevel selectedLevel = shippingServiceLevels.get(selectedIndex - 1);
						final String oldLevel = shipment.getShippingServiceLevelGuid();
						if (!selectedLevel.getGuid().equals(oldLevel)) {  // NOPMD  '!='
							shipment.setShippingServiceLevelGuid(selectedLevel.getGuid());
							shipment.setCarrier(selectedLevel.getCarrier());
							shipment.setServiceLevel(selectedLevel.getDisplayName(CorePlugin.getDefault().getDefaultLocale(), true));
							fireShippingMethodChangeEvent();
						}
						return Status.OK_STATUS;
					}

				}, true);
	}

	@Override
	public void propertyChanged(final Object source, final int propId) {
		// nothing

	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {
		// nothing

	}

	// ---- DOCwidgetSelected
	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (event.getSource() == editShipAddressButton) {

			final OrderAddEditAddressDialog editAddressDialog = new OrderAddEditAddressDialog(editor.getSite().getShell(), order, shipment, false,
					false);
			if (editAddressDialog.open() == Window.OK) {

				final int selectedIndex = shippingAddressComboBox.getSelectionIndex();
				final Address modifiedAddress = addressList.get(selectedIndex);
				modifiedAddress.copyFrom(editAddressDialog.getShippingAddress());
				final String newAddressLabel = AddressUtil.formatAddress(modifiedAddress, true);
				shippingAddressComboBox.setItem(selectedIndex, newAddressLabel);
				shippingAddressComboBox.setText(newAddressLabel);
				final OrderAddress curShipAddress = updateAndSetShipmentAddress(modifiedAddress);

				refreshShippingMethodComboBox(curShipAddress);

				fireShippingAddressChangeEvent();
			}
		}
		if (event.getSource() == shippingAddressComboBox) {

			final Address modifiedAddress = addressList.get(shippingAddressComboBox.getSelectionIndex());

			final OrderAddress curShipAddress = updateAndSetShipmentAddress(modifiedAddress);

			refreshShippingMethodComboBox(curShipAddress);

			fireShippingAddressChangeEvent();

		}
	}

	/**
	 * Updates and sets the shipment address.
	 *
	 * @param modifiedAddress the modified address
	 * @return OrderAddress the shipment address that is updated
	 */
	private OrderAddress updateAndSetShipmentAddress(final Address modifiedAddress) {
		final OrderAddress curShipAddress = shipment.getShipmentAddress();
		curShipAddress.copyFrom(modifiedAddress);
		shipment.setShipmentAddress(curShipAddress);
		return curShipAddress;
	}

	/**
	 * Fires the shipping address change events.
	 */
	private void fireShippingAddressChangeEvent() {
		order.setModifiedBy(getEventOriginator());
		((OrderEditor) editor).addOrderShipmentToUpdate(shipment);
		// Log the shipping address change event.
		OrderEventCmHelper.getOrderEventHelper().logOrderShipmentAddressChanged(order, shipment);
		((OrderEditor) editor).fireAddNoteChanges();

		fireChangeEvent();
	}

	private EventOriginator getEventOriginator() {
		EventOriginatorHelper helper = ServiceLocator.getService(ContextIdNames.EVENT_ORIGINATOR_HELPER);

		return helper.getCmUserOriginator(LoginManager.getCmUser());
	}

	/**
	 * Fires the shipping method change events.
	 */
	private void fireShippingMethodChangeEvent() {
		order.setModifiedBy(getEventOriginator());
		((OrderEditor) editor).addOrderShipmentToUpdate(shipment);

		// Log the shipping method change event.
		OrderEventCmHelper.getOrderEventHelper().logOrderShipmentMethodChanged(order, shipment);
		((OrderEditor) editor).fireAddNoteChanges();

		fireChangeEvent();
	}

	/**
	 * Fires the editor control modified and re-calculation events.
	 */
	private void fireChangeEvent() {
		editor.controlModified();
		// fire changes to editor to notify summary sub section about address/service level change event
		((OrderEditor) editor).fireShipmentAddressMethodChanges();
	}
	// ---- DOCwidgetSelected


	/**
	 * Refreshes the shipping method combo box after a new address from the shipping address combo box is selected.
	 *
	 * @param selectedShipAddress the selected shipping address
	 */
	private void refreshShippingMethodComboBox(final OrderAddress selectedShipAddress) {

		final String previousLevel = shipment.getShippingServiceLevelGuid();
		// repopulate the method combo, and get a list of shipping method according to region specified in the address
		shippingServiceLevelComboBox.setItems(getShippingMethodListAsArray(selectedShipAddress));
		shippingServiceLevelComboBox.add(FulfillmentMessages.get().ShipmentSection_ShippingMethodComboBoxSelectAMethod, 0);
		// check if the service method stored in domain object is in the new list
		final int index = getSelectedShipMethodIndex(previousLevel);
		shippingServiceLevelComboBox.select(index);
	}

	/**
	 * It gets a list of shipping address and returns a list as an array.
	 *
	 * @return String[] of shipping address for the customer who placed the selected order
	 */
	private String[] getShippingAddressListAsArray() {
		final String[] addressArray = new String[addressList.size()];
		for (int i = 0; i < addressArray.length; i++) {
			final Address address = addressList.get(i);
			addressArray[i] = AddressUtil.formatAddress(address, true);
		}

		return addressArray;
	}

	/**
	 * It gets a list of shipping service level for a given address and returns a list as an array.
	 *
	 * @param address the ship address
	 * @return String[] of shipping service level for that address
	 */
	private String[] getShippingMethodListAsArray(final OrderAddress address) {
		final ShippingServiceLevelService serviceLevel = ServiceLocator.getService(
				ContextIdNames.SHIPPING_SERVICE_LEVEL_SERVICE);

		shippingServiceLevels = serviceLevel.retrieveShippingServiceLevel(order.getStoreCode(), address);

		final String[] serviceLevels = new String[shippingServiceLevels.size()];
		for (int i = 0; i < shippingServiceLevels.size(); i++) {
			serviceLevels[i] = retrieveServiceLevelName(shippingServiceLevels.get(i));
		}
		return serviceLevels;
	}

	/**
	 * Gets the name of the given ShippingServiceLevel for the current user's locale, and
	 * appends a message to the name if the SSL is not active.
	 * @param shippingServiceLevel the shipping service level
	 * @return the display name of the shipping service level
	 */
	private String retrieveServiceLevelName(final ShippingServiceLevel shippingServiceLevel) {
		String name = shippingServiceLevel.getDisplayName(CorePlugin.getDefault().getDefaultLocale(), true);
		if (name == null) {
			name = shippingServiceLevel.getCarrier();
		}
		StringBuilder serviceLevelName = new StringBuilder(name);
		if (!shippingServiceLevel.isEnabled()) {
			serviceLevelName.append(" [").append(FulfillmentMessages.get().ShipmentSection_NotActiveShippingLevel).append(']');
		}
		return serviceLevelName.toString();
	}

	/**
	 * Gets the shipping method from the shipment and return the index of the shipping method in the list of shipping method.
	 *
	 * @return index of the shipping method from the list of shipping method
	 */
	private int getSelectedShipMethodIndex() {
		final String serviceLevelGuid = shipment.getShippingServiceLevelGuid();
		if (serviceLevelGuid == null) {
			return 0;
		}

		return getSelectedShipMethodIndex(serviceLevelGuid);
	}

	private int getSelectedShipMethodIndex(final String serviceLevelGuid) {
		int index = 0;
		for (ShippingServiceLevel shippingServiceLevel : shippingServiceLevels) {
			if (serviceLevelGuid.equals(shippingServiceLevel.getGuid())) {
				// because of the first item is not in the service level list, needs to add it back for indexing
				index++;
				return index;
			}

			index++;
		}
		return 0;
	}

	/**
	 * Gets the section title.
	 *
	 * @return string
	 */
	protected String getSectionTitle() {
		return FulfillmentMessages.get().ShipmentSection_SubSectionInfo;
	}

}
