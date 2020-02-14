/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.paymentconfigurations.dialogs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationMessages;
import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationsImageRegistry;
import com.elasticpath.cmclient.admin.paymentconfigurations.AdminPaymentConfigurationsPlugin;
import com.elasticpath.cmclient.admin.paymentconfigurations.views.PaymentConfigurationsListModel;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.helpers.LocaleComparator;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Abstract payment configuration dialog. Defines common layouts for create and edit payment configuration dialogs.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.PrematureDeclaration", "PMD.ExcessiveImports", "PMD.TooManyMethods"})
public abstract class AbstractPaymentConfigurationsDialog extends AbstractEpDialog {

	/**
	 * Logger.
	 */
	protected static final Logger LOG = Logger.getLogger(AbstractPaymentConfigurationsDialog.class);

	private static final int TEXT_FIELD_LIMIT = 255;

	/**
	 * Constant holds a value for properties' list key column.
	 */
	private static final int PROPERTY_KEY_WIDTH = 200;

	/**
	 * Constant holds a value for properties' list value column.
	 */
	private static final int PROPERTY_VALUE_WIDTH = 200;

	private static final int TEXT_FIELD_WIDTH = 225;

	private static final int COMBO_WIDTH = 225;

	private static final int LOCALE_LANGUAGE_COMBO_WIDTH = 175;

	private static final String PAYMENT_CONFIGURATION_PROPERTIES_TABLE = "Payment Configuration Properties Table"; //$NON-NLS-1$

	private static final int PAYMENT_CONFIGURATION_PROPERTIES_TABLE_HEIGHT = 150;

	private static final int FULL_COLOR_CODE = 255;

	/**
	 * This dialog's title. Depends from whether this is create or edit dialog
	 */
	private final String title;

	/**
	 * This dialog's image. Depends from whether this is create or edit dialog
	 */
	private final Image image;

	/**
	 * List of payment configuration's provider.
	 */
	private CCombo paymentConfigurationProviderCombo;

	/**
	 * List of payment configuration's provider's method.
	 */
	private CCombo paymentConfigurationMethodCombo;

	/**
	 * Payment payment configuration name.
	 */
	private Text paymentConfigurationNameText;

	/**
	 * Payment payment display name.
	 */
	private Text paymentDisplayNameText;

	private final List<Pair<Text, CCombo>> localizationControls;

	private IEpLayoutComposite localizationComposite;

	/**
	 * Table viewer to display/edit payment configuration's properties.
	 */
	private IEpTableViewer paymentConfigurationPropsTable;

	/**
	 * The cell modifier class. Responsible for properties' modification.
	 */
	private PropertyCellModifier paymentConfigurationPropertiesCellModifier;

	private final DataBindingContext dataBindingCtx;

	private String paymentConfigurationNewName;

	private IEpLayoutComposite mainComposite;

	private final List<Locale> availableLocales;

	private final WritableList<IViewerObservableValue> localeComboObservables;

	/**
	 * The hyper link image for deleting the first localized display name. It is invisible for the default entry and made only visible when
	 * multiple entries are added to localized display name.
	 */
	private Hyperlink hiddenLocalizedNamesDeleteLink;

	/**
	 * Creates the dialog.
	 *
	 * @param parentShell the parent Eclipse shell
	 * @param image       the image for this dialog
	 * @param title       the title for this dialog
	 */
	protected AbstractPaymentConfigurationsDialog(final Shell parentShell, final Image image, final String title) {
		super(parentShell, 1, true);
		this.title = title;
		this.image = image;
		dataBindingCtx = new DataBindingContext();
		localizationControls = new LinkedList<>();
		localeComboObservables = new WritableList<>();
		availableLocales = getLocales();
	}

	/**
	 * Instantiates create payment dialog.
	 *
	 * @param parentShell the parent Eclipse shell
	 * @return create payment dialog instance
	 */
	public static AbstractPaymentConfigurationsDialog buildCreateDialog(final Shell parentShell) {
		return new PaymentConfigurationsCreateDialog(parentShell, AdminPaymentConfigurationsImageRegistry
				.getImage(AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS_CREATE),
				AdminPaymentConfigurationMessages.get().CreatePaymentConfigurationDialog);
	}

