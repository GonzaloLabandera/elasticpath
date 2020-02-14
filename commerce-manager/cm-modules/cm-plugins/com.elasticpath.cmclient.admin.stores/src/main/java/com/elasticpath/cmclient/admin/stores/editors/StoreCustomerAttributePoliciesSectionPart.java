/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.helpers.store.StoreCustomerAttributeModel;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.AttributePolicyService;

/**
 * Store customer attribute policies section part.
 */
public class StoreCustomerAttributePoliciesSectionPart extends AbstractCmClientEditorPageSectionPart implements ISelectionChangedListener {

	private static final int ATTRIBUTE_COLUMN = 0;

	private static final int POLICY_COLUMN = 1;

	private static final int PERMISSIONS_COLUMN = 2;

	private static final int ATTRIBUTE_COLUMN_WIDTH = 200;

	private static final int POLICY_COLUMN_WIDTH = 150;

	private static final int PERMISSIONS_COLUMN_WIDTH = 250;

	private static final String POLICY_PERMISSION_KEY_PREFIX = "PolicyPermission_";

	private final String tableName;

	private IEpTableViewer attributePoliciesTableViewer;

	private IEpLayoutComposite buttonsComposite;

	private Button addButton;

	private Button deleteButton;

	private Button editButton;

	private EpState rolePermission = EpState.EDITABLE;

	private final Map<String, Attribute> attributesMap;

	private final Map<PolicyKey, List<AttributePolicy>> policiesMap;

	private IEpLayoutComposite controlPane;

	/**
	 * Constructor.
	 *
	 * @param formPage the form page
	 * @param editor   the editor
	 * @param editable editable flag
	 */
	public StoreCustomerAttributePoliciesSectionPart(final FormPage formPage, final AbstractCmClientFormEditor editor, final boolean editable) {
		super(formPage, editor, ExpandableComposite.NO_TITLE);
		setEditable(editable);
		this.tableName = "Store Customer Attribute Policies";

		final AttributeService attributeService = BeanLocator.getSingletonBean(ContextIdNames.ATTRIBUTE_SERVICE, AttributeService.class);
		attributesMap = attributeService.getCustomerProfileAttributesMap();

		final AttributePolicyService attributePolicyService =
				BeanLocator.getSingletonBean(ContextIdNames.ATTRIBUTE_POLICY_SERVICE, AttributePolicyService.class);
		policiesMap = attributePolicyService.findAll().stream()
				.collect(Collectors.groupingBy(AttributePolicy::getPolicyKey));
	}

	/**
	 * Sets true if attribute policies are editable and false otherwise.
	 *
	 * @param isEditable true if attribute policies are editable and false otherwise
	 */
	protected void setEditable(final boolean isEditable) {
		this.rolePermission = EpState.READ_ONLY;
		if (isEditable) {
			this.rolePermission = EpState.EDITABLE;
		}
	}

	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		controlPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		final TableWrapData tableWrapdata = new TableWrapData(TableWrapData.FILL, TableWrapData.FILL);
		tableWrapdata.grabHorizontal = true;
		tableWrapdata.grabVertical = true;
		controlPane.setLayoutData(tableWrapdata);

