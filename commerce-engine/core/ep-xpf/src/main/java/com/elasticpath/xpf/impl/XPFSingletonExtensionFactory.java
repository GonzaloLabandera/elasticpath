/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.pf4j.DefaultExtensionFactory;
import org.pf4j.PluginRuntimeException;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.connectivity.context.XPFExtensionInitializationContext;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSetting;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.converters.XPFPluginSettingConverter;
import com.elasticpath.xpf.dto.PluginSettingDTO;

/**
 * Creates and caches extensions.
 */
public class XPFSingletonExtensionFactory extends DefaultExtensionFactory {

	private XPFPluginSettingConverter settingConverter;

	private BeanFactory beanFactory;

	private final Map<String, Object> singletonCache = new HashMap<>();

	/**
	 * Create, initialize, and cache an extension of the given class.
	 *
	 * @param extensionClass    extension class
	 * @param extensionGuid     extension GUID
	 * @param extensionSettings extension settings
	 */
	public void initializeAndCacheExtension(final Class<? extends XPFExtensionPointImpl> extensionClass, final String extensionGuid,
											final Set<PluginSettingDTO> extensionSettings) {
		try {
			final XPFExtensionPointImpl extension;
			if (extensionClass.isAnnotationPresent(com.elasticpath.xpf.annotations.XPFEmbedded.class)) {
				extension = beanFactory.getPrototypeBean(extensionClass.getName(), extensionClass);
			} else {
				extension = extensionClass.newInstance();
			}
			extension.setExtensionGuid(extensionGuid);
			extension.initialize(createContext(extensionSettings));

			singletonCache.put(extensionGuid, extension);
		} catch (Exception e) {
			throw new PluginRuntimeException(e);
		}

	}

	/**
	 * Resolve the extension from cache and return it.
	 *
	 * @param extensionClass extension class
	 * @param extensionGuid  extension GUID
	 * @param <T>            type of the extension
	 * @return the extension
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(final Class<T> extensionClass, final String extensionGuid) {
		return (T) singletonCache.get(extensionGuid);
	}

	private XPFExtensionInitializationContext createContext(final Set<PluginSettingDTO> extensionSettings) {
		final Map<String, XPFPluginSetting> settings = extensionSettings.stream()
				.map(setting -> settingConverter.convert(setting))
				.collect(Collectors.toMap(XPFPluginSetting::getSettingKey,
						Function.identity(),
						(first, second) -> first,
						CaseInsensitiveMap::new));

		return new XPFExtensionInitializationContext(settings);
	}

	protected Map<String, Object> getSingletonCache() {
		return singletonCache;
	}

	protected XPFPluginSettingConverter getSettingConverter() {
		return settingConverter;
	}

	public void setSettingConverter(final XPFPluginSettingConverter settingConverter) {
		this.settingConverter = settingConverter;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
