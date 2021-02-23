/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.order.jobs;

/**
 * Job which will determine if there are ACTIVE order holds that need resolution and publish an event so that
 * a call-to-action notification can be sent.
 */
public interface OrderHoldNotificationJob {

	/**
	 * Key for the setting used to enable hold notifications for stores.
	 */
	String COMMERCE_STORE_ENABLE_DATA_HOLD_NOTIFICATION = "COMMERCE/SYSTEM/ONHOLD/holdNotificationEnabled";

	/**
	 * Check for outstanding held orders that need resolution and publish a notification event, if found.
	 */
	void publishHoldNotificationEvent();

}
