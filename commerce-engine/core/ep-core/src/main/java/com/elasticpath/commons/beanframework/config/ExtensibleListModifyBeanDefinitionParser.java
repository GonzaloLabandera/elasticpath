/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.beanframework.config;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * An custom Spring BeanDefinitionParser that parses modifications to custom extensible list
 * Spring bean definitions that are defined using the custom extensibleList:modify namespace.
 *
 */
public class ExtensibleListModifyBeanDefinitionParser extends AbstractExtensibleListBeanDefinitionParser {

	/**
	 * Parse the supplied extensibleList:modify {@link Element} and create a new child extensible list for the specified parent.
	 * This new child bean definition will replace the current definition.
	 *
	 * @param element       the extensibleList:modify root element that contains the modifications to the parent list
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 *
	 * @return always returns null - the new child bean is internally registered
	 */
	@Override
	protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
		String parentBeanId = element.getAttribute("parent");
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.childBeanDefinition(parentBeanId);

		String parentValueType = getParentValueType(parserContext, parentBeanId);

		parseAndMergeList(element, parserContext, builder, "addToList", "sourceList", parentValueType);

		parseAndMergeList(element, parserContext, builder, "removeFromList", "removeList", parentValueType);

		String childBeanId = element.getAttribute("id");
		parserContext.getRegistry().registerBeanDefinition(childBeanId, builder.getBeanDefinition());

		return null;
	}

	/**
	 * Looks up the parent bean definition and returns the string representation of the valueType property.
	 *
	 * @param parserContext	the context to use for retrieval of the bean registry
	 * @param parent		the name of the parent bean to look for in the registry
	 * @return the String representation of the class name that all list elements must be instances of
	 */
	private String getParentValueType(final ParserContext parserContext, final String parent) {
		BeanDefinition parentBeanDefinition = parserContext.getRegistry().getBeanDefinition(parent);
		String parentValueType = null;
		if (parentBeanDefinition != null) {
			PropertyValue parentValueTypeProp = parentBeanDefinition.getPropertyValues().getPropertyValue("valueType");
			if (parentValueTypeProp != null) {
				 parentValueType = (String) parentValueTypeProp.getValue();
			}
		}
		return parentValueType;
	}

	/**
	 * Parses an individual list out of the element and merges it into the bean definition.
	 *
	 * @param element			the extensibleList:modify root element
	 * @param parserContext 	the object encapsulating the current state of the parsing process
	 * @param builder			the builder to merge the list into
	 * @param listChildTagName	the child element tag name that contains the list elements to merge
	 * @param listPropertyName  the builder property name that specifies the base list property to merge into
	 * @param parentValueType   the string representation of the class name that all list elements must be an instance of
	 */
	private void parseAndMergeList(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder,
			final String listChildTagName, final String listPropertyName, final String parentValueType) {
		Element listElement = DomUtils.getChildElementByTagName(element, listChildTagName);
		if (listElement != null) {
			builder.addPropertyValue(listPropertyName, super.parseList(listElement, parserContext, builder, parentValueType));
		}
	}

}