	/**
	 * Instantiates edit payment dialog.
	 *
	 * @param selectedConfig the config selected for editing
	 * @param parentShell    the parent Eclipse shell
	 * @return edit payment dialog instance
	 */
	public static AbstractPaymentConfigurationsDialog buildEditDialog(final PaymentConfigurationsListModel selectedConfig, final Shell parentShell) {
		return new PaymentConfigurationsEditDialog(selectedConfig, parentShell, AdminPaymentConfigurationsImageRegistry
				.getImage(AdminPaymentConfigurationsImageRegistry.IMAGE_PAYMENT_CONFIGURATIONS_EDIT),
				AdminPaymentConfigurationMessages.get().EditPaymentConfigurationDialog);
	}

	/**
	 * Gets the payment payment configuration this dialog is creating or editing.
	 *
	 * @return Payment Configuration this dialog has created or edited.
	 */
	public abstract PaymentProviderConfigDTO getPaymentProviderConfigDTO();

	@Override
	public Object getModel() {
		return getPaymentProviderConfigDTO();
	}

	@Override
	protected String getPluginId() {
		return AdminPaymentConfigurationsPlugin.PLUGIN_ID;
	}

	@Override
	protected abstract void populateControls();

	/**
	 * Convenience method to open dialog.
	 *
	 * @return true if dialog opened successfully, false otherwise.
	 */
	public final boolean openDialog() {
		return open() == Window.OK;
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {
		// mainComposite - This is the main composite, everything in the dialog box directly or indirectly resides inside this composite
		mainComposite = dialogComposite.addScrolledGridLayoutComposite(1, false, false, dialogComposite.createLayoutData(IEpLayoutData.FILL,
				IEpLayoutData.FILL, false, false));

		// paymentComposite - contains controls for basic payment info i.e. provider, method, configuration name etc...
		final IEpLayoutComposite paymentComposite = mainComposite.addGridLayoutComposite(2, true,
				mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));
		removeMarginHeight(paymentComposite);
		createLabelWithWarning(paymentComposite, AdminPaymentConfigurationMessages.get().PaymentConfigurationProviderLabel,
				AdminPaymentConfigurationMessages.get().PaymentTooltipProviderCannotBeEdited);
		createLabelWithWarning(paymentComposite, AdminPaymentConfigurationMessages.get().PaymentConfigurationMethodLabel,
				AdminPaymentConfigurationMessages.get().PaymentTooltipMethodCannotBeEdited);

		paymentConfigurationProviderCombo = createCCombo(paymentComposite, null);
		paymentConfigurationMethodCombo = createCCombo(paymentComposite, null);

		final IEpLayoutData labelData = paymentComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, true, false);
		final IEpLayoutData textFieldData = paymentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false, false);
		paymentComposite.addLabelBoldRequired(AdminPaymentConfigurationMessages.get().PaymentConfigurationNameLabel, EpState.EDITABLE, labelData);
		paymentComposite.addEmptyComponent(labelData);
		paymentConfigurationNameText = createTextField(paymentComposite, textFieldData);
		paymentComposite.addEmptyComponent(labelData);
		paymentComposite.addLabelBold(AdminPaymentConfigurationMessages.get().PaymentConfigurationDisplayNameLabel, labelData);
		paymentComposite.addEmptyComponent(labelData);
		paymentDisplayNameText = createTextField(paymentComposite, textFieldData);
		paymentComposite.addEmptyComponent(labelData);

		paymentComposite.addLabel(AdminPaymentConfigurationMessages.get().PaymentConfigurationLocalizedDisplayNameLabel, labelData);
		createAddLocalizedStringLink(paymentComposite);
		//An empty composite that is attached to mainComposite. Dynamically created locale text and combo are attached to this composite.
		localizationComposite = mainComposite.addGridLayoutComposite(1, false,
				mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));
		removeMarginHeight(localizationComposite);

		IEpLayoutComposite tableGridComposite = mainComposite.addGridLayoutComposite(1, false, mainComposite.createLayoutData(IEpLayoutData.FILL,
				IEpLayoutData.FILL));
		tableGridComposite.addLabelBold(AdminPaymentConfigurationMessages.get().PaymentProperty,
				tableGridComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL));
		paymentConfigurationPropsTable = createConfigurationDataSettingsTable(tableGridComposite);
	}

	private IEpTableViewer createConfigurationDataSettingsTable(final IEpLayoutComposite layoutComposite) {
		IEpTableViewer configurationDataSettingsTable = layoutComposite.addTableViewer(false, EpState.READ_ONLY,
				layoutComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false),
				PAYMENT_CONFIGURATION_PROPERTIES_TABLE);
		GridData tableLayoutData = (GridData) configurationDataSettingsTable.getSwtTable().getLayoutData();
		tableLayoutData.heightHint = PAYMENT_CONFIGURATION_PROPERTIES_TABLE_HEIGHT;
		configurationDataSettingsTable.getSwtTableViewer().getTable().setHeaderVisible(false);
		configurationDataSettingsTable.addTableColumn(AdminPaymentConfigurationMessages.get().PaymentPropertyListKey, PROPERTY_KEY_WIDTH);
		configurationDataSettingsTable.addTableColumn(AdminPaymentConfigurationMessages.get().PaymentPropertyListValue, PROPERTY_VALUE_WIDTH);
		configurationDataSettingsTable.setContentProvider(new PaymentConfigurationContentProvider());
		configurationDataSettingsTable.setLabelProvider(new PaymentConfigurationLabelProvider());
		return configurationDataSettingsTable;
	}

	private void createAddLocalizedStringLink(final IEpLayoutComposite layoutComposite) {
		final Hyperlink localizationStringLink = layoutComposite.addHyperLinkText(
				AdminPaymentConfigurationMessages.get().PaymentConfigurationAddLocalizationStringLink, EpState.EDITABLE,
				layoutComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL, true, false));
		localizationStringLink.setUnderlined(false);
		localizationStringLink.setForeground(new Color(localizationStringLink.getDisplay(), 0, 0, FULL_COLOR_CODE));
		localizationStringLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent hyperlinkEvent) {
				addLocaleArea(true);
			}
		});
	}

	private Text createTextField(final IEpLayoutComposite layoutComposite, final IEpLayoutData layoutData) {
		Text textField = layoutComposite.addTextField(EpState.EDITABLE, layoutData);
		((GridData) textField.getLayoutData()).widthHint = TEXT_FIELD_WIDTH;
		textField.setTextLimit(TEXT_FIELD_LIMIT);
		textField.addModifyListener((ModifyListener) event -> flushErrorMessage());
		return textField;
	}

	private CCombo createCCombo(final IEpLayoutComposite layoutComposite, final IEpLayoutData layoutData) {
		CCombo comboBox = layoutComposite.addComboBox(EpState.EDITABLE, layoutData);
		comboBox.setEditable(false);
		comboBox.addModifyListener((ModifyListener) event -> flushErrorMessage());
		((GridData) comboBox.getLayoutData()).widthHint = COMBO_WIDTH;
		return comboBox;
	}

	private void createLabelWithWarning(final IEpLayoutComposite layoutComposite, final String label, final String warningToolTip) {
		final IEpLayoutComposite labelLayoutComposite = layoutComposite.addGridLayoutComposite(2, false,
				layoutComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));
		removeMarginHeight(labelLayoutComposite);
		((GridLayout) labelLayoutComposite.getSwtComposite().getLayout()).marginWidth = 0;
		labelLayoutComposite.addLabelBoldRequired(label, EpState.EDITABLE,
				labelLayoutComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING, false, false));
		final Label warningImage = labelLayoutComposite.addImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_WARNING_SMALL),
				labelLayoutComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING, false, false));
		warningImage.setToolTipText(warningToolTip);
	}

	private void removeMarginHeight(final IEpLayoutComposite gridLayout) {
		((GridLayout) gridLayout.getSwtComposite().getLayout()).marginHeight = 0;
	}

	@Override
	protected void bindControls() {
		final boolean hideDecorationOnFirstValidation = true;

		final ObservableUpdateValueStrategy paymentConfigurationNameUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				paymentConfigurationNewName = (String) newValue;
				return Status.OK_STATUS;
			}
		};

		IValidator existingNameValidator = validator -> {
			if (!doesPaymentConfigurationNameNotExist(paymentConfigurationNameText.getText())) {
				return new Status(IStatus.ERROR,
						AdminPaymentConfigurationsPlugin.PLUGIN_ID,
						AdminPaymentConfigurationMessages.get().PaymentConfigurationNameAlreadyExist);
			}
			return Status.OK_STATUS;
		};

		EpControlBindingProvider.getInstance().bind(dataBindingCtx, paymentConfigurationNameText,
				new CompoundValidator(new IValidator[]{EpValidatorFactory.STRING_255_REQUIRED, EpValidatorFactory.NO_LEADING_TRAILING_SPACES,
						existingNameValidator}), null, paymentConfigurationNameUpdateStrategy, hideDecorationOnFirstValidation);

		EpControlBindingProvider.getInstance().bind(dataBindingCtx, paymentConfigurationProviderCombo,
				EpValidatorFactory.REQUIRED_COMBO_FIRST_ELEMENT_NOT_VALID, null, new ObservableUpdateValueStrategy() {
					@Override
					protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
						return Status.OK_STATUS;
					}
				}, hideDecorationOnFirstValidation);

		EpControlBindingProvider.getInstance().bind(dataBindingCtx, paymentConfigurationMethodCombo,
				EpValidatorFactory.REQUIRED_COMBO_FIRST_ELEMENT_NOT_VALID, null, new ObservableUpdateValueStrategy() {
					@Override
					protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
						return Status.OK_STATUS;
					}
				}, hideDecorationOnFirstValidation);

		EpDialogSupport.create(this, dataBindingCtx);
	}

	@Override
	protected void okPressed() {
		// fix for ROXY-93 Mac issue to force update on saving without clicking out of cell
		getPaymentConfigurationNameText().setFocus();
		// update model, so all strategies to have a chance to do their
		// work.
		dataBindingCtx.updateModels();
		if (validatePaymentConfigurationData() && doesPaymentConfigurationNameNotExist(getPaymentConfigurationNewName()) && prepareForSave()) {
			super.okPressed();
		}
	}

	private boolean validatePaymentConfigurationData() {
		final Map<String, String> properties = (Map<String, String>) (paymentConfigurationPropsTable.getSwtTableViewer().getInput());
		if (properties == null) {
			return true;
		}
		for (Object value : properties.values()) {
			if (!EpValidatorFactory.MAX_LENGTH_1000.validate(value).isOK()) {
				setError(AdminPaymentConfigurationMessages.get().PaymentPropertyInvalid);
				return false;
			}
		}
		return true;
	}

	/**
	 * Prepares the payment payment configuration for further saving.
	 *
	 * @return true if payment configuration is populated with UI data successfully, false otherwise.
	 */
	protected abstract boolean prepareForSave();

	/**
	 * Dynamically add default locale area Text field and combo box for localized display names.
	 * Delete hyperlink is invisible for locale area created by this method
	 */
	protected void addDefaultLocaleArea() {
		addLocaleArea(false);
	}

	/**
	 * Dynamically add Text field and combo box for localized display name.
	 *
	 * @param showDeleteLink - true if the delete hyperlink should be visible for this new area and any previously hidden delete hyperlinks
	 *                       and false otherwise.
	 * @return a pair of Text and CCombo controls that have been added.
	 */
	protected Pair<Text, CCombo> addLocaleArea(final boolean showDeleteLink) {
		final IEpLayoutComposite localizedEntryWrapper = localizationComposite.addGridLayoutComposite(3, false,
				localizationComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
		removeMarginHeight(localizedEntryWrapper);

		final IEpLayoutData textAndComboLayoutData = localizedEntryWrapper.createLayoutData(IEpLayoutData.FILL,
				IEpLayoutData.FILL, false, false);
		final Text localeTextField = createTextField(localizedEntryWrapper, textAndComboLayoutData);
		final CCombo localeCombo = createCCombo(localizedEntryWrapper, textAndComboLayoutData);
		((GridData) localeCombo.getLayoutData()).widthHint = LOCALE_LANGUAGE_COMBO_WIDTH;

		Pair<Text, CCombo> newControls = Pair.of(localeTextField, localeCombo);
		localizationControls.add(newControls);
		addLocaleComboBoxDataBinding(localeCombo);

		final ImageHyperlink deleteHyperLink = localizedEntryWrapper.addHyperLinkImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_REMOVE),
				EpState.EDITABLE, localizedEntryWrapper.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL, true, false));
		deleteHyperLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent hyperlinkEvent) {
				removeLocaleArea(localizedEntryWrapper, newControls);
			}
		});

		if (showDeleteLink) {
			if (hiddenLocalizedNamesDeleteLink != null) {
				hiddenLocalizedNamesDeleteLink.setVisible(true);
				hiddenLocalizedNamesDeleteLink = null;
			}
		} else {
			deleteHyperLink.setVisible(false);
			hiddenLocalizedNamesDeleteLink = deleteHyperLink;
		}
		refreshRootComposite();
		return newControls;
	}

	private void addLocaleComboBoxDataBinding(final CCombo localeCombo) {
		ComboViewer comboViewer = new ComboViewer(localeCombo);
		comboViewer.setContentProvider(new ObservableListContentProvider());
		IViewerObservableValue localeComboObservable = ViewersObservables.observeSingleSelection(comboViewer);
		this.localeComboObservables.add(localeComboObservable);
		IObservableList<String> filteredList = new ComputedList<String>() {

			private IViewerObservableValue currentComboObservable = localeComboObservable;

			@Override
			protected List<String> calculate() {
				return availableLocales.stream()
						.filter(this::isNotAlreadySelectedLocale)
						.map(Locale::getDisplayName)
						.collect(Collectors.toList());
			}

			private boolean isNotAlreadySelectedLocale(final Locale locale) {
				return localeComboObservables.stream()
						.allMatch(localeComboObservable -> !Objects.equals(localeComboObservable.getValue(), locale.getDisplayName())
								|| Objects.equals(localeComboObservable, currentComboObservable));
			}
		};

		comboViewer.setInput(filteredList);
	}

	private void removeLocaleArea(final IEpLayoutComposite localizedEntryWrapper, final Pair<Text, CCombo> controlsToDelete) {
		localizedEntryWrapper.getSwtComposite().dispose();
		localizationControls.remove(controlsToDelete);
		int indexOfDisposedObservable = -1;
		for (int i = 0; i < localeComboObservables.size(); ++i) {
			// Observable enters disposed state when the associated CCombo is disposed.
			if (localeComboObservables.get(i).isDisposed()) {
				indexOfDisposedObservable = i;
				break;
			}
		}
		if (indexOfDisposedObservable != -1) {
			localeComboObservables.remove(indexOfDisposedObservable);
		}
		if (localizationControls.isEmpty()) {
			addDefaultLocaleArea();
		}
		refreshRootComposite();
	}

	private void refreshRootComposite() {
		ScrolledComposite scrolledComposite = (ScrolledComposite) mainComposite.getSwtComposite().getParent();
		scrolledComposite.setRedraw(false);
		scrolledComposite.layout(true, true);
		scrolledComposite.setMinSize(mainComposite.getSwtComposite().computeSize(scrolledComposite.getClientArea().width, SWT.DEFAULT));
		scrolledComposite.setRedraw(true);
	}

	@Override
	protected final String getInitialMessage() {
		return AdminPaymentConfigurationMessages.get().PaymentDialogInitialMessage;
	}

	@Override
	protected final String getTitle() {
		return title;
	}

	@Override
	protected final Image getTitleImage() {
		return null;
	}

	@Override
	protected final String getWindowTitle() {
		return getTitle();
	}

	@Override
	protected Image getWindowImage() {
		return image;
	}

	protected final List<Locale> getAvailableLocales() {
		return availableLocales;
	}

	/**
	 * Payment Configuration properties content provider.
	 */
	private final class PaymentConfigurationContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(final Object inputElement) {
			return inputElement instanceof Map
					? ((Map) inputElement).entrySet().toArray()
					: null;
		}

		@Override
		public void dispose() {
			// do nothing
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// do nothing
		}
	}

	/**
	 * Payment Configuration properties label provider.
	 */
	private final class PaymentConfigurationLabelProvider extends LabelProvider implements ITableLabelProvider {

		private static final int PROP_KEY_INDEX = 0;

		private static final int PROP_VALUE_INDEX = 1;

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			if (columnIndex == PROP_VALUE_INDEX && isTableEditable()) {
				return CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_EDIT_CELL_SMALL);
			}
			return null;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {

			if (!(element instanceof Entry)) {
				return null;
			}

			final Entry<String, String> entry = (Entry<String, String>) element;
			final String key = entry.getKey();
			String value = entry.getValue();

			if ("".equals(value)) { //$NON-NLS-1$
				value = "<Enter a value>"; //$NON-NLS-1$
			}
			switch (columnIndex) {
				case PROP_KEY_INDEX:
					return key;
				case PROP_VALUE_INDEX:
					return value;
				default:
					return null;
			}
		}

		private boolean isTableEditable() {
			return paymentConfigurationPropsTable.getSwtTableViewer().getCellModifier() != null;
		}
	}

	/**
	 * This class gets new property input and saves it in the model.
	 */
	private final class PropertyCellModifier implements ICellModifier {

		private final TableViewer paymentConfigurationPropsViewer;

		/**
		 * Specifies whether selected payment configuration's properties were modified or not.
		 */
		private boolean paymentConfigurationPropsModified;

		private boolean isPaymentConfigurationPropsModified() {
			return paymentConfigurationPropsModified;
		}

		private void flushPaymentConfigurationPropertiesModified() {
			paymentConfigurationPropsModified = false;
		}

		/**
		 * Constructs this cell editor.
		 *
		 * @param propsViewer table viewer this cell modifier is associated with.
		 */
		PropertyCellModifier(final TableViewer propsViewer) {
			this.paymentConfigurationPropsViewer = propsViewer;
		}

		@Override
		public boolean canModify(final Object element, final String property) {
			return property.equals(AdminPaymentConfigurationMessages.get().PaymentPropertyListValue);
		}

		@Override
		public Object getValue(final Object element, final String property) {
			if (!(element instanceof Entry)) {
				return null;
			}
			flushErrorMessage();
			Entry<String, String> entry = (Entry<String, String>) element;
			return entry.getValue();
		}

		@Override
		public void modify(final Object element, final String property, final Object value) {
			Object entryObj;
			if (element instanceof Item) {
				entryObj = ((Item) element).getData();
			} else if (element instanceof Entry) {
				entryObj = element;
			} else {
				return;
			}

			Entry<String, String> entry = (Entry<String, String>) entryObj;

			if (EpValidatorFactory.MAX_LENGTH_255.validate(value).isOK()) {
				flushErrorMessage();
			}

			entry.setValue((String) value);
			paymentConfigurationPropsModified = true;

			paymentConfigurationPropsViewer.refresh();

			getOkButton().setEnabled(true);
		}
	}

	/**
	 * Returns true if current payment configuration properties were modified.
	 *
	 * @return true if current payment configuration properties were modified, false if stale.
	 */
	protected final boolean isPaymentConfigurationPropertiesModified() {
		Objects.requireNonNull(paymentConfigurationPropertiesCellModifier);
		return paymentConfigurationPropertiesCellModifier.isPaymentConfigurationPropsModified();
	}

	/**
	 * Set payment configuration properties to the cell modifier to make properties modifiable in the UI.
	 *
	 * @param properties properties to me modified.
	 */
	protected final void setPaymentConfigurationProperties(final Map properties) {
		paymentConfigurationPropsTable.setInput(properties);
		Optional.ofNullable(paymentConfigurationPropertiesCellModifier).ifPresent(PropertyCellModifier::flushPaymentConfigurationPropertiesModified);
	}

	/**
	 * Returns properties of current payment configuration. This properties may be modified.
	 *
	 * @return payment configuration's properties.
	 */
	protected final Map<String, String> getPaymentConfigurationProperties() {
		return (Map<String, String>) paymentConfigurationPropsTable.getSwtTableViewer().getInput();
	}

	/**
	 * Checks if is payment configuration name not exist.
	 *
	 * @param paymentConfigurationName the payment configuration name
	 * @return true, if is payment configuration name not exist
	 */
	protected boolean doesPaymentConfigurationNameNotExist(final String paymentConfigurationName) {
		final PaymentProviderConfigManagementService paymentProviderConfigManagementService =
				BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_CONFIG_MANAGEMENT_SERVICE, PaymentProviderConfigManagementService.class);
		final List<PaymentProviderConfigDTO> paymentConfigurations = paymentProviderConfigManagementService.findAll();

		for (final PaymentProviderConfigDTO existingConfiguration : paymentConfigurations) {
			if (paymentConfigurationName.equalsIgnoreCase(existingConfiguration.getConfigurationName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Set error message in the dialog's title area.
	 *
	 * @param errorMessage error message to be set.
	 */
	protected final void setError(final String errorMessage) {
		setErrorMessage(errorMessage);
	}

	/**
	 * Flushes previously set error message.
	 */
	protected void flushErrorMessage() {
		setErrorMessage(null);
	}

	/**
	 * @return the paymentConfigurationNameText
	 */
	protected Text getPaymentConfigurationNameText() {
		return paymentConfigurationNameText;
	}

	/**
	 * @return the paymentDisplayNameText
	 */
	protected Text getPaymentDisplayNameText() {
		return paymentDisplayNameText;
	}

	/**
	 * @return the paymentConfigurationProviderCombo
	 */
	protected CCombo getPaymentConfigurationProviderCombo() {
		return paymentConfigurationProviderCombo;
	}

	/**
	 * @return the paymentConfigurationMethodCombo
	 */
	protected CCombo getPaymentConfigurationMethodCombo() {
		return paymentConfigurationMethodCombo;
	}

	/**
	 * @return the payment configuration name entered by user.
	 */
	protected String getPaymentConfigurationNewName() {
		return paymentConfigurationNewName;
	}

	/**
	 * @return List of pairs of localized display name controls.
	 */
	protected List<Pair<Text, CCombo>> getLocalizationControls() {
		return localizationControls;
	}

	/**
	 * Make the Payment Configuration Data Settings editable. This makes the value field editable.
	 */
	protected void setEditablePaymentConfigurationData() {
		Objects.requireNonNull(paymentConfigurationPropsTable);
		final TableViewer swtTableViewer = paymentConfigurationPropsTable.getSwtTableViewer();
		final Table swtTable = swtTableViewer.getTable();
		swtTableViewer.setCellEditors(new CellEditor[]{new TextCellEditor(), new TextCellEditor(swtTable)});
		paymentConfigurationPropertiesCellModifier = new PropertyCellModifier(swtTableViewer);
		swtTableViewer.setCellModifier(paymentConfigurationPropertiesCellModifier);
	}

	/**
	 * Get the language tag from CCombo box for language.
	 *
	 * @param languageComboBox the CCombo control.
	 * @return the language tag.
	 */
	protected final String getLanguageTag(final CCombo languageComboBox) {
		if (languageComboBox.getSelectionIndex() == -1) {
			return null;
		}
		return getAvailableLocales().stream()
				.filter(locale -> locale.getDisplayName().equals(languageComboBox.getText()))
				.map(Locale::toLanguageTag)
				.map(localeLanguageTag -> localeLanguageTag.replaceAll("-", "_"))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Calculate payment configuration.
	 *
	 * @param paymentProviderPluginDTO is payment provider dto.
	 * @return calculated data map where key is config description and value is config description.
	 */
	protected ImmutableMap<String, String> getPaymentConfigurationData(final PaymentProviderPluginDTO paymentProviderPluginDTO) {
		final Map<String, String> configuration = new HashMap<>();
		getPaymentConfigurationProperties().keySet()
				.forEach(name -> configuration.put(mapDescriptionToKey(paymentProviderPluginDTO, name),
						getPaymentConfigurationProperties().get(name)));

		return ImmutableMap.copyOf(configuration);
	}

	private String mapDescriptionToKey(final PaymentProviderPluginDTO paymentProviderPluginDTO, final String name) {

		return paymentProviderPluginDTO.getConfigurationKeys()
				.stream()
				.filter(config -> name.equals(config.getKey()))
				.findAny()
				.map(config -> name)
				.orElse(paymentProviderPluginDTO.getConfigurationKeys()
						.stream()
						.filter(config -> name.equals(config.getDescription()))
						.findFirst()
						.map(config -> config.getKey())
						.orElse(null));
	}

	private List<Locale> getLocales() {
		List<Locale> locales = Arrays.stream(Locale.getAvailableLocales()).sorted(new LocaleComparator()).collect(Collectors.toList());
		locales.remove(Locale.ROOT);
		return locales;
	}
}
