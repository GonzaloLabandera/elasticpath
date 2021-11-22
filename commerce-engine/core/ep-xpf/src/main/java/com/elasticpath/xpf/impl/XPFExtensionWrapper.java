/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import org.pf4j.ExtensionWrapper;

/**
 * A wrapper for ExtensionWrapper to hold extension GUID.
 *
 * @param <T> type of the extension
 */
public class XPFExtensionWrapper<T> extends ExtensionWrapper<T> {
	private final XPFSingletonExtensionFactory xpfSingletonExtensionFactory;
	private final String extensionGuid;
	private T extension;

	/**
	 * Constructor.
	 *
	 * @param extensionGuid    extension GUID
	 * @param wrapper          extension wrapper
	 * @param extensionFactory extension factory
	 */
	public XPFExtensionWrapper(final String extensionGuid, final ExtensionWrapper<T> wrapper, final XPFSingletonExtensionFactory extensionFactory) {
		super(wrapper.getDescriptor(), extensionFactory);
		this.extensionGuid = extensionGuid;
		this.xpfSingletonExtensionFactory = extensionFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getExtension() {
		if (this.extension == null) {
			this.extension = (T) this.xpfSingletonExtensionFactory.create(super.getDescriptor().extensionClass, getExtensionGuid());
		}

		return this.extension;
	}

	/**
	 * Get extension GUID.
	 *
	 * @return the extension GUID
	 */
	public String getExtensionGuid() {
		return extensionGuid;
	}
}
