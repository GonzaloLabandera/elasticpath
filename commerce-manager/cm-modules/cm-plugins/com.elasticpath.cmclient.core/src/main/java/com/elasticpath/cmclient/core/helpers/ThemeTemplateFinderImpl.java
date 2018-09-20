/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.core.helpers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elasticpath.cmclient.core.ServiceLocator;
import com.elasticpath.commons.util.AssetRepository;
import com.elasticpath.commons.util.FileListingUtility;

/**
 * Finder class to find themes and templates. Themes and template list results are cached so that real-time lookup is only done once.
 */
public class ThemeTemplateFinderImpl implements ThemeTemplateFinder {

	private static final String TEMPLATE_MATCH_PATTERN = "(.*).vm"; //$NON-NLS-1$

	private static final String CATALOG_TEMPLATE_PATH = File.separator + "default" + File.separator + "templates" //$NON-NLS-1$ //$NON-NLS-2$
			+ File.separator + "velocity" + File.separator + "catalog"; //$NON-NLS-1$ //$NON-NLS-2$

	private final String themeAssetsPath;

	private final FileListingUtility fileListingUtility;

	private final Map<String, List<String>> themeTemplateMap;

	private List<String> themes;

	/**
	 * Constructor.
	 */
	public ThemeTemplateFinderImpl() {
		AssetRepository assetRepository = ServiceLocator.getService("assetRepository"); //$NON-NLS-1$
		this.themeAssetsPath = assetRepository.getThemesSubfolder();
		this.fileListingUtility = ServiceLocator.getService("assetFileListingUtility"); //$NON-NLS-1$
		this.themeTemplateMap = new HashMap<>();
	}

	/**
	 * Returns the list of themes in the assets directory.
	 * 
	 * @return the list of themes.
	 */
	public List<String> getThemes() {
		if (themes == null) {
			themes = fileListingUtility.findFolderNames(themeAssetsPath);
		}
		return themes;
	}

	/**
	 * Returns the list of category templates given the theme name and template name.
	 * 
	 * @param themeName the theme name
	 * @param templateName the template name
	 * @return the list of category templates
	 */
	public List<String> getTemplates(final String themeName, final String templateName) {
		if (!themeTemplateMap.containsKey(themeName)) {
			themeTemplateMap.put(themeName, fileListingUtility.findFileNames(getTemplatePath(themeName, templateName),
					TEMPLATE_MATCH_PATTERN, false));
		}
		return themeTemplateMap.get(themeName);
	}

	private String getTemplatePath(final String themeName, final String templateType) {
		return themeAssetsPath + File.separator + themeName + CATALOG_TEMPLATE_PATH + File.separator + templateType;
	}
}
