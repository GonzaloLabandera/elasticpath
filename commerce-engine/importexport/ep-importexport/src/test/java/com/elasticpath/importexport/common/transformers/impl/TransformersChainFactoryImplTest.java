/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.transformers.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.transformers.Transformer;
import com.elasticpath.importexport.common.transformers.TransformerConfiguration;
import com.elasticpath.importexport.common.transformers.TransformersChainFactory;

/**
 * Test for TransformersChainFactoryImpl.
 */
public class TransformersChainFactoryImplTest {

	/**
	 * The DummyTransformer.
	 */
	public static class DummyTransformer extends AbstractTransformerImpl {
		@Override
		protected void processTransformation(final InputStream input, final OutputStream output) {
			// empty, Do nothing
		}
	}

	/**
	 * IllegalAccessExceptionTransformer.
	 */	
	public static final class IllegalAccessExceptionTransformer extends DummyTransformer {
		private IllegalAccessExceptionTransformer() {
			// empty, No Default Constructor.
		}
	}

	/**
	 * InstantiationExceptionTransformer.
	 */
	public abstract static class AbstractInstantiationExceptionTransformer implements Transformer {
		// Empty Abstract Class
	}

	/*
	 * Creates TransformerConfigurationList With only one item in it. @param clazz - the class for TransformerConfiguration
	 */
	private List<TransformerConfiguration> createTransformerConfigurationList(final String className) {
		List<TransformerConfiguration> transformerConfigurationList = new ArrayList<>();
		TransformerConfiguration transformerConfiguration = new TransformerConfiguration();
		transformerConfiguration.setClassName(className);
		transformerConfigurationList.add(transformerConfiguration);
		return transformerConfigurationList;
	}

	/**
	 * Verify that transformers chain factory creates the chain of transformers properly.
	 */
	@Test
	public void testCreateTransformersChain() throws ConfigurationException {
		TransformersChainFactory transformersChainFactory = new TransformersChainFactoryImpl();

		List<Transformer> transformerList = transformersChainFactory.createTransformersChain(
			createTransformerConfigurationList(DummyTransformer.class.getName()));


		assertThat(transformerList).hasSize(1);

		Transformer transformer = transformerList.get(0);
		assertThat(transformer).isInstanceOf(DummyTransformer.class);

		transformer.transform(null); // Dummy Transformer can do nothing with nothing.
	}

	/**
	 * Tests CreateTransformersChain and Throws ConfigurationException Cause ClassNotFoundException.
	 */
	@Test
	public void testCreateTransformersChainThrowsConfigurationExceptionCauseClassNotFoundException() {
		TransformersChainFactory transformersChainFactory = new TransformersChainFactoryImpl();
		
		assertThatThrownBy(() -> transformersChainFactory.createTransformersChain(
					createTransformerConfigurationList("MyDummyUnknownClass")))
			.as("ConfigurationException must be thrown cause ClassNotFoundException")
			.isInstanceOf(ConfigurationException.class)
			.hasCauseInstanceOf(ClassNotFoundException.class);
	}

	/**
	 * Tests Create TransformersChain and Throws Exceptions Cause IllegalAccessException.
	 */
	@Test
	public void testCreateTransformersChainThrowsExceptionsCauseIllegalAccessException() {
		TransformersChainFactory transformersChainFactory = new TransformersChainFactoryImpl();

		assertThatThrownBy(() -> transformersChainFactory.createTransformersChain(
					createTransformerConfigurationList(IllegalAccessExceptionTransformer.class.getName())))
			.as("ConfigurationException must be thrown cause of IllegalAccessException")
			.isInstanceOf(ConfigurationException.class)
			.hasCauseInstanceOf(IllegalAccessException.class);
	}
	
	/**
	 * Tests CreateTransformersChain and Throws Exceptions Cause InstantiationException.
	 */
	@Test
	public void testCreateTransformersChainThrowsExceptionsCauseInstantiationException() {
		TransformersChainFactory transformersChainFactory = new TransformersChainFactoryImpl();

		assertThatThrownBy(() -> transformersChainFactory.createTransformersChain(
					createTransformerConfigurationList(AbstractInstantiationExceptionTransformer.class.getName())))
			.as("ConfigurationException must be thrown cause of InstantiationException")
			.isInstanceOf(ConfigurationException.class)
			.hasCauseInstanceOf(InstantiationException.class);
	}
}
