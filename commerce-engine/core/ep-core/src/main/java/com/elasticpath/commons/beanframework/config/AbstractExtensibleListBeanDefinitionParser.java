/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.beanframework.config;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A class that contains base functionality that is shared by all of the extensibleList Spring custom namespace BeanDefinitionParser classes.
 */
public abstract class AbstractExtensibleListBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final Logger LOG = LogManager.getLogger(AbstractExtensibleListBeanDefinitionParser.class);

	/**
	 * Parses the list of bean references from within the extensible list create tag.
	 * @param element - the extensibleList:create root element , with an optional value-type tag to enforce object type within the list
	 * @param parserContext - the object encapsulating the current state of the parsing process;
	 * @param builder - the builder to pass to delegate.parseListElement
	 * @param valueTypeClassName the String representation of the class name that all bean reference list elements must be an instance of
	 * @return the List of bean references
	 */
	protected List<Object> parseList(final Element element, final ParserContext parserContext, final BeanDefinitionBuilder builder,
			final String valueTypeClassName) {
		element.setAttribute(BeanDefinitionParserDelegate.MERGE_ATTRIBUTE, BeanDefinitionParserDelegate.TRUE_VALUE);
		List<Object> beanReferenceList = parserContext.getDelegate().parseListElement(element, builder.getBeanDefinition());
		if (StringUtils.hasText(valueTypeClassName)) {
			enforceValueType(element, parserContext, beanReferenceList, valueTypeClassName);
		}
		return beanReferenceList;
	}

	/**
	 * Enforces the rule that all of the bean references within the supplied list are of the value type specified by the element attribute.
	 * @param element			the root extensibleList:crete element
	 * @param parserContext		the context used to retrieve the bean registry to lookup the bean definitions for each bean reference
	 * @param beanReferenceList	the parsed List that contains the RuntimeBeanReference objects
	 * @param valueTypeClassName the String representation of the class name that all bean reference list elements must be an instance of
	 */
	private void enforceValueType(final Element element, final ParserContext parserContext, final List<Object> beanReferenceList,
			final String valueTypeClassName) {

			try {
				ClassLoader classLoader = parserContext.getReaderContext().getBeanClassLoader();
				Class<?> targetType = ClassUtils.forName(valueTypeClassName, classLoader);
				if (!isListValueTypesValid(parserContext, beanReferenceList, classLoader, targetType)) {
					parserContext.getReaderContext().error(
							"extensibleList bean [" + element.getAttribute("id") + "] contains a bean reference that"
									+ " is not of valueType [" + valueTypeClassName + "]",
							element);
				}
			} catch (ClassNotFoundException cnfE) {
				parserContext.getReaderContext().error(
						"Could not find a matching class definition to enforce extensibleList valueType",
						element,
						cnfE);
			}
	}

	/**
	 * Iterates through the bean reference list and verifies the type of each element is assignable from the target type.
	 *
	 * @param parserContext		the context used to retrieve the bean registry to lookup the bean definitions for each bean reference
	 * @param beanReferenceList	the parsed List that contains the RuntimeBeanReference objects
	 * @param classLoader		the ClassLoader to use to retrieve the class for each bean reference
	 * @param targetType		the target type class to verify each bean reference inherits from
	 * @throws ClassNotFoundException	if the bean class name for a RuntimeReferenceBean cannot be found
	 * @return true if all bean references are instances of the target type, false otherwise
	 */
	private boolean isListValueTypesValid(final ParserContext parserContext, final List<Object> beanReferenceList,
			final ClassLoader classLoader, final Class<?> targetType) throws ClassNotFoundException {
		for (Object beanRef : beanReferenceList) {
			if (beanRef instanceof RuntimeBeanReference) {
				String beanRefName = ((RuntimeBeanReference) beanRef).getBeanName();
				String beanRefClassName = parserContext.getRegistry().getBeanDefinition(beanRefName).getBeanClassName();
				if (beanRefClassName == null) {
					LOG.error("Bean definition [{}] that is referenced in extensible list could not be found.  It must be of type {}", beanRefName,
							targetType.getName());
				}
				Class<?> beanRefClass = ClassUtils.forName(beanRefClassName, classLoader);
				if (!targetType.isAssignableFrom(beanRefClass)) {
					return false;
				}
			}
		}
		return true;
	}
}
