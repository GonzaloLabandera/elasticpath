/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.pf4j.DefaultExtensionFinder;
import org.pf4j.ExtensionWrapper;
import org.pf4j.PluginManager;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.XPFExtensionResolver;

/**
 * Extended implementation of org.pf4j.DefaultExtensionFinder that finds extensions and wraps them to XPFExtensionWrapper.
 */
public class XPFExtensionFinder extends DefaultExtensionFinder {

	private final XPFSingletonExtensionFactory xpfSingletonExtensionFactory;
	private final BeanFactory beanFactory;

	/**
	 * Constructor.
	 *
	 * @param pluginManager             plugin manager
	 * @param beanFactory               bean factory
	 * @param singletonExtensionFactory extension factory
	 */
	public XPFExtensionFinder(final PluginManager pluginManager, final BeanFactory beanFactory,
							  final XPFSingletonExtensionFactory singletonExtensionFactory) {
		super(pluginManager);
		this.beanFactory = beanFactory;
		this.xpfSingletonExtensionFactory = singletonExtensionFactory;
	}

	@Override
	public <T> List<ExtensionWrapper<T>> find(final Class<T> type) {
		return createXPFWrappers(super.find(type));
	}

	private <T> List<ExtensionWrapper<T>> createXPFWrappers(final List<ExtensionWrapper<T>> wrappers) {
		List<XPFExtensionWrapper<T>> xpfWrappers = wrappers.stream()
				.flatMap(wrapper -> createXPFWrappers(wrapper).stream())
				.collect(Collectors.toList());
		return xpfWrappers.stream().map(wrapper -> (ExtensionWrapper<T>) wrapper).collect(Collectors.toList());
	}

	private <T> List<XPFExtensionWrapper<T>> createXPFWrappers(final ExtensionWrapper<T> wrapper) {
		final XPFExtensionResolver extensionResolver = beanFactory.getSingletonBean("xpfExtensionsResolver", XPFExtensionResolver.class);

		return extensionResolver.getAssignedExtensionConfigurations(wrapper.getDescriptor().extensionClass)
				.stream()
				.map(configuration -> new XPFExtensionWrapper<>(configuration.getExtensionGuid(), wrapper, xpfSingletonExtensionFactory))
				.collect(Collectors.toList());
	}

	@Override
	public <T> List<ExtensionWrapper<T>> find(final Class<T> type, final String pluginId) {
		return createXPFWrappers(super.find(type, pluginId));
	}

	//wrapping to XPFExtensionWrapper causes invocation of XPFInMemoryExtensionResolverImpl#init but the embedded extensions have not been added as
	// beans
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public List<ExtensionWrapper> find(final String pluginId) {
		return super.find(pluginId);
	}
}
