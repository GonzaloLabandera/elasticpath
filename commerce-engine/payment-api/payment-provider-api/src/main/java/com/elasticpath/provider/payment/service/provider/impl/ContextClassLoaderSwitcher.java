/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Thread Context Class Loader switcher for each target method invocation.
 */
public class ContextClassLoaderSwitcher implements InvocationHandler {

	private final ClassLoader classLoader;
	private final Object target;

	/**
	 * Constructor.
	 *
	 * @param classLoader class loader to switch to
	 * @param target      target object to invoke methods for
	 */
	public ContextClassLoaderSwitcher(final ClassLoader classLoader, final Object target) {
		this.classLoader = classLoader;
		this.target = target;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			return method.invoke(target, args);
		} catch (InvocationTargetException invocationTargetException) {
			throw invocationTargetException.getCause();
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}
