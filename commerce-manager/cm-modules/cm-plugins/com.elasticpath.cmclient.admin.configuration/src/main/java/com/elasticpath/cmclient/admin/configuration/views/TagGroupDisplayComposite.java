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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.impl.EpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.impl.GridLayoutComposite;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.tags.domain.TagGroup;

/**
 * A composite UI widget that displays a tag group for create/edit.
 */
public class TagGroupDisplayComposite extends GridLayoutComposite {

	private static final int LABEL_WIDTH = 90;
	private static final int TEXT_FIELD_WIDTH_HINT = 275;
	private static final int LANG_COMPOSITE_COLUMN_COUNT = 3;

	private Text guidTextField;
	private Text nameTextField;
	private CCombo languageSelector;
	private List<Locale> allLocales;
	private Locale selectedLocale;
	private TagGroup tagGroup;

	/**
	 * @param parent the parent composite
	 */
	public TagGroupDisplayComposite(final IEpLayoutComposite parent) {
		super(parent.getSwtComposite(), 2, false, parent.createLayoutData(EpLayoutData.BEGINNING, EpLayoutData.FILL, false, false));

		createControls();
	}

	/**
	 * Creates the UI elements and layout for the TagGroupDisplayComposite.
	 */
	private void createControls() {
		IEpLayoutData textLayout = createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, true, false);
		IEpLayoutData labelLayout = createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL, false, false);

		Label guidLabel = addLabelBoldRequired(AdminConfigurationMessages.get().guidLabel, EpControlFactory.EpState.EDITABLE, labelLayout);
		((GridData) guidLabel.getLayoutData()).widthHint = LABEL_WIDTH;

		guidTextField = addTextField(EpControlFactory.EpState.EDITABLE, textLayout);
		((GridData) guidTextField.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;
		guidTextField.setEditable(false);

		IEpLayoutData languageCompositeLayout = createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);
		IEpLayoutComposite langNameComposite = addGridLayoutComposite(LANG_COMPOSITE_COLUMN_COUNT, false, languageCompositeLayout);
		GridLayout gridLayout = (GridLayout) langNameComposite.getSwtComposite().getLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;

		Label displayNameLabel = langNameComposite
				.addLabelBoldRequired(AdminConfigurationMessages.get().nameLabel, EpControlFactory.EpState.EDITABLE,
						labelLayout);
		((GridData) displayNameLabel.getLayoutData()).widthHint = LABEL_WIDTH;
		languageSelector = langNameComposite.addComboBox(EpControlFactory.EpState.EDITABLE, createLayoutData(EpLayoutData.BEGINNING,
				EpLayoutData.FILL, false, true));
		((GridData) languageSelector.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;
		nameTextField = langNameComposite.addTextField(EpControlFactory.EpState.EDITABLE, textLayout);
		((GridData) nameTextField.getLayoutData()).widthHint = TEXT_FIELD_WIDTH_HINT;
		populateLanguageSelector();
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
				nameTextField.setText(StringUtils.defaultString(
						tagGroup.getLocalizedProperties().getValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, selectedLocale)));
			}
		});
	}

	/**
	 * Set the TagGroup that this composite will display.
	 *
	 * @param tagGroup the tag group to display
	 */
	public void setTagGroup(final TagGroup tagGroup) {
		this.tagGroup = tagGroup;
		guidTextField.setText(StringUtils.defaultString(tagGroup.getGuid()));
		String displayName = tagGroup.getLocalizedProperties().getValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, getSelectedLocale());
		nameTextField.setText(StringUtils.defaultString(displayName));
		languageSelector.select(languageSelector.indexOf(getSelectedLocale().getDisplayName()));
		if (!tagGroup.isPersisted()) {
			guidTextField.setEditable(true);
		}
	}

	/**
	 * Data bind the default value text box in order to validate the information being entered.
	 *
	 * @param bindingContext context
	 * @param dialog         dialog
	 */
	public void bindFields(final DataBindingContext bindingContext, final AbstractEpDialog dialog) {
		EpControlBindingProvider binder = EpControlBindingProvider.getInstance();

		binder.bind(bindingContext, guidTextField, EpValidatorFactory.TAG_GROUP_CODE, null,
				buildUpdateStrategy(EpValidatorFactory.TAG_GROUP_CODE), false);

		binder.bind(bindingContext, nameTextField, EpValidatorFactory.TAG_GROUP_NAME, null,
				buildUpdateStrategy(EpValidatorFactory.TAG_GROUP_NAME), false);

		binder.bind(bindingContext, languageSelector, EpValidatorFactory.REQUIRED, null, buildUpdateStrategy(EpValidatorFactory.REQUIRED), false);

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
	 * Gets the displayed TagGroup that has been updated from the current values in the UI.
	 *
	 * @return the updated TagGroup
	 */
	public TagGroup getUpdatedTagGroup() {
		tagGroup.setGuid(guidTextField.getText());
		tagGroup.getLocalizedProperties().setValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, getSelectedLocale(), nameTextField.getText());
		return tagGroup;
	}

	private Locale getSelectedLocale() {
		if (selectedLocale == null) {
			return Locale.getDefault();
		} else {
			return selectedLocale;
		}
	}

	private void setSelectedLocale(final Locale selectedLocale) {
		this.selectedLocale = selectedLocale;
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

}
