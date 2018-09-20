/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.catalog.dialogs.catalog;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.core.helpers.ThemeTemplateFinder;
import com.elasticpath.cmclient.core.helpers.ThemeTemplateFinderImpl;

/**
 * Manager for state and country combo's.
 */
public class ThemeTemplateSelectorControl {

	private CCombo themeCombo;

	private CCombo templateCombo;

	private final ThemeTemplateFinder themeTemplateFinder;
	
	private final String templateName; 

	/**
	 * Constructor.
	 * 
	 * @param templateName the template name
	 */
	public ThemeTemplateSelectorControl(final String templateName) {
		themeTemplateFinder = new ThemeTemplateFinderImpl();
		this.templateName = templateName;
	}

	/**
	 * Initialize the controls.
	 */
	public void init() {
		themeCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				populateTemplateCombo();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				/* default implementation */
			}
		});
	}

	/**
	 * Populate theme and template combo`s.
	 */
	public void populateThemeTemplateCombo() {
		themeCombo.setItems(themeTemplateFinder.getThemes().toArray(new String[] {}));
		if (themeCombo.getItems().length > 0) {
			themeCombo.select(0);
			populateTemplateCombo();
		}
	}

	private void populateTemplateCombo() {
		final int selectedIndex = themeCombo.getSelectionIndex();
		if (selectedIndex != -1) {
			String selectedTheme = themeCombo.getItem(themeCombo.getSelectionIndex());
			templateCombo.setItems(themeTemplateFinder.getTemplates(selectedTheme, templateName).toArray(new String[] {}));
			templateCombo.add(CatalogMessages.get().SelectTemplateDialog_DefaultTypeComboEntry, 0);
			templateCombo.select(0);
		}
	}

	/**
	 * @return the themeCombo
	 */
	public CCombo getThemeCombo() {
		return themeCombo;
	}

	/**
	 * @return the templateCombo
	 */
	public CCombo getTemplateCombo() {
		return templateCombo;
	}

	/**
	 * @param themeCombo the themeCombo to set
	 */
	public void setThemeCombo(final CCombo themeCombo) {
		this.themeCombo = themeCombo;
	}

	/**
	 * @param templateCombo the templateCombo to set
	 */
	public void setTemplateCombo(final CCombo templateCombo) {
		this.templateCombo = templateCombo;
	}

	/**
	 * @return selected template name.
	 */
	public String getSelectedTemplateName() {
		String selectedTemplateName = ""; //$NON-NLS-1$
		if (templateCombo.getSelectionIndex() > 0) {
			selectedTemplateName = templateCombo.getItem(templateCombo.getSelectionIndex());
		}

		return selectedTemplateName;
	}

}