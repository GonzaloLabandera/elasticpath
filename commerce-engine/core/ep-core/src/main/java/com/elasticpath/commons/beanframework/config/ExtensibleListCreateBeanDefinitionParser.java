/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.beanframework.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * An custom Spring BeanDefinitionParser that parses custom extensible list
 * Spring bean definitions that are defined using the custom extensibleList:create namespace.
 *
 * The ExtensibleListFactoryBean is used as the bean definition.
 *
 */
public class ExtensibleListCreateBeanDefinitionParser extends AbstractExtensibleListBeanDefinitionParser {
	/**
	 * Parse the supplied {@link Element} into two BeanDefinitions - 1 for the parent and 1 for the child.
	 *
	 * @param element       the root element that is to be parsed into the extensible list and its bean reference values
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 *                      provides access to a  BeanDefinitionRegistry
	 * @return null - the bean is registered internally
	 */
	@Override
	protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
		parseAndRegisterExtensibleList(element, parserContext);
		return null;
	}

	/**
	 * Parse the supplied Element into two BeanDefinitions - 1 for the parent and 1 for the default child.
	 *
	 * @param element       the root element that is to be parsed into the parent extensible list and its default child
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 *                      provides access to a  BeanDefinitionRegistry
	 */
	private void parseAndRegisterExtensibleList(final Element element, final ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ExtensibleListFactoryBean.class);
		factory.setAbstract(true);
		String valueTypeClassName = element.getAttribute("valueType");
		factory.addPropertyValue("valueType", valueTypeClassName);

		factory.addPropertyValue("sourceList", parseList(element, parserContext, factory, valueTypeClassName));

		final String parentBeanId = element.getAttribute("id");
		parserContext.getRegistry().registerBeanDefinition(parentBeanId, factory.getBeanDefinition());

		BeanDefinitionBuilder extensionElement = BeanDefinitionBuilder.childBeanDefinition(parentBeanId);
		String overridableId = element.getAttribute("overridableId");

		parserContext.getRegistry().registerBeanDefinition(overridableId, extensionElement.getBeanDefinition());
	}

}
