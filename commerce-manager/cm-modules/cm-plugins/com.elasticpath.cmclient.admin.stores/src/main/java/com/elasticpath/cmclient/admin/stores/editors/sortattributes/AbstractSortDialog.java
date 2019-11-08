/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.ServiceLocator;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortLocalizedName;

/**
 * Dialog for adding sort attributes.
 */
public abstract class AbstractSortDialog extends Dialog {

	/** Dropdown width. */
	static final int DROPDOWN_WIDTH = 188;

	/** Text field width. */
	static final int ROW_TEXT_FIELD_WIDTH = 178;

	private static final int ROW_TEXT_FIELD_HEIGHT = 15;

	private static final int DIALOG_WIDTH = 510;

	private static final int IMAGE_WIDTH = 20;

	private static final int DIALOG_HEIGHT = 345;
	private static final int FIRST_GROUP_WIDTH = 218;

	private final EpControlBindingProvider binder = EpControlBindingProvider.getInstance();

	private final DataBindingContext bindingContext = new DataBindingContext();

	private final AggregateValidationStatus aggregateValidationStatus;

	private IEpLayoutComposite parent;

	private SortAttribute selectedSortAttribute;

	private final Collection<Locale> locales;

	private final Locale defaultLocale;

	private String localeChosen;

	private Button okButton;

	private final Map<String, String> localeMap;

	private Text displayNameTextField;

	private CCombo directionDropDown;

	private CCombo localeDropdown;

	private IEpLayoutComposite warningGroup;

	/**
	 * Constructor.
	 * @param parentShell shell
	 * @param storeEditorModel store model
	 */
	public AbstractSortDialog(final Shell parentShell, final StoreEditorModel storeEditorModel) {
		super(parentShell);
		locales = storeEditorModel.getSupportedLocales();
		defaultLocale = storeEditorModel.getDefaultLocale();
		localeChosen = defaultLocale.getDisplayName();
		localeMap = new HashMap<>();
		aggregateValidationStatus = new AggregateValidationStatus(bindingContext.getBindings(), AggregateValidationStatus.MAX_SEVERITY);
	}

