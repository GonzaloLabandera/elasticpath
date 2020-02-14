/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.cmclient.catalog.dialogs.cartitemmodifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.catalog.CatalogPlugin;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.ObjectGuidReceiver;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareDialog;
import com.elasticpath.cmclient.policy.ui.IPolicyTargetLayoutComposite;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;
import com.elasticpath.domain.modifier.ModifierGroup;

/**
 * The dialog UI class for Adding/Editing cartItemModifierFieldOption.
 */
@SuppressWarnings({"PMD.GodClass"})
public class AddEditCartItemModifierFieldOptionDialog extends AbstractPolicyAwareDialog
		implements ObjectGuidReceiver, SelectionListener {

	private static final int DIALOG_NUMBER_OF_COLUMN = 1;

	private static final int MAIN_COMPOSITE_NUMBER_OF_COLUMN = 4;

	private static final int BINDING_CALLER = 0;

	private static final int LANGUAGE_CHANGED_CALLER = 1;

	private final DataBindingContext dataBindingContext;

	private ModifierFieldOption cartItemModifierFieldOption;

	private ModifierField cartItemModifierField;

	private Text optionDisplayNameText;

	private Text optionValueText;

	private CCombo optionLanguageCombo;

	private Locale selectedLocale;

	private String originalValue;

	private boolean editMode;

	private List<Locale> allLocales;

	private List<String> allLocalesTags;


	private ModifierGroup cartItemModifierGroup;

	/**
	 * Policy container for the dialog controls.
	 */
	private PolicyActionContainer addEditModifierFieldOptionContainer;

	/**
	 * Default Constructor.
	 */
	public AddEditCartItemModifierFieldOptionDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DIALOG_NUMBER_OF_COLUMN, false);
		this.dataBindingContext = new DataBindingContext();
	}

	/**
	 * Constructor.
	 *
	 * @param allLocales                  All Locales available.
	 * @param selectedLocale              the selected locale.
	 * @param cartItemModifierFieldOption the cart item modifier field option to be edited o added
	 *                                    {@link ModifierFieldOption}
	 * @param modifierField       the cart item modifier field associated {@link ModifierField}
	 * @param modifierGroup       the cart item modifier group
	 */
	public AddEditCartItemModifierFieldOptionDialog(final List<Locale> allLocales,
			final Locale selectedLocale, final ModifierFieldOption cartItemModifierFieldOption,
			final ModifierField modifierField, final ModifierGroup modifierGroup) {

		this();
		this.allLocales = allLocales;
		this.selectedLocale = selectedLocale;
		this.editMode = cartItemModifierFieldOption != null;

		if (editMode) {
			initializeDialog(cartItemModifierFieldOption);
		} else {
			createModifierFieldOption();
		}
		this.cartItemModifierField = modifierField;
		this.cartItemModifierGroup = modifierGroup;
	}

	private void initializeDialog(final ModifierFieldOption newField) {
		this.cartItemModifierFieldOption = newField;

		// remember the original code for validation purposes
		this.originalValue = this.cartItemModifierFieldOption.getValue();
	}

	/**
	 * @return the current ModifierField object edited or added.
	 */
	public ModifierFieldOption getModifierFieldOption() {
		if (cartItemModifierFieldOption == null) {
			createModifierFieldOption();
		}
		if (this.cartItemModifierFieldOption.getValue() != null) {
			this.originalValue = this.cartItemModifierFieldOption.getValue();
		}
		return cartItemModifierFieldOption;
	}

	private void createModifierFieldOption() {
		this.cartItemModifierFieldOption = BeanLocator.getPrototypeBean(ContextIdNames.MODIFIER_FIELD_OPTION, ModifierFieldOption.class);
		this.originalValue = "";
	}

	@Override
	protected void bindControls() {
		final EpControlBindingProvider bindingProvider = EpControlBindingProvider.getInstance();
		// update strategy for the cartItemModifierFieldOption value control
		final ObservableUpdateValueStrategy valueUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				final String value = optionValueText.getText();
				getModifierFieldOption().setValue(value);
				if (!editMode) {
					getModifierFieldOption().setOrdering(getMaximumFieldOptionOrdering(cartItemModifierField) + 1);
				}
				return Status.OK_STATUS;
			}
		};
		// update strategy for the cartItemModifierFieldOptionLDF
		final ObservableUpdateValueStrategy displayNameUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				checkLDFAndBind(selectedLocale.toString(), optionDisplayNameText.getText(), 0);
				return Status.OK_STATUS;
			}
		};
		// ModifierFieldOption value
		bindingProvider.bind(dataBindingContext, this.optionValueText,
				EpValidatorFactory.CARTITEM_MODIFIER_OPTION_VALUE, null, valueUpdateStrategy, true);
		// ModifierFieldOption displayName
		bindingProvider.bind(dataBindingContext, this.optionDisplayNameText, EpValidatorFactory.STRING_255_REQUIRED, null,
				displayNameUpdateStrategy, true);
		EpDialogSupport.create(this, dataBindingContext);
	}

	@Override
	protected String getTitle() {
		if (!editMode) {
			return CatalogMessages.get().AddEditModifierFieldOptionDialog_Iitle_AddModifierField;
		}
		return CatalogMessages.get().AddEditModifierFieldOptionDialog_Iitle_EditModifierField;
	}

	@Override
	protected String getInitialMessage() {
		if (!editMode) {
			return CatalogMessages.get().AddEditModifierFieldOptionDialog_InitMsg_AddNewModifierField;
		}
		return CatalogMessages.get().AddEditModifierFieldOptionDialog_InitMsg_EditAnModifierField;
	}

	@Override
	protected Image getWindowImage() {
		// no image available for the dialog.
		return null;
	}

	@Override
	protected String getWindowTitle() {
		if (!editMode) {
			return CatalogMessages.get().AddEditModifierFieldOptionDialog_WinIitle_AddModifierField;
		}
		return CatalogMessages.get().AddEditModifierFieldOptionDialog_WinIitle_EditModifierField;
	}

	@Override
	protected void okPressed() {
		setErrorMessage(null);
		if (validate()) {
			super.okPressed();
		}
	}

	/**
	 * Validate the Cart Item Modifier Group Field.
	 *
	 * @return <code>true</code> if the brand fields are valid, false otherwise.
	 */
	private boolean validate() {
		final String optFieldValue = optionValueText.getText();
		final String optFieldName = optionDisplayNameText.getText();
		if (optFieldValue.trim().isEmpty()) {
			setErrorMessage(CatalogMessages.get().AddEditModifierFieldOptionDialog_ErrorDialog_noValue_desc);
			return false;
		} else if (optFieldName.trim().isEmpty()) {
			setErrorMessage(CatalogMessages.get().AddEditModifierFieldOptionDialog_ErrorDialog_noName_desc);
			return false;
		} else {
			//validates the model (newly added items) to not have 2 values with same locale
			if (cartItemModifierField != null) {
				for (ModifierFieldOption itemModifierFieldOption : cartItemModifierField.getModifierFieldOptions()) {
					if (editMode && itemModifierFieldOption.getValue().equals(originalValue)) {
						return true;
					} else if ((!editMode && itemModifierFieldOption.getValue().equals(optFieldValue))
							&& itemModifierFieldOption.getModifierFieldOptionsLdfByLocale(this.selectedLocale.toString()) != null) {
						setErrorMessage(CatalogMessages.get().AddEditModifierFieldOptionDialog_ErrorDialog_AddInUse_Langdesc);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	protected void createDialogContent(final IPolicyTargetLayoutComposite dialogComposite) {
		this.addEditModifierFieldOptionContainer = addPolicyActionContainer("addEditModifierFieldOptionDialog");

		IPolicyTargetLayoutComposite mainComposite = dialogComposite.addGridLayoutComposite(MAIN_COMPOSITE_NUMBER_OF_COLUMN, false,
				dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true),
				addEditModifierFieldOptionContainer);

		if (editMode) {
			// force state policy to check if cartItemModifierGroup is in current change set
			this.addEditModifierFieldOptionContainer.setPolicyDependent(this.cartItemModifierGroup);
		} else {
			// force state policy to *not* check if cartItemModifierGroup in current change set
			this.addEditModifierFieldOptionContainer.setPolicyDependent(null);
		}

		final IEpLayoutData labelLayoutData = mainComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL, false, false);
		final IEpLayoutData fieldData1 = mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, false, 1, 1);
		final IEpLayoutData fieldData2 = mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);
		final IEpLayoutData textFieldLayoutData3 = mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 1, 1);
		final IEpLayoutData textFieldLayoutData2 = mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);

		mainComposite.addLabelBoldRequired(
				CatalogMessages.get().AddEditModifierFieldOptionDialog_Value, labelLayoutData,
				this.addEditModifierFieldOptionContainer);
		optionValueText = mainComposite.addTextField(textFieldLayoutData2, this.addEditModifierFieldOptionContainer);

		mainComposite.addEmptyComponent(textFieldLayoutData3, this.addEditModifierFieldOptionContainer);

		mainComposite.addLabelBoldRequired(CatalogMessages.get().AddEditModifierFieldOptionDialog_DisplayName,
				labelLayoutData, this.addEditModifierFieldOptionContainer);
		optionLanguageCombo = mainComposite.addComboBox(fieldData1, this.addEditModifierFieldOptionContainer);
		optionLanguageCombo.addSelectionListener(this);
		optionDisplayNameText = mainComposite.addTextField(fieldData2, this.addEditModifierFieldOptionContainer);
	}

	@Override
	protected Object getDependentObject() {
		return this.cartItemModifierGroup;
	}

	@Override
	protected void refreshLayout() {
		// Do nothing
	}

	@Override
	protected PolicyActionContainer getOkButtonPolicyActionContainer() {
		return this.addEditModifierFieldOptionContainer;
	}


	@Override
	protected String getPluginId() {
		return CatalogPlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return cartItemModifierFieldOption;
	}

	@Override
	protected void populateControls() {
		this.optionValueText.setText(this.originalValue);
		this.allLocalesTags = new ArrayList<>();
		this.optionLanguageCombo.removeAll();
		for (Locale lang : this.allLocales) {
			allLocalesTags.add(lang.toLanguageTag());
			this.optionLanguageCombo.add(lang.getDisplayName());
		}
		optionLanguageCombo.select(this.allLocales.indexOf(this.selectedLocale));
		optionLanguageCombo.notifyListeners(SWT.Selection, new Event());
	}

	/**
	 * Depending of the Dialog Mode set Ok Button Text.
	 *
	 * @return The OK button text
	 */
	private String setButtonLabel() {
		String bttLabel = CatalogMessages.get().AddEditModifierFieldOptionDialog_Add;
		if (editMode) {
			bttLabel = CoreMessages.get().AbstractEpDialog_ButtonOK;
		}
		return bttLabel;
	}

	@Override
	public String getTargetIdentifier() {
		return "addEditModifierFieldOptionDialog";
	}

	@Override
	public void setObjectGuid(final String objectGuid) {
		if (objectGuid == null) {
			createModifierFieldOption();
			editMode = false;
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
	 * Check CartItemModifierOption LDF and Bind.
	 *
	 * @param lang    The current locale.
	 * @param display The current display name.
	 */
	private void checkLDFAndBind(final String lang, final String display, final int caller) {
		//If called from binding (ui to model bind)
		if (caller == BINDING_CALLER) {
			if (!display.isEmpty()) {
				ModifierFieldOptionLdf currentOptionLDF =
						this.getModifierFieldOption().getModifierFieldOptionsLdfByLocale(lang);
				if (currentOptionLDF == null) {
					currentOptionLDF = BeanLocator.getPrototypeBean(ContextIdNames.MODIFIER_OPTION_LDF, ModifierFieldOptionLdf.class);
					currentOptionLDF.setDisplayName(display);
					currentOptionLDF.setLocale(lang);
					this.getModifierFieldOption().addModifierFieldOptionLdf(currentOptionLDF);
				} else {
					currentOptionLDF.setDisplayName(display);
					currentOptionLDF.setLocale(lang);
				}
			}
		} else if (caller == LANGUAGE_CHANGED_CALLER) { // If called when language changed (model to ui bind)
			optionDisplayNameText.setText(CatalogMessages.EMPTY_STRING);
			ModifierFieldOptionLdf currentOptionLDF = getModifierFieldOption().getModifierFieldOptionsLdfByLocale(lang);
			if (currentOptionLDF != null) {
				optionDisplayNameText.setText(currentOptionLDF.getDisplayName());
			}
		}
	}

	@Override
	public void widgetSelected(final SelectionEvent selectionEvent) {
		if (selectionEvent.getSource().equals(this.optionLanguageCombo)) {
			this.selectedLocale = Locale.forLanguageTag(this.allLocalesTags.get(this.optionLanguageCombo.getSelectionIndex()));
			checkLDFAndBind(selectedLocale.toString(), optionDisplayNameText.getText(), 1);
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent selectionEvent) {
		// Not Used.
	}

	/**
	 * Method to retrieve the maximum field order of a ModifierField.
	 *
	 * @param field ModifierField to get field options  max order.
	 * @return maximum field option index.
	 */
	private int getMaximumFieldOptionOrdering(final ModifierField field) {
		int maximumIndex = 0;
		if (field != null) {
			for (ModifierFieldOption option : field.getModifierFieldOptions()) {
				if (option.getOrdering() > maximumIndex) {
					maximumIndex = option.getOrdering();
				}
			}
		}
		return maximumIndex;
	}
}
