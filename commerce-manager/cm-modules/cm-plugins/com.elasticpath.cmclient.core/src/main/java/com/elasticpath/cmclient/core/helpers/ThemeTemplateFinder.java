/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.core.helpers;

import java.util.List;

/**
 * Finder class to find themes and templates.
 */
public interface ThemeTemplateFinder {

	/**
	 * Returns the list of themes in the assets directory.
	 * 
	 * @return the list of themes.
	 */
	List<String> getThemes();

	/**
	 * Returns the list of category templates given the theme name and template name.
	 * 
	 * @param themeName the theme name
	 * @param templateName the template name
	 * @return the list of category templates
	 */
	List<String> getTemplates(String themeName, String templateName);

}