	@Override
	protected Control createDialogArea(final Composite composite) {
		final Composite area = (Composite) super.createDialogArea(composite);
		parent = CompositeFactory.createGridLayoutComposite(area, 1, false);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		warningGroup = parent.addGroup(2, false, parent.createLayoutData());
		IEpLayoutData warningLayout = warningGroup.createLayoutData();
		Label infoImage = warningGroup.addImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_WARNING_SMALL), warningLayout);
		((GridData) infoImage.getLayoutData()).minimumWidth = IMAGE_WIDTH;

		IEpLayoutData labelLayout = warningGroup.createLayoutData();
		warningGroup.addLabel(CoreMessages.get().EpValidatorFactory_LocaleDisplayNamesRequired, labelLayout);

		warningGroup.getSwtComposite().setVisible(localeDisplayNamesNotPopulated());

		IEpLayoutComposite secondGroup = parent.addGroup(2, false, parent.createLayoutData());

		IEpLayoutComposite nameGroup = secondGroup.addGroup(1, false, parent.createLayoutData());
		nameGroup.addLabel(AdminStoresMessages.get().SortAttributeKeyDialogLabel, nameGroup.createLayoutData());

		((GridData) nameGroup.getSwtComposite().getLayoutData()).widthHint = FIRST_GROUP_WIDTH;
		createSortAttributeName(nameGroup);

		IEpLayoutComposite localeGroup = secondGroup.addGroup(1, false, parent.createLayoutData(IEpLayoutData.END, IEpLayoutData.END));
		localeGroup.addLabel(AdminStoresMessages.get().SortLanguage, localeGroup.createLayoutData());

		((GridData) localeGroup.getSwtComposite().getLayoutData()).horizontalIndent = IMAGE_WIDTH;

		IEpLayoutData localeLayout = localeGroup.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL);

		localeDropdown = localeGroup.addComboBox(EpControlFactory.EpState.READ_ONLY, localeLayout);
		localeDropdown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent selectionEvent) {
				String prevLocale = localeChosen;
				localeChosen = localeDropdown.getItem(localeDropdown.getSelectionIndex());
				if (!prevLocale.equals(localeChosen)) {
					updateDisplayNameTextField();
				}
			}
		});

		GridData textLayoutData = (GridData) localeDropdown.getLayoutData();
		textLayoutData.widthHint = DROPDOWN_WIDTH;

		String defaultLocaleDisplayName = defaultLocale.getDisplayName();
		localeDropdown.add(defaultLocaleDisplayName);
		localeMap.put(defaultLocaleDisplayName, defaultLocale.toString());

		for (Locale locale : locales) {
			if (locale.equals(defaultLocale)) {
				continue;
			}
			String displayName = locale.getDisplayName();
			String localeCode = locale.toString();
			localeDropdown.add(displayName);
			localeMap.put(displayName, localeCode);
		}
		localeDropdown.select(0);

		IEpLayoutComposite thirdGroup = parent.addGroup(2, false, parent.createLayoutData());

		createDisplayNameGroup(thirdGroup);
		createDirectionGroup(thirdGroup);
		enableDropdowns();
		return area;
	}

	/**
	 * Update selected sort attribute.
	 */
	void updateCurrentlySelectedSortAttribute() {
		String localeCode = localeMap.get(localeChosen);
		if (selectedSortAttribute != null) {
			String displayNameText = displayNameTextField.getText();
			Map<String, SortLocalizedName> displayName = selectedSortAttribute.getLocalizedNames();
			if (displayNameText.length() > 0) {
				if (!displayName.containsKey(localeCode)) {
					SortLocalizedName sortLocalizedName = ServiceLocator.getService(ContextIdNames.SORT_LOCALIZED_NAME);
					sortLocalizedName.setLocaleCode(localeCode);
					displayName.put(localeCode, sortLocalizedName);
				}
				displayName.get(localeCode).setName(displayNameText);
			} else {
				displayName.remove(localeCode);
			}
		}
	}

	/**
	 * Update display name.
	 */
	public void updateDisplayNameTextField() {
		if (selectedSortAttribute != null) {
			Map<String, SortLocalizedName> displayName = selectedSortAttribute.getLocalizedNames();
			String localeCode = localeMap.get(localeChosen);
			displayNameTextField.setText(displayName.containsKey(localeCode) ? displayName.get(localeCode).getName() : EMPTY);
			parent.getSwtComposite().layout();
		}
	}

	/**
	 * Create sort attribute name.
	 * @param parent parent
	 */
	public abstract void createSortAttributeName(final IEpLayoutComposite parent);

	private void createDisplayNameGroup(final IEpLayoutComposite parent) {
		IEpLayoutComposite displayNameGroup = parent.addGroup(1, false, parent.createLayoutData());
		displayNameGroup.addLabel(AdminStoresMessages.get().SortDisplayName, displayNameGroup.createLayoutData());
		displayNameTextField = displayNameGroup.addTextField(EpControlFactory.EpState.EDITABLE, displayNameGroup.createLayoutData());

		GridData textLayoutData = (GridData) displayNameTextField.getLayoutData();
		textLayoutData.widthHint = ROW_TEXT_FIELD_WIDTH;
		textLayoutData.heightHint = ROW_TEXT_FIELD_HEIGHT;

		((GridData) displayNameGroup.getSwtComposite().getLayoutData()).widthHint = FIRST_GROUP_WIDTH;

		updateDisplayNameTextField();
		displayNameTextField.addModifyListener(modifyEvent -> {
			changeOkButtonStatus(aggregateValidationStatus.getValue());
			updateCurrentlySelectedSortAttribute();
			warningGroup.getSwtComposite().setVisible(localeDisplayNamesNotPopulated());
		});

		binder.bind(bindingContext, displayNameTextField, EpValidatorFactory.STRING_255_REQUIRED, null,
				buildUpdateStrategy(EpValidatorFactory.STRING_255_REQUIRED), false);
	}

	private void createDirectionGroup(final IEpLayoutComposite parent) {
		IEpLayoutComposite directionGroup = parent.addGroup(1, false, parent.createLayoutData(IEpLayoutData.END, IEpLayoutData.END));
		((GridData) directionGroup.getSwtComposite().getLayoutData()).horizontalIndent = IMAGE_WIDTH;
		directionGroup.addLabel(AdminStoresMessages.get().SortOrder, directionGroup.createLayoutData());
		directionDropDown = directionGroup.addComboBox(EpControlFactory.EpState.READ_ONLY, directionGroup.createLayoutData());
		directionDropDown.add(AdminStoresMessages.get().SortAscending);
		directionDropDown.add(AdminStoresMessages.get().SortDescending);
		directionDropDown.select(selectedSortAttribute != null && selectedSortAttribute.isDescending() ? 1 : 0);
		directionDropDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				String direction = directionDropDown.getItem(directionDropDown.getSelectionIndex());
				changeOkButtonStatus(aggregateValidationStatus.getValue());
				if (selectedSortAttribute != null) {
					selectedSortAttribute.setDescending(direction.equals(AdminStoresMessages.get().SortDescending));
				}
			}
		});

		GridData textLayoutData = (GridData) directionDropDown.getLayoutData();
		textLayoutData.widthHint = DROPDOWN_WIDTH;
	}

	private ObservableUpdateValueStrategy buildUpdateStrategy(final IValidator iValidator) {
		return new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object value) {
				return iValidator.validate(value);
			}
		};
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(getTitle());
	}

	/**
	 * Get the title.
	 * @return title
	 */
	public abstract String getTitle();

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, CoreMessages.get().AbstractEpDialog_ButtonSave, true);
		createButton(parent, IDialogConstants.CANCEL_ID, CoreMessages.get().AbstractEpDialog_ButtonCancel, false);
		aggregateValidationStatus.addValueChangeListener(event -> changeOkButtonStatus(event.diff.getNewValue()));
		okButton.setEnabled(false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(DIALOG_WIDTH, DIALOG_HEIGHT);
	}

	private void changeOkButtonStatus(final IStatus iStatus) {
		if (selectedSortAttribute == null || (iStatus != null && iStatus.getSeverity() == IStatus.ERROR)) {
			okButton.setEnabled(false);
		} else {
			okButton.setEnabled(true);
		}
	}

	private boolean localeDisplayNamesNotPopulated() {
		if (selectedSortAttribute == null) {
			return true;
		}
		Map<String, SortLocalizedName> localizedNameMap = selectedSortAttribute.getLocalizedNames();
		for (Locale locale : locales) {
			String localeCode = locale.toString();
			if (!localizedNameMap.containsKey(localeCode) || localizedNameMap.get(localeCode).getName().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the selected sort attribute.
	 * @return sort attribute
	 */
	public SortAttribute getSelectedSortAttribute() {
		return selectedSortAttribute;
	}

	/**
	 * Set the selected sort attribute.
	 * @param selectedSortAttribute sort attribute
	 */
	public void setSelectedSortAttribute(final SortAttribute selectedSortAttribute) {
		this.selectedSortAttribute = selectedSortAttribute;
	}

	/**
	 * Enable dropdowns based on validity of selected sort attribute.
	 */
	public void enableDropdowns() {
		boolean selected = selectedSortAttribute != null;
		directionDropDown.setEnabled(selected);
		localeDropdown.setEnabled(selected);
		displayNameTextField.setEnabled(selected);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}
}
