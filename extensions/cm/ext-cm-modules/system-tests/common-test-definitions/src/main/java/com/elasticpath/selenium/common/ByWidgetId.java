package com.elasticpath.selenium.common;

import org.openqa.selenium.By;

/**
 * Finds element by the attribute 'widget-id'.
 */

public final class ByWidgetId {
	/**
	 * Private constructor.
	 */
	private ByWidgetId() {
	}

	/**
	 * finds element by the attribute 'widget-id'.
	 *
	 * @param widgetId 'widget-id' attribute value.
	 * @return a By object.
	 */
	@SuppressWarnings({"PMD.ShortMethodName"})
	public static By id(final String widgetId) {
		return By.xpath(".//*[@widget-id = \'" + widgetId + "\']");
	}
}
