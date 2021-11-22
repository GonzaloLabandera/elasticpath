/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.admin.configuration.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.ui.framework.impl.EpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.impl.GridLayoutComposite;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.domain.TagValueType;

/**
 * Displays the form to edit/add a new tag definition.
 */
public class TagDefinitionDisplayComposite extends GridLayoutComposite {

	private static final String DICTIONARY_TABLE_NAME = "Dictionaries";
	private static final int CODE_WIDTH = 175;
	private static final int NAME_WIDTH = 175;
	private static final int PURPOSE_WIDTH = 300;
	private static final int DICT_SELECT_WIDTH = 25;
	private static final int DICT_TABLE_HEIGHT = 210;
	private static final int DESCRIPTION_TEXT_LIMIT = 255;
	private static final int NAME_TEXT_LIMIT = 255;
	private static final int GUID_TEXT_LIMIT = 64;
	private static final int TEXT_FIELD_WIDTH_HINT = 275;
	private static final int DESCRIPTION_HEIGHT_HINT = 75;
	private static final int LANG_COMPOSITE_NUM_COLUMNS = 3;
	private static final int TAG_DICTIONARY_GUID_COLUMN_NUMBER = 1;
	private static final int TAG_DICTIONARY_NAME_COLUMN_NUMBER = 2;
	private static final int TAG_DICTIONARY_PURPOSE_COLUMN_NUMBER = 3;

	private final TagGroupModel model;

	private TagDefinition tagDefinition;

	private Text guidText;
	private Text nameText;
	private Text descText;
	private Text displayNameText;
	private CCombo fieldTypeSelector;
	private CCombo languageSelector;

	private List<Locale> allLocales;
	private Locale selectedLocale = Locale.getDefault();
	private IEpTableViewer tagDictTableViewer;
	private List<TagValueType> allTypes;
	private Text httpHeaderSampleText;

	/**
	 * Creates a new instance of this composite.
	 *
	 * @param parent the parent composite that will display this composite
	 * @param model  the tag group model that will be used to fetch updated data needed by this composite
	 */
	public TagDefinitionDisplayComposite(final IEpLayoutComposite parent, final TagGroupModel model) {
		super(parent.getSwtComposite(), 2, false,
				parent.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, false, false));

