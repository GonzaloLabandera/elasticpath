/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core.util;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.common.CMContextIdNames;
import com.elasticpath.cmclient.core.formatting.UIDateTimeUtil;

/**
 * Utility factory for date time formatting.
 */
public final class DateTimeUtilFactory {

	/**
	 * Private constructor.
	 */
	private DateTimeUtilFactory() {
		//do nothing
	}

	/**
	 * Convenience method that returns a UIDateTimeUtil from the bean factory.
	 *
	 * @return a UI date util
	 */
	public static UIDateTimeUtil getDateUtil() {
		return BeanLocator.getSingletonBean(CMContextIdNames.UI_DATE_FORMATTER, UIDateTimeUtil.class);
	}

}
