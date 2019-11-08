/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.web.listeners;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * This filter clears thread locals from {@link HDSSupportBean} on each request.
 */
@Component(property = {
	HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true"
})
public class HDSSupportRequestListener implements ServletRequestListener {

	@Reference
	private HDSSupportBean hdsSupportBean;

	@Override
	public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
		hdsSupportBean.clearAll();
	}

	@Override
	public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
		hdsSupportBean.clearAll();
	}
}