		this.model = model;
		createControls();
	}

	private void createControls() {
		final IEpLayoutData textFieldLayoutData = createLayoutData(EpLayoutData.BEGINNING, EpLayoutData.FILL, true, false);
		final IEpLayoutData labelLayoutData = createLayoutData(EpLayoutData.END, EpLayoutData.END, false, false);

		addLabelBoldRequired(AdminConfigurationMessages.get().TagDefinition_Label_TagCode, EpControlFactory.EpState.EDITABLE,
				labelLayoutData);
		guidText = addTextField(EpControlFactory.EpState.EDITABLE, textFieldLayoutData);
		guidText.setTextLimit(GUID_TEXT_LIMIT);
		guidText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent event) {
				updateHttpHeaderSampleText();
				super.focusLost(event);
			}
		});
		((GridData) guidText.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;

		addLabelBoldRequired(AdminConfigurationMessages.get().TagDefinition_Label_TagName, EpControlFactory.EpState.EDITABLE,
				labelLayoutData);
		nameText = addTextField(EpControlFactory.EpState.EDITABLE, textFieldLayoutData);
		nameText.setTextLimit(NAME_TEXT_LIMIT);
		((GridData) nameText.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;

		addLabel(AdminConfigurationMessages.get().TagDefinition_Label_Description,
				createLayoutData(EpLayoutData.END, EpLayoutData.BEGINNING, false, true));
		descText = addTextArea(EpControlFactory.EpState.EDITABLE, createLayoutData(EpLayoutData.BEGINNING, EpLayoutData.FILL, true, true));
		((GridData) descText.getLayoutData()).heightHint = DESCRIPTION_HEIGHT_HINT;
		((GridData) descText.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;
		descText.setTextLimit(DESCRIPTION_TEXT_LIMIT);

		IEpLayoutData fullSpanLayoutData = createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true,
				true, 2, 1);

		IEpLayoutComposite langNameComposite = addGridLayoutComposite(LANG_COMPOSITE_NUM_COLUMNS, false, fullSpanLayoutData);
		GridLayout gridLayout = (GridLayout) langNameComposite.getSwtComposite().getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		langNameComposite.addLabelBoldRequired(AdminConfigurationMessages.get().TagDefinition_Label_Name,
				EpControlFactory.EpState.EDITABLE,
				labelLayoutData);
		languageSelector = langNameComposite.addComboBox(EpControlFactory.EpState.EDITABLE, createLayoutData(EpLayoutData.BEGINNING,
				EpLayoutData.FILL, false, false));
		((GridData) languageSelector.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;
		IEpLayoutData langNameCompositeLayoutData = langNameComposite.createLayoutData(EpLayoutData.BEGINNING, EpLayoutData.FILL, true, false);
		displayNameText = langNameComposite.addTextField(EpControlFactory.EpState.EDITABLE, langNameCompositeLayoutData);
		((GridData) displayNameText.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;

		addLabelBoldRequired(AdminConfigurationMessages.get().TagDefinition_Label_FieldType,
				EpControlFactory.EpState.EDITABLE, labelLayoutData);
		final IEpLayoutData fieldTypeLayoutData = createLayoutData(EpLayoutData.BEGINNING, EpLayoutData.FILL, false, false);
		fieldTypeSelector = addComboBox(EpControlFactory.EpState.EDITABLE, fieldTypeLayoutData);

		createDictionaryTable();

		httpHeaderSampleText = addTextField(EpControlFactory.EpState.READ_ONLY, createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, false,
				false, 2, 1));

		populateLanguageSelector();

		populateFieldTypeSelector();

	}

	private void updateHttpHeaderSampleText() {
		if (StringUtils.isNotEmpty(guidText.getText())) {
			String prefix = AdminConfigurationMessages.get().TagDefinition_Label_SampleHttpHeaderPrefix;
			httpHeaderSampleText.setText(prefix + guidText.getText() + "=<value>");
		}
	}

	/**
	 * Sets the TagDefinition this dialog will display.  The display is directly updated from this method.
	 *
	 * @param tagDefinition the tag definition to display in this composite
	 */
	public void setTagDefinition(final TagDefinition tagDefinition) {
		this.tagDefinition = tagDefinition;
		guidText.setText(StringUtils.defaultString(tagDefinition.getGuid()));
		nameText.setText(StringUtils.defaultString(tagDefinition.getName()));
		if (tagDefinition.isPersisted()) {
			guidText.setEditable(false);
			nameText.setEditable(false);
		}
		descText.setText(StringUtils.defaultString(tagDefinition.getDescription()));
		String displayName = tagDefinition.getLocalizedProperties().getValue(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME,
				getSelectedLocale());
		displayNameText.setText(StringUtils.defaultString(displayName));

		if (tagDefinition.getValueType() != null) {
			fieldTypeSelector.select(fieldTypeSelector.indexOf(tagDefinition.getValueType().getGuid()));
		}

		languageSelector.select(languageSelector.indexOf(getSelectedLocale().getDisplayName()));
		List<TagDictionary> dictionaryList = model.getTagDictionariesForTag(tagDefinition);
		TagDictionary[] dictsForTagDef = dictionaryList.toArray(new TagDictionary[dictionaryList.size()]);
		((CheckboxTableViewer) tagDictTableViewer.getSwtTableViewer()).setCheckedElements(dictsForTagDef);

		updateHttpHeaderSampleText();
	}

	private void createDictionaryTable() {
		IEpLayoutComposite tableComposite = addGridLayoutComposite(2, false,
				createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING, true, true, 2, 1));
		GridLayout gridLayout = (GridLayout) tableComposite.getSwtComposite().getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		tableComposite.addLabel("Dictionaries", createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.END, false, false, 2, 1));
		tagDictTableViewer = tableComposite.addCheckboxTableViewer(true, EpControlFactory.EpState.EDITABLE,
				createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true), DICTIONARY_TABLE_NAME);

		tagDictTableViewer.addTableColumn("", DICT_SELECT_WIDTH, IEpTableColumn.TYPE_CHECKBOX);
		tagDictTableViewer.addTableColumn(AdminConfigurationMessages.get().TagDictionary_ColumnHeader_Code, CODE_WIDTH);
		tagDictTableViewer.addTableColumn(AdminConfigurationMessages.get().TagDictionary_ColumnHeader_Name, NAME_WIDTH);
		tagDictTableViewer.addTableColumn(AdminConfigurationMessages.get().TagDictionary_ColumnHeader_Purpose, PURPOSE_WIDTH);

		final TableViewer swtTabDictTableViewer = tagDictTableViewer.getSwtTableViewer();
		swtTabDictTableViewer.setCellEditors(new CellEditor[] { new CheckboxCellEditor(), null, null, null });

		tagDictTableViewer.setLabelProvider(new TagDictionaryLabelProvider());
		tagDictTableViewer.setContentProvider(new TagDictionaryContentProvider());
		tagDictTableViewer.setInput(getDictionaries());
		((GridData) tagDictTableViewer.getSwtTable().getLayoutData()).heightHint = DICT_TABLE_HEIGHT;

		tagDictTableViewer.getSwtTableViewer().setSorter(new ViewerSorter() {
			@Override
			public int compare(final Viewer viewer, final Object obj1, final Object obj2) {
				return ((TagDictionary) obj2).getGuid().compareTo(((TagDictionary) obj1).getGuid());
			}
		});
	}

	/**
	 * Data bind the fields in order to validate the information being entered.
	 *
	 * @param bindingContext context
	 * @param dialog         dialog
	 */
	public void bindFields(final DataBindingContext bindingContext, final AbstractEpDialog dialog) {
		EpControlBindingProvider binder = EpControlBindingProvider.getInstance();

		binder.bind(bindingContext, guidText, EpValidatorFactory.TAG_DEFINITION_CODE, null,
				buildUpdateStrategy(EpValidatorFactory.TAG_DEFINITION_CODE), false);

		binder.bind(bindingContext, nameText, EpValidatorFactory.TAG_DEFINITION_NAME, null,
				buildUpdateStrategy(EpValidatorFactory.TAG_DEFINITION_NAME), false);

		binder.bind(bindingContext, descText, EpValidatorFactory.MAX_LENGTH_255, null,
				buildUpdateStrategy(EpValidatorFactory.MAX_LENGTH_255), false);

		binder.bind(bindingContext, languageSelector, EpValidatorFactory.REQUIRED, null, buildUpdateStrategy(EpValidatorFactory.REQUIRED), false);

		binder.bind(bindingContext, displayNameText, EpValidatorFactory.TAG_DEFINITION_NAME, null,
				buildUpdateStrategy(EpValidatorFactory.TAG_DEFINITION_NAME), false);

		binder.bind(bindingContext, fieldTypeSelector, EpValidatorFactory.REQUIRED, null, buildUpdateStrategy(EpValidatorFactory.REQUIRED), false);

		EpDialogSupport.create(dialog, bindingContext);
	}

	private ObservableUpdateValueStrategy buildUpdateStrategy(final IValidator iValidator) {
		return new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object value) {
				return iValidator.validate(value);
			}
		};
	}

	/**
	 * This label provider returns the text that should appear in each column for a given <code>TagDictionary</code> object.
	 */
	class TagDictionaryLabelProvider extends LabelProvider implements ITableLabelProvider {

		/**
		 * Gets the image for each column.
		 *
		 * @param element     the image
		 * @param columnIndex the index for the column
		 * @return Image the image
		 */
		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			return null;
		}

		/**
		 * Gets the text for each column.
		 *
		 * @param element     the text
		 * @param columnIndex the index for the column
		 * @return String the text
		 */
		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			final TagDictionary tagDictionary = (TagDictionary) element;
			switch (columnIndex) {
				case 0:
					if (tagDictionary.getTagDefinitions().contains(tagDefinition)) {
						return null;
					} else {
						return null;
					}
				case TAG_DICTIONARY_GUID_COLUMN_NUMBER:
					return tagDictionary.getGuid();
				case TAG_DICTIONARY_NAME_COLUMN_NUMBER:
					return tagDictionary.getName();
				case TAG_DICTIONARY_PURPOSE_COLUMN_NUMBER:
					return tagDictionary.getPurpose();
				default:
					return null;
			}
		}
	}

	/**
	 * The content provider class is responsible for providing objects to the view. It can wrap existing objects in adapters or
	 * simply return objects as-is. These objects may be sensitive to the current input of the view, or ignore it and always show
	 * the same content.
	 */
	class TagDictionaryContentProvider implements IStructuredContentProvider {

		/**
		 * Gets the tag dictionary from the list for each row.
		 *
		 * @param inputElement the input TagDictionary element
		 * @return Object[] the Set of TagDictionary objects
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public Object[] getElements(final Object inputElement) {
			return ((List<TagDictionary>) inputElement).toArray();
		}

		/**
		 * dispose the provider.
		 */
		@Override
		public void dispose() {
			// does nothing
		}

		/**
		 * Notify the provider the input has changed.
		 *
		 * @param viewer   the viewer that is rendering the TagDictionaries
		 * @param oldInput the previous input
		 * @param newInput the current selected input
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// does nothing
		}
	}

	private void populateFieldTypeSelector() {
		allTypes = model.getAllTagValueTypes();
		String[] typeValuesAsStr = allTypes.stream().map(TagValueType::getGuid).toArray(String[]::new);
		fieldTypeSelector.setItems(typeValuesAsStr);
	}

	private void populateLanguageSelector() {
		List<Locale> allAvailableLocales = getAllAvailableLocales();
		final String[] localesForCCombo = allAvailableLocales.stream().map(Locale::getDisplayName).toArray(String[]::new);

		languageSelector.setItems(localesForCCombo);
		languageSelector.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				// do nothing.
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Locale selectedLocale = getAllAvailableLocales().get(languageSelector.getSelectionIndex());
				setSelectedLocale(selectedLocale);
				displayNameText.setText(StringUtils.defaultString(
						tagDefinition.getLocalizedProperties().getValue(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, selectedLocale)));
			}
		});
	}

	/**
	 * Get the tag definition instance being displayed by this composite.  The TagDefinition is updated with the current values being displayed in
	 * the composite.
	 *
	 * @return the updated TagDefinition instance
	 */
	public TagDefinition getUpdatedTagDefinition() {
		tagDefinition.setValueType(allTypes.get(fieldTypeSelector.getSelectionIndex()));
		tagDefinition.setGuid(guidText.getText());
		tagDefinition.setDescription(descText.getText());
		tagDefinition.setName(nameText.getText());
		tagDefinition.getLocalizedProperties()
				.setValue(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, getSelectedLocale(), displayNameText.getText());

		return tagDefinition;
	}

	/**
	 * Get the List of tag dictionaries that are currently selected in this composite.
	 *
	 * @return the list of selected TagDictionary instances
	 */
	public List<TagDictionary> getSelectedDictionaries() {
		Object[] checkedElements = ((CheckboxTableViewer) tagDictTableViewer.getSwtTableViewer()).getCheckedElements();
		TagDictionary[] dictionaryArray = new TagDictionary[checkedElements.length];
		System.arraycopy(checkedElements, 0, dictionaryArray, 0, checkedElements.length);
		return Arrays.asList(dictionaryArray);
	}

	private List<TagDictionary> getDictionaries() {
		return model.getAllTagDictionaries();
	}

	private List<Locale> getAllAvailableLocales() {
		if (allLocales == null) {
			allLocales = new ArrayList<>();
			final Locale[] availableLocales = Locale.getAvailableLocales();
			allLocales = Arrays.stream(availableLocales)
					.filter(locale -> StringUtils.isNotEmpty(locale.getDisplayName()))
					.collect(Collectors.toList());

		}
		return allLocales;
	}

	private Locale getSelectedLocale() {
		return selectedLocale;
	}

	private void setSelectedLocale(final Locale selectedLocale) {
		this.selectedLocale = selectedLocale;
	}
}
