/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.elasticpath.provider.payment.service.provider.ExternalPluginLoader;

/**
 * Default implementation of {@link ExternalPluginLoader} using Plexus Classworlds.
 */
public class ExternalPluginLoaderImpl implements ExternalPluginLoader {

	private static final String EXTERNAL_PLUGIN_BEAN_DEFINITION_LOCATION_PATTERN = "classpath*:META-INF/elasticpath/conf/spring/plugin.xml";

	private static final String JAR_PATH_PREFIX = "jar:";
	private static final String JAR_PATH_SUFFIX = "!/";

	private static final String WEB_INF_DIR = "/WEB-INF";
	private static final String CLASSES_DIR = "/classes";

	@Autowired
	private ApplicationContext applicationContext;

	@SuppressWarnings({"squid:S2095"})
	@Override
	public <T> Map<String, T> load(final Class<T> pluginInterface) throws IOException {
		final Map<String, T> externalPlugins = new HashMap<>();

		final ClassWorld world = new ClassWorld();
		final ClassRealm connectivityRealm = createConnectivityRealm(world, pluginInterface);
		final ClassRealm allPluginsRealm = createAllPluginsRealm(world, pluginInterface);
		final Resource[] resources = findResourcesByLocationPattern(allPluginsRealm, EXTERNAL_PLUGIN_BEAN_DEFINITION_LOCATION_PATTERN);

		for (final Resource resource : resources) {
			final URL containerUrl = getContainerUrl(resource);
			final ClassRealm pluginRealm = createPluginRealm(connectivityRealm, containerUrl, pluginInterface);

			final Map<String, T> beans = getBeansOfType(pluginRealm, pluginInterface, resource.getURL().toExternalForm())
					.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> createProxy(pluginRealm, pluginInterface, entry.getValue())));
			ensureBeansAreUnique(externalPlugins, beans);
			externalPlugins.putAll(beans);
		}

		return externalPlugins;
	}

	/**
	 * Creates connectivity realm around plugin interface container (jar file or directory).
	 *
	 * @param world           class world
	 * @param pluginInterface plugin interface
	 * @return connectivity class realm
	 */
	protected ClassRealm createConnectivityRealm(final ClassWorld world, final Class<?> pluginInterface) {
		try {
			final URL connectivitySourceLocation = getSourceLocation(pluginInterface);
			final ClassRealm connectivityRealm = world.newRealm(connectivitySourceLocation.toExternalForm());
			connectivityRealm.addURL(connectivitySourceLocation);
			return connectivityRealm;
		} catch (DuplicateRealmException e) {
			throw new IllegalStateException("Cannot create connectivity realm", e);
		}
	}

	/**
	 * Get .class file location of specified class.
	 *
	 * @param clazz java class object
	 * @return location associated with class code source
	 */
	protected URL getSourceLocation(final Class<?> clazz) {
		final CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new IllegalStateException("Cannot locate code source for class: " + clazz.getName());
		}
		return codeSource.getLocation();
	}

	/**
	 * Creates realm around plugins directory.
	 *
	 * @param world           class world
	 * @param pluginInterface plugin interface used as anchor to search for plugins directory
	 * @return all plugins class realm
	 * @throws IOException in case of I/O errors when searching for plugins
	 */
	@SuppressWarnings({"squid:S2095"})
	protected ClassRealm createAllPluginsRealm(final ClassWorld world, final Class<?> pluginInterface) throws IOException {
		try {
			final ClassRealm allPluginsRealm = world.newRealm("all-plugins-realm", null);
			final Resource[] pluginResources = findResourcesByLocationPattern(
					pluginInterface.getClassLoader(),
					getPluginsLocation(pluginInterface) + "/*");
			for (Resource pluginResource : pluginResources) {
				allPluginsRealm.addURL(pluginResource.getURL());
			}
			return allPluginsRealm;
		} catch (DuplicateRealmException e) {
			throw new IllegalStateException("Cannot create all plugins realm", e);
		}
	}

	/**
	 * Gets plugins directory location using plugin interface location as an anchor.
	 *
	 * @param pluginInterface plugin interface class
	 * @return path to {@code /WEB-INF/plugins}
	 */
	protected String getPluginsLocation(final Class<?> pluginInterface) {
		final String connectivityJarLocation = getSourceLocation(pluginInterface).toExternalForm();
		int rootLocationEndIndex = connectivityJarLocation.lastIndexOf(WEB_INF_DIR);
		if (rootLocationEndIndex == -1) {
			rootLocationEndIndex = connectivityJarLocation.lastIndexOf(CLASSES_DIR);
			if (rootLocationEndIndex == -1) {
				rootLocationEndIndex = connectivityJarLocation.lastIndexOf('/');
			}
		} else {
			rootLocationEndIndex = rootLocationEndIndex + WEB_INF_DIR.length();
		}
		final String webappLocation = connectivityJarLocation.substring(0, rootLocationEndIndex);
		return webappLocation + File.separatorChar + "plugins";
	}

	/**
	 * Resolve the given location pattern into Resource objects.
	 *
	 * @param classLoader     the ClassLoader to load classpath resources with
	 * @param locationPattern the location pattern to resolve
	 * @return the corresponding Resource objects
	 * @throws IOException in case of I/O errors
	 */
	protected Resource[] findResourcesByLocationPattern(final ClassLoader classLoader, final String locationPattern) throws IOException {
		final ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver(classLoader);
		return resourceLoader.getResources(locationPattern);
	}

	/**
	 * Creates a container URL based on any file resource within the container.
	 *
	 * @param resource any file resource within the container
	 * @return URL to container (jar file or directory)
	 * @throws IOException if the resource is not available as descriptor
	 */
	protected URL getContainerUrl(final Resource resource) throws IOException {
		final String filePath = resource.getURL().toExternalForm();
		if (filePath.startsWith(JAR_PATH_PREFIX)) {
			if (filePath.contains(JAR_PATH_SUFFIX)) {
				return new URL(filePath.substring(JAR_PATH_PREFIX.length(), filePath.lastIndexOf(JAR_PATH_SUFFIX)));
			} else {
				return new URL(filePath.substring(JAR_PATH_PREFIX.length()));
			}
		} else {
			throw new IllegalStateException("External plugins can only be jars, cannot load plugin from: " + filePath);
		}
	}

	/**
	 * Creates plugin realm, allowing it to access only connectivity package classes.
	 *
	 * @param connectivityRealm connectivity realm
	 * @param containerUrl      URL to plugin container (jar file or directory)
	 * @param pluginInterface   plugin interface
	 * @return plugin class realm
	 * @throws IOException if the plugin libraries cannot be loaded
	 */
	@SuppressWarnings({"squid:S2095"})
	protected ClassRealm createPluginRealm(final ClassRealm connectivityRealm,
										   final URL containerUrl,
										   final Class<?> pluginInterface) throws IOException {
		try {
			final ClassRealm pluginRealm = connectivityRealm.createChildRealm(containerUrl.toString());
			pluginRealm.addURL(containerUrl);
			pluginRealm.importFromParent(pluginInterface.getPackage().getName());

			final Resource[] libResources = findResourcesByLocationPattern(pluginRealm, "lib/*.jar");
			for (Resource libResource : libResources) {
				pluginRealm.addURL(new URL("lib-jar:" + libResource.getURL().toExternalForm()));
			}
			return pluginRealm;
		} catch (final DuplicateRealmException e) {
			throw new IllegalStateException("Cannot create plugin realm: " + containerUrl, e);
		}
	}

	/**
	 * Finds beans that implement a particular plugin interface by delegating the search to Spring Framework implicitly used by the plugin.
	 * </p>
	 * Basically creates {@link GenericXmlApplicationContext} using a plugin class loader and leverages its capabilities.
	 * Thus we are only bound to this class interface state in current version of Spring Framework and not to the whole framework.
	 * Theoretically this class may not even change with major version update of the framework,
	 * but at minimum we can claim to support plugins within current major range of it.
	 * </p>
	 * Also in future this method can be updated to detect newer/older versions of Spring Framework used by the plugins and adapt accordingly.
	 *
	 * @param pluginRealm            plugin class realm
	 * @param pluginInterface        plugin interface as class
	 * @param beanDefinitionLocation bean definition file location
	 * @param <T>                    plugin interface
	 * @return map of bean id/name to plugin bean
	 */
	@SuppressWarnings({"unchecked", "squid:S3878", "squid:S2658"})
	protected <T> Map<String, T> getBeansOfType(final ClassRealm pluginRealm, final Class<T> pluginInterface, final String beanDefinitionLocation) {
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(pluginRealm);
			final Class<?> appContextClass = pluginRealm.loadClass(GenericXmlApplicationContext.class.getName());
			final Object appContext = appContextClass.getConstructor().newInstance();
			appContextClass.getMethod("setClassLoader", ClassLoader.class).invoke(appContext, pluginRealm);
			appContextClass.getMethod("load", String[].class).invoke(appContext, new Object[]{new String[]{beanDefinitionLocation}});
			appContextClass.getMethod("refresh").invoke(appContext);
			return (Map<String, T>) appContextClass.getMethod("getBeansOfType", Class.class).invoke(appContext, pluginInterface);
		} catch (Exception exception) {
			throw new IllegalStateException("Reflection assumptions were invalid for " + pluginRealm
					+ ", Spring Framework versions incompatibility?", exception);
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	/**
	 * Creates Java proxy to ensure correct Thread Context Class Loader is used.
	 *
	 * @param classLoader     class loader to use
	 * @param pluginInterface plugin interface
	 * @param plugin          plugin instance
	 * @param <T>             plugin interface
	 * @return proxy around plugin interface
	 */
	@SuppressWarnings("unchecked")
	protected <T> T createProxy(final ClassLoader classLoader, final Class<T> pluginInterface, final T plugin) {
		return (T) Proxy.newProxyInstance(classLoader, new Class<?>[]{pluginInterface}, new ContextClassLoaderSwitcher(classLoader, plugin));
	}

	/**
	 * Checks that all newly loaded beans have a unique identifier (name).
	 *
	 * @param alreadyLoadedBeans beans that were already loaded for the same plugin interface
	 * @param newBeans           newly loaded beans
	 * @param <T>                plugin interface
	 */
	protected <T> void ensureBeansAreUnique(final Map<String, T> alreadyLoadedBeans, final Map<String, T> newBeans) {
		for (final Map.Entry<String, T> beanEntry : newBeans.entrySet()) {
			final String beanName = beanEntry.getKey();

			final T existingPlugin = alreadyLoadedBeans.get(beanName);
			if (existingPlugin != null) {
				throw new IllegalStateException(beanName + " is already defined by " + existingPlugin.getClass().getClassLoader());
			}

			if (applicationContext.containsBean(beanName)) {
				throw new IllegalStateException(beanName + " is already defined by " + applicationContext.getBean(beanName).getClass().getName());
			}
		}
	}

	protected ApplicationContext getBeanFactory() {
		return applicationContext;
	}
}