		final IEpLayoutData tableLayoutData = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, true);
		this.attributePoliciesTableViewer = controlPane.addTableViewer(false, rolePermission, tableLayoutData, tableName);

		attributePoliciesTableViewer.addTableColumn(AdminStoresMessages.get().Store_AttributePolicies_Attribute, ATTRIBUTE_COLUMN_WIDTH);
		attributePoliciesTableViewer.addTableColumn(AdminStoresMessages.get().Store_AttributePolicies_Policy, POLICY_COLUMN_WIDTH);
		attributePoliciesTableViewer.addTableColumn(AdminStoresMessages.get().Store_AttributePolicies_Permissions, PERMISSIONS_COLUMN_WIDTH);

		buttonsComposite = controlPane.addGridLayoutComposite(1, true, controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true,
				true));

		addButton = buttonsComposite.addPushButton(AdminStoresMessages.get().Store_AttributePolicies_AddValue,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_ADD), rolePermission, controlPane.createLayoutData(IEpLayoutData.FILL,
						IEpLayoutData.FILL));
		addButton.addSelectionListener(getAddListener());

		editButton = buttonsComposite.addPushButton(AdminStoresMessages.get().Store_AttributePolicies_EditValue,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT), rolePermission, controlPane.createLayoutData(IEpLayoutData.FILL,
						IEpLayoutData.FILL));
		editButton.addSelectionListener(getEditListener());

		deleteButton = buttonsComposite.addPushButton(AdminStoresMessages.get().Store_AttributePolicies_DeleteValue,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_REMOVE), rolePermission, controlPane.createLayoutData(IEpLayoutData.FILL,
						IEpLayoutData.FILL));
		deleteButton.addSelectionListener(getDeleteListener());

		setEditAttributeButtonsEnabled(false);
		addButton.setEnabled(rolePermission == EpState.EDITABLE);

		attributePoliciesTableViewer.setContentProvider(new ArrayContentProvider());
		attributePoliciesTableViewer.getSwtTableViewer().addSelectionChangedListener(this);
		attributePoliciesTableViewer.setLabelProvider(new AttributeLabelProvider());

	}

	private SelectionAdapter getAddListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				final StoreCustomerAttributeModel attributeModel = new StoreCustomerAttributeModel(UUID.randomUUID().toString(),
						((StoreEditorModel) getModel()).getStoreCode());
				final StoreCustomerAttributePolicyDialog attributePolicyDialog = new StoreCustomerAttributePolicyDialog(getEditor().getEditorSite()
						.getShell(), AdminStoresMessages.get().Store_AttributePolicies_AddValue, attributeModel, getInput(),
						getPolicyPermissionFormatter());
				if (attributePolicyDialog.open() == Window.OK) {
					getInput().add(attributeModel);
					refreshTableViewer();
				}
			}
		};
	}

	private SelectionAdapter getEditListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				final StoreCustomerAttributeModel attributeModel = getSelectedAttribute();
				final StoreCustomerAttributeModel dialogModel = new StoreCustomerAttributeModel(attributeModel.getGuid(),
						attributeModel.getAttributeKey(), attributeModel.getPolicyKey(), attributeModel.getStoreCode());
				final StoreCustomerAttributePolicyDialog attributePolicyDialog = new StoreCustomerAttributePolicyDialog(getEditor().getEditorSite()
						.getShell(), AdminStoresMessages.get().Store_AttributePolicies_EditValue, dialogModel, getInput(),
						getPolicyPermissionFormatter());
				if (attributePolicyDialog.open() == Window.OK) {
					attributeModel.setAttributeKey(dialogModel.getAttributeKey());
					attributeModel.setPolicyKey(dialogModel.getPolicyKey());
					refreshTableViewer();
				}
			}
		};
	}

	private SelectionAdapter getDeleteListener() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				final StoreCustomerAttributeModel attributeModel = getSelectedAttribute();
				final String attributeName = getAttributeName(attributeModel.getAttributeKey());

				boolean confirmed = MessageDialog.openConfirm(getEditor().getEditorSite().getShell(), AdminStoresMessages.get()
						.ConfirmDeleteAttributePolicyMsgBoxTitle, NLS.bind(AdminStoresMessages.get().ConfirmDeleteAttributePolicyMsgBoxText,
						attributeName));

				if (confirmed) {
					getInput().remove(attributeModel);
					refreshTableViewer();
				}
			}
		};
	}

	private Function<PolicyKey, String> getPolicyPermissionFormatter() {
		return this::getPermissionsText;
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) attributePoliciesTableViewer.getSwtTableViewer().getSelection();
		boolean enabled = rolePermission == EpState.EDITABLE && !selection.isEmpty();
		setEditAttributeButtonsEnabled(enabled);
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// not used
	}

	@Override
	protected void populateControls() {
		final List<StoreCustomerAttributeModel> input = getInput();
		attributePoliciesTableViewer.setInput(input.toArray(new StoreCustomerAttributeModel[input.size()]));
	}

	private void refreshTableViewer() {
		final List<StoreCustomerAttributeModel> input = getInput();
		attributePoliciesTableViewer.setInput(input.toArray(new StoreCustomerAttributeModel[input.size()]));
		attributePoliciesTableViewer.getSwtTableViewer().refresh();
		getEditor().controlModified();
	}

	/**
	 * Gets the list of attributes to fill the attributes table.
	 *
	 * @return the list of attributes
	 */
	protected List<StoreCustomerAttributeModel> getInput() {
		List<StoreCustomerAttributeModel> result = ((StoreEditorModel) getModel()).getStoreCustomerAttributes();
		result.sort((object1, object2) -> object1.getAttributeKey().compareToIgnoreCase(object2.getAttributeKey()));
		return result;
	}

	private StoreCustomerAttributeModel getSelectedAttribute() {
		final IStructuredSelection selection = (IStructuredSelection) attributePoliciesTableViewer.getSwtTableViewer().getSelection();
		return (StoreCustomerAttributeModel) selection.getFirstElement();
	}

	/**
	 * Provides labels for the Attributes TableViewer.
	 */
	protected class AttributeLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final StoreCustomerAttributeModel attribute = (StoreCustomerAttributeModel) element;
			switch (columnIndex) {
				case ATTRIBUTE_COLUMN:
					return getAttributeName(attribute.getAttributeKey());
				case POLICY_COLUMN:
					return attribute.getPolicyKey().getName();
				case PERMISSIONS_COLUMN:
					return getPermissionsText(attribute.getPolicyKey());
				default:
					return "";
			}
		}
	}

	private String getPermissionsText(final PolicyKey policyKey) {
		return StringUtils.join(policiesMap.get(policyKey).stream()
				.map(AttributePolicy::getPolicyPermission)
				.map(permission -> AdminStoresMessages.get().getMessage(POLICY_PERMISSION_KEY_PREFIX + permission))
				.collect(Collectors.toList()), ", ");
	}

	private String getAttributeName(final String attributeKey) {
		return attributesMap.get(attributeKey).getDisplayName(CorePlugin.getDefault().getDefaultLocale());
	}

	private void setEditAttributeButtonsEnabled(final boolean enabled) {
		editButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
	}
}