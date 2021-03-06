/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.cmclient.catalog.dialogs.cartitemmodifier;

import static com.elasticpath.cmclient.catalog.dialogs.cartitemmodifier.AddEditCartItemModifierGroupDialog.ignoreTableSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.catalog.CatalogPlugin;
import com.elasticpath.cmclient.catalog.dialogs.cartitemmodifier.tablelabelcontentprovider.CartItemModifierFieldLabelProvider;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.ObjectGuidReceiver;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareDialog;
import com.elasticpath.cmclient.policy.ui.IPolicyTargetLayoutComposite;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.service.modifier.ModifierService;

/**
 * The dialog UI class for Adding/Editing ModifierField.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.TooManyFields"})
public class AddEditCartItemModifierFieldDialog extends AbstractPolicyAwareDialog
		implements ObjectGuidReceiver, SelectionListener, ISelectionChangedListener, IDoubleClickListener {

	private static final int COLUMNS = 4; //$NON-NLS-1$

	private static final int THREE_COLUMNS = 3; //$NON-NLS-1$

	private static final String FIELD_TYPE_NAME_PREFIX = "ModifierFieldTypeName_";  //$NON-NLS-1$
	private static final String CART_MODIFIER_FIELD_TABLE = "Cart Modifier Field"; //$NON-NLS-1$

	private ModifierGroup cartItemModifierGroup;

	private static final int MINIMAL_DIALOG_WIDTH = 525; //$NON-NLS-1$

	private static final int MINIMAL_DIALOG_HEIGHT = 230; //$NON-NLS-1$

	private static final String DIGITS_ONLY = "[0-9]*"; //$NON-NLS-1$

	private static final int BINDING_CALLER = 0; //$NON-NLS-1$

	private static final int LANGUAGE_CHANGED_CALLER = 1; //$NON-NLS-1$

	private static final int SINGLE_OPTION_VALID_SIZE = 1;

	private ModifierService modifierService;

	private ModifierField cartItemModifierField;

	private Text fieldDisplayNameText;

	private Text fieldCodeText;

	private Text fieldMaxSizeText;

	private CCombo fieldLanguageCombo;

	private CCombo fieldTypeCombo;

	private Label fieldMaxSizeLabel;

	private Button editOptionBtt;

	private Button addOptionBtt;

	private Button removeOptionBtt;

	private Button orderUpBtt;

	private Button orderDownBtt;

	private Button requiredBtt;

	private Locale selectedLocale;

	private String originalCode;

	private IPolicyTargetLayoutComposite dialogComposite;

	private IEpTableViewer cartItemModifierFieldOptionsTableViewer;

	private boolean editMode;

	private boolean isRequired;

	private List<Locale> allLocales;

	private List<String> allLocalesTags;

	private IEpLayoutData labelLayoutData;

	private IEpLayoutData textFieldLayoutData3;

	private List<ModifierType> itemsTypes;

	private final DataBindingContext dataBindingContext;

	private IStructuredSelection sel;

	private boolean allowRefresh;

	private PolicyActionContainer addEditModifierFieldContainer;

	/**
	 * Default Constructor.
	 */
	public AddEditCartItemModifierFieldDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), COLUMNS, false);
		this.dataBindingContext = new DataBindingContext();
	}

	/**
	 * Constructor.
	 *
	 * @param allLocales            all the available locales.
	 * @param selectedLocale        the selected locale.
	 * @param cartItemModifierField the cart item modifier field to be edited o added
	 *                              {@link com.elasticpath.domain.modifier.ModifierField}
	 * @param cartItemModifierGroup the cart item modifier Group associated
	 *                              {@link com.elasticpath.domain.modifier.ModifierGroup}
	 */
	public AddEditCartItemModifierFieldDialog(final List<Locale> allLocales, final Locale selectedLocale,
			final ModifierField cartItemModifierField, final ModifierGroup cartItemModifierGroup) {

		this();
		this.editMode = cartItemModifierField != null;

		this.allLocales = allLocales;
		this.selectedLocale = selectedLocale;
		this.isRequired = false;

		if (this.editMode) {
			initializeDialog(cartItemModifierField);
		} else {
			createModifierField();
		}

		this.cartItemModifierGroup = cartItemModifierGroup;
		this.modifierService = BeanLocator.getSingletonBean(ContextIdNames.MODIFIER_SERVICE, ModifierService.class);
		this.allowRefresh = false;
	}

	private void initializeDialog(final ModifierField cartItemModifierField) {
		this.cartItemModifierField = cartItemModifierField;

		// remember the original code for validation purposes
		this.originalCode = this.cartItemModifierField.getCode();
		this.isRequired = this.cartItemModifierField.isRequired();
		}

	/**
	 * @return the current ModifierField objected edited or added.
	 */
	public ModifierField getModifierField() {
		if (cartItemModifierField == null) {
			createModifierField();
		}
		if (this.cartItemModifierField.getCode() != null) {
			this.originalCode = this.cartItemModifierField.getCode();
			this.isRequired = this.cartItemModifierField.isRequired();
		}
		return cartItemModifierField;
	}

	private void createModifierField() {
		this.cartItemModifierField = BeanLocator.getPrototypeBean(ContextIdNames.MODIFIER_FIELD, ModifierField.class);
		this.originalCode = "";
		this.isRequired = false;
	}

	@Override
	protected void bindControls() {
		final EpControlBindingProvider bindingProvider = EpControlBindingProvider.getInstance();
		// update strategy for the ModifierField code control
		final ObservableUpdateValueStrategy valueUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				final String code = fieldCodeText.getText();
				getModifierField().setCode(code);
				if (!editMode) {
					getModifierField().setOrdering(getMaximumFieldOrdering(cartItemModifierGroup) + 1);
				}
				return Status.OK_STATUS;
			}
		};
		// update strategy for the ModifierFieldLDF
		final ObservableUpdateValueStrategy displayNameUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				checkLDFAndBind(selectedLocale.toString(), fieldDisplayNameText.getText(), 0);
				return Status.OK_STATUS;
			}
		};
		// update strategy for the ModifierField field Type
		final ObservableUpdateValueStrategy fieldTypeUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				getModifierField().setFieldType(getActualModifierType(fieldTypeCombo.getSelectionIndex()));
				return Status.OK_STATUS;
			}
		};
		// ModifierField code
		bindingProvider.bind(dataBindingContext, this.fieldCodeText,
				EpValidatorFactory.CARTITEM_MODIFIER_FIELD_CODE, null, valueUpdateStrategy, true);
		// ModifierField displayName
		bindingProvider.bind(dataBindingContext, this.fieldDisplayNameText, EpValidatorFactory.STRING_255_REQUIRED, null,
				displayNameUpdateStrategy, true);
		// ModifierField Field Type
		bindingProvider.bind(dataBindingContext, this.fieldTypeCombo,
				EpValidatorFactory.createCComboFieldTypeSelectionValidator(this.fieldTypeCombo),
				null, fieldTypeUpdateStrategy, true);
		EpDialogSupport.create(this, dataBindingContext);

		if (editMode) {
			EpControlFactory.changeTextState(this.fieldCodeText, EpControlFactory.EpState.READ_ONLY);
		}

		this.allowRefresh = true;
	}

	@Override
	protected String getTitle() {
		if (!editMode) {
			return CatalogMessages.get().CatalogCartItemModifierGroupsSectionAddEditDialog_Iitle_AddModifierField;
		}
		return CatalogMessages.get().CatalogCartItemModifierGroupsSectionAddEditDialog_Iitle_EditModifierField;
	}

	@Override
	protected String getInitialMessage() {
		if (!editMode) {
			return CatalogMessages.get().CatalogCartItemModifierGroupsSectionAddEditDialog_InitMsg_AddNewModifierField;
		}
		return CatalogMessages.get().CatalogCartItemModifierGroupsSectionAddEditDialog_InitMsg_EditAnModifierField;
		}

	@Override
	protected Image getWindowImage() {
		// no image available for the dialog.
		return null;
	}

	@Override
	protected String getWindowTitle() {
		if (!editMode) {
			return CatalogMessages.get().CatalogCartItemModifierGroupsSectionAddEditDialog_WinIitle_AddModifierField;
		}
		return CatalogMessages.get().CatalogCartItemModifierGroupsSectionAddEditDialog_WinIitle_EditModifierField;
	}

	@Override
	protected void okPressed() {
		setErrorMessage(null);
		if (!validate()) {
			return;
		}

		if (getModifierField().getFieldType().equals(ModifierType.SHORT_TEXT)) {
			getModifierField().setMaxSize(Integer.parseInt(fieldMaxSizeText.getText()));
		} else {
			getModifierField().setMaxSize(null);
		}

		if (!getModifierField().getFieldType().equals(ModifierType.PICK_MULTI_OPTION)
				&& !getModifierField().getFieldType().equals(ModifierType.PICK_SINGLE_OPTION)) {
			removeAll();
		}

		if (getModifierField().getFieldType().equals(ModifierType.PICK_SINGLE_OPTION)
				&& getModifierField().getModifierFieldOptions().size() > SINGLE_OPTION_VALID_SIZE) {
			final Set<ModifierFieldOption> options = getModifierField().getModifierFieldOptions();
			removeAll();
			options.stream().findFirst().ifPresent(this::add);
		}

		getModifierField().setRequired(this.requiredBtt.getSelection());

		super.okPressed();
	}

	/**
	 * Validate the Cart Item Modifier Group Field.
	 *
	 * @return <code>true</code> if the brand fields are valid, false otherwise.
	 */
	private boolean validate() {
		final String groupFieldCode = fieldCodeText.getText();
		final String groupFieldName = fieldDisplayNameText.getText();
		if (groupFieldCode.trim().isEmpty()) {
			setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_noCode_desc);
			return false;
		} else if (groupFieldName.trim().isEmpty()) {
			setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_noName_desc);
			return false;
		} else if (getModifierField().getFieldType().equals(ModifierType.SHORT_TEXT)
				&& (fieldMaxSizeText.getText().trim().isEmpty()
				|| !fieldMaxSizeText.getText().trim().matches(DIGITS_ONLY))) { //Short Text Type
			setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_noMaxSize_desc);
			return false;
		} else if (getModifierField().getFieldType().equals(ModifierType.SHORT_TEXT)
				&& Double.parseDouble(fieldMaxSizeText.getText().trim()) > Integer.MAX_VALUE) { // Short Text Type; max size too large
			setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_maxSizeTooLarge);
			return false;
		} else if ((getModifierField().getFieldType().equals(ModifierType.PICK_MULTI_OPTION)
				|| getModifierField().getFieldType().equals(ModifierType.PICK_SINGLE_OPTION))
				&& this.getModifierField().getModifierFieldOptions().isEmpty()) { //Multi Select Option Type
			setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_noMulti_desc);
			return false;
		} else {
			final ModifierField groupField = this.modifierService.findModifierFieldByCode(groupFieldCode);
			if (groupField != null) {
				if (editMode && groupField.getCode().equals(originalCode)) {
					return true;
				}
				setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_AddInUse_desc);
				return false;
			}
			//validates the model (newly added items)
			if (cartItemModifierGroup != null) {
				for (ModifierField itemModifierField : cartItemModifierGroup.getModifierFields()) {
					if (editMode && itemModifierField.getCode().equals(originalCode)) {
						return true;
					} else if ((!editMode && itemModifierField.getCode().equals(groupFieldCode))
							&& itemModifierField.findModifierFieldLdfByLocale(this.selectedLocale.toString()) != null) {
						setErrorMessage(CatalogMessages.get().AddEditCartItemModifierFieldDialog_ErrorDialog_AddInUse_Langdesc);
						return false;
					}
				}
			}
		}
		if ((!this.getModifierField().getFieldType().equals(ModifierType.PICK_MULTI_OPTION)
				&& !this.getModifierField().getFieldType().equals(ModifierType.PICK_SINGLE_OPTION)) && !editMode) {
			Set<ModifierFieldOption> cartItemModifierFieldOptions = this.getModifierField().getModifierFieldOptions();
			if (!cartItemModifierFieldOptions.isEmpty()) {
				for (ModifierFieldOption cartItemModifierFieldOptionTMP : cartItemModifierFieldOptions) {
					this.getModifierField().removeModifierFieldOption(cartItemModifierFieldOptionTMP);
				}
			}
		}
		return true;
	}

	@Override
	protected void createDialogContent(final IPolicyTargetLayoutComposite dialogComposite) {

		this.dialogComposite = dialogComposite;

		final PolicyActionContainer codeFieldContainer = addPolicyActionContainer("cartItemModifierFieldCodeField"); //$NON-NLS-1$
		this.addEditModifierFieldContainer = addPolicyActionContainer("addEditModifierFieldDialog"); //$NON-NLS-1$
		if (editMode) {
			// force state policy to check if cartItemModifierGroup is in current change set
			this.addEditModifierFieldContainer.setPolicyDependent(this.cartItemModifierGroup);
			codeFieldContainer.setPolicyDependent(this.cartItemModifierGroup);
		} else {
			// force state policy to *not* check if cartItemModifierGroup in current change set
			this.addEditModifierFieldContainer.setPolicyDependent(null);
			codeFieldContainer.setPolicyDependent(null);
		}

		this.getShell().setMinimumSize(MINIMAL_DIALOG_WIDTH, MINIMAL_DIALOG_HEIGHT);
		labelLayoutData = this.dialogComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL, false, false);
		final IEpLayoutData fieldData1 = dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, false, 1, 1);
		final IEpLayoutData fieldData2 = dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);
		textFieldLayoutData3 = this.dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, false, THREE_COLUMNS, 1);

		this.dialogComposite.addLabelBoldRequired(
				CatalogMessages.get().AddEditCartItemModifierFieldDialog_Code, labelLayoutData, codeFieldContainer);
		fieldCodeText = this.dialogComposite.addTextField(textFieldLayoutData3, codeFieldContainer);

		this.dialogComposite.addLabelBoldRequired(
				CatalogMessages.get().AddEditCartItemModifierFieldDialog_DisplayName, labelLayoutData, this.addEditModifierFieldContainer);
		fieldLanguageCombo = this.dialogComposite.addComboBox(fieldData1, this.addEditModifierFieldContainer);
		fieldLanguageCombo.addSelectionListener(this);
		fieldDisplayNameText = this.dialogComposite.addTextField(fieldData2, this.addEditModifierFieldContainer);

		this.dialogComposite.addLabelBoldRequired(
				CatalogMessages.get().AddEditCartItemModifierFieldDialog_FieldType, labelLayoutData, this.addEditModifierFieldContainer);

		fieldTypeCombo = this.dialogComposite.addComboBox(textFieldLayoutData3, this.addEditModifierFieldContainer);
		fieldTypeCombo.addSelectionListener(this);
	}

	@Override
	protected Object getDependentObject() {
		return this.cartItemModifierField;
	}

	@Override
	protected void refreshLayout() {

		if (getShell() == null || !allowRefresh) {
			return;
		}

		getShell().layout(true, true);
		getShell().setSize(this.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
	}

	@Override
	protected PolicyActionContainer getOkButtonPolicyActionContainer() {
		return this.addEditModifierFieldContainer;
	}

	@Override
	protected void populateControls() {
		this.fieldCodeText.setText(this.originalCode);

		this.fieldTypeCombo.setItems(getModifierTypeStrings());

		this.allLocalesTags = new ArrayList<>();
		this.fieldLanguageCombo.removeAll();
		for (Locale lang : this.allLocales) {
			this.allLocalesTags.add(lang.toLanguageTag());
			this.fieldLanguageCombo.add(lang.getDisplayName());
		}
		this.fieldLanguageCombo.select(this.allLocales.indexOf(this.selectedLocale));
		this.fieldLanguageCombo.notifyListeners(SWT.Selection, new Event());

		int selectIndex = 0;
		if (this.editMode && this.cartItemModifierField != null) {
			selectIndex = this.itemsTypes.indexOf(this.cartItemModifierField.getFieldType()) + 1;
		}
		this.fieldTypeCombo.select(selectIndex);
		this.fieldTypeCombo.notifyListeners(SWT.Selection, new Event());
	}

	/**
	 * Depending of the Dialog Mode set Ok Button Text.
	 *
	 * @return The OK button text
	 */
	private String setButtonLabel() {
		String bttLabel = CatalogMessages.get().AddEditCartItemModifierFieldDialog_Add;
		if (this.editMode) {
			bttLabel = CoreMessages.get().AbstractEpDialog_ButtonOK;
		}
		return bttLabel;
		}

	@Override
	public String getTargetIdentifier() {
		return "addEditModifierFieldDialog"; //$NON-NLS-1$
	}

	@Override
	public void setObjectGuid(final String objectGuid) {
		if (objectGuid == null) {
			createModifierField();
			this.editMode = false;
		} else {
			throw new UnsupportedOperationException("Add via ObjectGuidReceiver is not implemented.");
		}
	}

	@Override
	public DataBindingContext getDataBindingContext() {
		return dataBindingContext;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createEpOkButton(parent, setButtonLabel(), null);
		createEpCancelButton(parent);
	}

	/**
	 * Set the columns name to the table viewer.
	 *
	 * @param table table of the table viewer.
	 */
	private void addColumns(final IEpTableViewer table) {
		final String[] columnNames = {CatalogMessages.get().AddEditCartItemModifierFieldDialog_TableCodeColumn,
				CatalogMessages.get().AddEditCartItemModifierFieldDialog_TableDisplayNameColumn};
		final int[] columnWidths = {150, 150};

		for (int i = 0; i < columnNames.length; ++i) {
			table.addTableColumn(columnNames[i], columnWidths[i]);
		}
	}

	/**
	 * Check ModifierField LDF and Bind.
	 *
	 * @param lang    The current locale.
	 * @param display The current display name.
	 */
	private void checkLDFAndBind(final String lang, final String display, final int caller) {
		//If called from binding (ui to model bind)
		if (caller == BINDING_CALLER) {
			if (!display.isEmpty()) {
				ModifierFieldLdf currentFieldLDF = this.getModifierField().findModifierFieldLdfByLocale(lang);
				if (currentFieldLDF == null) {
					currentFieldLDF = BeanLocator.getPrototypeBean(ContextIdNames.MODIFIER_FIELD_LDF, ModifierFieldLdf.class);
					currentFieldLDF.setDisplayName(display);
					currentFieldLDF.setLocale(lang);
					this.getModifierField().addModifierFieldLdf(currentFieldLDF);
				} else {
					currentFieldLDF.setDisplayName(display);
					currentFieldLDF.setLocale(lang);
				}
			}
		} else if (caller == LANGUAGE_CHANGED_CALLER) { // If called when language changed (model to ui bind)
			this.fieldDisplayNameText.setText(CatalogMessages.EMPTY_STRING);
			ModifierFieldLdf currentFieldLDF = this.getModifierField().findModifierFieldLdfByLocale(lang);
			if (currentFieldLDF != null) {
				this.fieldDisplayNameText.setText(currentFieldLDF.getDisplayName());
			}
		}
	}

	@Override
	public void widgetSelected(final SelectionEvent selectionEvent) {
		// When change the language
		if (selectionEvent.getSource().equals(this.fieldLanguageCombo)) {
			this.selectedLocale = Locale.forLanguageTag(this.allLocalesTags.get(this.fieldLanguageCombo.getSelectionIndex()));
			checkLDFAndBind(selectedLocale.toString(), fieldDisplayNameText.getText(), 1);
		} else if (selectionEvent.getSource().equals(fieldTypeCombo)) { // When Select A Field Type
			recreateDialogControls(getActualModifierType(fieldTypeCombo.getSelectionIndex()));
		} else if (selectionEvent.getSource().equals(addOptionBtt)) { // When add an option
			final AddEditCartItemModifierFieldOptionDialog dialog = new AddEditCartItemModifierFieldOptionDialog(this.allLocales,
					this.selectedLocale, null, this.getModifierField(), this.cartItemModifierGroup);
			if (dialog.open() == Window.OK) {
				this.add(dialog.getModifierFieldOption());
			}
		} else if (selectionEvent.getSource().equals(editOptionBtt) && sel != null) { // When edit an option
			ModifierFieldOption selectedOpt = (ModifierFieldOption) sel.getFirstElement();
			this.edit(selectedOpt);
		} else if (selectionEvent.getSource().equals(removeOptionBtt) && sel != null) { // When remove an option
			ModifierFieldOption selectedOpt = (ModifierFieldOption) sel.getFirstElement();
			this.remove(selectedOpt);
			this.refreshTableViewer();
			sel = null;
		} else if (selectionEvent.getSource().equals(orderUpBtt)) { // When order up an option
			ModifierFieldOption selectedOpt = (ModifierFieldOption) sel.getFirstElement();
			changeOptionOrder(selectedOpt, -1);
			refreshTableViewer();
			this.cartItemModifierFieldOptionsTableViewer.getSwtTable().setFocus();
			this.cartItemModifierFieldOptionsTableViewer.getSwtTableViewer().setSelection(sel);
		} else if (selectionEvent.getSource().equals(orderDownBtt)) { // When order down an option
			ModifierFieldOption selectedOpt = (ModifierFieldOption) sel.getFirstElement();
			changeOptionOrder(selectedOpt, +1);
			refreshTableViewer();
			this.cartItemModifierFieldOptionsTableViewer.getSwtTable().setFocus();
			this.cartItemModifierFieldOptionsTableViewer.getSwtTableViewer().setSelection(sel);
		} else if (selectionEvent.getSource().equals(requiredBtt)) { // When change required checkbox value
			Button btn = (Button) selectionEvent.widget;
			this.isRequired = btn.getSelection();
		}
		updateEditRemoveUpDownButtons();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent selectionEvent) {
		//Not applied
	}

	@Override
	public void doubleClick(final DoubleClickEvent doubleClickEvent) {
		ModifierFieldOption selectedOpt = (ModifierFieldOption) sel.getFirstElement();
		this.edit(selectedOpt);
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent selectionChangedEvent) {

		sel = (IStructuredSelection) selectionChangedEvent.getSelection();

		if (ignoreTableSelection(getStatePolicy(), getOkButtonPolicyActionContainer())) {
			return;
		}

		updateEditRemoveUpDownButtons();

	}

	@Override
	public void updateButtons() {
		super.updateButtons();
		updateEditRemoveUpDownButtons();
	}

	@Override
	protected String getPluginId() {
		return CatalogPlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return cartItemModifierField;
	}

	/**
	 * Method to recreate the controls by the field type selected.
	 *
	 * @param type ModifierType selected.
	 */
	private void recreateDialogControls(final ModifierType type) {
		if (type != null) {
			if (type.equals(ModifierType.SHORT_TEXT)) {
				this.showHideFields(true, false);
				return;
			} else if (type.equals(ModifierType.PICK_MULTI_OPTION) || type.equals(ModifierType.PICK_SINGLE_OPTION)) {
				this.showHideFields(false, true);
				return;
			}
		}
		this.showHideFields(false, false);
	}

	/***
	 * @param showMaxSize      Show or not Short Max Size field.
	 * @param showMultiOption show or not List of Cart item Modifer field options.
	 */
	private void showHideFields(final boolean showMaxSize, final boolean showMultiOption) {
		if (showMaxSize && !showMultiOption) { // SHORT TEXT
			this.resetFieldOptionsViewer();
			this.resetRequiredButton();

			if (fieldMaxSizeLabel == null) {
				fieldMaxSizeLabel = this.dialogComposite.addLabelBoldRequired(CatalogMessages.get().AddEditCartItemModifierFieldDialog_MaxSize,
						labelLayoutData, this.addEditModifierFieldContainer);
			}

			if (fieldMaxSizeText == null) {
				fieldMaxSizeText = this.dialogComposite.addTextField(textFieldLayoutData3, this.addEditModifierFieldContainer);
				if (editMode) {
					fieldMaxSizeText.setText(String.valueOf(this.getModifierField().getMaxSize()));
				}
			}
			this.createRequiredButton(this.isRequired);
		} else if (!showMaxSize && showMultiOption) { // MULTI & SINGLE OPTION
			this.createTable();
		} else {
			this.resetShortTextViewer();
			this.resetFieldOptionsViewer();
			this.resetRequiredButton();
			this.createRequiredButton(this.isRequired);
		}
		this.refreshLayout();
	}

	/**
	 * Removing and resetting cartItemModifierFieldOptionsTableViewer from Layout Composite.
	 */
	private void resetFieldOptionsViewer() {
		if (cartItemModifierFieldOptionsTableViewer != null) {
			cartItemModifierFieldOptionsTableViewer.getSwtTable().dispose();
			cartItemModifierFieldOptionsTableViewer = null;
		}
		if (addOptionBtt != null) {
			addOptionBtt.dispose();
			addOptionBtt = null;
		}
		if (editOptionBtt != null) {
			editOptionBtt.dispose();
			editOptionBtt = null;
		}
		if (removeOptionBtt != null) {
			removeOptionBtt.dispose();
			removeOptionBtt = null;
		}
		if (orderUpBtt != null) {
			orderUpBtt.dispose();
			orderUpBtt = null;
		}
		if (orderDownBtt != null) {
			orderDownBtt.dispose();
			orderDownBtt = null;
		}

	}

	/**
	 * Removing and resetting requiredBtt from Layout Composite.
	 */
	private void resetRequiredButton() {
		if (requiredBtt != null) {
			requiredBtt.dispose();
			requiredBtt = null;
		}
	}

	/**
	 * Removing and resetting fieldMaxSizeLabel and fieldMaxSizeText from Layout Composite.
	 */
	private void resetShortTextViewer() {
		if (fieldMaxSizeLabel != null) {
			fieldMaxSizeLabel.dispose();
			fieldMaxSizeLabel = null;
		}
		if (fieldMaxSizeText != null) {
			fieldMaxSizeText.dispose();
			fieldMaxSizeText = null;
		}
	}

	/**
	 * Method to create the table viewer to the composite.
	 */
	private void createTable() {
		this.resetFieldOptionsViewer();
		if (cartItemModifierFieldOptionsTableViewer == null) {
			this.resetShortTextViewer();
			this.resetRequiredButton();
			final IEpLayoutData tableLayoutData =
					this.dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true, 3, 7);
			this.cartItemModifierFieldOptionsTableViewer =
				this.dialogComposite.addTableViewer(false, tableLayoutData, this.addEditModifierFieldContainer, CART_MODIFIER_FIELD_TABLE);
			this.addColumns(cartItemModifierFieldOptionsTableViewer);
			this.createModifierFieldOptionsTableViewerButtons();
			this.cartItemModifierFieldOptionsTableViewer.setContentProvider(new ArrayContentProvider());
			this.refreshTableViewer();
			this.cartItemModifierFieldOptionsTableViewer.getSwtTableViewer().addSelectionChangedListener(this);
			this.cartItemModifierFieldOptionsTableViewer.getSwtTableViewer().addDoubleClickListener(this);
			this.createRequiredButton(this.isRequired);
		} else {
			this.refreshTableViewer();
		}
	}

	/**
	 * Method to create TableViewer operation buttons to the composite layout.
	 */
	private void createModifierFieldOptionsTableViewerButtons() {
		IEpLayoutData buttonLayout = this.dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);
		IEpLayoutData buttonLayout2 = this.dialogComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, true, false);

		// create edit button for invoking the editing
		Image myimg = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT);
		editOptionBtt = this.dialogComposite.addPushButton(CatalogMessages.get().AddEditCartItemModifierFieldDialog_TableEditButton, myimg,
				buttonLayout, this.addEditModifierFieldContainer);

		editOptionBtt.addSelectionListener(this);

		// create add button for invoking the editing
		myimg = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_ADD);
		addOptionBtt = this.dialogComposite.addPushButton(CatalogMessages.get().AddEditCartItemModifierFieldDialog_TableAddButton, myimg,
				buttonLayout, this.addEditModifierFieldContainer);

		addOptionBtt.addSelectionListener(this);

		// create remove button for invoking the editing
		myimg = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_REMOVE);
		removeOptionBtt = this.dialogComposite.addPushButton(CatalogMessages.get().AddEditCartItemModifierFieldDialog_TableRemoveButton, myimg,
				buttonLayout, this.addEditModifierFieldContainer);

		removeOptionBtt.addSelectionListener(this);

		// create order up button for invoking the editing
		myimg = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_UP_ARROW);
		orderUpBtt = this.dialogComposite.addPushButton(CoreMessages.get().button_MoveUp, myimg,
				buttonLayout2, this.addEditModifierFieldContainer);

		orderUpBtt.addSelectionListener(this);

		// create order down button for invoking the editing
		myimg = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_DOWN_ARROW);
		orderDownBtt = this.dialogComposite.addPushButton(CoreMessages.get().button_MoveDown, myimg,
				buttonLayout2, this.addEditModifierFieldContainer);

		orderDownBtt.addSelectionListener(this);
	}

	/**
	 * Add Cart Item Modifier Group Field Options to Field and refresh table viewer data.
	 *
	 * @param option ModifierFieldOption to be added to ModifierField.
	 */
	private void add(final ModifierFieldOption option) {
		this.getModifierField().addModifierFieldOption(option);
		refreshTableViewer();
	}

	/**
	 * Edit Cart Item Modifier Group Field Option of Group Field and refresh table viewer data.
	 *
	 * @param selectedFieldOption ModifierFieldOption to be edited of a CartItemModifierGroup.
	 */
	private void edit(final ModifierFieldOption selectedFieldOption) {
		if (selectedFieldOption != null) {
			final AddEditCartItemModifierFieldOptionDialog dialog = new AddEditCartItemModifierFieldOptionDialog(this.allLocales,
					this.selectedLocale, selectedFieldOption, getModifierField(), this.cartItemModifierGroup);
			dialog.open();
			this.refreshTableViewer();
		}
	}

	/**
	 * Remove Cart Item Modifier Group Field Options to Field and refresh table viewer data.
	 *
	 * @param option ModifierFieldOption to be removed to ModifierField.
	 */
	private void remove(final ModifierFieldOption option) {
		this.getModifierField().removeModifierFieldOption(option);
		refreshTableViewer();
	}


	/**
	 * Remove Cart Item Modifier Group Field Options to Field.
	 */
	private void removeAll() {
		final Set<ModifierFieldOption> optionsSet = getModifierField().getModifierFieldOptions();
		final List<ModifierFieldOption> optionsList = new ArrayList<>(optionsSet.size());
		CollectionUtils.addAll(optionsList, optionsSet);

		optionsList.forEach(this::remove);
	}

	/**
	 * Method to create required button to the composite layout.
	 *
	 * @param requiredValue The required value for the required button.
	 */
	private void createRequiredButton(final boolean requiredValue) {
		if (this.requiredBtt == null) {
			this.requiredBtt = this.dialogComposite.addCheckBoxButton(CatalogMessages.get().AddEditCartItemModifierFieldDialog_required,
					textFieldLayoutData3, this.addEditModifierFieldContainer);
			this.requiredBtt.setSelection(requiredValue);
			this.requiredBtt.addSelectionListener(this);
		}
	}

	/**
	 * Method to refresh table viewer content.
	 */
	private void refreshTableViewer() {
		if (cartItemModifierFieldOptionsTableViewer != null) {
			List<ModifierFieldOption> cartItemModifierFieldOptions =
					new ArrayList<>(this.getModifierField().getModifierFieldOptions());
			Collections.sort(cartItemModifierFieldOptions);
			this.cartItemModifierFieldOptionsTableViewer.setLabelProvider(new CartItemModifierFieldLabelProvider(this.selectedLocale));
			this.cartItemModifierFieldOptionsTableViewer.setInput(cartItemModifierFieldOptions);
		}
	}

	/**
	 * Method to get field types.
	 *
	 * @return String[] Of the Field Types names.
	 */
	private String[] getModifierTypeStrings() {
		itemsTypes = new ArrayList<>(ModifierType.values());
		int isize = itemsTypes.size();
		int iterator = 1;
		String[] cartItemTypeStrings = new String[isize + 1];
		cartItemTypeStrings[0] = "Select a type...";
		for (ModifierType item : itemsTypes) {
			cartItemTypeStrings[iterator] = CatalogMessages.get().getMessage(FIELD_TYPE_NAME_PREFIX + item.getName());
			iterator++;
		}
		return cartItemTypeStrings;
	}

	/**
	 * Method to get the selected field type.
	 *
	 * @param index index to get the object.
	 * @return ModifierType selected.
	 */
	private ModifierType getActualModifierType(final int index) {
		if (index <= 0) {
			return null;
		}
		return itemsTypes.get(index - 1);
	}

	/**
	 * Method to reorder the field options on the table viewer.
	 *
	 * @param selectedOption the selected field on the table viewer
	 * @param swapOperation  -1=order up +1= order down
	 */
	private void changeOptionOrder(final ModifierFieldOption selectedOption, final int swapOperation) {
		List<ModifierFieldOption> cartItemModifierFieldOptionsOrdered =
				new ArrayList<>(this.getModifierField().getModifierFieldOptions());
		Collections.sort(cartItemModifierFieldOptionsOrdered);
		int selectedOptionIndex = cartItemModifierFieldOptionsOrdered.indexOf(selectedOption);
		ModifierFieldOption optionToSwap = cartItemModifierFieldOptionsOrdered.get(selectedOptionIndex + swapOperation);
		int selectedOptionOrder = selectedOption.getOrdering();
		int optionToSwapOrder = optionToSwap.getOrdering();
		for (ModifierFieldOption cartItemModifierFieldOption : getModifierField().getModifierFieldOptions()) {
			if (cartItemModifierFieldOption.equals(selectedOption)) {
				cartItemModifierFieldOption.setOrdering(optionToSwapOrder);
			}
			if (cartItemModifierFieldOption.equals(optionToSwap)) {
				cartItemModifierFieldOption.setOrdering(selectedOptionOrder);
			}
		}
	}

	/**
	 * Method to retrieve the maximum field order of a CartItemModifierGroup.
	 *
	 * @param group ModifierGroup to get field max order.
	 * @return maximum field index.
	 */
	private int getMaximumFieldOrdering(final ModifierGroup group) {
		int maximumIndex = 0;
		if (group != null) {
			for (ModifierField field : group.getModifierFields()) {
				if (field.getOrdering() > maximumIndex) {
					maximumIndex = field.getOrdering();
				}
			}
		}
		return maximumIndex;
	}

	/**
	 * Helper method to update edit, remove, up and down buttons.
	 */
	private void updateEditRemoveUpDownButtons() {

		if (this.cartItemModifierFieldOptionsTableViewer == null) {
			return;
		}

		int items;

		try {
			items = this.cartItemModifierFieldOptionsTableViewer.getSwtTableViewer().getTable().getItemCount();
		} catch (SWTException e) {
			return;
		}

		if (sel == null || items < 1) { // no table item selected
			this.editOptionBtt.setEnabled(false);
			this.removeOptionBtt.setEnabled(false);
			this.orderUpBtt.setEnabled(false);
			this.orderDownBtt.setEnabled(false);
		} else if (items > 1) {
			ModifierFieldOption selectedOpt = (ModifierFieldOption) sel.getFirstElement();
			this.editOptionBtt.setEnabled(selectedOpt != null);
			this.removeOptionBtt.setEnabled(selectedOpt != null);
			final int selectedItem = this.cartItemModifierFieldOptionsTableViewer.getSwtTableViewer().getTable().getSelectionIndex() + 1;
			if (selectedItem == 1) {
				if (this.orderUpBtt.isEnabled()) {
					this.orderUpBtt.setEnabled(false);
				}
				this.orderDownBtt.setEnabled(true);
			} else if (selectedItem == items) {
				if (this.orderDownBtt.isEnabled()) {
					this.orderDownBtt.setEnabled(false);
				}
				this.orderUpBtt.setEnabled(true);
			} else if (selectedItem > 1 && selectedItem < items) {
				if (!this.orderUpBtt.isEnabled()) {
					this.orderUpBtt.setEnabled(true);
				}
				if (!this.orderDownBtt.isEnabled()) {
					this.orderDownBtt.setEnabled(true);
				}
			}
		} else if (items == 1) {
			this.editOptionBtt.setEnabled(true);
			this.removeOptionBtt.setEnabled(true);
			this.orderUpBtt.setEnabled(false);
			this.orderDownBtt.setEnabled(false);
		}
	}
}
