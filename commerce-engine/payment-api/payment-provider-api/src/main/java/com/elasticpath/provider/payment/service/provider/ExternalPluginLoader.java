/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider;

import java.io.IOException;
import java.util.Map;

/**
 * Loader class for externally defined plugins - isolated jars with dependencies packed within them.
 * </p>
 * Isolation is supported in terms of separating class loaders for connectivity interface implementations, treated as Spring beans.
 * </p>
 * It is implied that plugin has a dependency on Spring Framework. Though strictly speaking there is no commitment to a particular version
 * of this framework, the recommendation is to stay within corresponding major version range of Spring libraries used by current project
 * (ep-commerce).
 */
public interface ExternalPluginLoader {

	/**
	 * Finds and loads external plugins as Spring beans by the interface they implement.
	 * </p>
	 * Each bean definition file found by the pattern will be treated as a separate plugin,
	 * meaning that it will obtain its own class loader (realm).
	 *
	 * @param <T>             plugin interface
	 * @param pluginInterface plugin interface class object
	 * @return map of bean id/name to plugin bean
	 * @throws IOException if bean definition file location cannot be loaded
	 */
	<T> Map<String, T> load(Class<T> pluginInterface) throws IOException;

}
