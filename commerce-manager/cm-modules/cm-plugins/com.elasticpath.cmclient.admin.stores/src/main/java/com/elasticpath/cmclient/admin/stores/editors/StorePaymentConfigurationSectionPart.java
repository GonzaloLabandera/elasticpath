/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.editors.CancelSaveException;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.helpers.store.StorePaymentConfigurationModel;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Store payment configuration section part.
 */
public class StorePaymentConfigurationSectionPart extends AbstractCmClientEditorPageSectionPart {

	private static final int SELECTED_COLUMN = 0;

	private static final int CONFIGURATION_NAME_COLUMN = 1;

	private static final int PROVIDER_NAME_COLUMN = 2;

	private static final int METHOD_NAME_COLUMN = 3;

	private static final int SELECTED_COLUMN_WIDTH = 75;

	private static final int CONFIGURATION_NAME_COLUMN_WIDTH = 200;

	private static final int PROVIDER_NAME_COLUMN_WIDTH = 200;

	private static final int METHOD_NAME_COLUMN_WIDTH = 200;

	private static final int PAYENT_CONFIG_TABLE_MAX_HEIGHT = 370;

	private static final String TABLE_NAME = "Store Payment Provider Configurations";

	private IEpTableViewer storePaymentConfigurationsTableViewer;

	private List<StorePaymentConfigurationModel> input;

	private final AbstractCmClientFormEditor editor;

	private final StoreEditorModel storeEditorModel;

