/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.beanframework.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 *  Allows for extensible list Spring beans to be created using a custom XML namespace.
 *
 *  The schema definition for this namespace is defined in ./extensibleList.xsd.
 *
 */
public class ExtensibleListNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * Initialize the bean definitions that this namespace handler is responsible for.
	 */
	public void init() {
		registerBeanDefinitionParser("create", new ExtensibleListCreateBeanDefinitionParser());
		registerBeanDefinitionParser("modify", new ExtensibleListModifyBeanDefinitionParser());
	}

}
