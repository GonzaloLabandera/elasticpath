/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageBuilder;
import com.elasticpath.tools.sync.dstmessagelistener.messages.DataSyncEventMessageService;

/**
 * Service responsible for publishing Event Messages describing the result of a Change Set publishing attempt.
 */
public class DataSyncEventMessageServiceImpl implements DataSyncEventMessageService {

	/**
	 * The bean name for the DataSyncEventMessageBuilderImpl bean.
	 */
	public static final String DATA_SYNC_EVENT_MESSAGE_BUILDER_BEAN_NAME = "dataSyncEventMessageBuilderImpl";
	private BeanFactory beanFactory;

	@Override
	public <T extends DataSyncEventMessageBuilder<T>> DataSyncEventMessageBuilder<T> prepareMessage() {

		return beanFactory.getBean(DATA_SYNC_EVENT_MESSAGE_BUILDER_BEAN_NAME);
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}