	private final PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	/**
	 * Constructor.
	 *
	 * @param formPage         the form page
	 * @param editor           the editor
	 * @param storeEditorModel the Store editor model
	 */
	public StorePaymentConfigurationSectionPart(
			final FormPage formPage, final AbstractCmClientFormEditor editor, final StoreEditorModel storeEditorModel) {
		super(formPage, editor, ExpandableComposite.NO_TITLE);
		this.editor = editor;
		this.storeEditorModel = storeEditorModel;
		this.paymentProviderConfigManagementService =
				BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_CONFIG_MANAGEMENT_SERVICE, PaymentProviderConfigManagementService.class);
	}


	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		IEpLayoutComposite controlPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		final TableWrapData tableWrapdata = new TableWrapData(TableWrapData.FILL, TableWrapData.FILL);
		tableWrapdata.grabHorizontal = true;
		tableWrapdata.grabVertical = true;
		controlPane.setLayoutData(tableWrapdata);

		final IEpLayoutData tableLayoutData = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, true);
		this.storePaymentConfigurationsTableViewer = controlPane.addTableViewer(false, EpState.EDITABLE, tableLayoutData, TABLE_NAME);
		TableWrapData tableWrapData = (TableWrapData) storePaymentConfigurationsTableViewer.getSwtTable().getLayoutData();
		tableWrapData.maxHeight = PAYENT_CONFIG_TABLE_MAX_HEIGHT;
		createSelectedCheckboxColumn();
		storePaymentConfigurationsTableViewer.addTableColumn(
				AdminStoresMessages.get().Store_PaymentConfigurations_ConfigurationName, CONFIGURATION_NAME_COLUMN_WIDTH);
		storePaymentConfigurationsTableViewer.addTableColumn(
				AdminStoresMessages.get().Store_PaymentConfigurations_Provider, PROVIDER_NAME_COLUMN_WIDTH);
		storePaymentConfigurationsTableViewer.addTableColumn(AdminStoresMessages.get().Store_PaymentConfigurations_Method, METHOD_NAME_COLUMN_WIDTH);

		storePaymentConfigurationsTableViewer.setContentProvider(new ArrayContentProvider());
		storePaymentConfigurationsTableViewer.setLabelProvider(new PaymentConfigurationLabelProvider());
	}

	private void createSelectedCheckboxColumn() {
		IEpTableColumn searchableColumn = storePaymentConfigurationsTableViewer.addTableColumn(
				AdminStoresMessages.get().Store_PaymentConfigurations_Select, SELECTED_COLUMN_WIDTH, IEpTableColumn.TYPE_CHECKBOX);


		searchableColumn.setEditingSupport(new SelectedCheckboxEditingSupport(storePaymentConfigurationsTableViewer, editor) {
			@Override
			protected void setValue(final Object element, final Object value) {
				super.setValue(element, value);
				markDirty();
			}
		});
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// not used
	}

	@Override
	protected void populateControls() {
		input = getInput();
		storePaymentConfigurationsTableViewer.setInput(input.toArray(new StorePaymentConfigurationModel[input.size()]));
	}

	/**
	 * Gets the list of available payment configurations sorted by configuration name. This is used as an input to payment configurations
	 * tableViewer.
	 *
	 * @return the list of {@link StorePaymentConfigurationModel}.
	 */
	protected List<StorePaymentConfigurationModel> getInput() {
		final List<StorePaymentConfigurationModel> paymentConfigurationModels = ((StoreEditorModel) getModel()).getStorePaymentConfigurations();
		final List<PaymentProviderConfigDTO> paymentProviders = findPaymentProviderConfigDTOThatMeetStorePaymentConfig(paymentConfigurationModels);
		final List<PaymentProviderConfigDTO> notMatchedPaymentProviderConfigDTO =
				findNotMatchedPaymentProviderConfigDTO(paymentConfigurationModels, paymentProviders);

		if (!notMatchedPaymentProviderConfigDTO.isEmpty()) {
			MessageDialog.openInformation(editor.getSite().getShell(),
					AdminStoresMessages.get().Payment_Configuration_Is_Disabled_Error_Title,
					NLS.bind(AdminStoresMessages.get().Payment_Configuration_Is_Disabled_Error_Message,
							getPaymentConfigurationNames(notMatchedPaymentProviderConfigDTO)));

			cancelSelectedStorePaymentConfiguration(paymentConfigurationModels, notMatchedPaymentProviderConfigDTO);
		}

		paymentConfigurationModels.sort((object1, object2) -> object1.getConfigurationName().compareToIgnoreCase(object2.getConfigurationName()));
		return paymentConfigurationModels;
	}

	/**
	 * Find PaymentProviderConfigDTO that meet the StorePaymentConfiguration.
	 *
	 * @param storePaymentConfigurationModel         model that contains info about {@link StorePaymentConfigurationModel}.
	 * @return list {@link PaymentProviderConfigDTO} that's not meet StorePaymentConfig.
	 */
	private List<PaymentProviderConfigDTO> findPaymentProviderConfigDTOThatMeetStorePaymentConfig(
			final List<StorePaymentConfigurationModel> storePaymentConfigurationModel) {

		final List<String> paymentProviderConfigurationGuids = storePaymentConfigurationModel.stream()
				.filter(StorePaymentConfigurationModel::isSelected)
				.map(StorePaymentConfigurationModel::getConfigurationGuid)
				.collect(Collectors.toList());

		return paymentProviderConfigManagementService.findByGuids(paymentProviderConfigurationGuids);
	}

	/**
	 * Find not matched PaymentProviderConfigDTO.
	 *
	 * @param storePaymentConfigurationModel model that contains info about {@link StorePaymentConfigurationModel}.
	 * @param paymentProviders               {@link PaymentProviderConfigDTO}.
	 * @return list of non matched {@link PaymentProviderConfigDTO}.
	 */
	private List<PaymentProviderConfigDTO> findNotMatchedPaymentProviderConfigDTO(
			final List<StorePaymentConfigurationModel> storePaymentConfigurationModel,
			final List<PaymentProviderConfigDTO> paymentProviders) {

		return storePaymentConfigurationModel.stream()
				.flatMap(storeConfig -> paymentProviders.stream()
						.filter(paymentProvider -> paymentProvider.getGuid().equals(storeConfig.getConfigurationGuid()))
						.filter(paymentProvider -> !paymentProvider.getStatus().equals(getStatusFromStorePaymentConfigurationModel(storeConfig))))
				.collect(Collectors.toList());
	}

	/**
	 * Cancel selected StorePaymentConfiguration.
	 *
	 * @param storePaymentConfigurationModel     model that contains info about {@link StorePaymentConfigurationModel}.
	 * @param notMatchedPaymentProviderConfigDTO list of non matched {@link PaymentProviderConfigDTO} with StorePaymentConfiguration.
	 */
	private void cancelSelectedStorePaymentConfiguration(final List<StorePaymentConfigurationModel> storePaymentConfigurationModel,
														 final List<PaymentProviderConfigDTO> notMatchedPaymentProviderConfigDTO) {
		notMatchedPaymentProviderConfigDTO.forEach(nonMatchConfig -> storePaymentConfigurationModel.stream()
				.filter(res -> res.getConfigurationGuid().equals(nonMatchConfig.getGuid()))
				.forEach(res -> res.setSelected(false)));
	}

	/**
	 * Get Payment configuration names from list of {@link PaymentProviderConfigDTO}.
	 *
	 * @param paymentProviderConfigDTO {@link PaymentProviderConfigDTO}.
	 * @return string that contains payment provider configuration names separated by commas.
	 */
	private String getPaymentConfigurationNames(final List<PaymentProviderConfigDTO> paymentProviderConfigDTO) {
		return paymentProviderConfigDTO.stream()
				.map(PaymentProviderConfigDTO::getConfigurationName)
				.collect(Collectors.joining(", "));
	}

	/**
	 * Convert boolean status of the StorePaymentConfigurationModel to {@link PaymentProviderConfigurationStatus}.
	 *
	 * @param storePaymentConfigurationModel {@link StorePaymentConfigurationModel}.
	 * @return {@link PaymentProviderConfigurationStatus}.
	 */
	private PaymentProviderConfigurationStatus getStatusFromStorePaymentConfigurationModel(
			final StorePaymentConfigurationModel storePaymentConfigurationModel) {
		return storePaymentConfigurationModel.isSelected() ? PaymentProviderConfigurationStatus.ACTIVE : PaymentProviderConfigurationStatus.DISABLED;
	}

	/**
	 * Provides labels for the available payment configurations table.
	 */
	protected class PaymentConfigurationLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			if (columnIndex == SELECTED_COLUMN) {
				if (((StorePaymentConfigurationModel) element).isSelected()) {
					return CoreImageRegistry.getImage(CoreImageRegistry.CHECKBOX_CHECKED);
				} else {
					return CoreImageRegistry.getImage(CoreImageRegistry.CHECKBOX_UNCHECKED);
				}
			}
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final StorePaymentConfigurationModel storePaymentConfiguration = (StorePaymentConfigurationModel) element;
			switch (columnIndex) {
				case CONFIGURATION_NAME_COLUMN:
					return storePaymentConfiguration.getConfigurationName();
				case PROVIDER_NAME_COLUMN:
					return storePaymentConfiguration.getProvider();
				case METHOD_NAME_COLUMN:
					return storePaymentConfiguration.getMethod();
				default:
					return "";
			}
		}
	}

	@Override
	public void commit(final boolean onSave) {
		if (!storeEditorModel.isStorePaymentConfigurationSavable()) {
			MessageDialog.openError(editor.getSite().getShell(), AdminStoresMessages.get().Store_Payment_Configuration_Save_Error_Title,
					AdminStoresMessages.get().Store_Payment_Configuration_Save_Error_Message);
			throw new CancelSaveException("Failed to commit");
		}
		super.commit(onSave);
	}

	/**
	 * Provides {@link EditingSupport} for payment provider selection checkbox.
	 */
	private class SelectedCheckboxEditingSupport extends EditingSupport {

		private final IEpTableViewer tableViewer;
		private final AbstractCmClientFormEditor editor;

		/**
		 * Constructor.
		 *
		 * @param tableViewer table viewer
		 * @param editor      editor
		 */
		SelectedCheckboxEditingSupport(final IEpTableViewer tableViewer, final AbstractCmClientFormEditor editor) {
			super(tableViewer.getSwtTableViewer());
			this.tableViewer = tableViewer;
			this.editor = editor;
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			return new CheckboxCellEditor((Composite) tableViewer.getSwtTableViewer().getControl());
		}

		@Override
		protected boolean canEdit(final Object element) {
			return true;
		}

		@Override
		protected Object getValue(final Object element) {
			return ((StorePaymentConfigurationModel) element).isSelected();
		}

		@Override
		protected void setValue(final Object element, final Object value) {
			boolean selected = (boolean) value;
			StorePaymentConfigurationModel storePaymentConfigurationModel = (StorePaymentConfigurationModel) element;
			storePaymentConfigurationModel.setSelected(selected);

			tableViewer.getSwtTableViewer().refresh();
			editor.controlModified();
		}
	}
}