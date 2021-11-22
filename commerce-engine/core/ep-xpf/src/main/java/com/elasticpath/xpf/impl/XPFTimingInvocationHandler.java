/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.xpf.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic Proxy to obtain time taken logs for XPF Extensions.
 * @param <T> extension type for which dynamic proxy needs to be wrapped on
 */
public class XPFTimingInvocationHandler<T> implements InvocationHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(XPFTimingInvocationHandler.class);
	private static final int EXECUTION_TIME_THRESHOLD = 100;

	private final T proxied;

	/**
	 * Constructor.
	 *
	 * @param proxied extension to be proxied
	 */
	public XPFTimingInvocationHandler(final T proxied) {
		this.proxied = proxied;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		long start = System.currentTimeMillis();
		Object result = method.invoke(proxied, args);
		long time = System.currentTimeMillis() - start;

		// Do not log for proxy class invocations
		if (!Proxy.isProxyClass(proxied.getClass())) {
			if (time > EXECUTION_TIME_THRESHOLD) {
				LOGGER.warn("Extension class {} method {} took {} ms to execute.", proxied.getClass().getCanonicalName(), method.getName(), time);
			} else {
				LOGGER.trace("Extension class {} method {} took {} ms to execute.", proxied.getClass().getCanonicalName(), method.getName(), time);
			}
		}
		return result;
	}

	/**
	 * Helper method to obtain the proxied class.
	 * @return proxied class
	 */
	public Class<?> getProxiedClass() {
		return proxied.getClass();
	}
}
